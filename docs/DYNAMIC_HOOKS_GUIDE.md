# 🎣 Dynamic Hooks Guide - Claude Code 실시간 검증

이 프로젝트는 **두 가지 훅 시스템**을 사용합니다:

---

## 🔄 Two-Tier Hook System

### 1️⃣ **Claude Code Dynamic Hooks** (실시간 검증)
**위치**: `.claude/hooks/`

**실행 시점**: Claude가 코드를 생성하는 **실시간**

**목적**:
- Claude에게 모듈별 규칙 주입
- 코드 생성 직후 즉시 검증
- 위반사항 실시간 피드백

### 2️⃣ **Git Pre-Commit Hooks** (커밋 시점 검증)
**위치**: `hooks/` → `.git/hooks/`

**실행 시점**: `git commit` 실행 시

**목적**:
- 최종 게이트키퍼 역할
- ArchUnit 테스트 실행
- 전체 파일 통합 검증

---

## 🎯 Dynamic Hooks 상세

### Hook 1: `user-prompt-submit.sh`

**트리거**: 사용자가 요청을 제출할 때

**동작**:
```bash
사용자 요청 분석
    ↓
모듈 키워드 감지
  - "domain", "도메인" → Domain 가이드라인
  - "usecase", "서비스" → Application 가이드라인
  - "controller", "api" → Adapter 가이드라인
  - "repository", "jpa" → Persistence 가이드라인
    ↓
모듈별 규칙을 Claude에게 주입
    ↓
글로벌 규칙 리마인드 (Lombok 금지 등)
```

**예시**:
```bash
사용자: "Order 도메인 엔티티를 만들어줘"
    ↓
Hook 감지: "도메인" 키워드
    ↓
Claude에게 주입:
  ❌ NO Spring, NO JPA, NO Lombok
  ✅ Pure Java만 허용
  📝 Javadoc + @author 필수
  🧪 90% 커버리지 목표
```

---

### Hook 2: `after-tool-use.sh`

**트리거**: Claude가 Write/Edit 도구를 사용한 직후

**동작**:
```bash
파일 경로 분석
    ↓
모듈 감지 (domain/application/adapter)
    ↓
해당 모듈 validator 실행
  - domain-validator.sh
  - application-validator.sh
  - adapter-in-validator.sh
  - adapter-out-validator.sh
    ↓
공통 검증 (Javadoc, @author)
    ↓
데드코드 감지
    ↓
❌ 실패 시 경고 + 수정 요청
✅ 통과 시 계속 진행
```

**예시**:
```bash
Claude: Order.java 파일을 domain/에 생성
    ↓
Hook 트리거: after-tool-use (Write)
    ↓
파일 경로: domain/model/Order.java
    ↓
domain-validator.sh 실행
    ↓
검사 항목:
  ❌ Spring import 있나? → 차단
  ❌ Lombok annotation 있나? → 차단
  ❌ JPA annotation 있나? → 차단
  ✅ 모두 통과 → 성공
```

---

## 🎨 Hook이 주입하는 가이드라인

### Domain Module
```markdown
❌ ABSOLUTELY FORBIDDEN
- NO Spring Framework
- NO JPA/Hibernate
- NO Lombok
- NO Jackson annotations

✅ ALLOWED
- Pure Java
- Apache Commons Lang3
- Domain value objects

📝 REQUIRED
- Javadoc with @author
- 90%+ test coverage
- Manual getters/setters (no Lombok)
```

### Application Module
```markdown
❌ ABSOLUTELY FORBIDDEN
- NO Adapter imports
- NO Lombok
- NO direct JPA usage

✅ ALLOWED
- Domain imports
- Spring DI (@Service, @Transactional)
- Port interfaces

📝 REQUIRED
- UseCase suffix for use cases
- 80%+ test coverage
```

### Adapter Module
```markdown
❌ ABSOLUTELY FORBIDDEN
- NO Lombok
- NO business logic

✅ ALLOWED
- Spring Framework
- JPA, AWS SDK, etc.
- Infrastructure code

📝 REQUIRED
- Controller/Repository suffix
- 70%+ test coverage
- Testcontainers for integration tests
```

---

## 🚀 실전 시나리오

### 시나리오 1: Domain 엔티티 생성

```bash
# 사용자 요청
"Order 도메인 엔티티를 만들어줘"

# user-prompt-submit.sh 실행
→ "도메인" 키워드 감지
→ Domain 가이드라인 주입

# Claude 코드 생성
public class Order {
    private String id;
    // ... pure Java
}

# after-tool-use.sh 실행
→ domain/model/Order.java 감지
→ domain-validator.sh 실행
→ ✅ Spring/JPA/Lombok 없음 확인
→ ✅ 통과
```

