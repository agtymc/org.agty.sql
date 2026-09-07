#!/usr/bin/env bash
set -euo pipefail

BASELINE_JAR="${1:?Usage: package/run-2x-upgrade-test.sh BASELINE_JAR CURRENT_JAR}"
CURRENT_JAR="${2:?Usage: package/run-2x-upgrade-test.sh BASELINE_JAR CURRENT_JAR}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK_DIR="$(mktemp -d)"

cleanup() {
  rm -rf "${WORK_DIR}"
}
trap cleanup EXIT

[[ -f "${BASELINE_JAR}" ]] || {
  echo "Baseline JAR does not exist: ${BASELINE_JAR}" >&2
  exit 1
}
[[ -f "${CURRENT_JAR}" ]] || {
  echo "Current JAR does not exist: ${CURRENT_JAR}" >&2
  exit 1
}

mkdir -p "${WORK_DIR}/classes"
javac \
  --release 18 \
  -cp "${BASELINE_JAR}" \
  -d "${WORK_DIR}/classes" \
  "${ROOT_DIR}/compatibility/2.0.4/Legacy2xClient.java"

"${ROOT_DIR}/mvnw" \
  --batch-mode \
  -q \
  -f "${ROOT_DIR}/pom.xml" \
  dependency:build-classpath \
  -Dmdep.outputFile="${WORK_DIR}/runtime-classpath.txt"

RUNTIME_CLASSPATH="$(<"${WORK_DIR}/runtime-classpath.txt")"
java \
  -ea \
  -cp "${WORK_DIR}/classes:${CURRENT_JAR}:${RUNTIME_CLASSPATH}" \
  org.agty.sql.compatibility.Legacy2xClient \
  "${WORK_DIR}/upgrade-db"

echo "2.x binary upgrade test passed: compiled with baseline, executed with current JAR"
