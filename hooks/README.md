# 🔒 Git Hooks - Commit-Time Architecture Guardian

**커밋 시점**에 헥사고날 아키텍처 경계를 보호하는 Git Pre-commit Hook 시스템입니다.

> ⚠️ **중요**: 이것은 **Git Hooks**입니다. **Claude Code 동적 훅**과는 다릅니다.
> - **Git Hooks** (`hooks/`): `git commit` 실행 시 자동 검증 (이 문서)
> - **Claude Hooks** (`.claude/hooks/`): Claude Code가 코드 생성/수정 시 실행 ([문서](../.claude/hooks/README.md))

---

## 📋 목차

- [개요](#개요)
- [Git Hook vs Claude Hook 비교](#git-hook-vs-claude-hook-비교)
- [설치 방법](#설치-방법)
- [전체 흐름도](#전체-흐름도)
- [Validator 스크립트 설명](#validator-스크립트-설명)
- [검증 규칙 상세](#검증-규칙-상세)
- [트러블슈팅](#트러블슈팅)

---

## 🎯 개요

### 목적
**커밋 시점**에 헥사고날 아키텍처 규칙 위반을 자동으로 감지하여 잘못된 코드가 레포지토리에 들어가는 것을 차단합니다.

### 주요 기능
- ✅ **모듈별 경계 검증**: Domain, Application, Adapter 계층 간 의존성 규칙 강제
- ✅ **코드 품질 검사**: Lombok 금지, 네이밍 규칙, Javadoc 필수
- ✅ **아키텍처 테스트**: ArchUnit 테스트 자동 실행
- ✅ **데드코드 감지**: 사용되지 않는 Helper/Utils 클래스 탐지

### 검증 레벨
```
Level 1: Pre-commit Hook (즉시 차단)
   ↓
Level 2: ArchUnit Tests (컴파일 타임)
   ↓
Level 3: CI/CD Pipeline (배포 전 최종 검증)
```

---

## ⚡ Git Hook vs Claude Hook 비교

| 항목 | Git Hooks (`hooks/`) | Claude Hooks (`.claude/hooks/`) |
|------|----------------------|----------------------------------|
| **실행 시점** | `git commit` 실행 시 | Claude가 코드 생성/수정 시 |
| **실행 주체** | Git (개발자 로컬) | Claude Code AI |
| **목적** | 잘못된 코드 커밋 차단 | AI 코드 생성 가이드 제공 |
| **검증 방식** | Shell 스크립트 패턴 검색 | 프롬프트 주입 + 실시간 검증 |
| **차단 여부** | ❌ 실패 시 커밋 차단 | ⚠️ 경고만 제공 (차단 안 함) |
| **대상** | 커밋할 파일 | Claude가 작성/수정하는 파일 |
| **우회 가능** | `--no-verify` 플래그 | 우회 불가 (항상 실행) |

### 실행 흐름 비교

**Git Hooks 흐름**:
```
개발자가 코드 작성
    ↓
git add .
    ↓
git commit -m "..."
    ↓
hooks/pre-commit 실행  ← 이 문서
    ↓
검증 통과 → 커밋 완료
검증 실패 → 커밋 차단
```

**Claude Hooks 흐름**:
```
Claude에게 코드 요청
    ↓
.claude/hooks/user-prompt-submit.sh 실행  ← 요청 전 가이드 주입
    ↓
Claude가 코드 생성
    ↓
.claude/hooks/after-tool-use.sh 실행  ← 생성 후 즉시 검증
    ↓
경고 발견 → 사용자에게 알림 (코드는 생성됨)
문제 없음 → 완료
```

### 언제 어느 것이 실행되나?

**Claude Hooks** (AI 코드 생성 시):
- `claude write src/Order.java` → `.claude/hooks` 실행
- 목적: Claude가 처음부터 올바른 코드를 생성하도록 유도

**Git Hooks** (커밋 시):
- `git commit -m "..."` → `hooks/pre-commit` 실행
- 목적: 수동 작성 코드나 Claude가 놓친 규칙 위반 최종 차단

### 두 시스템의 시너지

```
Claude Hooks (사전 예방)
    ↓
개발자 추가 수정
    ↓
Git Hooks (최종 검증)
    ↓
커밋 완료
```

**Best Practice**: 두 시스템을 모두 활성화하여 이중 안전망 구축

---

## 🚀 설치 방법

### 1. Git Hook 활성화

```bash
# 프로젝트 루트에서 실행
ln -s ../../hooks/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
chmod +x hooks/validators/*.sh
```

### 2. 설치 확인

```bash
# 테스트 커밋 시도
git add .
git commit -m "test: hook validation"

# ✅ 성공 시 출력 예시:
# ℹ️  Analyzing staged changes...
# ℹ️  Changed files by module:
#   📦 Domain: 2 files
# ✅ All validations passed! ✨
```

---

## 🔄 전체 흐름도

```
┌─────────────────────────────────────────────────────────────┐
│                      git commit 시도                          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│              pre-commit (Master Hook)                        │
│  - 변경 파일 분석                                              │
│  - 모듈별 파일 분류                                            │
└─────────────────────────────────────────────────────────────┘
                            ↓
        ┌───────────────────┼───────────────────┐
        ↓                   ↓                   ↓
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   Domain     │   │ Application  │   │   Adapter    │
│  Validator   │   │  Validator   │   │  Validators  │
└──────────────┘   └──────────────┘   └──────────────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│              Common Validator                                │
│  - Javadoc 검증                                               │
│  - @author 태그 확인                                          │
│  - Lombok 금지 검사                                           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│            Dead Code Detector                                │
│  - Utils/Helper 클래스 사용처 검사                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│              ArchUnit Tests                                  │
│  - Domain 아키텍처 테스트                                      │
│  - Application 아키텍처 테스트                                 │
│  - Adapter 아키텍처 테스트                                     │
└─────────────────────────────────────────────────────────────┘
                            ↓
                    ┌───────┴───────┐
                    ↓               ↓
            ✅ 모든 검증 통과     ❌ 검증 실패
            커밋 허용            커밋 차단
```

---

## 📦 Validator 스크립트 설명

### 1. `pre-commit` (Master Hook)

**역할**: 변경된 파일을 분석하여 적절한 validator로 라우팅

**주요 기능**:
- 변경 파일을 모듈별로 분류 (Domain, Application, Adapter-In, Adapter-Out)
- 각 모듈에 맞는 validator 실행
- 공통 검증 및 데드코드 감지 실행
- ArchUnit 테스트 자동 실행
- 최종 결과 요약 및 커밋 허용/차단 결정

**파일 분류 규칙**:
```bash
domain/*                         → Domain Validator
application/*                    → Application Validator
adapter/adapter-in-admin-web/*   → Controller Validator (특수)
adapter/adapter-out-persistence-jpa/* → Persistence Validator (특수)
adapter/adapter-in-*             → Adapter-In Validator (일반)
adapter/adapter-out-*            → Adapter-Out Validator (일반)
```

---

### 2. `domain-validator.sh`

**역할**: Domain 모듈의 순수성(purity) 검증

**검증 항목** (총 7개 규칙):

#### ❌ 절대 금지 항목
1. **Spring Framework 의존성**
   ```java
   // ❌ VIOLATION
   import org.springframework.*;
   @Service, @Component, @Autowired
   ```

2. **JPA/Hibernate 의존성**
   ```java
   // ❌ VIOLATION
   import jakarta.persistence.*;
   import org.hibernate.*;
   @Entity, @Table, @Id, @Column
   ```

3. **Lombok 사용**
   ```java
   // ❌ VIOLATION
   import lombok.*;
   @Data, @Builder, @Getter, @Setter
   ```

#### ⚠️  경고 항목
4. **Jackson 직렬화 어노테이션**
   ```java
   // ⚠️  WARNING (domain은 직렬화에 무관해야 함)
   @JsonProperty, @JsonIgnore, @JsonFormat
   ```

**위반 시 출력**:
```
❌ DOMAIN VIOLATION: domain/model/Order.java contains Spring Framework import
   import org.springframework.stereotype.Component;

DOMAIN PURITY POLICY:
- Domain must remain pure Java
- NO Spring, NO JPA, NO Lombok, NO infrastructure
- See: docs/architecture/hexagonal-architecture.md
```

---

### 3. `application-validator.sh`

**역할**: Application 계층의 의존성 방향 검증

**검증 항목** (총 5개 규칙):

#### ❌ 절대 금지 항목
1. **Adapter 직접 참조**
   ```java
   // ❌ VIOLATION
   import com.company.template.adapter.*;
   ```

2. **Lombok 사용**
   ```java
   // ❌ VIOLATION
   @Data, @Builder
   ```

#### ⚠️  경고 항목
3. **UseCase 인터페이스 누락**
   ```java
   // ⚠️  WARNING
   public class CreateOrderService {  // UseCase 미구현
   ```

4. **@Transactional 누락**
   ```java
   // ⚠️  WARNING
   public class CreateOrderService implements UseCase {
       // @Transactional 없음
   ```

---

### 4. `persistence-validator.sh`

**역할**: Persistence Adapter의 JPA 사용 규칙 검증

**검증 항목** (총 6개 규칙):

#### ❌ 절대 금지 항목
1. **JPA 관계 어노테이션**
   ```java
   // ❌ VIOLATION
   @OneToMany, @ManyToOne, @OneToOne, @ManyToMany

   // ✅ CORRECT - Long FK 사용
   @Column(nullable = false)
   private Long userId;  // NOT @ManyToOne User user
   ```

2. **Entity 클래스의 Setter 메서드**
   ```java
   // ❌ VIOLATION
   public void setName(String name) {
       this.name = name;
   }
   ```

3. **Entity의 public 생성자**
   ```java
   // ❌ VIOLATION
   public OrderEntity(Long userId) { ... }

   // ✅ CORRECT
   protected OrderEntity() {}  // JPA용
   private OrderEntity(Long userId) { ... }  // 실제 생성
   public static OrderEntity create(Long userId) { ... }  // Factory
   ```

4. **Adapter에서 @Transactional 사용**
   ```java
   // ❌ VIOLATION
   @Transactional  // Application 레이어에서만 허용
   public class OrderPersistenceAdapter { ... }
   ```

#### ⚠️  경고 항목
5. **Entity에 비즈니스 로직 존재 가능성**
   ```java
   // ⚠️  WARNING
   public class OrderEntity {
       public void calculate() { ... }  // 비즈니스 로직은 Domain으로
   ```

6. **Mapper 클래스 누락**
   ```java
   // ⚠️  WARNING
   public class OrderPersistenceAdapter {
       // Mapper 사용 권장
   ```

---

### 5. `controller-validator.sh`

**역할**: Controller Adapter의 DTO 및 의존성 규칙 검증

**검증 항목** (총 6개 규칙):

#### ❌ 절대 금지 항목
1. **Controller 내부 클래스로 Request/Response 정의**
   ```java
   // ❌ VIOLATION
   @RestController
   public class OrderController {
       class CreateOrderRequest { ... }  // 별도 파일로 분리 필요
   ```

2. **Request/Response가 record가 아닌 경우**
   ```java
   // ❌ VIOLATION
   public class CreateOrderRequest { ... }

   // ✅ CORRECT
   public record CreateOrderRequest(String orderId, int amount) {
       // Compact constructor에 validation
       public CreateOrderRequest {
           if (amount <= 0) throw new IllegalArgumentException();
       }
   }
   ```

3. **Repository/Entity 직접 의존**
   ```java
   // ❌ VIOLATION
   @RestController
   public class OrderController {
       private final OrderRepository repository;  // UseCase만 의존해야 함
       private final OrderEntity entity;  // DTO만 사용해야 함
   ```

#### ⚠️  경고 항목
4. **Controller에 비즈니스 로직 존재**
   ```java
   // ⚠️  WARNING
   public void createOrder() {
       double total = price * quantity;  // 로직은 UseCase/Domain으로
   ```

5. **Request에 toCommand() 메서드 누락**
   ```java
   // ⚠️  WARNING
   public record CreateOrderRequest(...) {
       // toCommand() 메서드 권장
   ```

6. **Response에 from() 메서드 누락**
   ```java
   // ⚠️  WARNING
   public record OrderResponse(...) {
       // static from(Order order) 메서드 권장
   ```

---

### 6. `adapter-in-validator.sh`

**역할**: 일반 Inbound Adapter (REST API 외) 검증

**검증 항목**:
- Lombok 사용 금지
- Domain 직접 참조 금지 (UseCase 인터페이스 사용)
- @author 태그 필수

---

### 7. `adapter-out-validator.sh`

**역할**: 일반 Outbound Adapter (Persistence 외) 검증

**검증 항목**:
- Lombok 사용 금지
- @Transactional 사용 금지
- Port 인터페이스 구현 확인

---

### 8. `common-validator.sh`

**역할**: 모든 Java 파일에 대한 공통 규칙 검증

**검증 항목** (총 3개 규칙):

1. **Public API Javadoc 필수**
   ```java
   // ❌ VIOLATION
   public Order createOrder() { ... }

   // ✅ CORRECT
   /**
    * 주문을 생성합니다.
    * @param request 주문 생성 요청
    * @return 생성된 주문
    */
   public Order createOrder(CreateOrderRequest request) { ... }
   ```

2. **@author 태그 필수**
   ```java
   /**
    * @author 홍길동 (hong@company.com)
    * @since 2024-01-01
    */
   public class OrderService { ... }
   ```

3. **Lombok 전체 금지** (전 계층 공통)
   ```java
   // ❌ VIOLATION
   @Data, @Builder, @Getter, @Setter
   @AllArgsConstructor, @NoArgsConstructor
   ```

---

### 9. `dead-code-detector.sh`

**역할**: 사용되지 않는 Helper/Utils 클래스 탐지

**검증 로직**:
```bash
1. Utils/Helper 클래스 파일 탐지
2. 프로젝트 전체에서 해당 클래스 사용처 검색
3. 사용처가 1개 이하면 데드코드로 간주 (자기 자신 참조)
```

**탐지 패턴**:
```java
// 감지 대상
- *Utils.java
- *Helper.java
- *Util.java
```

**출력 예시**:
```
⚠️  DEAD CODE: StringUtils.java is not used anywhere
   Consider removing unused utility classes
```

---

## 🔍 검증 규칙 상세

### Domain 계층 (7개 규칙)

| 규칙 | 레벨 | 설명 |
|------|------|------|
| NO Spring | ❌ 차단 | Spring Framework 의존성 금지 |
| NO JPA | ❌ 차단 | JPA/Hibernate 의존성 금지 |
| NO Lombok | ❌ 차단 | Lombok 사용 금지 |
| NO Jackson | ⚠️ 경고 | 직렬화 어노테이션 지양 |

### Application 계층 (5개 규칙)

| 규칙 | 레벨 | 설명 |
|------|------|------|
| NO Adapter 참조 | ❌ 차단 | Adapter 직접 의존 금지 |
| NO Lombok | ❌ 차단 | Lombok 사용 금지 |
| UseCase 구현 | ⚠️ 경고 | UseCase 인터페이스 구현 권장 |
| @Transactional | ⚠️ 경고 | 트랜잭션 관리 권장 |

### Persistence Adapter (6개 규칙)

| 규칙 | 레벨 | 설명 |
|------|------|------|
| NO JPA 관계 | ❌ 차단 | @OneToMany 등 금지, Long FK 사용 |
| NO Setter | ❌ 차단 | Entity 불변성 유지 |
| NO Public 생성자 | ❌ 차단 | Static factory 패턴 강제 |
| NO @Transactional | ❌ 차단 | Application 레이어에서만 허용 |
| NO 비즈니스 로직 | ⚠️ 경고 | Entity는 데이터만 |
| Mapper 사용 | ⚠️ 경고 | Entity ↔ Domain 변환 |

### Controller Adapter (6개 규칙)

| 규칙 | 레벨 | 설명 |
|------|------|------|
| NO Inner Class | ❌ 차단 | DTO는 별도 파일 |
| Record 필수 | ❌ 차단 | Request/Response는 record |
| NO Repository | ❌ 차단 | UseCase만 의존 |
| NO Entity | ❌ 차단 | DTO만 사용 |
| NO 비즈니스 로직 | ⚠️ 경고 | 얇은 컨트롤러 유지 |
| 변환 메서드 | ⚠️ 경고 | toCommand(), from() 권장 |

### 공통 규칙 (3개 규칙)

| 규칙 | 레벨 | 설명 |
|------|------|------|
| Javadoc 필수 | ❌ 차단 | Public API 문서화 |
| @author 필수 | ❌ 차단 | 작성자 명시 |
| NO Lombok | ❌ 차단 | 전체 프로젝트 금지 |

---

## 🛠️ 트러블슈팅

### 문제 1: Hook이 실행되지 않음

**증상**:
```bash
git commit -m "test"
# → Hook 실행 없이 바로 커밋됨
```

**해결**:
```bash
# 1. Hook 파일 권한 확인
ls -la .git/hooks/pre-commit

# 2. 실행 권한 부여
chmod +x .git/hooks/pre-commit
chmod +x hooks/validators/*.sh

# 3. Symlink 재생성
rm .git/hooks/pre-commit
ln -s ../../hooks/pre-commit .git/hooks/pre-commit
```

---

### 문제 2: 특정 validator만 비활성화하고 싶음

**방법 1: 임시로 특정 검증 건너뛰기**
```bash
# Domain validator만 비활성화 (임시)
mv hooks/validators/domain-validator.sh hooks/validators/domain-validator.sh.disabled

git commit -m "test"

# 복구
mv hooks/validators/domain-validator.sh.disabled hooks/validators/domain-validator.sh
```

**방법 2: pre-commit 수정**
```bash
# hooks/pre-commit에서 해당 validator 호출 부분 주석 처리
# if [ ${#DOMAIN_FILES[@]} -gt 0 ]; then
#     log_info "Validating Domain module..."
#     ...
# fi
```

---

### 문제 3: Hook 전체를 건너뛰고 커밋

**긴급 상황용 (권장하지 않음)**:
```bash
git commit -m "emergency fix" --no-verify
```

**⚠️ 주의**:
- CI/CD에서 동일한 검증을 다시 실행하므로 결국 빌드가 실패할 수 있습니다
- 팀 코드 품질 저하의 원인이 됩니다

---

### 문제 4: ArchUnit 테스트가 너무 오래 걸림

**해결**:
```bash
# hooks/pre-commit 수정
# ArchUnit 테스트를 주석 처리하고 CI/CD에서만 실행
# if ./gradlew :domain:test --tests "*HexagonalArchitectureTest" -q; then
#     log_success "ArchUnit tests passed"
# fi
```

**대안**: Git hook에서는 빠른 검증만 수행하고, CI/CD에서 전체 검증

---

### 문제 5: False Positive (잘못된 위반 감지)

**예시**:
```bash
❌ DOMAIN VIOLATION: domain/model/Order.java contains Spring Framework import
   import org.springframework.util.Assert;  # 실제로는 허용해야 함
```

**해결**:
```bash
# hooks/validators/domain-validator.sh 수정
# 특정 패턴 제외 추가
if grep -q "import org\.springframework\." "$file" | grep -v "util.Assert"; then
    log_error "$file contains Spring Framework import"
fi
```

---

## 📚 관련 문서

- **[CODING_STANDARDS.md](../docs/CODING_STANDARDS.md)** - 87개 코딩 규칙 전체 목록
- **[DYNAMIC_HOOKS_GUIDE.md](../docs/DYNAMIC_HOOKS_GUIDE.md)** - Claude Code 동적 훅 가이드
- **[헥사고날 아키텍처](../docs/architecture/hexagonal-architecture.md)** - 아키텍처 상세 설명 (TODO)

---

## 🤝 기여

새로운 validator 추가 시:

1. `hooks/validators/` 에 새 스크립트 작성
2. `hooks/pre-commit`에 호출 로직 추가
3. 이 README.md 업데이트
4. CODING_STANDARDS.md에 규칙 문서화

---

**🎯 목표**: 코드 커밋 시점부터 아키텍처 품질을 보장하여 기술 부채 최소화

© 2024 Company Name. All Rights Reserved.