---

### 시나리오 2: Lombok 사용 시도 (차단)

```bash
# 사용자 요청 (잘못된 요청)
"Order 엔티티를 Lombok으로 만들어줘"

# user-prompt-submit.sh 실행
→ Lombok 금지 경고 주입

# Claude가 순수 Java로 생성 (가이드라인 따름)
public class Order {
    private final String id;

    public Order(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}

# 만약 Claude가 실수로 Lombok 사용했다면?
# after-tool-use.sh 실행
→ domain-validator.sh 실행
→ ❌ Lombok import 감지
→ ❌ 검증 실패, Claude에게 수정 요청
```

---

### 시나리오 3: Application → Adapter 의존성 시도 (차단)

```bash
# 사용자 요청
"CreateOrderService에서 OrderController를 호출해줘"

# user-prompt-submit.sh 실행
→ "서비스" 키워드 감지
→ Application 가이드라인 주입
→ "NO Adapter imports" 경고

# Claude가 올바르게 Port 사용
public class CreateOrderService {
    private final OrderRepository repository; // Port 인터페이스
    // Controller 호출 안함
}

# after-tool-use.sh 실행
→ application/service/ 감지
→ application-validator.sh 실행
→ ✅ Adapter import 없음 확인
→ ✅ 통과
```

---

## 📊 Hook 실행 흐름 다이어그램

```
┌─────────────────────────────────────────────┐
│  사용자: "Order 도메인 엔티티 만들어줘"      │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  HOOK 1: user-prompt-submit.sh              │
│  - 키워드 분석: "도메인" → Domain context   │
│  - 가이드라인 주입                          │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  Claude: 코드 생성                          │
│  - Domain 가이드라인 따라 순수 Java 작성   │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│  HOOK 2: after-tool-use.sh                  │
│  - 파일: domain/model/Order.java            │
│  - Validator: domain-validator.sh 실행      │
│  - 검증: Spring/JPA/Lombok 체크             │
└─────────────────────────────────────────────┘
                    ↓
            ✅ 통과 또는 ❌ 실패
```

---

## 🔧 설정 방법

### 1. Hook 활성화 확인

Claude Code는 자동으로 `.claude/hooks/` 디렉토리의 훅을 인식합니다.

**확인 방법**:
```bash
ls -la .claude/hooks/
# user-prompt-submit.sh (실행 권한 있어야 함)
# after-tool-use.sh (실행 권한 있어야 함)
```

### 2. 권한 설정

```bash
chmod +x .claude/hooks/*.sh
```

### 3. 테스트

```bash
# Claude에게 요청
"Order 도메인 엔티티를 만들어줘"

# Hook이 작동하는지 확인
# → 가이드라인이 주입된 메시지가 표시되어야 함
# → 코드 생성 후 검증 결과가 표시되어야 함
```

---

## 🎯 Benefits

### ✅ 실시간 피드백
- 커밋 전에 문제 발견
- Claude가 즉시 수정 가능

### ✅ 컨텍스트 주입
- 모듈별 규칙 자동 주입
- Claude가 규칙을 이해하고 따름

### ✅ 이중 안전망
- Dynamic Hook (실시간)
- Git Hook (커밋 시점)

### ✅ 개발자 경험 향상
- 명확한 에러 메시지
- 즉각적인 수정 가이드

---

## 📝 Hook 커스터마이징

### 모듈별 규칙 추가

`user-prompt-submit.sh`에서 규칙 수정:

```bash
case $MODULE_CONTEXT in
    domain)
        cat << 'EOF'
# 여기에 Domain 규칙 추가
- NEW RULE: 모든 엔티티는 ID를 가져야 함
EOF
        ;;
esac
```

### Validator 강화

`hooks/validators/domain-validator.sh`에서 검증 로직 추가:

```bash
# 새로운 검증 규칙 추가
if grep -q "public class.*Entity" "$file"; then
    if ! grep -q "private.*id" "$file"; then
        log_error "$file: Entity must have an 'id' field"
    fi
fi
```

---

## 🚨 Troubleshooting

### Hook이 실행되지 않을 때

```bash
# 1. 권한 확인
ls -la .claude/hooks/
# -rwxr-xr-x (x가 있어야 함)

# 2. 권한 부여
chmod +x .claude/hooks/*.sh

# 3. 스크립트 문법 확인
bash -n .claude/hooks/user-prompt-submit.sh
```

### Validator 실패 디버깅

```bash
# 직접 실행해보기
bash hooks/validators/domain-validator.sh domain/model/Order.java

# 상세 로그
set -x  # 스크립트 상단에 추가
```

