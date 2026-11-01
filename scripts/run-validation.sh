#!/usr/bin/env bash
# Creates actual Maven logs for all three required mutation experiments.
set -uo pipefail
cd "$(dirname "$0")/.."
source_file="src/main/java/mn/edu/must/sqat/CourseRegistrationService.java"
original="$(mktemp)"
cp "$source_file" "$original"
restore() { cp "$original" "$source_file"; rm -f "$original"; }
trap restore EXIT
mkdir -p results

if ! command -v mvn >/dev/null 2>&1; then
  echo 'mvn command not found. Debian: sudo apt install maven' >&2
  exit 2
fi

run_and_record() {
  local label="$1"
  local out="$2"
  echo "=== $label ==="
  mvn test 2>&1 | tee "$out"
  local status=${PIPESTATUS[0]}
  echo "${label}: exit=${status}" | tee -a results/validation-summary.txt
  LAST_STATUS=$status
}

: > results/validation-summary.txt
run_and_record 'Baseline before mutants' results/mvn-test.txt
if [[ $LAST_STATUS -ne 0 ]]; then
  echo 'Baseline failed: fix tests before attempting mutations.' >&2
  exit 1
fi

make_mutant() {
  local mutant="$1"
  cp "$original" "$source_file"
  python3 - "$source_file" "$mutant" <<'PY_MUTANT'
from pathlib import Path
import sys
file=Path(sys.argv[1]); mutant=int(sys.argv[2])
source=file.read_text(encoding='utf-8')
changes={
  1: ('if (!student.active()) return "ERROR_INACTIVE_STUDENT";', '// MUTANT 1: inactive check removed'),
  2: ('repository.saveRegistration(studentId, courseId);', '// MUTANT 2: saveRegistration call removed'),
  3: ('if (studentId == null || courseId == null) {', 'if (studentId == null) { // MUTANT 3: courseId null guard removed'),
}
old,new=changes[mutant]
if source.count(old) != 1:
    raise SystemExit(f'Could not safely apply mutation {mutant}: expected exactly one match')
file.write_text(source.replace(old,new,1),encoding='utf-8')
PY_MUTANT
}

unexpected=0
for n in 1 2 3; do
  make_mutant "$n" || exit 1
  run_and_record "Mutant ${n}" "results/mvn-test-mutant-${n}.txt"
  if [[ $LAST_STATUS -eq 0 ]]; then
    echo "WARNING: mutant ${n} survived. Add a stronger test before submitting." >&2
    unexpected=1
  fi
done

cp "$original" "$source_file"
run_and_record 'Final baseline after restoration' results/mvn-test.txt
if [[ $LAST_STATUS -ne 0 ]]; then
  echo 'Final baseline failed; original source restored on exit.' >&2
  exit 1
fi
if [[ $unexpected -eq 1 ]]; then
  echo 'Some mutations survived. Inspect results and strengthen tests.' >&2
  exit 1
fi

echo 'All three mutants caused a failing Maven run. Original source restored.'
