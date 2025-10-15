# 📦 Version Management Guide

이 프로젝트는 **Gradle Version Catalog**를 사용하여 모든 의존성 버전을 중앙에서 관리합니다.

---

## 🎯 Version Catalog란?

Gradle 7.0+에서 도입된 **타입 안전한 의존성 관리 시스템**입니다.

### ✅ 장점

1. **중앙 집중식 버전 관리**
   - 모든 버전을 한 곳에서 관리
   - 멀티 모듈 프로젝트에서 버전 불일치 방지

2. **타입 안전성**
   - IDE 자동완성 지원
   - 컴파일 타임 오류 감지

3. **재사용 가능성**
   - 번들(bundles)로 자주 사용하는 의존성 그룹화
   - 여러 모듈에서 쉽게 재사용

4. **명확한 구조**
   - 의존성이 어디서 정의되었는지 명확
   - 버전 업그레이드가 쉬움

---

## 📁 파일 구조

```
프로젝트/
├── gradle/
│   └── libs.versions.toml    ← 모든 버전 정의
├── gradle.properties          ← 프로젝트 설정
├── build.gradle.kts           ← Version Catalog 사용
└── 모듈/
    └── build.gradle.kts       ← libs.xxx.xxx 형식으로 사용
```

---

## 📝 libs.versions.toml 구조

### 1. `[versions]` 섹션
**버전 번호만 정의**

```toml
[versions]
springBoot = "3.5.6"         # Spring Boot 버전
postgresql = "42.7.3"        # PostgreSQL 드라이버 버전
querydsl = "5.1.0"           # QueryDSL 버전
```

### 2. `[libraries]` 섹션
**실제 의존성 정의**

```toml
[libraries]
# 버전 참조 방식 (version.ref)
spring-boot-starter-web = {
    module = "org.springframework.boot:spring-boot-starter-web"
}

# 버전 직접 지정 (version.ref)
postgresql = {
    module = "org.postgresql:postgresql",
    version.ref = "postgresql"
}

# 버전 직접 입력
commons-lang3 = {
    module = "org.apache.commons:commons-lang3",
    version = "3.14.0"
}
```

### 3. `[bundles]` 섹션
**자주 함께 사용하는 의존성 그룹**

```toml
[bundles]
spring-web = [
    "spring-boot-starter-web",
    "spring-boot-starter-validation",
    "jackson-databind"
]

testing-basic = [
    "junit-jupiter",
    "assertj-core",
    "mockito-core"
]
```

### 4. `[plugins]` 섹션
**Gradle 플러그인 정의**

```toml
[plugins]
spring-boot = {
    id = "org.springframework.boot",
    version.ref = "springBoot"
}
```

---

## 🔧 사용 방법

### 1️⃣ 루트 `build.gradle.kts`에서 플러그인 사용

```kotlin
plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.spotbugs) apply false
}
```

**변환 규칙**:
```toml
[plugins]
spring-boot = ...           → libs.plugins.spring.boot
spring-dependency-management → libs.plugins.spring.dependency.management
```

### 2️⃣ 모듈 `build.gradle.kts`에서 의존성 사용

```kotlin
dependencies {
    // 단일 라이브러리
    implementation(libs.spring.boot.starter.web)
    implementation(libs.postgresql)

    // 번들 사용
    implementation(libs.bundles.spring.web)

    // 테스트 의존성
    testImplementation(libs.junit.jupiter)
}
```

**변환 규칙**:
```toml
[libraries]
spring-boot-starter-web = ...  → libs.spring.boot.starter.web
postgresql = ...               → libs.postgresql
commons-lang3 = ...            → libs.commons.lang3

[bundles]
spring-web = [...]             → libs.bundles.spring.web
```

### 3️⃣ 버전 참조

```kotlin
checkstyle {
    toolVersion = libs.versions.checkstyle.get()
}

spotbugs {
    toolVersion.set(libs.versions.spotbugs.get())
}
```

**변환 규칙**:
```toml
[versions]
checkstyle = "10.14.0"  → libs.versions.checkstyle
spotbugs = "4.8.3"      → libs.versions.spotbugs
```

---

## 🔄 버전 업그레이드 방법

### Spring Boot 버전 업그레이드 예시

**Before (3.5.6)**:
```toml
# gradle/libs.versions.toml
[versions]
springBoot = "3.5.6"
```

**After (3.6.0)**:
```toml
# gradle/libs.versions.toml
[versions]
springBoot = "3.6.0"
```

**단 한 곳만 수정하면 모든 모듈에 자동 적용!** ✨

---

## 📊 실제 사례

### 케이스 1: 새로운 의존성 추가

**목표**: Jackson Kotlin 모듈 추가

#### 1. `libs.versions.toml`에 추가

```toml
[libraries]
jackson-kotlin = {
    module = "com.fasterxml.jackson.module:jackson-module-kotlin"
}
```

