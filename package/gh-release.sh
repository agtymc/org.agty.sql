#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:?Usage: package/gh-release.sh VERSION}"
TAG="v${VERSION}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Release requires a clean Git worktree" >&2
  exit 1
fi

POM_VERSION="$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)"
CODE_VERSION="$(sed -n 's/.*VERSION = "\([^"]*\)".*/\1/p' src/main/java/org/agty/sql/AgtySQL.java)"

if [[ "${POM_VERSION}" != "${VERSION}" || "${CODE_VERSION}" != "${VERSION}" ]]; then
  echo "Version mismatch: requested=${VERSION}, pom=${POM_VERSION}, code=${CODE_VERSION}" >&2
  exit 1
fi

for readme in README.md README.ru.md; do
  grep -Fq "<version>${VERSION}</version>" "${readme}" || {
    echo "${readme} does not reference release version ${VERSION}" >&2
    exit 1
  }
done

BENCHMARK_VERSION="$(./mvnw -f benchmarks/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)"
BENCHMARK_LIBRARY_VERSION="$(./mvnw -f benchmarks/pom.xml help:evaluate -Dexpression=org.agty.sql.version -q -DforceStdout)"
if [[ "${BENCHMARK_VERSION}" != "${VERSION}" || "${BENCHMARK_LIBRARY_VERSION}" != "${VERSION}" ]]; then
  echo "Benchmark version mismatch: project=${BENCHMARK_VERSION}, library=${BENCHMARK_LIBRARY_VERSION}" >&2
  exit 1
fi

grep -Fq "## ${VERSION} -" CHANGELOG.md || {
  echo "CHANGELOG.md has no ${VERSION} release section" >&2
  exit 1
}

if gh release view "${TAG}" >/dev/null 2>&1; then
  echo "GitHub Release ${TAG} already exists" >&2
  exit 1
fi

package/check-api-compatibility.sh
package/run-benchmarks.sh smoke

if ! git rev-parse --verify "refs/tags/${TAG}" >/dev/null 2>&1; then
  git tag --sign "${TAG}" --message "org.agty.sql ${VERSION}"
fi
git verify-tag "${TAG}"
HEAD_COMMIT="$(git rev-parse HEAD)"
[[ "$(git rev-list -n 1 "${TAG}")" == "${HEAD_COMMIT}" ]] || {
  echo "Tag ${TAG} does not point to HEAD" >&2
  exit 1
}

git push origin "refs/tags/${TAG}"

CI_RUN_ID=""
for _ in {1..30}; do
  CI_RUN_ID="$(gh run list \
    --workflow ci.yml \
    --event push \
    --branch "${TAG}" \
    --limit 10 \
    --json databaseId,headSha \
    --jq ".[] | select(.headSha == \"${HEAD_COMMIT}\") | .databaseId" \
    | head -n 1)"
  [[ -n "${CI_RUN_ID}" ]] && break
  sleep 2
done
[[ -n "${CI_RUN_ID}" ]] || {
  echo "Unable to find hosted CI run for ${TAG}" >&2
  exit 1
}
gh run watch "${CI_RUN_ID}" --exit-status

gh workflow run publish-github-packages.yml --ref "${TAG}" -f "version=${VERSION}"
PUBLISH_RUN_ID=""
for _ in {1..30}; do
  PUBLISH_RUN_ID="$(gh run list \
    --workflow publish-github-packages.yml \
    --event workflow_dispatch \
    --branch "${TAG}" \
    --limit 10 \
    --json databaseId,headSha \
    --jq ".[] | select(.headSha == \"${HEAD_COMMIT}\") | .databaseId" \
    | head -n 1)"
  [[ -n "${PUBLISH_RUN_ID}" ]] && break
  sleep 2
done
[[ -n "${PUBLISH_RUN_ID}" ]] || {
  echo "Unable to find release publication run for ${TAG}" >&2
  exit 1
}
gh run watch "${PUBLISH_RUN_ID}" --exit-status
