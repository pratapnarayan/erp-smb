#!/usr/bin/env bash
set -euo pipefail

# Cross-platform runner for all services with Flyway migrations on startup.
# - Uses local profile (application-local.yml), which points to localhost Postgres by default
# - Verifies DB reachability using psql if available, else nc, else PowerShell, else warns and proceeds
# - Starts services sequentially and performs basic health checks
#
# Prereqs (any one for DB check):
#   - psql (from PostgreSQL client), or
#   - nc (netcat), or
#   - PowerShell Test-NetConnection (on Windows)
#
# Optional env overrides:
#   POSTGRES_HOST (default: localhost)
#   POSTGRES_PORT (default: 5432)
#   POSTGRES_DB   (default: erp)
#   POSTGRES_USER (default: postgres)
#   POSTGRES_PASSWORD (default: postgres)

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

PGHOST="${POSTGRES_HOST:-localhost}"
PGPORT="${POSTGRES_PORT:-5432}"
PGDB="${POSTGRES_DB:-erp}"
PGUSER="${POSTGRES_USER:-postgres}"
PGPASSWORD_VAL="${POSTGRES_PASSWORD:-postgres}"

# Service definitions: module => port
# Ports are set explicitly via --server.port; they do not require gateway

declare -A SERVICES=(
  ["auth-service"]=8081
  ["user-service"]=8082
  ["product-service"]=8083
  ["order-service"]=8084
  ["sales-service"]=8085
  ["finance-service"]=8086
  ["hrms-service"]=8087
  ["enquiry-service"]=8088
)

PIDS=()

has_cmd() { command -v "$1" >/dev/null 2>&1; }

check_db_psql() {
  if has_cmd psql; then
    PGPASSWORD="$PGPASSWORD_VAL" psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDB" -c "select 1;" -tA >/dev/null 2>&1
    return $?
  fi
  return 127
}

check_db_nc() {
  if has_cmd nc; then
    nc -z "$PGHOST" "$PGPORT" >/dev/null 2>&1
    return $?
  fi
  return 127
}

check_db_powershell() {
  if has_cmd powershell.exe; then
    powershell.exe -NoProfile -Command "exit (Test-NetConnection -ComputerName '$PGHOST' -Port $PGPORT).TcpTestSucceeded ? 0 : 1" >/dev/null 2>&1 || return 1
    return 0
  fi
  return 127
}

preflight_db_check() {
  echo "Checking PostgreSQL at ${PGHOST}:${PGPORT} (db=${PGDB}, user=${PGUSER})..."
  if check_db_psql; then
    echo "PostgreSQL reachable (psql)."
    return 0
  fi
  if check_db_nc; then
    echo "PostgreSQL port reachable (nc)."
    return 0
  fi
  if check_db_powershell; then
    echo "PostgreSQL port reachable (PowerShell)."
    return 0
  fi
  echo "Warning: Could not verify PostgreSQL with psql/nc/PowerShell. Proceeding anyway..."
  return 0
}

wait_for_port() {
  local host=$1 port=$2 retries=${3:-120} sleep_s=${4:-1}
  echo "Waiting for ${host}:${port} to be open..."
  for ((i=1;i<=retries;i++)); do
    if command -v nc >/dev/null 2>&1; then
      if nc -z "$host" "$port" >/dev/null 2>&1; then
        echo "Port ${port} open (nc)."; return 0
      fi
    fi
    if command -v powershell.exe >/dev/null 2>&1; then
      if powershell.exe -NoProfile -Command "exit (Test-NetConnection -ComputerName '$host' -Port $port).TcpTestSucceeded ? 0 : 1" >/dev/null 2>&1; then
        echo "Port ${port} open (PowerShell)."; return 0
      fi
    fi
    # Fallback: try TCP connect via bash /dev/tcp if available
    if exec 3<>/dev/tcp/"$host"/"$port" 2>/dev/null; then
      exec 3>&-; exec 3<&-; echo "Port ${port} open (/dev/tcp)."; return 0
    fi
    sleep "$sleep_s"
  done
  echo "Timeout waiting for ${host}:${port}" >&2
  return 1
}

