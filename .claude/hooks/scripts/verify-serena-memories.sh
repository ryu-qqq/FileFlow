#!/bin/bash

# =====================================================
# Serena Memory Verification Tool
# Purpose: Verify Serena MCP memories are properly set up
# Usage: bash verify-serena-memories.sh
# =====================================================

set -euo pipefail

echo "🔍 Serena Memory Verification Tool"
echo "=================================="
echo ""

# 색상
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 프로젝트 루트
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../" && pwd)"
SERENA_DIR="$PROJECT_ROOT/.serena/memories"

# 결과 카운터
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 테스트 헬퍼
test_pass() {
    echo -e "${GREEN}✅ PASS${NC}: $1"
    PASSED_TESTS=$((PASSED_TESTS + 1))
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
}

test_fail() {
    echo -e "${RED}❌ FAIL${NC}: $1"
    FAILED_TESTS=$((FAILED_TESTS + 1))
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
}

test_warn() {
    echo -e "${YELLOW}⚠️  WARN${NC}: $1"
}

test_info() {
    echo -e "${BLUE}ℹ️  INFO${NC}: $1"
}

echo "## 1. Serena 디렉토리 구조 검증"
echo "-----------------------------------"

# 1.1 .serena/memories 디렉토리 존재 확인
if [ -d "$SERENA_DIR" ]; then
    test_pass ".serena/memories 디렉토리 존재"
else
    test_fail ".serena/memories 디렉토리 없음"
    echo ""
    echo "💡 해결 방법:"
    echo "   bash .claude/hooks/scripts/setup-serena-conventions.sh"
    exit 1
fi

# 1.2 필수 메모리 파일 존재 확인
REQUIRED_MEMORIES=(
    "coding_convention_index.md"
    "coding_convention_domain_layer.md"
    "coding_convention_application_layer.md"
    "coding_convention_persistence_layer.md"
    "coding_convention_rest_api_layer.md"
)

echo ""
for memory in "${REQUIRED_MEMORIES[@]}"; do
    if [ -f "$SERENA_DIR/$memory" ]; then
        test_pass "메모리 파일: $memory"
    else
        test_fail "메모리 파일 없음: $memory"
    fi
done

echo ""
echo "## 2. 메모리 파일 내용 검증"
echo "-----------------------------------"

# 2.1 파일 크기 확인 (0바이트가 아닌지)
echo ""
for memory in "${REQUIRED_MEMORIES[@]}"; do
    file_path="$SERENA_DIR/$memory"

    if [ -f "$file_path" ]; then
        file_size=$(wc -c < "$file_path" | tr -d ' ')

        if [ "$file_size" -gt 0 ]; then
            test_pass "$memory: $file_size bytes"
        else
            test_fail "$memory: 파일이 비어있음 (0 bytes)"
        fi
    fi
done

# 2.2 메모리 파일 구조 검증 (헤더, 섹션 존재)
echo ""
echo "## 3. 메모리 파일 구조 검증"
echo "-----------------------------------"
echo ""

for memory in "${REQUIRED_MEMORIES[@]}"; do
    file_path="$SERENA_DIR/$memory"

    if [ -f "$file_path" ]; then
        # Markdown 헤더 확인
        if grep -q "^# " "$file_path"; then
            test_pass "$memory: Markdown 헤더 존재"
        else
            test_fail "$memory: Markdown 헤더 없음"
        fi

        # 규칙 섹션 확인
        if grep -qE "(금지|필수|Zero-Tolerance)" "$file_path"; then
            test_pass "$memory: 규칙 섹션 존재"
        else
            test_warn "$memory: 규칙 섹션이 명확하지 않음"
        fi
    fi
done

echo ""
echo "## 4. Serena MCP 연결 테스트 (선택)"
echo "-----------------------------------"
echo ""

# Serena MCP가 설정되어 있는지 확인
if command -v claude &> /dev/null; then
    test_info "Claude Code CLI 설치됨"

    # MCP 서버 설정 확인
    if [ -f "$HOME/.claude/config.json" ]; then
        if grep -q "serena" "$HOME/.claude/config.json"; then
            test_pass "Serena MCP 서버 설정 확인됨"
        else
            test_warn "Serena MCP 서버가 config.json에 없음"
            test_info "Serena MCP 설정 방법: https://github.com/serena-mcp"
        fi
    else
        test_warn "Claude Code 설정 파일 없음 (~/.claude/config.json)"
    fi
else
    test_warn "Claude Code CLI 미설치"
fi

echo ""
echo "## 5. Hook 시스템과의 통합 검증"
echo "-----------------------------------"
echo ""

# user-prompt-submit.sh에서 Serena 메모리 로드 코드 확인
HOOK_SCRIPT="$PROJECT_ROOT/.claude/hooks/user-prompt-submit.sh"

if [ -f "$HOOK_SCRIPT" ]; then
    if grep -q "read_memory" "$HOOK_SCRIPT"; then
        test_pass "Hook에서 read_memory 참조 확인됨"
    else
        test_warn "Hook에서 read_memory 호출 없음 (출력만 함)"
    fi

    if grep -q "coding_convention_" "$HOOK_SCRIPT"; then
        test_pass "Hook에서 코딩 컨벤션 메모리 참조"
    else
        test_fail "Hook에서 코딩 컨벤션 메모리 참조 없음"
    fi
else
    test_fail "Hook 스크립트 없음: $HOOK_SCRIPT"
fi

echo ""
echo "## 6. 최근 Hook 로그 분석"
echo "-----------------------------------"
echo ""

LOG_FILE="$PROJECT_ROOT/.claude/hooks/logs/hook-execution.jsonl"

if [ -f "$LOG_FILE" ]; then
    test_pass "Hook 로그 파일 존재: hook-execution.jsonl"

    # 최근 Serena 로드 이벤트 확인
    recent_serena=$(grep "serena_memory_load" "$LOG_FILE" 2>/dev/null | tail -1)

    if [ -n "$recent_serena" ]; then
        test_pass "최근 Serena 메모리 로드 이벤트 발견"
        test_info "내용: $recent_serena"
    else
        test_warn "Serena 메모리 로드 이벤트 없음"
        test_info "Hook이 실행되지 않았거나 키워드 매칭이 안 됨"
    fi

    # 최근 세션 수 확인
    session_count=$(grep "session_start" "$LOG_FILE" 2>/dev/null | wc -l | tr -d ' ')
    test_info "총 세션 수: $session_count"
else
    test_warn "Hook 로그 파일 없음 (아직 Hook이 실행되지 않음)"
fi

echo ""
echo "=================================="
echo "## 📊 검증 결과 요약"
echo "=================================="
echo ""
echo "총 테스트: $TOTAL_TESTS"
echo -e "${GREEN}통과: $PASSED_TESTS${NC}"
echo -e "${RED}실패: $FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✅ 모든 검증 통과!${NC}"
    echo ""
    echo "💡 다음 단계:"
    echo "   1. Claude Code 실행: claude code"
    echo "   2. /cc:load 명령어로 컨벤션 로드"
    echo "   3. 코드 생성 시 Serena 메모리 자동 참조됨"
    exit 0
else
    echo -e "${RED}❌ 일부 검증 실패${NC}"
    echo ""
    echo "💡 문제 해결:"
    echo "   1. 메모리 재생성: bash .claude/hooks/scripts/setup-serena-conventions.sh"
    echo "   2. Hook 로그 확인: tail -f .claude/hooks/logs/hook-execution.jsonl"
    echo "   3. 로그 요약 확인: python3 .claude/hooks/scripts/summarize-hook-logs.py"
    exit 1
fi
