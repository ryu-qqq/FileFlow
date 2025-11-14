# Spring Standards Template Migration Guide

기존 Spring Boot 프로젝트에 이 템플릿의 컨벤션과 자동화를 적용하는 가이드입니다.

---

## 📋 개요

이 가이드는 기존 Spring Boot 프로젝트에 다음을 적용합니다:

- ✅ **Kent Beck TDD + Tidy First** 철학 및 커밋 규칙
- ✅ **Zero-Tolerance Rules** (Lombok 금지, Law of Demeter, Long FK 전략 등)
- ✅ **Hexagonal Architecture** 레이어 구조 (점진적 적용 가능)
- ✅ **자동화 도구** (Git Hooks, ArchUnit, LangFuse 메트릭)
- ✅ **AI 도구 통합** (Claude Code, Cursor IDE, CodeRabbit)

---

## 🎯 적용 전략

### 전략 A: 컨벤션만 적용 (빠른 시작)
기존 코드 구조는 유지하고 컨벤션, Git Hooks, AI 도구만 적용합니다.
- **시간**: 30분~1시간
- **위험도**: 낮음
- **추천**: 진행 중인 프로젝트, 기존 아키텍처 유지

### 전략 B: 점진적 마이그레이션 (권장)
컨벤션 적용 후 헥사고날 아키텍처로 단계적 전환합니다.
- **시간**: 수 주~수 개월 (프로젝트 크기에 따라)
- **위험도**: 중간
- **추천**: 장기 프로젝트, 기술 부채 해소

### 전략 C: 전면 재구성 (신중)
프로젝트 전체를 헥사고날 아키텍처로 재구성합니다.
- **시간**: 수 개월
- **위험도**: 높음
- **추천**: 레거시 리팩토링, 신규 프로젝트 수준 재구성

**이 가이드는 전략 A와 B를 다룹니다.**

---

## 🔧 사전 요구사항

### 필수
- Java 21+
- Gradle 8.10+ (또는 Maven 호환 버전)
- Git 2.x+
- Spring Boot 3.5.x+

### 권장
- IntelliJ IDEA 또는 VS Code with Cursor
- GitHub/GitLab (CodeRabbit 사용 시)
- LangFuse 계정 (메트릭 추적 시)

---

## 📦 1단계: 컨벤션 파일 복사

### 1.1 템플릿 클론 (임시)

```bash
# 템플릿을 임시 디렉토리에 클론
git clone https://github.com/<your-org>/claude-spring-standards.git /tmp/spring-template
```

### 1.2 필수 컨벤션 파일 복사

```bash
# 프로젝트 루트로 이동
cd /path/to/your/existing/project

# 1. Cursor IDE 컨벤션
cp /tmp/spring-template/.cursorrules .

# 2. CodeRabbit 컨벤션
cp /tmp/spring-template/.coderabbit.yaml .

# 3. Claude Code 설정
cp -r /tmp/spring-template/.claude .

# 4. 코딩 컨벤션 문서 (docs/coding_convention/)
mkdir -p docs
cp -r /tmp/spring-template/docs/coding_convention docs/

# 5. Git ignore 업데이트 (선택)
cat /tmp/spring-template/.gitignore >> .gitignore
```

### 1.3 컨벤션 파일 커스터마이징

#### `.cursorrules` 수정
```bash
# 프로젝트명 변경
sed -i '' 's/claude-spring-standards/your-project-name/g' .cursorrules
```

#### `.coderabbit.yaml` 수정
```bash
# 프로젝트명 변경
sed -i '' 's/claude-spring-standards/your-project-name/g' .coderabbit.yaml
```

#### `.claude/CLAUDE.md` 수정
```bash
# 프로젝트 설명 업데이트
vim .claude/CLAUDE.md
# → 프로젝트명, 도메인, 팀 정보 수정
```

---

## 🎣 2단계: Git Hooks 설정

### 2.1 Post-Commit Hook 복사

```bash
# Git hooks 디렉토리로 이동
cd /path/to/your/existing/project

# Post-commit hook 복사
cp /tmp/spring-template/.git/hooks/post-commit .git/hooks/
chmod +x .git/hooks/post-commit

# LangFuse 로거 스크립트 복사 (이미 .claude/ 복사했으면 Skip)
cp /tmp/spring-template/.claude/scripts/log-to-langfuse.py .claude/scripts/
chmod +x .claude/scripts/log-to-langfuse.py
```

### 2.2 Git Hooks Path 설정

```bash
# 프로젝트별 hooks path 설정 (global hooks와 충돌 방지)
git config core.hooksPath .git/hooks
```

