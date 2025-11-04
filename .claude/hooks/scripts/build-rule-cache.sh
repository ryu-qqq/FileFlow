#!/bin/bash

# =================================================================
# Rule Cache Builder
# Purpose: Convert docs/coding_convention/**/*.md to JSON cache
# Usage: ./build-rule-cache.sh
# Output: .claude/cache/rules/*.json + index.json
# =================================================================

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
DOCS_DIR="$PROJECT_ROOT/docs/coding_convention"
CACHE_DIR="$PROJECT_ROOT/.claude/cache/rules"
INDEX_FILE="$CACHE_DIR/index.json"

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}==================================================${NC}"
echo -e "${BLUE}Rule Cache Builder${NC}"
echo -e "${BLUE}==================================================${NC}"
echo ""

# Cache 디렉토리 생성
mkdir -p "$CACHE_DIR"

# 기존 Cache 정리 (optional - 주석 처리하면 누적 빌드)
# rm -f "$CACHE_DIR"/*.json

# 카운터
TOTAL_FILES=0
SUCCESS_COUNT=0
SKIP_COUNT=0

# Index 초기화
cat > "$INDEX_FILE" << 'EOF'
{
  "version": "1.0.0",
  "buildDate": "",
  "totalRules": 0,
  "keywordIndex": {},
  "layerIndex": {
    "domain": [],
    "application": [],
    "adapter-rest": [],
    "adapter-persistence": [],
    "testing": [],
    "java21": [],
    "enterprise": [],
    "error-handling": []
  }
}
EOF

# 임시 인덱스 데이터 저장 (연관 배열)
declare -A KEYWORD_MAP
declare -A LAYER_MAP

# =================================================================
# 함수: 파일 경로에서 레이어 추출
# =================================================================
extract_layer() {
    local filepath="$1"

    if echo "$filepath" | grep -q "02-domain-layer"; then
        echo "domain"
    elif echo "$filepath" | grep -q "03-application-layer"; then
        echo "application"
    elif echo "$filepath" | grep -q "01-adapter-rest-api-layer"; then
        echo "adapter-rest"
    elif echo "$filepath" | grep -q "04-persistence-layer"; then
        echo "adapter-persistence"
    elif echo "$filepath" | grep -q "05-testing"; then
        echo "testing"
    elif echo "$filepath" | grep -q "06-java21-patterns"; then
        echo "java21"
    elif echo "$filepath" | grep -q "07-enterprise-patterns"; then
        echo "enterprise"
    elif echo "$filepath" | grep -q "08-error-handling"; then
        echo "error-handling"
    else
        echo "general"
    fi
}

# =================================================================
# 함수: 파일명에서 키워드 추출
# =================================================================
extract_keywords_from_filename() {
    local filename="$1"
    local basename=$(basename "$filename" .md)

    # 숫자 prefix 제거 (01_, 02_ 등)
    basename=$(echo "$basename" | sed 's/^[0-9]\+_//')

    # 하이픈을 공백으로 변환하여 단어 분리
    echo "$basename" | tr '-' ' '
}

# =================================================================
# 함수: 제목(# Heading)에서 키워드 추출
# =================================================================
extract_keywords_from_title() {
    local filepath="$1"

    # 첫 번째 # 제목 추출
    local title=$(grep -m 1 '^# ' "$filepath" | sed 's/^# //' | tr '[:upper:]' '[:lower:]')

    # 괄호 내용 제거, 특수문자 제거
    title=$(echo "$title" | sed 's/([^)]*)//g' | sed 's/[—–-]/ /g')

    echo "$title"
}

# =================================================================
# 함수: 금지 패턴 추출 (❌, 금지, 안됨 등)
# =================================================================
extract_prohibited_patterns() {
    local filepath="$1"

    # ❌로 시작하는 라인 추출
    grep -E '^\s*-\s*❌' "$filepath" | head -5 | sed 's/^\s*-\s*//' || echo ""
}

# =================================================================
# 함수: 허용 패턴 추출 (✅)
# =================================================================
extract_allowed_patterns() {
    local filepath="$1"

    # ✅로 시작하는 라인 추출
    grep -E '^\s*-\s*✅' "$filepath" | head -5 | sed 's/^\s*-\s*//' || echo ""
}

