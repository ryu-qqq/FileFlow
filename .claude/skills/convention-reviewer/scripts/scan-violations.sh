#!/bin/bash

# Spring DDD Convention Violation Scanner
# Usage: bash scan-violations.sh [target_path]
# Example: bash scan-violations.sh domain/

set -e

# Colors for output
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
TARGET_PATH="${1:-.}"
OUTPUT_DIR=".claude/work-orders"
OUTPUT_FILE="$OUTPUT_DIR/refactoring-todos.md"
TIMESTAMP=$(date "+%Y-%m-%d %H:%M:%S")

# Counters
CRITICAL_COUNT=0
IMPORTANT_COUNT=0
RECOMMENDED_COUNT=0
TOTAL_FILES=0

echo -e "${BLUE}🔍 Spring DDD Convention Violation Scanner${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""
echo "Target: $TARGET_PATH"
echo "Output: $OUTPUT_FILE"
echo ""

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Initialize output file
cat > "$OUTPUT_FILE" << 'EOF'
# Spring DDD Convention Refactoring TODOs

**생성일시**: TIMESTAMP_PLACEHOLDER
**스캔 범위**: SCAN_SCOPE_PLACEHOLDER
**스캔한 파일 수**: FILE_COUNT_PLACEHOLDER개
**발견된 위반 사항**: TOTAL_VIOLATIONS_PLACEHOLDER개

---

## 📊 요약

| 우선순위 | 위반 건수 | 예상 시간 |
|----------|-----------|-----------|
| 🔴 Critical | CRITICAL_COUNT_PLACEHOLDER개 | CRITICAL_TIME_PLACEHOLDER시간 |
| 🟡 Important | IMPORTANT_COUNT_PLACEHOLDER개 | IMPORTANT_TIME_PLACEHOLDER시간 |
| 🟢 Recommended | RECOMMENDED_COUNT_PLACEHOLDER개 | RECOMMENDED_TIME_PLACEHOLDER시간 |
| **합계** | **TOTAL_VIOLATIONS_PLACEHOLDER개** | **TOTAL_TIME_PLACEHOLDER시간** |

---

## 🔴 Critical (Zero-Tolerance 위반)

Zero-Tolerance 규칙 위반은 **즉시 수정 필수**입니다.

EOF

# Count total Java files
TOTAL_FILES=$(find "$TARGET_PATH" -name "*.java" 2>/dev/null | wc -l | tr -d ' ')
echo -e "${BLUE}📁 Found $TOTAL_FILES Java files${NC}"
echo ""

# ==========================================
# 🔴 Critical: Lombok 검사
# ==========================================
echo -e "${RED}🔴 Scanning for Lombok violations...${NC}"
LOMBOK_FILES=$(grep -r -l "@Data\|@Builder\|@Getter\|@Setter\|@AllArgsConstructor\|@NoArgsConstructor\|@RequiredArgsConstructor" "$TARGET_PATH" --include="*.java" 2>/dev/null || true)

if [ -n "$LOMBOK_FILES" ]; then
    echo "$LOMBOK_FILES" | while IFS= read -r file; do
        LINE_NUM=$(grep -n "@Data\|@Builder\|@Getter\|@Setter\|@AllArgsConstructor\|@NoArgsConstructor\|@RequiredArgsConstructor" "$file" | head -1 | cut -d: -f1)
        LOMBOK_TYPE=$(grep "@Data\|@Builder\|@Getter\|@Setter\|@AllArgsConstructor\|@NoArgsConstructor\|@RequiredArgsConstructor" "$file" | head -1 | sed 's/.*\(@[A-Za-z]*\).*/\1/')

        echo "  - $file:$LINE_NUM ($LOMBOK_TYPE)"
        CRITICAL_COUNT=$((CRITICAL_COUNT + 1))

        # Add to TODO file
        cat >> "$OUTPUT_FILE" << EOF

### [ ] LOMBOK-$CRITICAL_COUNT: Lombok 사용 금지 위반

