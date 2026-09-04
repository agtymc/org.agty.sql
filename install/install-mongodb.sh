#!/usr/bin/env bash
set -euo pipefail

# MongoDB is not a supported org.agty.sql driver. This helper is retained only
# for unrelated local experiments and is not part of the SQL test matrix.

CONTAINER_NAME="agty-sql-mongodb"
IMAGE_NAME="${MONGODB_IMAGE:-mongo:8}"
HOST_PORT="27018"
DATABASE_NAME="agty_sql"
DATABASE_USER="agty_sql"
DATABASE_PASSWORD="${AGTY_SQL_MONGODB_PASSWORD:?Set AGTY_SQL_MONGODB_PASSWORD}"
VOLUME_NAME="agty-sql-mongodb-data"

DOCKER_BIN="sudo docker"

if ${DOCKER_BIN} ps -a --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
  ${DOCKER_BIN} start "${CONTAINER_NAME}" >/dev/null
  echo "Container ${CONTAINER_NAME} started"
  exit 0
fi

${DOCKER_BIN} pull "${IMAGE_NAME}"

${DOCKER_BIN} run -d \
  --name "${CONTAINER_NAME}" \
  -p "127.0.0.1:${HOST_PORT}:27017" \
  -e MONGO_INITDB_ROOT_USERNAME="${DATABASE_USER}" \
  -e MONGO_INITDB_ROOT_PASSWORD="${DATABASE_PASSWORD}" \
  -e MONGO_INITDB_DATABASE="${DATABASE_NAME}" \
  -v "${VOLUME_NAME}:/data/db" \
  "${IMAGE_NAME}" >/dev/null

echo "Container ${CONTAINER_NAME} created on port ${HOST_PORT}"
