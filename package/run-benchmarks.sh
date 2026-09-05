#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-smoke}"
shift || true
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="2.1.0"
JAR="${ROOT_DIR}/benchmarks/target/org-agty-sql-benchmarks-${VERSION}.jar"

case "${MODE}" in
  smoke)
    RESULT_FILE="${ROOT_DIR}/benchmarks/target/jmh-smoke.json"
    JMH_OPTIONS=(-wi 1 -i 1 -w 1s -r 1s -f 1)
    ;;
  full)
    RESULT_FILE="${ROOT_DIR}/benchmarks/target/jmh-result.json"
    JMH_OPTIONS=(-wi 3 -i 5 -w 2s -r 3s -f 2)
    ;;
  *)
    echo "Usage: package/run-benchmarks.sh [smoke|full] [JMH options]" >&2
    exit 1
    ;;
esac

cd "${ROOT_DIR}"
./mvnw --batch-mode \
  -DskipTests \
  -Djacoco.skip=true \
  -Dspotbugs.skip=true \
  -Dpmd.skip=true \
  -Dmaven.javadoc.skip=true \
  install
./mvnw --batch-mode -f benchmarks/pom.xml clean package

java -jar "${JAR}" \
  "${JMH_OPTIONS[@]}" \
  -rf json \
  -rff "${RESULT_FILE}" \
  "$@"

EXPECTED_BENCHMARKS=(
  "CorePathBenchmark.readConvertedRow"
  "CorePathBenchmark.rebuildStructuredQuery"
  "CorePathBenchmark.renderLegacyUpdate"
  "CorePathBenchmark.renderPreparedUpdate"
  "PooledDataSourceBenchmark.borrowSelectAndReturn"
  "PooledDataSourceTimeoutBenchmark.borrowUnderSaturation"
  '"successfulBorrows"'
  '"timeouts"'
)
for benchmark in "${EXPECTED_BENCHMARKS[@]}"; do
  grep -Fq "${benchmark}" "${RESULT_FILE}" || {
    echo "JMH result is missing benchmark: ${benchmark}" >&2
    exit 1
  }
done

awk '
  /"successfulBorrows"[[:space:]]*:/ { metric = "successfulBorrows"; next }
  /"timeouts"[[:space:]]*:/ { metric = "timeouts"; next }
  metric != "" && /"score"[[:space:]]*:/ {
    value = $3
    gsub(/,/, "", value)
    if (value + 0 > 0) seen[metric] = 1
    metric = ""
  }
  END { exit !(seen["successfulBorrows"] && seen["timeouts"]) }
' "${RESULT_FILE}" || {
  echo "JMH timeout benchmark must report positive successfulBorrows and timeouts" >&2
  exit 1
}
