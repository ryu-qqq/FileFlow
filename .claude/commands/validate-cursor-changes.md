# Validate Cursor Changes Command

**Cursor AI가 생성한 코드 자동 검증**

---

## 🎯 목적

Cursor AI가 생성한 코드를 Claude Code가 검증:
1. **변경 파일 추적**: `.claude/cursor-changes.md` 읽기
2. **컨벤션 검증**: validation-helper.py 실행
3. **아키텍처 검증**: ArchUnit 테스트 실행
4. **위반 리포트**: 구체적인 수정 방법 제시

---

## 📝 사용법

```bash
# 기본 검증 (cursor-changes.md 참조)
/validate-cursor-changes

# 특정 파일만 검증
/validate-cursor-changes domain/order/model/OrderDomain.java

# 특정 레이어 검증
/validate-cursor-changes --layer domain

# 전체 프로젝트 검증
/validate-cursor-changes --all
```

---

## 🔄 실행 프로세스

### Step 1: 변경 파일 확인

**.claude/cursor-changes.md 읽기:**

```markdown
# Cursor 변경 파일 (2024-11-04 17:30)

## Domain Layer
- domain/order/model/OrderDomain.java (NEW)
- domain/order/model/OrderId.java (NEW)
- domain/order/model/OrderStatus.java (NEW)

## Application Layer
- application/order/port/in/CreateOrderPort.java (NEW)
- application/order/usecase/CreateOrderUseCase.java (NEW)
- application/order/dto/command/CreateOrderCommand.java (NEW)

## REST API Layer
- adapter/in/web/order/controller/OrderController.java (NEW)
- adapter/in/web/order/dto/request/CreateOrderRequest.java (NEW)
```

### Step 2: Layer별 검증 전략

**Domain Layer 검증:**
```bash
# validation-helper.py 실행 (Cache 기반)
python3 .claude/hooks/scripts/validation-helper.py \
  domain/order/model/OrderDomain.java

# 검증 항목:
# ✅ Lombok 금지 (@Data, @Builder 등)
# ✅ Law of Demeter (Getter 체이닝 금지)
# ✅ Javadoc 필수 (@author, @since)
# ✅ Factory Pattern 사용
# ✅ Tell, Don't Ask 패턴
```

**Application Layer 검증:**
```bash
python3 .claude/hooks/scripts/validation-helper.py \
  application/order/usecase/CreateOrderUseCase.java

# 검증 항목:
# ✅ @Transactional 경계 (외부 API 호출 금지)
# ✅ UseCase 네이밍 (*UseCase.java)
# ✅ Port 인터페이스 구현
# ✅ Command/Query 분리
```

**Persistence Layer 검증:**
```bash
python3 .claude/hooks/scripts/validation-helper.py \
  adapter/out/persistence/order/entity/OrderEntity.java

# 검증 항목:
# ✅ Long FK Strategy (관계 어노테이션 금지)
# ✅ Immutable Entity
# ✅ Constructor Pattern
```

**REST API Layer 검증:**
```bash
python3 .claude/hooks/scripts/validation-helper.py \
  adapter/in/web/order/controller/OrderController.java

# 검증 항목:
# ✅ Controller Thin (비즈니스 로직 없음)
# ✅ Validation 적용 (@Valid)
# ✅ ApiResponse 표준화
```

### Step 3: ArchUnit 테스트 실행

**레이어별 ArchUnit 실행:**

```bash
# Domain Layer ArchUnit
./gradlew :domain:test --tests "*ArchitectureTest"

# Application Layer ArchUnit
./gradlew :application:test --tests "*ArchitectureTest"

# 전체 ArchUnit
./gradlew test --tests "*ArchitectureTest"
```

**검증 규칙:**
- Domain → Application 의존성 금지
- Domain → Infrastructure 의존성 금지
- Naming Convention (UseCase, Port, Repository)
- Annotation 규칙 (@Transactional 위치)

### Step 4: 위반 리포트 생성

**리포트 파일: `.claude/validation-report.md`**

