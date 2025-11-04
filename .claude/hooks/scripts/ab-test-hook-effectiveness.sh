#!/bin/bash

# =====================================================
# A/B 테스트: Hook 시스템 효과 검증
# =====================================================

set -e

PROJECT_ROOT=$(pwd)
SETTINGS_FILE=".claude/settings.local.json"
BACKUP_FILE=".claude/settings.local.json.backup"
TEST_OUTPUT_DIR=".claude/hooks/logs/ab-test-results"

echo "🧪 A/B 테스트: Hook 시스템 효과 검증"
echo "=========================================="
echo ""

# 테스트 출력 디렉토리 생성
mkdir -p "$TEST_OUTPUT_DIR"

# 현재 설정 백업
echo "📦 1. 현재 설정 백업"
if [ -f "$SETTINGS_FILE" ]; then
    cp "$SETTINGS_FILE" "$BACKUP_FILE"
    echo "✅ 백업 완료: $BACKUP_FILE"
else
    echo "⚠️  settings.local.json 없음 (Hook 비활성화 상태)"
fi
echo ""

# 테스트 시나리오 정의
cat > "$TEST_OUTPUT_DIR/test-scenarios.md" << 'EOF'
# A/B 테스트 시나리오

## 시나리오 1: Domain Aggregate 생성

**프롬프트**:
```
Order Aggregate를 생성해줘.
- 주문 생성 (placeOrder)
- 주문 취소 (cancelOrder)
- 고객 정보, 배송 주소 포함
```

**체크리스트**:
- [ ] Lombok 미사용 (`@Data`, `@Getter`, `@Setter` 없음)
- [ ] Getter 체이닝 미사용 (`order.getCustomer().getAddress()` 없음)
- [ ] Tell, Don't Ask 패턴 준수
- [ ] 비즈니스 로직은 Domain에 위치
- [ ] Javadoc 포함 (`@author`, `@since`)

---

## 시나리오 2: UseCase 생성

**프롬프트**:
```
PlaceOrderUseCase를 생성해줘.
- 외부 결제 API 호출 필요
- 주문 생성 후 이메일 발송
```

**체크리스트**:
- [ ] `@Transactional` 내 외부 API 호출 없음
- [ ] Private 메서드에 `@Transactional` 없음
- [ ] 트랜잭션 경계가 명확히 분리됨
- [ ] UseCase는 얇게 유지 (비즈니스 로직은 Domain에)
- [ ] Command/Query 분리

---

## 시나리오 3: JPA Entity 생성

**프롬프트**:
```
OrderEntity를 생성해줘.
- Order와 Customer 관계 표현
- Audit 정보 포함
```

**체크리스트**:
- [ ] `@ManyToOne`, `@OneToMany` 미사용 (Long FK 전략)
- [ ] Setter 메서드 없음
- [ ] Public constructor 없음 (protected + static factory)
- [ ] BaseAuditEntity 상속
- [ ] Lombok 미사용

---

## 측정 기준

### 정량적 지표
1. **컨벤션 위반 건수**: 체크리스트에서 ❌ 개수
2. **토큰 사용량**: 프롬프트 제출 시 예상 토큰 (로그에서 확인)
3. **생성 시간**: 요청 → 응답 완료까지 시간
4. **코드 줄 수**: 생성된 코드의 총 라인 수

### 정성적 지표
1. **코드 품질**: Zero-Tolerance 규칙 준수 여부
2. **가독성**: 명확한 네이밍, 구조
3. **완성도**: 즉시 사용 가능한 수준인지

---

## 실행 방법

### Test A: Hook ON (현재 상태)
```bash
# settings.local.json 활성화 (현재 상태 유지)
# Claude Code에서 각 시나리오 실행
# 결과를 test-results-hook-on.md에 기록
```

### Test B: Hook OFF
```bash
# 1. Hook 비활성화
mv .claude/settings.local.json .claude/settings.local.json.backup

# 2. 새 Claude Code 세션 시작
# 3. 동일 시나리오 실행
# 4. 결과를 test-results-hook-off.md에 기록

# 5. Hook 복구
mv .claude/settings.local.json.backup .claude/settings.local.json
```

---

## 결과 분석

### 예상 결과 (Hook ON vs Hook OFF)

| 지표 | Hook ON | Hook OFF | 개선율 |
|------|---------|----------|--------|
| 컨벤션 위반 | 0-2회 | 8-12회 | 78-100% |
| 토큰 사용 | 500-1,000 | 50,000 | 90% 절감 |
| Zero-Tolerance 준수 | 95-100% | 50-70% | 40-50%p |

### 성공 기준
- **컨벤션 위반 감소**: 50% 이상
- **토큰 효율**: 80% 이상 절감
- **Zero-Tolerance 준수**: 90% 이상

EOF

echo "✅ 테스트 시나리오 생성 완료"
echo "   위치: $TEST_OUTPUT_DIR/test-scenarios.md"
echo ""

# 테스트 결과 기록 템플릿 생성
cat > "$TEST_OUTPUT_DIR/test-results-hook-on.md" << 'EOF'
# A/B 테스트 결과: Hook ON

## 테스트 환경
- 날짜: YYYY-MM-DD HH:MM:SS
- Hook 상태: ✅ 활성화
- settings.local.json: 존재함

---

## 시나리오 1: Domain Aggregate 생성

