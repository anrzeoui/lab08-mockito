#!/usr/bin/env bash
# Insert the real local Java/Maven version and validate actual Maven evidence files.
set -euo pipefail
cd "$(dirname "$0")/.."
python3 - <<'PY_ENV'
from pathlib import Path
import re
import subprocess
import sys
readme = Path('README.md')
if not readme.exists():
    raise SystemExit('ERROR: README.md not found')
source = readme.read_text(encoding='utf-8')
start_tag = '<!-- ENVIRONMENT_START -->'
end_tag = '<!-- ENVIRONMENT_END -->'
if start_tag not in source or end_tag not in source:
    raise SystemExit('ERROR: README environment markers missing')

def cmd(args):
    try:
        p = subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, timeout=30)
    except (OSError, subprocess.TimeoutExpired) as exc:
        raise SystemExit(f'ERROR: {args[0]} failed: {exc}')
    if p.returncode != 0:
        raise SystemExit(f'ERROR: {args[0]} returned {p.returncode}: {p.stdout}')
    return p.stdout.strip()

java=cmd(['java','-version'])
mvn=cmd(['mvn','-version'])
block = ('<!-- ENVIRONMENT_START -->\n'
    '**`java -version` (бодит гаралт):**\n\n```text\n'+java+'\n```\n\n'
    '**`mvn -version` (бодит гаралт):**\n\n```text\n'+mvn+'\n```\n'
    '<!-- ENVIRONMENT_END -->')
source=re.sub(re.escape(start_tag)+r'.*?'+re.escape(end_tag), lambda _: block, source, count=1, flags=re.S)
readme.write_text(source,encoding='utf-8')
print('Updated README.md using real Java and Maven version output.\n')
required={'mvn-test.txt':('BUILD SUCCESS',0),'mvn-test-mutant-1.txt':('BUILD FAILURE',2),'mvn-test-mutant-2.txt':('BUILD FAILURE',2),'mvn-test-mutant-3.txt':('BUILD FAILURE',1)}
valid=True
for fname, (status, expected_failures) in required.items():
    p=Path('results')/fname
    if not p.is_file():
        print('ERROR: Missing', p)
        valid=False
        continue
    data=p.read_text(errors='replace')
    matches=re.findall(r'Tests run:\s*(\d+),\s*Failures:\s*(\d+),\s*Errors:\s*(\d+),\s*Skipped:\s*(\d+)',data)
    stats=tuple(map(int,matches[-1])) if matches else None
    ok=status in data and stats==(10, expected_failures, 0, 0)
    print(('OK' if ok else 'ERROR')+': '+str(p)+f' -> {stats}, {status if status in data else "status missing"}')
    valid=valid and ok
print('\nAll four files verified.' if valid else '\nPlease check the listed results files.')
if not valid: sys.exit(1)
PY_ENV
