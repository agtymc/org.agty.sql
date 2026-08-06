#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="agty-sql-pgsql"
IMAGE_NAME="postgres:17"
HOST_PORT="25432"
DATABASE_NAME="agty_sql"
DATABASE_USER="agty_sql"
DATABASE_PASSWORD="agty_sql_pgsql_pass"
VOLUME_NAME="agty-sql-pgsql-data"

DOCKER_BIN="sudo docker"

if ${DOCKER_BIN} ps -a --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
  ${DOCKER_BIN} update --restart unless-stopped "${CONTAINER_NAME}" >/dev/null
  ${DOCKER_BIN} start "${CONTAINER_NAME}" >/dev/null
  echo "Container ${CONTAINER_NAME} started"
  exit 0
fi

${DOCKER_BIN} pull "${IMAGE_NAME}"

${DOCKER_BIN} run -d \
  --name "${CONTAINER_NAME}" \
  --restart unless-stopped \
  -p "${HOST_PORT}:5432" \
  -e POSTGRES_DB="${DATABASE_NAME}" \
  -e POSTGRES_USER="${DATABASE_USER}" \
  -e POSTGRES_PASSWORD="${DATABASE_PASSWORD}" \
  -v "${VOLUME_NAME}:/var/lib/postgresql/data" \
  "${IMAGE_NAME}" >/dev/null

echo "Container ${CONTAINER_NAME} created on port ${HOST_PORT}"
