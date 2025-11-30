#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR=$(cd "$(dirname "$0")/../.." && pwd)
cd "$ROOT_DIR"
EXIT=0
# BOM check
if git ls-files | grep -E "\.java$" | xargs -I{} bash -c 'if head -c 3 "{}" | grep -q "$(printf "\xEF\xBB\xBF")"; then echo "BOM found: {}"; exit 10; fi'; then :; else EXIT=1; fi
# Escaped quote check that looks like generated junk patterns in annotations
if grep -R --line-number --include='*.java' '\\"' backend || true; then
  echo "Found suspicious escaped quotes in Java sources (\\\"). Please fix."; EXIT=1; fi
exit $EXIT