---

## 🎉 결론

**Dynamic Hooks = Claude가 코드 작성 중 실시간으로 규칙을 따르게 만드는 시스템**

- **Before 코드 작성**: 규칙 주입
- **After 코드 작성**: 즉시 검증
- **커밋 전**: Git Hook으로 최종 확인

**이제 Claude는 항상 프로젝트 규칙을 따릅니다!** 🚀

---

## ⚠️ USE AT YOUR OWN RISK

### 🔒 Security Considerations

Dynamic Hook 스크립트는 **사용자 권한으로 실행**되므로 보안에 주의해야 합니다.

**중요 원칙**:
- ✅ **Hook scripts execute with your user permissions** - 파일 시스템 접근, 네트워크 호출 등 모든 권한 보유
- ✅ **Review all hook scripts before activation** - 실행 전 스크립트 내용을 반드시 검토
- ✅ **Never run hooks from untrusted sources** - 신뢰할 수 없는 출처의 Hook 절대 실행 금지
- ✅ **Validate script content before chmod +x** - 실행 권한 부여 전 스크립트 검증

### 🛡️ Best Practices

#### 1. Version Control
```bash
# Hook 스크립트를 버전 관리에 포함
git add .claude/hooks/*.sh
git commit -m "Add hook scripts"

# 변경 이력 추적
git log -- .claude/hooks/
```

#### 2. Code Review
```bash
# Hook 변경사항은 반드시 코드 리뷰
# Pull Request에서 다른 팀원의 승인 필요

# .claude/hooks/ 디렉토리 변경 시 알림 설정 권장
```

#### 3. Safe Testing Environment
```bash
# 1. 테스트 브랜치에서 먼저 검증
git checkout -b test/new-hook
# ... hook 수정 ...
# ... 안전성 검증 ...

# 2. 스크립트 문법 검증
bash -n .claude/hooks/user-prompt-submit.sh
shellcheck .claude/hooks/user-prompt-submit.sh

# 3. 안전한 환경에서 실행 테스트
# (예: 격리된 디렉토리, Docker 컨테이너 등)
```

#### 4. Principle of Least Privilege
```bash
# Hook 스크립트는 최소한의 권한만 사용
# - 읽기 전용 작업 선호
# - 파일 수정은 명시적 확인 후에만
# - 외부 네트워크 호출 최소화

# Bad Example: ❌
rm -rf /some/path  # Dangerous!

# Good Example: ✅
echo "Validation failed" >&2
exit 1
```

### 🚨 Security Warnings

**절대 하지 말아야 할 것**:
- ❌ Hook에서 민감한 정보(API 키, 비밀번호) 하드코딩
- ❌ Hook에서 외부 URL로 코드 다운로드 후 실행
- ❌ Hook에서 sudo 권한 요구
- ❌ 검증되지 않은 사용자 입력 직접 실행 (`eval`, `exec` 등)

**권장 사항**:
- ✅ Hook 스크립트는 읽기 전용 검증만 수행
- ✅ 환경 변수를 통한 설정 관리
- ✅ 로그 파일은 안전한 위치에 저장
- ✅ 정기적인 보안 감사

### 🔍 Hook Script Validation Checklist

Hook 스크립트를 추가/수정하기 전 확인:

```bash
# 1. 스크립트 문법 검증
bash -n script.sh

# 2. ShellCheck으로 잠재적 문제 탐지
shellcheck script.sh

# 3. 실행 권한 확인
ls -la .claude/hooks/*.sh

# 4. 스크립트 내용 리뷰
cat .claude/hooks/script.sh | less

# 5. 위험한 명령어 검색
grep -E "(rm -rf|sudo|curl.*\| bash|eval|exec)" .claude/hooks/*.sh
```

### 📋 Incident Response

Hook 스크립트에서 문제 발견 시:

1. **즉시 실행 권한 제거**
   ```bash
   chmod -x .claude/hooks/suspicious-script.sh
   ```

2. **Git에서 제거 (필요시)**
   ```bash
   git rm .claude/hooks/suspicious-script.sh
   git commit -m "Remove suspicious hook script"
   ```

3. **팀에 알림**
   - 다른 개발자들에게 즉시 공유
   - 잠재적 영향 범위 분석

4. **검증 후 재도입**
   - 문제 해결 후 코드 리뷰
   - 안전성 재확인 후 추가

---

**⚠️ 결론**: Dynamic Hook은 강력한 도구이지만, 보안에 항상 주의해야 합니다. 신뢰할 수 있는 소스의 스크립트만 사용하고, 정기적으로 검토하세요.