#### 2. 모듈에서 사용

```kotlin
// adapter-in-admin-web/build.gradle.kts
dependencies {
    implementation(libs.jackson.kotlin)
}
```

### 케이스 2: 번들 생성

**목표**: AWS 관련 의존성을 자주 사용하므로 번들로 묶기

#### 1. `libs.versions.toml`에 번들 정의

```toml
[bundles]
aws-s3 = [
    "aws-s3",
    "aws-s3-transfer",
    "aws-apache-client"
]
```

#### 2. 모듈에서 번들 사용

```kotlin
// adapter-out-aws-s3/build.gradle.kts
dependencies {
    implementation(libs.bundles.aws.s3)
}
```

**Before**:
```kotlin
implementation(libs.aws.s3)
implementation(libs.aws.s3.transfer)
implementation(libs.aws.apache.client)
```

**After**:
```kotlin
implementation(libs.bundles.aws.s3)  // 한 줄로 해결!
```

### 케이스 3: 버전 통일

**문제**: 여러 모듈에서 각각 다른 버전의 Jackson 사용

**해결**:
```toml
[versions]
jackson = "2.17.0"

[libraries]
jackson-databind = {
    module = "com.fasterxml.jackson.core:jackson-databind",
    version.ref = "jackson"
}
jackson-datatype-jsr310 = {
    module = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310",
    version.ref = "jackson"
}
```

→ 모든 Jackson 의존성이 동일한 버전 사용 보장!

---

## 🎯 현재 프로젝트 주요 버전

| 라이브러리 | 버전 | 변수명 |
|-----------|------|--------|
| **Spring Boot** | 3.5.6 | `springBoot` |
| **Java** | 21 | (gradle.properties) |
| **PostgreSQL** | 42.7.3 | `postgresql` |
| **QueryDSL** | 5.1.0 | `querydsl` |
| **AWS SDK** | 2.25.11 | `awsSdk` |
| **ArchUnit** | 1.2.1 | `archunit` |
| **Testcontainers** | 1.19.7 | `testcontainers` |

---

## 🛠️ 유지보수 팁

### 1. 정기적인 버전 업그레이드

```bash
# 1. libs.versions.toml에서 버전 업데이트
# 2. 빌드 테스트
./gradlew clean build

# 3. 테스트 실행
./gradlew test

# 4. 문제 없으면 커밋
git commit -m "chore: upgrade Spring Boot to 3.6.0"
```

### 2. 의존성 분석

```bash
# 의존성 트리 확인
./gradlew dependencies

# 특정 모듈 의존성
./gradlew :domain:dependencies

# 의존성 업데이트 확인 (플러그인 필요)
./gradlew dependencyUpdates
```

### 3. 버전 충돌 해결

```kotlin
// build.gradle.kts에서 강제 버전 지정
configurations.all {
    resolutionStrategy {
        force("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    }
}
```

---

## 📖 추가 리소스

### 공식 문서
- [Gradle Version Catalogs](https://docs.gradle.org/current/userguide/platforms.html)
- [TOML 문법](https://toml.io/)

### 베스트 프랙티스
1. **Semantic Versioning** 준수
2. **Major 버전 업그레이드**는 별도 브랜치에서 테스트
3. **BOM (Bill of Materials)** 활용 (Spring Boot, AWS SDK 등)
4. **번들 사용**으로 의존성 그룹화

---

## 🔍 FAQ

### Q: gradle.properties vs libs.versions.toml 차이는?

**A:**
- **gradle.properties**: 빌드 설정 (JVM 옵션, 프로젝트 메타데이터)
- **libs.versions.toml**: 의존성 버전 관리

### Q: BOM과 Version Catalog 함께 사용?

**A:** ✅ 가능합니다!

```kotlin
// Spring Boot BOM 사용 (버전 자동 관리)
implementation(libs.spring.boot.starter.web)  // 버전 명시 불필요

// 명시적 버전 관리
implementation(libs.postgresql)  // 버전 명시됨
```

### Q: libs가 인식 안 될 때?

**A:**
```bash
# Gradle 캐시 삭제 후 재빌드
./gradlew clean build --refresh-dependencies

# IntelliJ: File → Invalidate Caches / Restart
```

### Q: 레거시 프로젝트에서 마이그레이션?

**A:**
1. `gradle/libs.versions.toml` 생성
2. 기존 버전들을 `[versions]`, `[libraries]`로 옮기기
3. 한 모듈씩 `build.gradle.kts` 변환
4. 테스트 후 다음 모듈 진행

---

**🎉 이제 버전 관리가 훨씬 쉬워졌습니다!**

단 한 파일(`gradle/libs.versions.toml`)만 수정하면 전체 프로젝트의 의존성 버전이 업데이트됩니다.