# =================================================================
# 함수: 우선순위 결정 (critical, high, medium, low)
# =================================================================
determine_priority() {
    local filepath="$1"
    local content=$(cat "$filepath")

    # Critical: Zero-tolerance, 금지, 절대
    if echo "$content" | grep -qiE '(zero-tolerance|절대|금지|critical)'; then
        echo "critical"
        return
    fi

    # High: 필수, 중요, important
    if echo "$content" | grep -qiE '(필수|중요|important|must)'; then
        echo "high"
        return
    fi

    # Medium: 권장, recommended
    if echo "$content" | grep -qiE '(권장|recommended|should)'; then
        echo "medium"
        return
    fi

    # Default
    echo "low"
}

# =================================================================
# 함수: 토큰 수 추정 (간단한 계산: 단어 수 * 1.3)
# =================================================================
estimate_tokens() {
    local filepath="$1"
    local word_count=$(wc -w < "$filepath" | tr -d ' ')
    echo $((word_count * 13 / 10))  # 1.3배
}

# =================================================================
# 함수: JSON 생성
# =================================================================
generate_json() {
    local filepath="$1"
    local rule_id="$2"
    local layer="$3"
    local priority="$4"

    # 메타데이터 추출
    local filename_keywords=$(extract_keywords_from_filename "$filepath")
    local title_keywords=$(extract_keywords_from_title "$filepath")
    local prohibited=$(extract_prohibited_patterns "$filepath")
    local allowed=$(extract_allowed_patterns "$filepath")
    local token_estimate=$(estimate_tokens "$filepath")

    # Primary keywords (파일명 기반)
    local primary_keywords=$(echo "$filename_keywords" | jq -R 'split(" ") | map(select(length > 0))')

    # Secondary keywords (제목 기반)
    local secondary_keywords=$(echo "$title_keywords" | jq -R 'split(" ") | map(select(length > 0))')

    # Anti-patterns (금지 패턴에서 코드 블록 추출)
    local anti_patterns=""
    if [[ -n "$prohibited" ]]; then
        anti_patterns=$(echo "$prohibited" | grep -oE '`[^`]+`' | tr -d '`' | jq -R . | jq -s .)
    else
        anti_patterns="[]"
    fi

    # Prohibited rules (JSON 배열)
    local prohibited_json=""
    if [[ -n "$prohibited" ]]; then
        prohibited_json=$(echo "$prohibited" | jq -R . | jq -s .)
    else
        prohibited_json="[]"
    fi

    # Allowed rules (JSON 배열)
    local allowed_json=""
    if [[ -n "$allowed" ]]; then
        allowed_json=$(echo "$allowed" | jq -R . | jq -s .)
    else
        allowed_json="[]"
    fi

    # JSON 생성
    cat > "$CACHE_DIR/$rule_id.json" << EOF
{
  "id": "$rule_id",
  "sourceFile": "$filepath",
  "metadata": {
    "keywords": {
      "primary": $primary_keywords,
      "secondary": $secondary_keywords,
      "anti": $anti_patterns
    },
    "layer": "$layer",
    "priority": "$priority",
    "tokenEstimate": $token_estimate
  },
  "rules": {
    "prohibited": $prohibited_json,
    "allowed": $allowed_json
  },
  "documentation": {
    "path": "${filepath#$PROJECT_ROOT/}",
    "summary": "Auto-generated from $(basename "$filepath")"
  }
}
EOF
}

# =================================================================
# 메인 처리 루프
# =================================================================
echo -e "${YELLOW}Processing markdown files...${NC}"
echo ""

# docs/coding_convention/**/*.md 파일 순회
find "$DOCS_DIR" -name "*.md" -type f | sort | while read -r filepath; do
    TOTAL_FILES=$((TOTAL_FILES + 1))

    # 파일명에서 rule_id 생성
    relative_path="${filepath#$DOCS_DIR/}"
    rule_id=$(echo "$relative_path" | sed 's/\.md$//' | tr '/' '-' | sed 's/^[0-9]\+-//')

    # README, OVERVIEW 등 스킵
    if echo "$(basename "$filepath")" | grep -qiE '(readme|overview|roadmap)'; then
        echo -e "  ${YELLOW}⏭  Skipped:${NC} $relative_path (non-rule document)"
        SKIP_COUNT=$((SKIP_COUNT + 1))
        continue
    fi

    # 레이어, 우선순위 추출
    layer=$(extract_layer "$filepath")
    priority=$(determine_priority "$filepath")

    # JSON 생성
    generate_json "$filepath" "$rule_id" "$layer" "$priority"

    echo -e "  ${GREEN}✅ Generated:${NC} $rule_id.json (layer: $layer, priority: $priority)"
    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