### 2.3 환경 변수 설정 (LangFuse 사용 시)

프로젝트 루트에 `.env` 파일 생성:

```bash
cat > .env << 'EOF'
# LangFuse TDD Metrics Tracking (선택)
LANGFUSE_PUBLIC_KEY=pk-lf-your-public-key
LANGFUSE_SECRET_KEY=sk-lf-your-secret-key
LANGFUSE_HOST=https://us.cloud.langfuse.com
EOF

# .gitignore에 추가 (민감 정보 보호)
echo ".env" >> .gitignore
```

**LangFuse 키 발급**: https://cloud.langfuse.com → Settings → API Keys

### 2.4 테스트

```bash
# 테스트 커밋
echo "test" > test-file.txt
git add test-file.txt
git commit -m "test: Git hooks 테스트"

# 로그 확인
tail -f ~/.claude/logs/tdd-cycle.jsonl

# 출력 예시:
# {"timestamp":"2025-11-14T...", "event_type":"tdd_commit", "data":{"commit_msg":"test: Git hooks 테스트", "tdd_phase":"red", ...}}
```

성공 시:
- ✅ `~/.claude/logs/tdd-cycle.jsonl`에 커밋 로그 저장
- ✅ LangFuse 대시보드에 Span 업로드 (환경 변수 설정 시)

---

## 🏗️ 3단계: Gradle 설정 적용

### 3.1 Dependencies 추가

기존 프로젝트의 `build.gradle` (또는 `build.gradle.kts`)에 필수 의존성 추가:

```gradle
dependencies {
    // ArchUnit (아키텍처 테스트)
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'

    // QueryDSL (CQRS Query Adapter)
    implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.1.0:jakarta'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'

    // Test Fixtures (레이어별 테스트 데이터)
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

**전체 참고**: `/tmp/spring-template/build.gradle`

### 3.2 Multi-Module 구조 (선택)

헥사고날 아키텍처로 전환 시 Multi-Module 구조 권장:

```bash
# 템플릿의 settings.gradle 참고
cat /tmp/spring-template/settings.gradle

# 출력:
# rootProject.name = 'claude-spring-standards'
# include 'domain'
# include 'application'
# include 'adapter-in:rest-api'
# include 'adapter-out:persistence-mysql'
# include 'adapter-out:persistence-redis'
# include 'test-fixtures'
```

**점진적 적용 전략**:
1. 기존 Single Module 유지 → 패키지 분리 (`domain`, `application`, `adapter`)
2. Module 분리 → `domain/`, `application/`, `adapter-in/`, `adapter-out/`
3. 의존성 역전 → Port/Adapter 패턴 적용

---

## 🧪 4단계: ArchUnit 테스트 추가

### 4.1 ArchUnit 테스트 디렉토리 생성

```bash
# 기존 프로젝트의 테스트 디렉토리 구조에 맞게 조정
mkdir -p src/test/java/com/yourcompany/yourproject/architecture
```

### 4.2 템플릿에서 ArchUnit 테스트 복사

```bash
# 예시: Domain Layer ArchUnit 테스트
cp /tmp/spring-template/domain/src/test/java/com/company/template/architecture/DomainLayerArchTest.java \
   src/test/java/com/yourcompany/yourproject/architecture/

# 패키지명 변경
sed -i '' 's/com.company.template/com.yourcompany.yourproject/g' \
    src/test/java/com/yourcompany/yourproject/architecture/DomainLayerArchTest.java
```

### 4.3 기본 ArchUnit 테스트 작성

최소한의 아키텍처 검증부터 시작:

```java
package com.yourcompany.yourproject.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(
    packages = "com.yourcompany.yourproject",
    importOptions = ImportOption.DoNotIncludeTests.class
)
public class BasicArchitectureTest {

    @ArchTest
    static final ArchRule layer_dependencies_are_respected =
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")

            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");
}
```

### 4.4 테스트 실행

```bash
# ArchUnit 테스트 실행
./gradlew test --tests "*ArchitectureTest"

# 전체 테스트 실행
./gradlew test
```

---

## 📊 5단계: LangFuse 메트릭 설정 (선택)

### 5.1 Python 의존성 설치

```bash
# LangFuse SDK 설치
pip3 install langfuse

# 설치 확인
python3 -c "from langfuse import observe; print('LangFuse SDK OK')"
```

### 5.2 환경 변수 설정

이미 2.3에서 `.env` 파일을 생성했다면 Skip.

```bash
# .env 파일 확인
cat .env