start_service() {
  local module=$1 port=$2
  echo "Starting ${module} on port ${port}..."
  (cd "$ROOT_DIR/$module" && mvn -q -DskipTests clean spring-boot:run -Dspring-boot.run.arguments="--server.port=${port} --spring.profiles.active=local --eureka.client.enabled=false --spring.cloud.discovery.enabled=false" ) &
  local pid=$!
  PIDS+=("$pid:$module:$port")
  wait_for_port localhost "$port" 180 1
}

stop_services() {
  echo "Stopping services..."
  for entry in "${PIDS[@]}"; do
    IFS=":" read -r pid module port <<<"$entry"
    if ps -p "$pid" >/dev/null 2>&1; then
      echo "Stopping ${module} (pid ${pid})"
      kill "$pid" 2>/dev/null || true
    fi
  done
}

trap stop_services EXIT

preflight_db_check

# Build common-lib first to ensure availability in local repo
( cd "$ROOT_DIR/common-lib" && mvn -q -DskipTests clean install )

# Start services sequentially so each applies Flyway
for module in "auth-service" "user-service" "product-service" "order-service" "sales-service" "finance-service" "hrms-service" "enquiry-service"; do
  start_service "$module" "${SERVICES[$module]}"
  sleep 2
done

auth_login_and_checks() {
  local AUTH_PORT=${SERVICES["auth-service"]}
  local LOGIN_URL="http://localhost:${AUTH_PORT}/api/auth/login"
  echo "Logging into auth-service at ${LOGIN_URL}..."
  local TOKEN_JSON
  TOKEN_JSON=$(curl -s -X POST -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin"}' "$LOGIN_URL" || true)
  echo "Auth response: ${TOKEN_JSON}"
  if ! echo "$TOKEN_JSON" | grep -qiE 'accessToken|access_token'; then
    echo "Auth-service login FAILED. Check migrations/seeds." >&2
    return 2
  fi
  local ACCESS_TOKEN
  ACCESS_TOKEN=$(echo "$TOKEN_JSON" | sed -n 's/.*"accessToken"\s*:\s*"\([^"]*\)".*/\1/p')
  if [[ -z "$ACCESS_TOKEN" ]]; then
    ACCESS_TOKEN=$(echo "$TOKEN_JSON" | sed -n 's/.*"access_token"\s*:\s*"\([^"]*\)".*/\1/p')
  fi
  local AUTH_HEADER="Authorization: Bearer ${ACCESS_TOKEN}"

  # Basic list checks
  local PRODUCTS_PORT=${SERVICES["product-service"]}
  local ORDERS_PORT=${SERVICES["order-service"]}
  local SALES_PORT=${SERVICES["sales-service"]}

  check_list "products" "http://localhost:${PRODUCTS_PORT}/api/products" "$AUTH_HEADER"
  check_list "orders"   "http://localhost:${ORDERS_PORT}/api/orders"     "$AUTH_HEADER"
  check_list "sales"    "http://localhost:${SALES_PORT}/api/sales"       "$AUTH_HEADER"
}

check_list() {
  local name=$1 url=$2 header=$3
  echo "Checking ${name} at ${url}..."
  body=$(curl -s -H "$header" "$url" || true)
  code=$(curl -s -o /dev/null -w "%{http_code}" -H "$header" "$url" || true)
  if [[ "$code" != "200" ]]; then
    echo "${name} check FAILED: HTTP ${code}" >&2
    echo "Response: ${body}" >&2
    return 3
  fi
  echo "${name} OK"
}

# Run auth login + sanity checks
if ! auth_login_and_checks; then
  exit 4
fi

echo "All services started and checks passed. Press Ctrl+C to stop."
wait
