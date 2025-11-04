#!/bin/bash

# Hook Integration Test Script
# Claude Code가 Hook을 실행하는 것을 시뮬레이션

echo "=========================================="
echo "Claude Code Hook Integration Test"
echo "=========================================="
echo ""

# 로그 파일 초기화
rm -f .claude/logs/hook-execution.log

# Test Case 1: Aggregate 키워드
echo "Test 1: Order Aggregate 만들어줘"
echo "Order Aggregate 만들어줘" | .claude/hooks/user-prompt-submit.sh > /tmp/hook-output-1.txt
echo "✅ Hook executed"
echo ""

# Test Case 2: Controller 키워드
echo "Test 2: OrderController 만들어줘"
echo "OrderController 만들어줘" | .claude/hooks/user-prompt-submit.sh > /tmp/hook-output-2.txt
echo "✅ Hook executed"
echo ""

# Test Case 3: 일반 요청 (Low Confidence)
echo "Test 3: 파일 읽어줘"
echo "파일 읽어줘" | .claude/hooks/user-prompt-submit.sh > /tmp/hook-output-3.txt
echo "✅ Hook executed"
echo ""

echo "=========================================="
echo "Hook Execution Log"
echo "=========================================="
cat .claude/logs/hook-execution.log
echo ""

echo "=========================================="
echo "Hook Output Comparison"
echo "=========================================="

echo "--- Test 1 Output (High Score) ---"
cat /tmp/hook-output-1.txt
echo ""

echo "--- Test 2 Output (High Score) ---"
cat /tmp/hook-output-2.txt
echo ""

echo "--- Test 3 Output (Low Score) ---"
cat /tmp/hook-output-3.txt
echo ""

echo "=========================================="
echo "Validation"
echo "=========================================="

# 검증 1: 로그 파일 생성됨?
if [[ -f .claude/logs/hook-execution.log ]]; then
    echo "✅ Log file created"
else
    echo "❌ Log file NOT created"
fi

# 검증 2: High Score에서 상세 규칙 주입됨?
if grep -q "Law of Demeter" /tmp/hook-output-1.txt; then
    echo "✅ Detailed rules injected for high score"
else
    echo "❌ Detailed rules NOT injected"
fi

# 검증 3: Low Score에서 기본 규칙만 주입됨?
if grep -q "Zero-Tolerance" /tmp/hook-output-3.txt && ! grep -q "Law of Demeter" /tmp/hook-output-3.txt; then
    echo "✅ Basic rules only for low score"
else
    echo "❌ Rule injection strategy incorrect"
fi

# 검증 4: 원본 입력 보존됨?
if grep -q "Order Aggregate 만들어줘" /tmp/hook-output-1.txt; then
    echo "✅ Original input preserved"
else
    echo "❌ Original input NOT preserved"
fi

echo ""
echo "=========================================="
echo "Test Complete!"
echo "=========================================="