# 출력:
# LANGFUSE_PUBLIC_KEY=pk-lf-...
# LANGFUSE_SECRET_KEY=sk-lf-...
# LANGFUSE_HOST=https://us.cloud.langfuse.com
```

### 5.3 테스트 커밋

```bash
# Red Phase
git commit --allow-empty -m "test: LangFuse 메트릭 테스트"

# Green Phase
git commit --allow-empty -m "feat: LangFuse 메트릭 구현"

# Structural Phase
git commit --allow-empty -m "struct: LangFuse 메트릭 리팩토링"
```

### 5.4 LangFuse 대시보드 확인

https://cloud.langfuse.com → Traces 탭

**확인 사항**:
- ✅ 3개의 Span 생성 (🔴 Red, 🟢 Green, ♻️ Structural)
- ✅ Trace ID로 그룹핑
- ✅ Analytics 탭에서 p50/p99 확인

---

## 🏛️ 6단계: 레이어 구조 마이그레이션 (점진적)

헥사고날 아키텍처 적용은 점진적으로 진행합니다.

### 6.1 현재 구조 평가

```bash
# 기존 프로젝트 구조 확인
tree -L 3 src/main/java
```

**일반적인 Spring Boot 구조**:
```
src/main/java/com/company/project/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
└── config/
```

### 6.2 패키지 분리 (Phase 1)

기존 코드를 유지하면서 패키지만 재구성:

```
src/main/java/com/company/project/
├── domain/           # Entity, VO, Domain Service
├── application/      # UseCase, Service
├── adapter/
│   ├── in/
│   │   └── rest/    # Controller, DTO
│   └── out/
│       └── persistence/  # Repository, Mapper
└── config/
```

**마이그레이션 스크립트** (예시):
```bash
# Controller → adapter/in/rest
mkdir -p src/main/java/com/company/project/adapter/in/rest
git mv src/main/java/com/company/project/controller/* \
       src/main/java/com/company/project/adapter/in/rest/

# Service → application
mkdir -p src/main/java/com/company/project/application
git mv src/main/java/com/company/project/service/* \
       src/main/java/com/company/project/application/

# Repository → adapter/out/persistence
mkdir -p src/main/java/com/company/project/adapter/out/persistence
git mv src/main/java/com/company/project/repository/* \
       src/main/java/com/company/project/adapter/out/persistence/

# Entity → domain (신중하게, 의존성 확인 후)
mkdir -p src/main/java/com/company/project/domain
# Entity는 나중에 마이그레이션
```

### 6.3 Port/Adapter 패턴 적용 (Phase 2)

**예시: UserService → UseCase + Port**

기존:
```java
// service/UserService.java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User createUser(UserDto dto) {
        User user = new User(dto.getName(), dto.getEmail());
        return userRepository.save(user);
    }
}
```

마이그레이션 후:
```java
// application/port/in/command/CreateUserUseCase.java
public interface CreateUserUseCase {
    User createUser(CreateUserCommand command);
}

// application/port/out/command/SaveUserPort.java
public interface SaveUserPort {
    User save(User user);
}

// application/service/command/CreateUserService.java
@Service
public class CreateUserService implements CreateUserUseCase {
    private final SaveUserPort saveUserPort;

    public CreateUserService(SaveUserPort saveUserPort) {
        this.saveUserPort = saveUserPort;
    }

    @Override
    public User createUser(CreateUserCommand command) {
        User user = User.forNew(command.name(), command.email());
        return saveUserPort.save(user);
    }
}

// adapter/out/persistence/UserPersistenceAdapter.java
@Component
public class UserPersistenceAdapter implements SaveUserPort {
    private final UserJpaRepository repository;

    @Override
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        UserEntity saved = repository.save(entity);
        return UserMapper.toDomain(saved);
    }
}
```

**점진적 전환 전략**:
1. 새로운 기능부터 Port/Adapter 패턴 적용
2. 기존 코드는 리팩토링 기회가 있을 때 전환
3. Critical Path부터 우선 적용

### 6.4 참고 자료

```bash
# 템플릿의 레이어별 가이드 참고
ls -la /tmp/spring-template/docs/coding_convention/

# 출력:
# 00-project-setup/
# 01-adapter-in-layer/rest-api/
# 02-domain-layer/
# 03-application-layer/
# 04-persistence-layer/
# 05-testing/
```

각 디렉토리의 `*-guide.md` 파일 참고.

---

## ✅ 7단계: 검증

### 7.1 컨벤션 검증

```bash
# Cursor IDE에서 코드 작성 시 .cursorrules 적용 확인
# → Lombok 사용 시 경고

# CodeRabbit PR 리뷰 확인
# → TDD 커밋 패턴 검증
```

### 7.2 Git Hooks 검증

```bash
# 테스트 커밋
git commit --allow-empty -m "test: Hooks 검증"

# 로그 확인
tail -1 ~/.claude/logs/tdd-cycle.jsonl

# 예상 출력:
# {"timestamp":"...", "event_type":"tdd_commit", "data":{"tdd_phase":"red", ...}}
```

### 7.3 ArchUnit 검증

```bash
# 아키텍처 테스트 실행
./gradlew test --tests "*ArchitectureTest"

# 실패 시:
# → 레이어 의존성 위반 확인
# → 점진적 수정
```

---

## 🚨 트러블슈팅

### 문제 1: Git Hooks가 실행되지 않음

**증상**: 커밋 후 `~/.claude/logs/tdd-cycle.jsonl`에 로그가 없음

**해결**:
```bash
# 1. Hooks path 확인
git config --get core.hooksPath
# → .git/hooks 여야 함

# 2. Hooks path 설정
git config core.hooksPath .git/hooks

# 3. 실행 권한 확인
ls -l .git/hooks/post-commit
# → -rwxr-xr-x (실행 가능해야 함)

# 4. 권한 부여
chmod +x .git/hooks/post-commit

# 5. 테스트
git commit --allow-empty -m "test: Hooks 테스트"
tail -1 ~/.claude/logs/tdd-cycle.jsonl
```

### 문제 2: LangFuse 업로드 실패

**증상**: JSONL 로그는 있지만 LangFuse 대시보드에 없음

**해결**:
```bash
# 1. 환경 변수 확인
cat .env
# → LANGFUSE_PUBLIC_KEY, LANGFUSE_SECRET_KEY 확인

# 2. Python SDK 설치 확인
python3 -c "from langfuse import observe; print('OK')"
# → ImportError 발생 시 pip3 install langfuse

# 3. 수동 테스트
python3 .claude/scripts/log-to-langfuse.py \
    --event-type "tdd_commit" \
    --project "test-project" \
    --commit-hash "abc123" \
    --commit-msg "test: Manual test" \
    --tdd-phase "red" \
    --files-changed "1 file changed" \
    --lines-changed "10 insertions" \
    --timestamp "$(date -u +%Y-%m-%dT%H:%M:%SZ)"

# 4. 로그 확인
tail -f ~/.claude/logs/langfuse-hook.log
```

### 문제 3: ArchUnit 테스트 실패

**증상**: 아키텍처 테스트가 계속 실패

**해결**:
```bash
# 1. 실패 메시지 확인
./gradlew test --tests "*ArchitectureTest" --info

# 2. 점진적 적용
# → 기존 코드는 제외하고 새 코드만 검증
# → @AnalyzeClasses의 packages 범위 축소

# 3. 규칙 완화 (임시)
# → Zero-Tolerance 규칙은 나중에 적용
# → 기본 레이어 의존성만 검증
```

### 문제 4: 패키지 이동 후 컴파일 에러

**증상**: 패키지 재구성 후 import 에러

**해결**:
```bash
# 1. IntelliJ IDEA: Refactor > Move 사용 (자동 import 수정)
# 2. VS Code: Find and Replace (정규식)
# 3. 점진적 이동 (한 번에 전체 이동 금지)

# 테스트 실행으로 검증
./gradlew test
```

---

## 📚 참고 문서

### 템플릿 문서
- `docs/coding_convention/` - 88개 상세 규칙
- `.claude/CLAUDE.md` - 프로젝트 설정
- `.cursorrules` - Cursor IDE 컨벤션
- `.coderabbit.yaml` - CodeRabbit 설정

### 외부 링크
- [Kent Beck TDD](https://www.amazon.com/Test-Driven-Development-Kent-Beck/dp/0321146530)
- [Tidy First?](https://www.oreilly.com/library/view/tidy-first/9781098151232/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [LangFuse Documentation](https://langfuse.com/docs)

---

## 🎓 다음 단계

### 즉시 적용 가능
- ✅ 커밋 메시지 규칙 (test:/feat:/struct:)
- ✅ Git Hooks로 TDD 메트릭 자동 수집
- ✅ Cursor/CodeRabbit으로 AI 코드 리뷰

### 점진적 적용 (수 주)
- 🔄 패키지 재구성 (domain, application, adapter)
- 🔄 ArchUnit 테스트 추가
- 🔄 Zero-Tolerance 규칙 적용

### 장기 목표 (수 개월)
- 🎯 헥사고날 아키텍처 전환 완료
- 🎯 모든 레이어에 Port/Adapter 패턴 적용
- 🎯 90%+ 테스트 커버리지 달성

---

**문의**: 프로젝트 README 또는 팀 채널 참고