done

# =================================================================
# Index 파일 업데이트
# =================================================================
echo ""
echo -e "${YELLOW}Building index...${NC}"

# 모든 JSON 파일을 읽어서 index 생성
for json_file in "$CACHE_DIR"/*.json; do
    [[ "$json_file" == "$INDEX_FILE" ]] && continue

    rule_id=$(jq -r '.id' "$json_file")
    layer=$(jq -r '.metadata.layer' "$json_file")
    keywords=$(jq -r '.metadata.keywords.primary[]' "$json_file")

    # Layer Index 업데이트 (bash는 연관배열로 누적)
    if [[ -z "${LAYER_MAP[$layer]}" ]]; then
        LAYER_MAP[$layer]="$rule_id"
    else
        LAYER_MAP[$layer]="${LAYER_MAP[$layer]},$rule_id"
    fi

    # Keyword Index 업데이트
    for keyword in $keywords; do
        keyword=$(echo "$keyword" | tr -d '"')
        if [[ -z "${KEYWORD_MAP[$keyword]}" ]]; then
            KEYWORD_MAP[$keyword]="$rule_id"
        else
            KEYWORD_MAP[$keyword]="${KEYWORD_MAP[$keyword]},$rule_id"
        fi
    done
done

# Index JSON 재생성
cat > "$INDEX_FILE" << EOF
{
  "version": "1.0.0",
  "buildDate": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "totalRules": $SUCCESS_COUNT,
  "keywordIndex": {
EOF

# Keyword Index 작성
first_keyword=true
for keyword in "${!KEYWORD_MAP[@]}"; do
    rule_ids=$(echo "${KEYWORD_MAP[$keyword]}" | jq -R 'split(",")' | jq -c .)
    if [[ "$first_keyword" == true ]]; then
        echo "    \"$keyword\": $rule_ids" >> "$INDEX_FILE"
        first_keyword=false
    else
        echo "    ,\"$keyword\": $rule_ids" >> "$INDEX_FILE"
    fi
done

cat >> "$INDEX_FILE" << EOF
  },
  "layerIndex": {
EOF

# Layer Index 작성
first_layer=true
for layer in domain application adapter-rest adapter-persistence testing java21 enterprise error-handling; do
    if [[ -n "${LAYER_MAP[$layer]}" ]]; then
        rule_ids=$(echo "${LAYER_MAP[$layer]}" | jq -R 'split(",")' | jq -c .)
        if [[ "$first_layer" == true ]]; then
            echo "    \"$layer\": $rule_ids" >> "$INDEX_FILE"
            first_layer=false
        else
            echo "    ,\"$layer\": $rule_ids" >> "$INDEX_FILE"
        fi
    else
        if [[ "$first_layer" == true ]]; then
            echo "    \"$layer\": []" >> "$INDEX_FILE"
            first_layer=false
        else
            echo "    ,\"$layer\": []" >> "$INDEX_FILE"
        fi
    fi
done

cat >> "$INDEX_FILE" << 'EOF'
  }
}
EOF

# =================================================================
# 요약 출력
# =================================================================
echo ""
echo -e "${BLUE}==================================================${NC}"
echo -e "${GREEN}✅ Rule Cache Build Complete!${NC}"
echo -e "${BLUE}==================================================${NC}"
echo ""
echo -e "  Total Files: ${YELLOW}$TOTAL_FILES${NC}"
echo -e "  Generated:   ${GREEN}$SUCCESS_COUNT${NC}"
echo -e "  Skipped:     ${YELLOW}$SKIP_COUNT${NC}"
echo ""
echo -e "  Output: ${BLUE}$CACHE_DIR/${NC}"
echo -e "  Index:  ${BLUE}$INDEX_FILE${NC}"
echo ""
echo -e "${GREEN}You can now use these rules in hooks and slash commands!${NC}"
echo ""
