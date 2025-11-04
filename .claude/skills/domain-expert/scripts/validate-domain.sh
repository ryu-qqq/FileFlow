#!/bin/bash

# Domain Layer Validation Script
# Usage: bash validate-domain.sh [file_path]

set -e

FILE_PATH="${1}"

if [ -z "$FILE_PATH" ]; then
    echo "Usage: bash validate-domain.sh [file_path]"
    exit 1
fi

if [ ! -f "$FILE_PATH" ]; then
    echo "Error: File not found: $FILE_PATH"
    exit 1
fi

echo "🔍 Validating Domain Layer: $FILE_PATH"
echo ""

VIOLATIONS=0

# Check Lombok (Zero-Tolerance)
if grep -q "@Data\|@Builder\|@Getter\|@Setter\|@AllArgsConstructor\|@NoArgsConstructor\|@RequiredArgsConstructor" "$FILE_PATH"; then
    echo "❌ ZERO-TOLERANCE: Lombok is absolutely prohibited in Domain layer"
    VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check Law of Demeter (Getter chaining)
if grep -q "\.get[A-Z][a-zA-Z]*()\.get[A-Z][a-zA-Z]*()\.get" "$FILE_PATH"; then
    echo "❌ ZERO-TOLERANCE: Getter chaining violates Law of Demeter"
    VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check JPA relationship annotations (should not be in Domain)
if grep -q "@ManyToOne\|@OneToMany\|@OneToOne\|@ManyToMany" "$FILE_PATH"; then
    echo "❌ JPA relationship annotations should not be in Domain layer"
    VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check if ValueObject uses Record
if grep -q "class.*ValueObject\|class.*Address\|class.*Money\|class.*Email" "$FILE_PATH" && ! grep -q "public record" "$FILE_PATH"; then
    echo "⚠️  Consider using Record pattern for ValueObject"
fi

# Check if Domain entity has business methods
if grep -q "class.*Entity\|class [A-Z][a-zA-Z]*Domain" "$FILE_PATH" && ! grep -q "public void\|public boolean\|public.*calculate\|public.*validate" "$FILE_PATH"; then
    echo "⚠️  Domain entity should have business methods (Tell, Don't Ask)"
fi

# Check if setters are used (should use business methods instead)
if grep -q "\.set[A-Z][a-zA-Z]*(" "$FILE_PATH"; then
    echo "⚠️  Avoid using setters, prefer business methods"
fi

if [ $VIOLATIONS -eq 0 ]; then
    echo "✅ All Domain validations passed!"
    exit 0
else
    echo ""
    echo "❌ Found $VIOLATIONS violations"
    exit 1
fi