```markdown
# Cursor 코드 검증 리포트

## 📊 검증 요약

| Layer | 파일 수 | 통과 | 위반 | 상태 |
|-------|--------|------|------|------|
| Domain | 3 | 2 | 1 | ⚠️ |
| Application | 3 | 3 | 0 | ✅ |
| REST API | 2 | 2 | 0 | ✅ |

**총 8개 파일 중 7개 통과, 1개 위반**

---

## ❌ 위반 사항

### Domain Layer

**파일**: `domain/order/model/OrderDomain.java:45`

**위반 규칙**: Law of Demeter (Getter 체이닝)

**코드:**
```java
// ❌ 잘못된 코드
String zipCode = order.getCustomer().getAddress().getZipCode();
```

**수정 방법:**
```java
// ✅ 올바른 코드
public String getCustomerZipCode() {
    return this.customer.getAddressZipCode();
}
```

**참고 문서**: 
- docs/coding_convention/02-domain-layer/law-of-demeter/01_getter-chaining-prohibition.md

---

## ✅ 통과 항목

- Lombok 금지 규칙 준수
- Javadoc 모든 public 메서드 포함
- Factory Pattern 사용
- Transaction 경계 준수
- Long FK Strategy 준수

---

## 📝 다음 단계

1. **위반 사항 수정** (Claude Code)
   - OrderDomain.java Law of Demeter 위반 수정
   
2. **재검증**
   ```bash
   /validate-cursor-changes
   ```

3. **통과 시 Merge**
   ```bash
   git worktree remove ../wt-order
   git merge feature/order
   ```
```

---

## 🎯 검증 레벨

### Level 1: Real-time (After-tool-use Hook)

**실행 시점**: Cursor가 파일 저장할 때마다

**검증 범위**: 변경된 파일만

**속도**: 148ms (Cache 기반)

**결과**: 즉시 경고

### Level 2: Pre-commit (Git Hook)

**실행 시점**: Git commit 직전

**검증 범위**: Staged 파일

**검증 내용**:
- Transaction 경계 (외부 API 호출)
- Spring 프록시 제약사항 (Private/Final)

**결과**: Commit 차단 또는 허용

### Level 3: Build-time (ArchUnit)

**실행 시점**: 빌드 시

**검증 범위**: 전체 프로젝트

**검증 내용**:
- Layer 의존성 규칙
- Naming Convention
- Annotation 규칙

**결과**: 빌드 성공/실패

---

## 📦 출력

**성공 케이스:**
```
✅ 검증 완료: 모든 파일 통과

📋 검증 결과:
- Domain Layer: 3/3 통과
- Application Layer: 3/3 통과
- REST API Layer: 2/2 통과

✨ Cursor AI가 생성한 코드가 모든 컨벤션을 준수합니다!

📝 다음 단계:
1. 비즈니스 로직 구현 (Claude Code)
2. 테스트 생성 (/generate-fixtures Order)
```

**실패 케이스:**
```
❌ 검증 실패: 1개 파일 위반

📋 위반 파일:
- domain/order/model/OrderDomain.java (Law of Demeter)

📄 상세 리포트: .claude/validation-report.md

🔧 수정 가이드:
1. .claude/validation-report.md 확인
2. 위반 사항 수정
3. /validate-cursor-changes 재실행
```

---

## 🔗 통합 워크플로우

**전체 프로세스:**

```bash
# 1. Claude Code: 설계 분석
/design-analysis Order

# 2. Git Worktree 생성
git worktree add ../wt-order feature/order

# 3. Cursor AI: Boilerplate 생성 (Worktree)
# → .cursorrules 자동 로드
# → docs/coding_convention/ 참조
# → 코드 생성

# 4. Cursor AI: Git Commit
git add .
git commit -m "feat: Order Aggregate 생성"
# → Hook 실행: .claude/cursor-changes.md 생성

# 5. Claude Code: 검증 (Main)
/validate-cursor-changes
# → validation-helper.py 실행
# → ArchUnit 실행
# → 리포트 생성

# 6. 통과 시 비즈니스 로직 구현
# 위반 시 수정 후 재검증
```

---

## 🔧 validation-helper.py 상세

**위치**: `.claude/hooks/scripts/validation-helper.py`

**입력**: 파일 경로

**처리**:
1. Cache에서 Layer 규칙 로드 (O(1))
2. 정규식 패턴 매칭
3. 위반 시 구체적인 수정 방법 제시

**출력**: JSON 포맷 검증 결과

**성능**: 148ms (90% 토큰 절감, 73.6% 속도 향상)

---

**✅ 이 커맨드는 Cursor AI 코드 자동 검증을 담당합니다!**
