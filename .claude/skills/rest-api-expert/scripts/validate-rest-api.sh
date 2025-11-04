#!/bin/bash

# REST API Layer Validation Script
# Usage: bash validate-rest-api.sh [file_path]

set -e

FILE_PATH="${1}"

if [ -z "$FILE_PATH" ]; then
    echo "Usage: bash validate-rest-api.sh [file_path]"
    exit 1
fi

if [ ! -f "$FILE_PATH" ]; then
    echo "Error: File not found: $FILE_PATH"
    exit 1
fi

echo "🔍 Validating REST API Layer: $FILE_PATH"
echo ""

VIOLATIONS=0

# Check @RestController
if grep -q "class.*Controller" "$FILE_PATH" && ! grep -q "@RestController" "$FILE_PATH"; then
    echo "❌ Controller must use @RestController annotation"
    VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check @RequestMapping
if grep -q "@RestController" "$FILE_PATH" && ! grep -q "@RequestMapping" "$FILE_PATH"; then
    echo "❌ Controller must have @RequestMapping"
    VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check Record for Request DTO
if grep -q "class.*Request" "$FILE_PATH" && ! grep -q "public record.*Request" "$FILE_PATH"; then
    echo "❌ Request DTO must use Record pattern"
    VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check Record for Response DTO
if grep -q "class.*Response" "$FILE_PATH" && ! grep -q "public record.*Response" "$FILE_PATH"; then
    echo "❌ Response DTO must use Record pattern"
    VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check @Valid for Request
if grep -q "@RequestBody" "$FILE_PATH" && ! grep -q "@Valid.*@RequestBody" "$FILE_PATH"; then
    echo "❌ @RequestBody must use @Valid annotation"
    VIOLATIONS=$((VIOLATIONS + 1))
fi

if [ $VIOLATIONS -eq 0 ]; then
    echo "✅ All REST API validations passed!"
    exit 0
else
    echo ""
    echo "❌ Found $VIOLATIONS violations"
    exit 1
fi