**파일**: \`$file:$LINE_NUM\`
**위반 규칙**: Lombok 금지 (Zero-Tolerance)
**심각도**: 🔴 Critical

**현재 코드**:
\`\`\`java
$LOMBOK_TYPE
public class XxxEntity {
    // Domain layer에서 Lombok 사용
}
\`\`\`

**문제점**:
- Domain layer에서 Lombok 어노테이션 사용은 Zero-Tolerance 위반
- \`$LOMBOK_TYPE\` 어노테이션 제거 필요

**수정 방법**:
\`\`\`java
// Lombok 제거, Pure Java getter/setter 작성
public class XxxEntity {
    private Long id;
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
\`\`\`

**참고 문서**:
- \`docs/coding_convention/02-domain-layer/law-of-demeter/02_lombok-prohibition.md\`

**예상 작업 시간**: 10분

EOF
    done
fi

# ==========================================
# 🔴 Critical: Law of Demeter (Getter 체이닝)
# ==========================================
echo -e "${RED}🔴 Scanning for Law of Demeter violations...${NC}"
LOD_FILES=$(grep -r -n "\.get[A-Z][a-zA-Z]*()\.get[A-Z][a-zA-Z]*()\.get" "$TARGET_PATH" --include="*.java" 2>/dev/null || true)

if [ -n "$LOD_FILES" ]; then
    echo "$LOD_FILES" | while IFS=: read -r file line_num code; do
        echo "  - $file:$line_num"
        CRITICAL_COUNT=$((CRITICAL_COUNT + 1))

        # Add to TODO file
        cat >> "$OUTPUT_FILE" << EOF

### [ ] LOD-$CRITICAL_COUNT: Law of Demeter 위반 (Getter 체이닝)

**파일**: \`$file:$line_num\`
**위반 규칙**: Law of Demeter (Zero-Tolerance)
**심각도**: 🔴 Critical

**현재 코드**:
\`\`\`java
// Getter 체이닝 발견
$code
\`\`\`

**문제점**:
- Getter 체이닝은 Law of Demeter 위반
- "Tell, Don't Ask" 원칙 미준수

**수정 방법**:
\`\`\`java
// Domain 객체에 행동 메서드 추가
String value = domainObject.getTargetValue();

// Domain 클래스에 추가:
public String getTargetValue() {
    return this.child.getChildValue();
}
\`\`\`

**참고 문서**:
- \`docs/coding_convention/02-domain-layer/law-of-demeter/01_getter-chaining-prohibition.md\`

**예상 작업 시간**: 15분

EOF
    done
fi

# ==========================================
# 🔴 Critical: JPA 관계 어노테이션
# ==========================================
echo -e "${RED}🔴 Scanning for JPA relationship violations...${NC}"
JPA_FILES=$(grep -r -n "@ManyToOne\|@OneToMany\|@OneToOne\|@ManyToMany" "$TARGET_PATH" --include="*.java" 2>/dev/null || true)

if [ -n "$JPA_FILES" ]; then
    echo "$JPA_FILES" | while IFS=: read -r file line_num code; do
        echo "  - $file:$line_num"
        CRITICAL_COUNT=$((CRITICAL_COUNT + 1))

        JPA_TYPE=$(echo "$code" | sed 's/.*\(@[A-Za-z]*\).*/\1/')

        # Add to TODO file
        cat >> "$OUTPUT_FILE" << EOF

### [ ] JPA-$CRITICAL_COUNT: JPA 관계 어노테이션 사용 금지

**파일**: \`$file:$line_num\`
**위반 규칙**: Long FK 전략 (Zero-Tolerance)
**심각도**: 🔴 Critical

**현재 코드**:
\`\`\`java
$JPA_TYPE
private Customer customer;  // ❌ Entity 직접 참조
\`\`\`

**문제점**:
- JPA 관계 어노테이션 (\`$JPA_TYPE\`) 사용은 Zero-Tolerance 위반
- Entity 간 직접 참조는 금지

**수정 방법**:
\`\`\`java
// Long FK 사용
private Long customerId;  // ✅ Long FK 전략
\`\`\`

**참고 문서**:
- \`docs/coding_convention/04-persistence-layer/jpa-entity-design/01_long-fk-strategy.md\`

**예상 작업 시간**: 20분

EOF
    done
fi

# ==========================================
# 🔴 Critical: @Transactional 경계
# ==========================================
echo -e "${RED}🔴 Scanning for Transaction boundary violations...${NC}"
# Find @Transactional methods
TRANSACTIONAL_METHODS=$(grep -r -A 30 "@Transactional" "$TARGET_PATH" --include="*.java" 2>/dev/null | grep -E "restTemplate|webClient|feignClient|httpClient" || true)

if [ -n "$TRANSACTIONAL_METHODS" ]; then
    echo "  - Found potential transaction boundary violations (requires manual review)"
    CRITICAL_COUNT=$((CRITICAL_COUNT + 1))

    # Add to TODO file
    cat >> "$OUTPUT_FILE" << EOF

### [ ] TXN-1: @Transactional 내 외부 API 호출 의심

**파일**: \`[수동 검토 필요]\`
**위반 규칙**: Transaction 경계 (Zero-Tolerance)
**심각도**: 🔴 Critical

**문제점**:
- \`@Transactional\` 메서드 내에서 외부 API 호출 감지 (restTemplate, webClient, feignClient 등)
- Transaction 내 외부 API 호출은 Zero-Tolerance 위반

**수정 방법**:
\`\`\`java
// ❌ Before
@Transactional
public void processOrder(OrderCommand cmd) {
    Order order = orderRepository.save(new Order(cmd));
    paymentClient.processPayment(order);  // ❌ 외부 API
}

// ✅ After
public void processOrder(OrderCommand cmd) {
    Order order = processOrderInTransaction(cmd);
    paymentClient.processPayment(order);  // ✅ 트랜잭션 밖
}

@Transactional
private Order processOrderInTransaction(OrderCommand cmd) {
    return orderRepository.save(new Order(cmd));
}
\`\`\`

**참고 문서**:
- \`docs/coding_convention/03-application-layer/transaction-management/01_transaction-boundary.md\`

**예상 작업 시간**: 30분

EOF
fi

# ==========================================
# Summary
# ==========================================
echo ""
echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}📊 Scan Complete${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""
echo -e "🔴 Critical violations: ${RED}$CRITICAL_COUNT${NC}"
echo -e "🟡 Important violations: ${YELLOW}$IMPORTANT_COUNT${NC}"
echo -e "🟢 Recommended improvements: ${GREEN}$RECOMMENDED_COUNT${NC}"
echo ""
TOTAL_VIOLATIONS=$((CRITICAL_COUNT + IMPORTANT_COUNT + RECOMMENDED_COUNT))
echo -e "Total violations: ${RED}$TOTAL_VIOLATIONS${NC}"
echo ""
echo -e "${GREEN}✅ TODO file generated: $OUTPUT_FILE${NC}"
echo ""

# Calculate estimated time
CRITICAL_TIME=$((CRITICAL_COUNT * 15))  # 15 minutes per critical
IMPORTANT_TIME=$((IMPORTANT_COUNT * 10))  # 10 minutes per important
RECOMMENDED_TIME=$((RECOMMENDED_COUNT * 5))  # 5 minutes per recommended
TOTAL_MINUTES=$((CRITICAL_TIME + IMPORTANT_TIME + RECOMMENDED_TIME))
TOTAL_HOURS=$(awk "BEGIN {printf \"%.1f\", $TOTAL_MINUTES/60}")

# Update placeholders in TODO file
sed -i.bak "s/TIMESTAMP_PLACEHOLDER/$TIMESTAMP/" "$OUTPUT_FILE"
sed -i.bak "s|SCAN_SCOPE_PLACEHOLDER|$TARGET_PATH|" "$OUTPUT_FILE"
sed -i.bak "s/FILE_COUNT_PLACEHOLDER/$TOTAL_FILES/" "$OUTPUT_FILE"
sed -i.bak "s/TOTAL_VIOLATIONS_PLACEHOLDER/$TOTAL_VIOLATIONS/" "$OUTPUT_FILE"
sed -i.bak "s/CRITICAL_COUNT_PLACEHOLDER/$CRITICAL_COUNT/" "$OUTPUT_FILE"
sed -i.bak "s/IMPORTANT_COUNT_PLACEHOLDER/$IMPORTANT_COUNT/" "$OUTPUT_FILE"
sed -i.bak "s/RECOMMENDED_COUNT_PLACEHOLDER/$RECOMMENDED_COUNT/" "$OUTPUT_FILE"
sed -i.bak "s/CRITICAL_TIME_PLACEHOLDER/$(awk "BEGIN {printf \"%.1f\", $CRITICAL_TIME/60}")/" "$OUTPUT_FILE"
sed -i.bak "s/IMPORTANT_TIME_PLACEHOLDER/$(awk "BEGIN {printf \"%.1f\", $IMPORTANT_TIME/60}")/" "$OUTPUT_FILE"
sed -i.bak "s/RECOMMENDED_TIME_PLACEHOLDER/$(awk "BEGIN {printf \"%.1f\", $RECOMMENDED_TIME/60}")/" "$OUTPUT_FILE"
sed -i.bak "s/TOTAL_TIME_PLACEHOLDER/$TOTAL_HOURS/" "$OUTPUT_FILE"
rm "$OUTPUT_FILE.bak"

echo -e "${YELLOW}⏱️  Estimated refactoring time: $TOTAL_HOURS hours${NC}"
echo ""
echo -e "${BLUE}Next steps:${NC}"
echo "1. Review the TODO file: $OUTPUT_FILE"
echo "2. Pass to Cursor AI for refactoring"
echo "3. Execute tasks in priority order (🔴 → 🟡 → 🟢)"
echo ""
