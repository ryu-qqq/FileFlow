#!/usr/bin/env bash
# Common configuration and helpers for pipeline scripts
# Part of SSOT (Single Source of Truth) architecture

set -Eeuo pipefail

# Gradle common options
GRADLE_COMMON_OPTS=(--scan --no-daemon --stacktrace)
export JAVA_TOOL_OPTIONS="-XX:+UseZGC"

# Project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CACHE_DIR="${PROJECT_ROOT}/.cascade"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging helpers
log() {
    echo -e "${BLUE}[PIPELINE]${NC} $(date +%FT%T) $*"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $(date +%FT%T) $*"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date +%FT%T) $*"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $(date +%FT%T) $*"
}

# Metric collection
# Usage: metric <task_name> <status_code> <duration_seconds>
metric() {
    local task=$1
    local status=$2
    local duration=$3
    local timestamp=$(date +%s)

    # Ensure cache directory exists
    mkdir -p "${CACHE_DIR}"

    # Append to metrics.jsonl
    echo "${timestamp}\t${task}\t${status}\t${duration}" >> "${CACHE_DIR}/metrics.jsonl"

    if [[ $status -eq 0 ]]; then
        log_success "Task '${task}' completed in ${duration}s"
    else
        log_error "Task '${task}' failed with code ${status} after ${duration}s"
    fi
}

# Report generation helper
# Usage: report_failure <task_name> <error_message>
report_failure() {
    local task=$1
    local error_msg=$2

    # Ensure cache directory exists
    mkdir -p "${CACHE_DIR}"

    # Update report.md
    {
        echo "## Pipeline Failure Report"
        echo "**Date**: $(date +%FT%T)"
        echo "**Task**: ${task}"
        echo "**Error**: ${error_msg}"
        echo ""
        echo "### Next Steps"
        echo "1. Review the error details above"
        echo "2. Fix the issues in your code"
        echo "3. Re-run the pipeline"
        echo ""
        echo "### Build Reports"
        echo "- Checkstyle: build/reports/checkstyle/main.html"
        echo "- SpotBugs: build/reports/spotbugs/main.html"
        echo "- Test: build/reports/tests/test/index.html"
    } > "${CACHE_DIR}/report.md"

    log_warning "Failure report written to ${CACHE_DIR}/report.md"
}

# Check if Gradle wrapper exists
check_gradle() {
    if [[ ! -f "${PROJECT_ROOT}/gradlew" ]]; then
        log_error "Gradle wrapper not found. Run from project root."
        exit 1
    fi
}

# Check Java version
check_java() {
    if ! command -v java &> /dev/null; then
        log_error "Java not found. Please install Java 21+"
        exit 1
    fi

    local java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    log "Using Java version: ${java_version}"
}

# Initialize pipeline
init_pipeline() {
    log "Initializing pipeline..."
    check_gradle
    check_java

    # Create cache directory if not exists
    mkdir -p "${CACHE_DIR}"

    log "Pipeline initialized successfully"
}

# Export functions for use in other scripts
export -f log
export -f log_success
export -f log_error
export -f log_warning
export -f metric
export -f report_failure
export -f check_gradle
export -f check_java
export -f init_pipeline
