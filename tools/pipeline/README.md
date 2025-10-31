# tools/pipeline/ - Pipeline Scripts (SSOT)

**Purpose**: Single Source of Truth for all pipeline operations

## Architecture

```
Cascade Workflows (.windsurf/workflows/)
        ↓ (얇은 래퍼)
CI Jobs (.github/workflows/)
        ↓ (동일한 스크립트 호출)
tools/pipeline/ (SSOT - 실제 로직)
        ↓
.pipeline-metrics/ (메트릭 & 리포트)
```

## Scripts

### 1. common.sh
**Purpose**: Shared configuration and helper functions

**Exports**:
- `log()` - Standard logging
- `log_success()` - Success messages (green)
- `log_error()` - Error messages (red)
- `log_warning()` - Warning messages (yellow)
- `metric()` - Metric collection
- `report_failure()` - Failure report generation
- `init_pipeline()` - Pipeline initialization

**Usage**:
```bash
source "$(dirname "$0")/common.sh"
init_pipeline
log "Starting task..."
metric task_name 0 120  # Success, 120 seconds
```

### 2. validate_conventions.sh
**Purpose**: Zero-Tolerance rule validation

**Checks**:
- ✅ Lombok prohibition
- ✅ Law of Demeter (getter chaining)
- ✅ JPA relationship annotations
- ✅ Setter methods in Domain/Entity
- ✅ Transaction boundaries
- ✅ Spring proxy constraints
- ✅ Code formatting (Spotless)

**Usage**:
```bash
./tools/pipeline/validate_conventions.sh
```

**Exit Codes**:
- `0`: All checks passed
- `1`: Violations detected

**Output**:
- Metrics: `.pipeline-metrics/metrics.jsonl`
- Report: `.pipeline-metrics/report.md` (on failure)

### 3. test_unit.sh
**Purpose**: Unit test execution with optional impacted-only mode

**Options**:
- `--impacted`: Run only tests for changed packages (Fast lane)
- `--no-parallel`: Disable parallel execution

**Usage**:
```bash
# Fast lane (impacted only)
./tools/pipeline/test_unit.sh --impacted

# Full lane (all tests, parallel)
./tools/pipeline/test_unit.sh

# Sequential mode (debugging)
./tools/pipeline/test_unit.sh --no-parallel
```

**Exit Codes**:
- `0`: All tests passed
- `1`: Tests failed

**Output**:
- Metrics: `.pipeline-metrics/metrics.jsonl`
- Report: `build/reports/tests/test/index.html`

### 4. pr_gate.sh
**Purpose**: Complete PR validation pipeline

**Steps**:
1. Code Format Check (Spotless)
2. Convention Validation
3. Unit Tests (All)
4. Architecture Validation (ArchUnit)
5. Test Coverage Verification

**Usage**:
```bash
./tools/pipeline/pr_gate.sh
```

**Exit Codes**:
- `0`: All steps passed
- `1`: One or more steps failed

**Output**:
- Metrics: `.pipeline-metrics/metrics.jsonl`
- Reports: Multiple locations (see output)

**Expected Duration**:
- Fast lane: 30s ~ 2min
- Full lane: 2min ~ 5min

## Fast Lane vs Full Lane

| Mode | Command | When | Duration |
|------|---------|------|----------|
| **Fast Lane** | `--impacted` flag | Local development | < 2 min |
| **Full Lane** | Default | PR gate, CI | 2-5 min |

## Metrics Collection

All scripts automatically write metrics to `.pipeline-metrics/metrics.jsonl`:

```
<timestamp>\t<task_name>\t<status_code>\t<duration_seconds>
```

**Example**:
```
1698765432	validate_conventions	0	5
1698765437	test_unit	1	120
1698765557	pr_gate	0	180
```

**Analysis**:
```bash
# Average duration by task
cat .pipeline-metrics/metrics.jsonl | awk '{sum[$2]+=$4; count[$2]++} END {for (task in sum) print task, sum[task]/count[task]}'

# Success rate by task
cat .pipeline-metrics/metrics.jsonl | awk '{total[$2]++; if($3==0) success[$2]++} END {for (task in total) print task, (success[task]/total[task])*100 "%"}'

# Last 10 executions
tail -10 .pipeline-metrics/metrics.jsonl | column -t
```

## Failure Reports

On failure, scripts generate `.pipeline-metrics/report.md`:

```markdown
## Pipeline Failure Report
**Date**: 2024-10-29T14:30:00
**Task**: validate_conventions
**Error**: Lombok annotations detected

### Next Steps
1. Review the error details above
2. Fix the issues in your code
3. Re-run the pipeline

### Build Reports
- Checkstyle: build/reports/checkstyle/main.html
```

## CI Integration

### GitHub Actions Example

```yaml
jobs:
  pr-gate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
      - name: Run PR Gate (SSOT)
        run: ./tools/pipeline/pr_gate.sh
      - name: Upload Metrics
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: pipeline-metrics
          path: .pipeline-metrics/
```

## Adding New Scripts

### Template

```bash
#!/usr/bin/env bash
# Description of the script

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/common.sh"

set +e  # Don't exit on error immediately

start=$(date +%s)
log "Starting <task_name>..."

# Navigate to project root
cd "${PROJECT_ROOT}" || exit 1

# Your logic here
status=0

# Calculate duration
end=$(date +%s)
duration=$((end - start))

# Record metrics
metric <task_name> "${status}" "${duration}"

exit $status
```

### Steps

1. Create script: `tools/pipeline/new_task.sh`
2. Make executable: `chmod +x tools/pipeline/new_task.sh`
3. Test locally: `./tools/pipeline/new_task.sh`
4. Create Cascade workflow (thin wrapper)
5. Update CI to use the script

## Benefits of SSOT

- ✅ **No Drift**: Cascade and CI use identical logic
- ✅ **Single Update**: Change logic in one place
- ✅ **Version Control**: Git tracks all changes
- ✅ **Metrics**: Automatic collection and analysis
- ✅ **Consistency**: Guaranteed across environments

## Related

- **Cascade Workflows**: `.windsurf/workflows/`
- **Metrics**: `.pipeline-metrics/metrics.jsonl`
- **Reports**: `.pipeline-metrics/report.md`
- **Documentation**: `claudedocs/windsurf-refactoring-summary.md`
