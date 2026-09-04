#!/usr/bin/env bash
set -euo pipefail

BASELINE_VERSION="2.0.4"
BASELINE_COMMIT="a5368c11299327e88ae2bd3c3d650339c8490a0b"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASELINE_DIR="$(mktemp -d)"

cleanup() {
  rm -rf "${BASELINE_DIR}"
}
trap cleanup EXIT

cd "${ROOT_DIR}"
git cat-file -e "${BASELINE_COMMIT}^{commit}"
git archive "${BASELINE_COMMIT}" | tar -x -C "${BASELINE_DIR}"

"${ROOT_DIR}/mvnw" --batch-mode \
  -f "${BASELINE_DIR}/pom.xml" \
  -DskipTests \
  -Dmaven.javadoc.skip=true \
  clean package

BASELINE_JAR="${BASELINE_DIR}/target/org-agty-sql-${BASELINE_VERSION}.jar"
[[ -f "${BASELINE_JAR}" ]] || {
  echo "Baseline artifact was not built: ${BASELINE_JAR}" >&2
  exit 1
}

"${ROOT_DIR}/mvnw" --batch-mode \
  -Papi-compatibility \
  -Djapicmp.baseline.jar="${BASELINE_JAR}" \
  "$@" \
  clean verify
