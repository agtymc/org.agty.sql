#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="agty-sql-mariadb"
IMAGE_NAME="${MARIADB_IMAGE:-mariadb:11.8.9}"
HOST_ADDRESS="${HOST_ADDRESS:-127.0.0.1}"
HOST_PORT="23316"
DATABASE_NAME="agty_sql"
DATABASE_USER="agty_sql"
DATABASE_PASSWORD="${AGTY_SQL_MARIADB_PASSWORD:?Set AGTY_SQL_MARIADB_PASSWORD}"
ROOT_PASSWORD="${AGTY_SQL_MARIADB_ROOT_PASSWORD:?Set AGTY_SQL_MARIADB_ROOT_PASSWORD}"
VOLUME_NAME="agty-sql-mariadb-data"

DOCKER_BIN="sudo docker"

if ${DOCKER_BIN} ps -a --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
  ${DOCKER_BIN} start "${CONTAINER_NAME}" >/dev/null
  echo "Container ${CONTAINER_NAME} started"
  exit 0
fi

${DOCKER_BIN} pull "${IMAGE_NAME}"

${DOCKER_BIN} run -d \
  --name "${CONTAINER_NAME}" \
  -p "${HOST_ADDRESS}:${HOST_PORT}:3306" \
  -e MARIADB_DATABASE="${DATABASE_NAME}" \
  -e MARIADB_USER="${DATABASE_USER}" \
  -e MARIADB_PASSWORD="${DATABASE_PASSWORD}" \
  -e MARIADB_ROOT_PASSWORD="${ROOT_PASSWORD}" \
  -v "${VOLUME_NAME}:/var/lib/mysql" \
  "${IMAGE_NAME}" >/dev/null

echo "Container ${CONTAINER_NAME} created on port ${HOST_PORT}"