### 생성된 코드
```java
// 여기에 생성된 코드 붙여넣기
```

### 체크리스트
- [ ] Lombok 미사용
- [ ] Getter 체이닝 미사용
- [ ] Tell, Don't Ask 패턴 준수
- [ ] 비즈니스 로직은 Domain에 위치
- [ ] Javadoc 포함

### 측정 지표
- 컨벤션 위반: ___ 건
- 토큰 사용: ___ tokens
- 생성 시간: ___ 초
- 코드 줄 수: ___ 줄

---

## 시나리오 2: UseCase 생성

### 생성된 코드
```java
// 여기에 생성된 코드 붙여넣기
```

### 체크리스트
- [ ] `@Transactional` 내 외부 API 호출 없음
- [ ] Private 메서드에 `@Transactional` 없음
- [ ] 트랜잭션 경계 명확
- [ ] UseCase는 얇게 유지
- [ ] Command/Query 분리

### 측정 지표
- 컨벤션 위반: ___ 건
- 토큰 사용: ___ tokens
- 생성 시간: ___ 초
- 코드 줄 수: ___ 줄

---

## 시나리오 3: JPA Entity 생성

### 생성된 코드
```java
// 여기에 생성된 코드 붙여넣기
```

### 체크리스트
- [ ] Long FK 전략 사용
- [ ] Setter 메서드 없음
- [ ] Public constructor 없음
- [ ] BaseAuditEntity 상속
- [ ] Lombok 미사용

### 측정 지표
- 컨벤션 위반: ___ 건
- 토큰 사용: ___ tokens
- 생성 시간: ___ 초
- 코드 줄 수: ___ 줄

---

## 전체 요약

### 정량적 지표
- 총 컨벤션 위반: ___ 건
- 평균 토큰 사용: ___ tokens
- 평균 생성 시간: ___ 초

### 정성적 평가
- 코드 품질: ⭐⭐⭐⭐⭐ (5점 만점)
- 가독성: ⭐⭐⭐⭐⭐
- 완성도: ⭐⭐⭐⭐⭐

EOF

cp "$TEST_OUTPUT_DIR/test-results-hook-on.md" "$TEST_OUTPUT_DIR/test-results-hook-off.md"
sed -i.bak 's/Hook ON/Hook OFF/g' "$TEST_OUTPUT_DIR/test-results-hook-off.md"
sed -i.bak 's/✅ 활성화/❌ 비활성화/g' "$TEST_OUTPUT_DIR/test-results-hook-off.md"
sed -i.bak 's/존재함/존재하지 않음/g' "$TEST_OUTPUT_DIR/test-results-hook-off.md"
rm "$TEST_OUTPUT_DIR/test-results-hook-off.md.bak"

echo "✅ 결과 기록 템플릿 생성 완료"
echo "   - Hook ON: $TEST_OUTPUT_DIR/test-results-hook-on.md"
echo "   - Hook OFF: $TEST_OUTPUT_DIR/test-results-hook-off.md"
echo ""

# Hook 비활성화 함수
disable_hooks() {
    echo "🔴 2. Hook 비활성화"
    if [ -f "$SETTINGS_FILE" ]; then
        mv "$SETTINGS_FILE" "$BACKUP_FILE"
        echo "✅ settings.local.json → settings.local.json.backup"
        echo "⚠️  새 Claude Code 세션을 시작하세요!"
    else
        echo "⚠️  이미 비활성화 상태"
    fi
}

# Hook 복구 함수
enable_hooks() {
    echo "🟢 3. Hook 복구"
    if [ -f "$BACKUP_FILE" ]; then
        mv "$BACKUP_FILE" "$SETTINGS_FILE"
        echo "✅ settings.local.json.backup → settings.local.json"
        echo "⚠️  새 Claude Code 세션을 시작하세요!"
    else
        echo "⚠️  백업 파일 없음"
    fi
}

# 사용법 출력
echo "📋 사용법"
echo "=========================================="
echo ""
echo "## Step 1: Test A (Hook ON) 실행"
echo "1. 현재 상태에서 Claude Code 실행"
echo "2. test-scenarios.md의 시나리오를 순서대로 실행"
echo "3. 결과를 test-results-hook-on.md에 기록"
echo ""
echo "## Step 2: Hook 비활성화"
echo "bash $0 disable"
echo ""
echo "## Step 3: Test B (Hook OFF) 실행"
echo "1. 새 Claude Code 세션 시작"
echo "2. 동일 시나리오를 순서대로 실행"
echo "3. 결과를 test-results-hook-off.md에 기록"
echo ""
echo "## Step 4: Hook 복구"
echo "bash $0 enable"
echo ""
echo "## Step 5: 결과 비교"
echo "- test-results-hook-on.md vs test-results-hook-off.md"
echo "- 컨벤션 위반, 토큰 사용, 코드 품질 비교"
echo ""
echo "=========================================="

# 명령어 처리
case "${1:-}" in
    disable)
        disable_hooks
        ;;
    enable)
        enable_hooks
        ;;
    *)
        echo ""
        echo "✅ 준비 완료!"
        echo ""
        echo "다음 명령어로 Hook을 제어할 수 있습니다:"
        echo "  bash $0 disable  # Hook 비활성화"
        echo "  bash $0 enable   # Hook 복구"
        ;;
esac
