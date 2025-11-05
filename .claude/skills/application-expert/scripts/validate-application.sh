#!/bin/bash

# Application Layer Validation Script
# Usage: bash validate-application.sh [file_path]

set -e

FILE_PATH="${1}"

if [ -z "$FILE_PATH" ]; then
    echo "Usage: bash validate-application.sh [file_path]"
    exit 1
fi

if [ ! -f "$FILE_PATH" ]; then
    echo "Error: File not found: $FILE_PATH"
    exit 1
fi

echo "🔍 Validating Application Layer: $FILE_PATH"
echo ""

VIOLATIONS=0

# Check @Transactional on private methods (Zero-Tolerance)
if grep -q "private.*@Transactional\|@Transactional.*private" "$FILE_PATH"; then
    echo "❌ ZERO-TOLERANCE: @Transactional on private methods does not work (Spring Proxy)"
    VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check Transaction boundary (external API calls)
if grep -A 30 "@Transactional" "$FILE_PATH" | grep -q "restTemplate\|webClient\|feignClient\|httpClient"; then
    echo "❌ ZERO-TOLERANCE: External API calls inside @Transactional (Transaction boundary violation)"
    VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check final class with @Transactional
if grep -q "public final class.*Service" "$FILE_PATH" && grep -q "@Transactional" "$FILE_PATH"; then
    echo "❌ ZERO-TOLERANCE: @Transactional on final class does not work (Spring Proxy)"
    VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check UseCase interface
if grep -q "class.*Service" "$FILE_PATH" && ! grep -q "implements.*UseCase" "$FILE_PATH"; then
    echo "⚠️  UseCase should implement Port/In interface"
fi

if [ $VIOLATIONS -eq 0 ]; then
    echo "✅ All Application validations passed!"
    exit 0
else
    echo ""
    echo "❌ Found $VIOLATIONS violations"
    exit 1
fi
