#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="agty-sql-mongodb"
IMAGE_NAME="mongo:8"
HOST_PORT="27018"
DATABASE_NAME="agty_sql"
DATABASE_USER="agty_sql"
DATABASE_PASSWORD="agty_sql_mongodb_pass"
VOLUME_NAME="agty-sql-mongodb-data"

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
  -p "${HOST_PORT}:27017" \
  -e MONGO_INITDB_ROOT_USERNAME="${DATABASE_USER}" \
  -e MONGO_INITDB_ROOT_PASSWORD="${DATABASE_PASSWORD}" \
  -e MONGO_INITDB_DATABASE="${DATABASE_NAME}" \
  -v "${VOLUME_NAME}:/data/db" \
  "${IMAGE_NAME}" >/dev/null

echo "Container ${CONTAINER_NAME} created on port ${HOST_PORT}"
