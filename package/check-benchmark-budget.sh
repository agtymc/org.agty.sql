#!/usr/bin/env bash
set -euo pipefail

RESULT_FILE="${1:?Usage: package/check-benchmark-budget.sh JMH_RESULT_JSON}"

[[ -f "${RESULT_FILE}" ]] || {
  echo "JMH result does not exist: ${RESULT_FILE}" >&2
  exit 1
}

# Ratios compare work performed in the same fork and are deliberately broad
# enough for shared runners. Absolute timings remain diagnostic baselines.
awk '
  /"benchmark"[[:space:]]*:/ {
    benchmark = $3
    gsub(/[",]/, "", benchmark)
    next
  }
  /"primaryMetric"[[:space:]]*:/ {
    primary = 1
    next
  }
  primary && /"score"[[:space:]]*:/ {
    value = $3
    gsub(/,/, "", value)
    scores[benchmark] = value + 0
    primary = 0
  }
  END {
    legacy = scores["org.agty.sql.benchmarks.CorePathBenchmark.renderLegacyUpdate"]
    prepared = scores["org.agty.sql.benchmarks.CorePathBenchmark.renderPreparedUpdate"]
    rebuild = scores["org.agty.sql.benchmarks.CorePathBenchmark.rebuildStructuredQuery"]
    row = scores["org.agty.sql.benchmarks.CorePathBenchmark.readConvertedRow"]

    if (!(legacy > 0 && prepared > 0 && rebuild > 0 && row > 0)) {
      print "Performance budget is missing one or more positive primary scores" > "/dev/stderr"
      exit 1
    }

    preparedRatio = prepared / legacy
    rebuildRatio = rebuild / legacy
    rowRatio = row / legacy
    printf "Performance ratios: prepared/legacy=%.3f, rebuild/legacy=%.3f, row/legacy=%.3f\n", \
      preparedRatio, rebuildRatio, rowRatio

    failed = 0
    if (preparedRatio > 1.25) {
      print "Prepared rendering exceeded the 1.25x legacy-rendering budget" > "/dev/stderr"
      failed = 1
    }
    if (rebuildRatio > 0.75) {
      print "Structured query rebuilding exceeded the 0.75x legacy-rendering budget" > "/dev/stderr"
      failed = 1
    }
    if (rowRatio > 0.25) {
      print "Row conversion exceeded the 0.25x legacy-rendering budget" > "/dev/stderr"
      failed = 1
    }
    exit failed
  }
' "${RESULT_FILE}"
