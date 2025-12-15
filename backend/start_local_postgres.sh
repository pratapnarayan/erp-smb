#!/usr/bin/env bash
set -euo pipefail

# Tiny helper to start a local Postgres via Docker and wait until it's ready.
# Defaults can be overridden by environment variables.
#
# Env vars (with defaults):
#   PG_IMAGE=postgres:16
#   PG_CONTAINER=erp-postgres
#   POSTGRES_DB=erp
#   POSTGRES_USER=postgres
#   POSTGRES_PASSWORD=postgres
#   HOST_PORT=5432        # Host port to bind
#   CONTAINER_PORT=5432   # Container port
#
# Usage:
#   bash start_local_postgres.sh
#   HOST_PORT=55432 POSTGRES_DB=erp_dev bash start_local_postgres.sh

PG_IMAGE=${PG_IMAGE:-postgres:16}
PG_CONTAINER=${PG_CONTAINER:-erp-postgres}
POSTGRES_DB=${POSTGRES_DB:-erp}
POSTGRES_USER=${POSTGRES_USER:-postgres}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-postgres}
HOST_PORT=${HOST_PORT:-5432}
CONTAINER_PORT=${CONTAINER_PORT:-5432}

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is required but not found on PATH." >&2
  exit 1
fi

echo "Ensuring image ${PG_IMAGE} is available..."
docker pull "${PG_IMAGE}" >/dev/null 2>&1 || true

exists=$(docker ps -a --format '{{.Names}}' | grep -Fx "${PG_CONTAINER}" || true)
if [[ -z "$exists" ]]; then
  echo "Creating container ${PG_CONTAINER}..."
  docker run -d --name "${PG_CONTAINER}" \
    -e POSTGRES_DB="${POSTGRES_DB}" \
    -e POSTGRES_USER="${POSTGRES_USER}" \
    -e POSTGRES_PASSWORD="${POSTGRES_PASSWORD}" \
    -p "${HOST_PORT}:${CONTAINER_PORT}" \
    "${PG_IMAGE}" >/dev/null
else
  echo "Container ${PG_CONTAINER} already exists. Starting it..."
  docker start "${PG_CONTAINER}" >/dev/null
fi

# Wait until Postgres is ready using pg_isready inside the container
echo "Waiting for Postgres to be ready (container: ${PG_CONTAINER})..."
for i in {1..60}; do
  if docker exec "${PG_CONTAINER}" pg_isready -h localhost -p "${CONTAINER_PORT}" -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; then
    echo "Postgres is ready on localhost:${HOST_PORT} (db=${POSTGRES_DB}, user=${POSTGRES_USER})."
    exit 0
  fi
  sleep 1
  if (( i % 10 == 0 )); then
    echo "Still waiting... (${i}s)"
  fi
done

echo "Timed out waiting for Postgres readiness." >&2
# show last few logs for diagnostics
docker logs --tail 50 "${PG_CONTAINER}" || true
exit 2
