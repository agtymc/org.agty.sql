#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="agty-sql-mariadb"
IMAGE_NAME="mariadb:11.8"
HOST_PORT="23316"
DATABASE_NAME="agty_sql"
DATABASE_USER="agty_sql"
DATABASE_PASSWORD="agty_sql_mariadb_pass"
ROOT_PASSWORD="agty_sql_mariadb_root_pass"
VOLUME_NAME="agty-sql-mariadb-data"

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
  -p "${HOST_PORT}:3306" \
  -e MARIADB_DATABASE="${DATABASE_NAME}" \
  -e MARIADB_USER="${DATABASE_USER}" \
  -e MARIADB_PASSWORD="${DATABASE_PASSWORD}" \
  -e MARIADB_ROOT_PASSWORD="${ROOT_PASSWORD}" \
  -v "${VOLUME_NAME}:/var/lib/mysql" \
  "${IMAGE_NAME}" >/dev/null

echo "Container ${CONTAINER_NAME} created on port ${HOST_PORT}"
