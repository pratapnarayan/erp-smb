#!/usr/bin/env bash
set -euo pipefail

# One-command local test for Auth + Reporting (no Docker compose)
# Prereqs: Java + Maven + Postgres reachable at localhost:5432 (db=erp, user=postgres, password=postgres)
# Optional: psql for quick checks

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
PGHOST=${POSTGRES_HOST:-localhost}
PGPORT=${POSTGRES_PORT:-5432}
PGDB=${POSTGRES_DB:-erp}
PGUSER=${POSTGRES_USER:-postgres}
PGPASSWORD_VAL=${POSTGRES_PASSWORD:-postgres}

has_cmd(){ command -v "$1" >/dev/null 2>&1; }

# 1) Build common-lib first
( cd "$ROOT_DIR/common-lib" && mvn -q -DskipTests clean install )

# 2) Start auth-service (port 8081)
( cd "$ROOT_DIR/auth-service" && \
  mvn -q -DskipTests spring-boot:run \
    -Dspring-boot.run.arguments="--server.port=8081 --spring.profiles.active=local --eureka.client.enabled=false --spring.cloud.discovery.enabled=false" \
) &
AUTH_PID=$!

echo "Waiting for auth-service on :8081 ..."
for i in {1..120}; do
  if nc -z localhost 8081 >/dev/null 2>&1; then echo "auth-service up"; break; fi
  sleep 1
  if (( i % 10 == 0 )); then echo "still waiting... ($i s)"; fi
  if (( i == 120 )); then echo "auth-service failed to start" >&2; exit 2; fi
done

# 3) Start reporting-service (port 9100) with local storage dir
mkdir -p "$ROOT_DIR/reporting-service/data/reports"
( cd "$ROOT_DIR/reporting-service" && \
  REPORTS_STORAGE_DIR="$ROOT_DIR/reporting-service/data/reports" \
  mvn -q -DskipTests spring-boot:run \
    -Dspring-boot.run.arguments="--server.port=9100 --spring.profiles.active=local --spring.datasource.url=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDB} --spring.datasource.username=${PGUSER} --spring.datasource.password=${PGPASSWORD_VAL} --eureka.client.enabled=false --spring.cloud.discovery.enabled=false" \
) &
REPORT_PID=$!

echo "Waiting for reporting-service on :9100 ..."
for i in {1..180}; do
  if nc -z localhost 9100 >/dev/null 2>&1; then echo "reporting-service up"; break; fi
  sleep 1
  if (( i % 10 == 0 )); then echo "still waiting... ($i s)"; fi
  if (( i == 180 )); then echo "reporting-service failed to start" >&2; kill $AUTH_PID || true; exit 3; fi
done

# 4) Login admin/admin to get token
LOGIN_JSON=$(curl -s -X POST http://localhost:8081/api/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin"}')
ACCESS=$(echo "$LOGIN_JSON" | sed -n 's/.*"accessToken"\s*:\s*"\([^"]*\)".*/\1/p')
if [[ -z "$ACCESS" ]]; then echo "Login failed: $LOGIN_JSON"; kill $AUTH_PID $REPORT_PID || true; exit 4; fi
AUTH_HEADER="Authorization: Bearer $ACCESS"

echo "Auth OK. Querying report definitions..."
curl -s -H "$AUTH_HEADER" http://localhost:9100/v1/reports/definitions | jq . || true

echo "Querying metrics (period=month)..."
curl -s -H "$AUTH_HEADER" "http://localhost:9100/v1/reports/metrics?period=month" | jq . || true

echo "Queueing a report run (sales_performance_monthly, CSV)..."
RUN_JSON=$(curl -s -X POST -H "$AUTH_HEADER" -H 'Content-Type: application/json' \
  -d '{"definitionCode":"sales_performance_monthly","paramsJson":"{\"dateFrom\":\"'$(date -v-30d +%Y-%m-%d 2>/dev/null || date -d "30 days ago" +%Y-%m-%d)\",\"dateTo\":\"'$(date +%Y-%m-%d)\"}","format":"CSV"}' \
  http://localhost:9100/v1/reports/run)
RUN_ID=$(echo "$RUN_JSON" | sed -n 's/.*"runId"\s*:\s*\([0-9][0-9]*\).*/\1/p')
if [[ -z "$RUN_ID" ]]; then echo "Run queue failed: $RUN_JSON"; kill $AUTH_PID $REPORT_PID || true; exit 5; fi

echo "Polling run status (#$RUN_ID)..."
for i in {1..60}; do
  STATUS=$(curl -s -H "$AUTH_HEADER" http://localhost:9100/v1/reports/runs/$RUN_ID | sed -n 's/.*"status"\s*:\s*"\([^"]*\)".*/\1/p')
  echo "status: $STATUS"
  if [[ "$STATUS" == "completed" ]]; then break; fi
  if [[ "$STATUS" == "failed" ]]; then echo "Run failed"; break; fi
  sleep 2
 done

echo "Fetching run details..."
curl -s -H "$AUTH_HEADER" http://localhost:9100/v1/reports/runs/$RUN_ID | jq . || true

# Leave both services running in background until user stops the script
trap 'echo; echo "Stopping services..."; kill $AUTH_PID $REPORT_PID 2>/dev/null || true; exit 0' INT TERM

echo "Services running. Press Ctrl+C to stop."
wait
