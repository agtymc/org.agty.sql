#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="agty-sql-mssql"
IMAGE_NAME="${MSSQL_IMAGE:-mcr.microsoft.com/mssql/server:2022-CU26-ubuntu-22.04}"
HOST_ADDRESS="${HOST_ADDRESS:-127.0.0.1}"
HOST_PORT="21433"
DATABASE_NAME="agty_sql"
DATABASE_USER="sa"
DATABASE_PASSWORD="${AGTY_SQL_MSSQL_PASSWORD:?Set AGTY_SQL_MSSQL_PASSWORD}"
VOLUME_NAME="agty-sql-mssql-data"
SQLCMD_BIN="/opt/mssql-tools18/bin/sqlcmd"

DOCKER_BIN="sudo docker"

if ${DOCKER_BIN} ps -a --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
  ${DOCKER_BIN} start "${CONTAINER_NAME}" >/dev/null
  echo "Container ${CONTAINER_NAME} started"
else
  ${DOCKER_BIN} pull "${IMAGE_NAME}"

  ${DOCKER_BIN} run -d \
    --name "${CONTAINER_NAME}" \
    -p "${HOST_ADDRESS}:${HOST_PORT}:1433" \
    -e ACCEPT_EULA=Y \
    -e MSSQL_PID=Developer \
    -e MSSQL_SA_PASSWORD="${DATABASE_PASSWORD}" \
    -v "${VOLUME_NAME}:/var/opt/mssql" \
    "${IMAGE_NAME}" >/dev/null

  echo "Container ${CONTAINER_NAME} created on port ${HOST_PORT}"
fi

for attempt in $(seq 1 30); do
  if ${DOCKER_BIN} exec "${CONTAINER_NAME}" "${SQLCMD_BIN}" -C -S localhost -U "${DATABASE_USER}" -P "${DATABASE_PASSWORD}" -Q "SELECT 1" >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

${DOCKER_BIN} exec "${CONTAINER_NAME}" "${SQLCMD_BIN}" -C -S localhost -U "${DATABASE_USER}" -P "${DATABASE_PASSWORD}" -Q "
IF DB_ID(N'${DATABASE_NAME}') IS NULL
BEGIN
    CREATE DATABASE [${DATABASE_NAME}];
END
" >/dev/null

echo "Database ${DATABASE_NAME} is ready"
echo "Login: ${DATABASE_USER}"
