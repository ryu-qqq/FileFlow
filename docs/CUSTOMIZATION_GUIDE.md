# 🔧 Customization Guide

이 템플릿을 자신의 프로젝트에 맞게 커스터마이징하는 방법을 단계별로 안내합니다.

---

## 📋 목차

- [시작하기 전에](#시작하기-전에)
- [1단계: 프로젝트 복제 및 초기화](#1단계-프로젝트-복제-및-초기화)
- [2단계: 프로젝트 메타데이터 변경](#2단계-프로젝트-메타데이터-변경)
- [3단계: 패키지명 변경](#3단계-패키지명-변경)
- [4단계: 모듈 구성 커스터마이징](#4단계-모듈-구성-커스터마이징)
- [5단계: 규칙 완화/강화](#5단계-규칙-완화강화)
- [6단계: Git Hooks 설치](#6단계-git-hooks-설치)
- [7단계: 검증](#7단계-검증)
- [고급 커스터마이징](#고급-커스터마이징)

---

## 🎯 시작하기 전에

### 체크리스트

- [ ] Java 21 설치 확인 (`java -version`)
- [ ] Git 설치 확인 (`git --version`)
- [ ] 프로젝트명 결정 (예: `order-management-system`)
- [ ] 패키지명 결정 (예: `com.yourcompany.ordersystem`)
- [ ] 필요한 어댑터 파악 (AWS S3? SQS? 외부 API?)

### 예상 소요 시간

- **기본 커스터마이징**: 30분
- **모듈 추가/제거**: 추가 15-30분
- **규칙 조정**: 추가 15분

---

## 1단계: 프로젝트 복제 및 초기화

### 방법 A: Fork (권장)

GitHub에서 Fork 후 클론:

```bash
# 1. GitHub에서 Fork 버튼 클릭
# 2. 자신의 레포지토리로 클론
git clone git@github.com:YOUR_USERNAME/claude-spring-standards.git my-project
cd my-project

# 3. 원본 레포지토리를 upstream으로 추가 (업데이트 받기 위해)
git remote add upstream git@github.com:ryu-qqq/claude-spring-standards.git
```

### 방법 B: Template 사용

GitHub에서 "Use this template" 버튼 사용:

```bash
# 1. GitHub에서 "Use this template" 클릭
# 2. 새 레포지토리 생성
# 3. 클론
git clone git@github.com:YOUR_USERNAME/my-project.git
cd my-project
```

### 방법 C: 직접 클론

```bash
git clone git@github.com:ryu-qqq/claude-spring-standards.git my-project
cd my-project

# Git 히스토리 제거하고 새로 시작
rm -rf .git
git init
git add .
git commit -m "chore: initialize project from template"
```

---

## 2단계: 프로젝트 메타데이터 변경

### 2.1 Gradle 프로젝트 설정

**`settings.gradle.kts`** 수정:

```kotlin
// Before
rootProject.name = "claude-spring-standards"

// After
rootProject.name = "order-management-system"  // 여기를 변경
```

**`gradle.properties`** 수정:

```properties
# Before
group=com.company
version=0.0.1-SNAPSHOT

# After
group=com.yourcompany              # 변경
version=0.0.1-SNAPSHOT
projectDescription=Order Management System  # 변경
```

### 2.2 README.md 수정

**`README.md`**에서 다음 내용 변경:

```markdown
# Before
# 🏛️ Enterprise Spring Boot Hexagonal Architecture Template

# After
# 📦 Order Management System

## 프로젝트 설명
주문 관리를 위한 헥사고날 아키텍처 기반 시스템

## 팀
- **Architecture Team**: your-team@yourcompany.com
- **슬랙 채널**: #order-system

© 2024 Your Company Name. All Rights Reserved.
```

---

## 3단계: 패키지명 변경

### 3.1 패키지 구조 계획

```
현재: com.company.template
목표: com.yourcompany.ordersystem
```

### 3.2 자동 변경 스크립트

**`scripts/rename-packages.sh`** 생성:

```bash
#!/bin/bash

OLD_PACKAGE="com.company.template"
NEW_PACKAGE="com.yourcompany.ordersystem"

OLD_PATH="com/company/template"
NEW_PATH="com/yourcompany/ordersystem"

echo "🔄 Renaming packages from $OLD_PACKAGE to $NEW_PACKAGE..."

# 1. Java 파일 내용 변경
find . -name "*.java" -type f -exec sed -i '' "s/$OLD_PACKAGE/$NEW_PACKAGE/g" {} +

# 2. build.gradle.kts 파일 변경
find . -name "build.gradle.kts" -type f -exec sed -i '' "s/$OLD_PACKAGE/$NEW_PACKAGE/g" {} +

# 3. 디렉토리 구조 변경
for dir in $(find . -type d -path "*/$OLD_PATH"); do
    NEW_DIR=$(echo "$dir" | sed "s|$OLD_PATH|$NEW_PATH|g")
    mkdir -p "$(dirname "$NEW_DIR")"
    mv "$dir" "$NEW_DIR"
    echo "✅ Moved: $dir -> $NEW_DIR"
done

echo "✨ Package rename complete!"
echo "⚠️  Please verify changes with: git diff"
```

**실행**:

```bash
chmod +x scripts/rename-packages.sh
./scripts/rename-packages.sh

# 변경 사항 확인
git diff

# 문제 없으면 커밋
git add .
git commit -m "chore: rename package to com.yourcompany.ordersystem"
```

### 3.3 수동 변경 (소규모 프로젝트)

IntelliJ IDEA 사용:

1. `src/test/java/com/company/template` 우클릭
2. `Refactor → Rename`
3. `com.yourcompany.ordersystem` 입력
4. `Refactor` 클릭
5. 모든 모듈에 반복

---

## 4단계: 모듈 구성 커스터마이징

### 4.1 불필요한 어댑터 제거

**예시: AWS SQS를 사용하지 않는 경우**

```bash
# 1. 모듈 디렉토리 삭제
rm -rf adapter/adapter-out-aws-sqs

# 2. settings.gradle.kts에서 제거
# Before
include(":adapter:adapter-out-aws-sqs")

# After (해당 줄 삭제)

# 3. bootstrap에서 의존성 제거
# bootstrap/bootstrap-web-api/build.gradle.kts
# Before
implementation(project(":adapter:adapter-out-aws-sqs"))

# After (해당 줄 삭제)

# 4. 커밋
git add .
git commit -m "chore: remove unused AWS SQS adapter"
```

### 4.2 새로운 어댑터 추가

**예시: Redis 캐시 어댑터 추가**

```bash
# 1. 디렉토리 생성
mkdir -p adapter/adapter-out-redis/{src/main/java,src/test/java}

# 2. build.gradle.kts 생성
cat > adapter/adapter-out-redis/build.gradle.kts << 'EOF'
plugins {
    `java-library`
}

dependencies {
    api(project(":application"))
    api(project(":domain"))

    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.junit)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestCoverageVerification)
}
EOF

# 3. settings.gradle.kts에 추가
echo 'include(":adapter:adapter-out-redis")' >> settings.gradle.kts

# 4. bootstrap에 의존성 추가
# bootstrap/bootstrap-web-api/build.gradle.kts에 추가:
# implementation(project(":adapter:adapter-out-redis"))
```

### 4.3 모듈 이름 변경

**예시: `adapter-in-admin-web` → `adapter-in-rest-api`**

```bash
# 1. 디렉토리 이름 변경
mv adapter/adapter-in-admin-web adapter/adapter-in-rest-api

# 2. settings.gradle.kts 수정
sed -i '' 's/adapter-in-admin-web/adapter-in-rest-api/g' settings.gradle.kts

# 3. bootstrap build.gradle.kts 수정
sed -i '' 's/adapter-in-admin-web/adapter-in-rest-api/g' bootstrap/bootstrap-web-api/build.gradle.kts

# 4. 검증
./gradlew projects
```

---

## 5단계: 규칙 완화/강화

### 5.1 ArchUnit 규칙 조정

**Domain 규칙 완화 예시**:

`domain/src/test/java/.../HexagonalArchitectureTest.java`:

```java
// 특정 규칙 비활성화
@Disabled("Temporarily disabled - will be enforced later")
@Test
void noSetterMethods() {
    // ...
}

// 또는 예외 추가
@Test
void domainShouldNotDependOnSpring() {
    ArchRule rule = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAPackage("org.springframework..")
        .because("Domain must remain framework-independent")
        .allowEmptyShould(true);  // 예외 허용

    rule.check(domainClasses);
}
```

### 5.2 Git Hook Validator 비활성화

**임시 비활성화**:

```bash
# persistence-validator만 비활성화
mv hooks/validators/persistence-validator.sh hooks/validators/persistence-validator.sh.disabled
```

**영구 비활성화** (hooks/pre-commit 수정):

```bash
# Persistence validation 섹션을 주석 처리
# if [ ${#PERSISTENCE_FILES[@]} -gt 0 ]; then
#     log_info "Validating Persistence module..."
#     if bash "$VALIDATORS_DIR/persistence-validator.sh" "${PERSISTENCE_FILES[@]}"; then
#         log_success "Persistence validation passed"
#     else
#         log_error "Persistence validation failed"
#         VALIDATION_FAILED=1
#     fi
# fi
```

### 5.3 Checkstyle 규칙 조정

**`config/checkstyle/checkstyle.xml`** 수정:

```xml
<!-- JPA 관계 어노테이션 검사 비활성화 -->
<!--
<module name="RegexpSingleline">
    <property name="format" value="@(OneToMany|ManyToOne|OneToOne|ManyToMany)"/>
    <property name="message" value="JPA relationship annotations STRICTLY PROHIBITED"/>
</module>
-->

<!-- 라인 길이 제한 완화 -->
<module name="LineLength">
    <property name="max" value="150"/>  <!-- 기존 120에서 150으로 -->
</module>
```

### 5.4 테스트 커버리지 조정

**모듈별 `build.gradle.kts`** 수정:

```kotlin
// Before (Domain - 90%)
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

// After (70%로 완화)
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}
```

---

## 6단계: Git Hooks 설치

### 6.1 Pre-commit Hook 활성화

```bash
# 심볼릭 링크 생성
ln -s ../../hooks/pre-commit .git/hooks/pre-commit

# 실행 권한 부여
chmod +x .git/hooks/pre-commit
chmod +x hooks/validators/*.sh

# 테스트
git add .
git commit -m "test: verify git hooks"
# → Hook이 실행되어야 함
```

### 6.2 Claude Code 동적 훅 확인

`.claude/hooks/` 스크립트가 실행 권한이 있는지 확인:

```bash
chmod +x .claude/hooks/*.sh
```

---

## 7단계: 검증

### 7.1 빌드 테스트

```bash
# 전체 빌드
./gradlew clean build

# 각 모듈별 테스트
./gradlew :domain:test
./gradlew :application:test

# ArchUnit 테스트만
./gradlew test --tests "*ArchitectureTest"
```

### 7.2 코드 품질 검사

```bash
# Checkstyle
./gradlew checkstyleMain

# SpotBugs
./gradlew spotbugsMain

# 전체 품질 검사
./gradlew check
```

### 7.3 프로젝트 구조 확인

```bash
./gradlew projects

# 출력 예시:
# Root project 'order-management-system'
# +--- Project ':domain'
# +--- Project ':application'
# +--- Project ':adapter'
# |    +--- Project ':adapter:adapter-in-rest-api'
# |    +--- Project ':adapter:adapter-out-persistence-jpa'
# ...
```

---

## 🔧 고급 커스터마이징

### 1. 새로운 아키텍처 규칙 추가

**ArchUnit 테스트 추가** (`domain/.../HexagonalArchitectureTest.java`):

```java
@Test
void customRule_domainEventsMustEndWithEvent() {
    ArchRule rule = classes()
        .that().resideInAPackage("..domain.event..")
        .should().haveSimpleNameEndingWith("Event")
        .because("Domain events must follow naming convention");

    rule.check(domainClasses);
}
```

### 2. 커스텀 Validator 추가

**`hooks/validators/custom-validator.sh`** 생성:

```bash
#!/bin/bash
# Custom business rule validator

VIOLATION_FOUND=0

for file in "$@"; do
    [[ $file != *.java ]] && continue

    # 예: 특정 deprecated API 사용 금지
    if grep -q "OldDeprecatedClass" "$file"; then
        echo "❌ $file: Using deprecated OldDeprecatedClass"
        VIOLATION_FOUND=1
    fi
done

[ $VIOLATION_FOUND -eq 1 ] && exit 1
exit 0
```

**`hooks/pre-commit`에 추가**:

```bash
# Custom validation
if [ ${#ALL_FILES[@]} -gt 0 ]; then
    log_info "Running custom validation..."
    if bash "$VALIDATORS_DIR/custom-validator.sh" "${ALL_FILES[@]}"; then
        log_success "Custom validation passed"
    else
        log_error "Custom validation failed"
        VALIDATION_FAILED=1
    fi
fi
```

### 3. Gradle Version Catalog 확장

**`gradle/libs.versions.toml`**에 의존성 추가:

```toml
[versions]
redis = "3.1.0"

[libraries]
spring-data-redis = {
    module = "org.springframework.boot:spring-boot-starter-data-redis",
    version.ref = "redis"
}

[bundles]
cache = [
    "spring-data-redis",
    "lettuce-core"
]
```

### 4. 다국어 지원 (한국어 → 영어)

모든 문서와 주석을 영어로 변환:

```bash
# 스크립트 또는 수동으로 변환
# README.md, CODING_STANDARDS.md 등
```

---

## 📚 커스터마이징 체크리스트

완료 후 확인:

### 필수
- [ ] 프로젝트명 변경 (`settings.gradle.kts`)
- [ ] 패키지명 변경 (`com.company.template` → 본인 패키지)
- [ ] README.md 업데이트
- [ ] Git Hooks 설치 및 테스트
- [ ] 빌드 성공 확인 (`./gradlew clean build`)
- [ ] ArchUnit 테스트 통과

### 선택
- [ ] 불필요한 어댑터 제거
- [ ] 새로운 어댑터 추가
- [ ] 규칙 완화/강화
- [ ] 커스텀 validator 추가
- [ ] 테스트 커버리지 기준 조정
- [ ] 문서 다국어 변환

---

## 🆘 트러블슈팅

### 문제 1: 패키지 변경 후 빌드 실패

**증상**:
```
error: package com.company.template does not exist
```

**해결**:
```bash
# 1. Gradle 캐시 삭제
./gradlew clean
rm -rf .gradle build

# 2. 패키지명 변경 누락 확인
grep -r "com.company.template" --include="*.java" .
grep -r "com.company.template" --include="*.kts" .

# 3. IntelliJ 캐시 무효화
# File → Invalidate Caches / Restart
```

### 문제 2: Git Hook이 실행되지 않음

**증상**:
```bash
git commit -m "test"
# → Hook 실행 안 됨
```

**해결**:
```bash
# 1. Symlink 확인
ls -la .git/hooks/pre-commit

# 2. 권한 확인 및 부여
chmod +x .git/hooks/pre-commit
chmod +x hooks/validators/*.sh

# 3. Symlink 재생성
rm .git/hooks/pre-commit
ln -s ../../hooks/pre-commit .git/hooks/pre-commit
```

### 문제 3: ArchUnit 테스트 실패

**증상**:
```
Architecture Violation: classes that reside in package '..domain..'
should not depend on classes that reside in package 'org.springframework..'
```

**해결**:

임시로 해당 규칙 비활성화하거나 코드 수정:

```java
// 옵션 1: 테스트 비활성화
@Disabled("Will be enforced in next sprint")
@Test
void domainShouldNotDependOnSpring() { ... }

// 옵션 2: 코드 수정 (Spring 의존성 제거)
```

---

## 📞 도움말

### 추가 문서
- [CODING_STANDARDS.md](CODING_STANDARDS.md) - 87개 규칙 상세
- [Git Hooks README](../hooks/README.md) - Hook 시스템 설명
- [Claude Hooks README](../.claude/hooks/README.md) - 동적 훅 가이드

### 커뮤니티
- **GitHub Issues**: 템플릿 이슈 보고
- **Discussions**: 커스터마이징 관련 질문

---

**🎉 커스터마이징 완료!**

이제 자신의 비즈니스 로직을 작성할 준비가 되었습니다.

다음 단계:
1. Domain 모델 작성 (`domain/src/main/java/.../model/`)
2. UseCase 정의 (`application/src/main/java/.../port/in/`)
3. Adapter 구현 (Controller, Repository 등)

Happy Coding! 🚀

© 2024 Ryu-qqq. All Rights Reserved.
