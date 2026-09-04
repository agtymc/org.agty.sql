#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="agty-sql-pgsql"
IMAGE_NAME="${PGSQL_IMAGE:-postgres:17.11}"
HOST_ADDRESS="${HOST_ADDRESS:-127.0.0.1}"
HOST_PORT="25432"
DATABASE_NAME="agty_sql"
DATABASE_USER="agty_sql"
DATABASE_PASSWORD="${AGTY_SQL_PGSQL_PASSWORD:?Set AGTY_SQL_PGSQL_PASSWORD}"
VOLUME_NAME="agty-sql-pgsql-data"

DOCKER_BIN="sudo docker"

if ${DOCKER_BIN} ps -a --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
  ${DOCKER_BIN} start "${CONTAINER_NAME}" >/dev/null
  echo "Container ${CONTAINER_NAME} started"
  exit 0
fi

${DOCKER_BIN} pull "${IMAGE_NAME}"

${DOCKER_BIN} run -d \
  --name "${CONTAINER_NAME}" \
  -p "${HOST_ADDRESS}:${HOST_PORT}:5432" \
  -e POSTGRES_DB="${DATABASE_NAME}" \
  -e POSTGRES_USER="${DATABASE_USER}" \
  -e POSTGRES_PASSWORD="${DATABASE_PASSWORD}" \
  -v "${VOLUME_NAME}:/var/lib/postgresql/data" \
  "${IMAGE_NAME}" >/dev/null

echo "Container ${CONTAINER_NAME} created on port ${HOST_PORT}"
