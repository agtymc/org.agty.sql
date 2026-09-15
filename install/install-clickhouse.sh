#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="agty-sql-clickhouse"
IMAGE_NAME="${CLICKHOUSE_IMAGE:-clickhouse/clickhouse-server:26.8.4.11}"
HOST_ADDRESS="${HOST_ADDRESS:-127.0.0.1}"
HOST_HTTP_PORT="28123"
HOST_NATIVE_PORT="29000"
DATABASE_NAME="agty_sql"
DATABASE_USER="agty_sql"
DATABASE_PASSWORD="${AGTY_SQL_CLICKHOUSE_PASSWORD:-agty_sql}"
VOLUME_NAME="agty-sql-clickhouse-data"

DOCKER_BIN="${DOCKER_BIN:-docker}"

if ! ${DOCKER_BIN} ps >/dev/null 2>&1; then
  DOCKER_BIN="sudo docker"
fi

if ${DOCKER_BIN} ps -a --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
  ${DOCKER_BIN} start "${CONTAINER_NAME}" >/dev/null
  echo "Container ${CONTAINER_NAME} started"
  exit 0
fi

${DOCKER_BIN} pull "${IMAGE_NAME}"

${DOCKER_BIN} run -d \
  --name "${CONTAINER_NAME}" \
  --ulimit nofile=262144:262144 \
  -p "${HOST_ADDRESS}:${HOST_HTTP_PORT}:8123" \
  -p "${HOST_ADDRESS}:${HOST_NATIVE_PORT}:9000" \
  -e CLICKHOUSE_DB="${DATABASE_NAME}" \
  -e CLICKHOUSE_USER="${DATABASE_USER}" \
  -e CLICKHOUSE_PASSWORD="${DATABASE_PASSWORD}" \
  -e CLICKHOUSE_DEFAULT_ACCESS_MANAGEMENT=1 \
  -v "${VOLUME_NAME}:/var/lib/clickhouse" \
  "${IMAGE_NAME}" >/dev/null

echo "Container ${CONTAINER_NAME} created on HTTP port ${HOST_HTTP_PORT} and native port ${HOST_NATIVE_PORT}"
