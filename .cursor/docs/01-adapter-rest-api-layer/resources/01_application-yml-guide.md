# application.yml 설정 가이드 - Spring Boot REST API

> **목적**: Spring Boot REST API 애플리케이션의 표준화된 application.yml 설정 가이드
>
> **위치**: `bootstrap/bootstrap-web-api/src/main/resources/application.yml`
>
> **관련 문서**:
> - `./02_message-i18n-guide.md` - 메시지 국제화
> - `../config/01_configuration-properties.md` - Configuration Properties 패턴
>
> **필수 버전**: Spring Boot 3.5+, Java 21+

---

## 📌 핵심 원칙

### 1. 설정의 계층 구조

**Base (공통) → Profile (환경별) 계층화**

```
application.yml          # 공통 설정 (모든 환경)
    ↓ 상속
application-local.yml    # 로컬 개발 환경
application-dev.yml      # 개발 서버 환경
application-stage.yml    # 스테이징 환경
application-prod.yml     # 프로덕션 환경
```

**규칙**:
- ✅ **공통 설정**: `application.yml`에 모든 환경에서 공통으로 사용하는 설정
- ✅ **환경별 설정**: Profile별 YAML로 분리하거나 `---` 구분자로 같은 파일 내 분리
- ✅ **민감 정보**: 환경 변수 또는 외부 설정 서버 사용 (application.yml에 하드코딩 금지)
- ❌ **프로덕션 설정**: 절대 repository에 커밋하지 않음 (환경 변수 사용)

### 2. 주석 작성 원칙

**모든 설정에 명확한 주석 필수**

```yaml
# ✅ Good: 설정의 목적, 권장값, 영향 범위 명시
server:
  # Tomcat 서블릿 컨테이너 설정
  tomcat:
    # 스레드풀 설정
    threads:
      # 최소 스레드 수 (항상 살아있는 스레드)
      min-spare: 10
      # 최대 스레드 수 (동시 처리 가능한 최대 요청 수)
      # CPU 코어 수 * 2 ~ 4 권장
      max: 200

# ❌ Bad: 주석 없음
server:
  tomcat:
    threads:
      min-spare: 10
      max: 200
```

**주석 규칙**:
- 설정 목적 설명
- 권장값 및 범위 명시
- 변경 시 영향받는 부분 설명
- 외부 문서 참조 (`@see` 주석)

### 3. 프로덕션 안전 설정

**개발/프로덕션 환경 분리**

```yaml
# ✅ Good: Profile별 다른 설정
---
# Local 환경
spring:
  config:
    activate:
      on-profile: local

logging:
  level:
    root: DEBUG
    com.ryuqq: TRACE

---
# Production 환경
spring:
  config:
    activate:
      on-profile: prod

logging:
  level:
    root: WARN
    com.ryuqq: INFO
    org.hibernate.SQL: WARN  # 프로덕션에서 SQL 로깅 비활성화

# Graceful Shutdown Timeout
server:
  graceful-shutdown-timeout: 30s
```

---

## 🔧 표준 설정 구조

### 전체 구조 (application.yml)

```yaml
# ===============================================
# Spring Boot Web API Application Configuration
# ===============================================
# Spring Boot 3.5.x + Java 21 기준
# Bootstrap 모듈 - 실행 가능한 애플리케이션 설정
#
# @author [Your Team]
# @since 1.0.0
# ===============================================

# ===============================================
# Server Configuration (Tomcat)
# ===============================================
server:
  port: 8080

  # Tomcat 서블릿 컨테이너 설정
  tomcat:
    # 스레드풀 설정
    threads:
      min-spare: 10  # 최소 스레드 수
      max: 200        # 최대 스레드 수 (CPU 코어 * 2~4 권장)

    # 커넥션 설정
    connection-timeout: 20s   # 커넥션 타임아웃
    keep-alive-timeout: 60s   # Keep-Alive 타임아웃

    # 큐 설정
    accept-count: 100         # 대기 큐 크기

    # 최대 커넥션 수
    max-connections: 8192     # NIO 커넥터 기본값

  # Graceful Shutdown (Spring Boot 2.3+)
  shutdown: graceful          # 진행 중인 요청 완료 후 종료

# ===============================================
# Spring Configuration
# ===============================================
spring:
  application:
    name: spring-standards-rest-api

  # Profile 설정
  profiles:
    active: local  # local, dev, stage, prod

  # ===============================================
  # MessageSource (i18n)
  # ===============================================
  messages:
    # 메시지 번들 베이스 이름 (messages_{locale}.properties 로딩)
    basename: messages
    # 한글 깨짐 방지: UTF-8 고정
    encoding: UTF-8
    # 서버 시스템 로케일에 의존하지 않음
    fallback-to-system-locale: false

  # ===============================================
  # Jackson (JSON 직렬화/역직렬화)
  # ===============================================
  jackson:
    # 날짜/시간 포맷
    date-format: yyyy-MM-dd'T'HH:mm:ss
    time-zone: Asia/Seoul

    # Serialization 설정
    serialization:
      # ISO-8601 포맷 사용 (타임스탬프 숫자 대신)
      WRITE_DATES_AS_TIMESTAMPS: false
      # 빈 객체 직렬화 허용
      FAIL_ON_EMPTY_BEANS: false
      # 들여쓰기 (개발 환경에서만)
      INDENT_OUTPUT: false

    # Deserialization 설정
    deserialization:
      # 알 수 없는 필드 무시 (API 버전 호환성)
      FAIL_ON_UNKNOWN_PROPERTIES: false
      # 빈 문자열을 null로 처리
      ACCEPT_EMPTY_STRING_AS_NULL_OBJECT: true

    # Default Property Inclusion
    default-property-inclusion: non_null  # null 필드 제외

    # Locale 설정
    locale: ko_KR

  # ===============================================
  # Validation
  # ===============================================
  validation:
    # Fail Fast 모드 (첫 번째 에러에서 중단)
    fail-fast: false  # 모든 에러 수집

  # ===============================================
  # Web Configuration
  # ===============================================
  web:
    # Locale 설정
    locale: ko_KR
    locale-resolver: fixed  # 고정 로케일

  # ===============================================
  # MVC Configuration
  # ===============================================
  mvc:
    # Content Negotiation
    contentnegotiation:
      favor-parameter: false        # URL 파라미터로 Content-Type 결정 비활성화
      favor-path-extension: false   # 확장자로 Content-Type 결정 비활성화

    # PathMatch 설정
    pathmatch:
      matching-strategy: ant_path_matcher  # Ant 스타일 패턴 매칭

    # Throw Exception if No Handler Found
    throw-exception-if-no-handler-found: true

  # ===============================================
  # Flyway Migration
  # ===============================================
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
    out-of-order: false
    table: flyway_schema_history
    sql-migration-prefix: V
    sql-migration-separator: __
    sql-migration-suffixes: .sql
    placeholder-replacement: true
    placeholders:
      database: spring_standards
      charset: utf8mb4
      collation: utf8mb4_unicode_ci
    clean-disabled: true
    fail-on-missing-locations: true

# ===============================================
# Management & Actuator (모니터링)
# ===============================================
management:
  endpoints:
    web:
      exposure:
        # Actuator 엔드포인트 노출 (프로덕션에서는 제한 필요)
        include: health,info,metrics,prometheus
      base-path: /actuator

  endpoint:
    health:
      show-details: when-authorized  # 인증된 사용자에게만 상세 정보 노출

  metrics:
    export:
      prometheus:
        enabled: true  # Prometheus 메트릭 노출

# ===============================================
# Logging Configuration
# ===============================================
logging:
  level:
    # Root 로거
    root: INFO

    # Application 로거
    com.ryuqq: DEBUG

    # Spring Framework
    org.springframework.web: INFO
    org.springframework.security: INFO

    # Hibernate (JPA)
    org.hibernate.SQL: DEBUG  # SQL 쿼리 로깅
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # 바인딩 파라미터 로깅

    # Tomcat
    org.apache.tomcat: INFO
    org.apache.catalina: INFO

  # 로그 패턴
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

  # 로그 파일 설정 (프로덕션)
  file:
    name: logs/application.log
    max-size: 10MB   # 파일 최대 크기
    max-history: 30  # 보관 일수

# ===============================================
# Profile-Specific Configurations
# ===============================================
---
# Local 환경
spring:
  config:
    activate:
      on-profile: local

logging:
  level:
    root: DEBUG
    com.ryuqq: TRACE

server:
  tomcat:
    threads:
      max: 50  # 로컬에서는 스레드 수 제한

---
# Development 환경
spring:
  config:
    activate:
      on-profile: dev

logging:
  level:
    root: DEBUG
    com.ryuqq: DEBUG

server:
  tomcat:
    threads:
      max: 100

---
# Production 환경
spring:
  config:
    activate:
      on-profile: prod

logging:
  level:
    root: WARN
    com.ryuqq: INFO
    org.hibernate.SQL: WARN

server:
  tomcat:
    threads:
      max: 200

  # Graceful Shutdown Timeout
  graceful-shutdown-timeout: 30s

management:
  endpoints:
    web:
      exposure:
        # 프로덕션에서는 health, metrics만 노출
        include: health,metrics
```

---

## 🎯 카테고리별 상세 설명

### 1. Server Configuration (Tomcat)

#### 스레드풀 설정

```yaml
server:
  tomcat:
    threads:
      min-spare: 10  # 최소 스레드 수 (항상 활성 상태)
      max: 200       # 최대 스레드 수 (동시 처리 요청 수)
```

**권장값**:
- **로컬**: `max: 50` (리소스 절약)
- **개발**: `max: 100` (중간 부하)
- **프로덕션**: `max: 200` (CPU 코어 수 * 2~4)

**계산 방법**:
```
최대 스레드 수 = CPU 코어 수 * (2~4)
예: 8코어 서버 → 16~32 권장
```

#### 커넥션 설정

```yaml
server:
  tomcat:
    connection-timeout: 20s   # 클라이언트 연결 대기 시간
    keep-alive-timeout: 60s   # Keep-Alive 연결 유지 시간
    accept-count: 100         # 큐 대기 요청 수
    max-connections: 8192     # 동시 연결 수 (NIO 기본값)
```

**튜닝 가이드**:
- `connection-timeout`: 느린 클라이언트 대응 (일반적으로 20~30초)
- `keep-alive-timeout`: HTTP/1.1 Keep-Alive 유지 시간 (60초 권장)
- `accept-count`: max threads 초과 시 대기 큐 크기
- `max-connections`: 동시 연결 수 (NIO는 메모리만 충분하면 높게 설정 가능)

#### Graceful Shutdown

```yaml
server:
  shutdown: graceful
  graceful-shutdown-timeout: 30s  # 프로덕션에서만 설정
```

**동작**:
1. 종료 신호 수신 → 새 요청 거부
2. 진행 중인 요청 완료 대기 (최대 30초)
3. 타임아웃 후 강제 종료

---

### 2. Jackson (JSON 처리)

#### 날짜/시간 처리

```yaml
spring:
  jackson:
    date-format: yyyy-MM-dd'T'HH:mm:ss
    time-zone: Asia/Seoul
    serialization:
      WRITE_DATES_AS_TIMESTAMPS: false  # ISO-8601 문자열 사용
```

**결과**:
```json
{
  "createdAt": "2025-11-04T14:30:00"  // ISO-8601 포맷
}
```

#### Null 처리

```yaml
spring:
  jackson:
    default-property-inclusion: non_null  # null 필드 제외
```

**결과**:
```json
// ✅ Good: null 필드 제외
{
  "id": 1,
  "name": "Order"
}

// ❌ Bad: null 필드 포함
{
  "id": 1,
  "name": "Order",
  "description": null
}
```

#### 역직렬화 설정

```yaml
spring:
  jackson:
    deserialization:
      FAIL_ON_UNKNOWN_PROPERTIES: false  # 알 수 없는 필드 무시
```

**효과**: API 버전 호환성 향상 (클라이언트가 새 필드를 보내도 에러 없음)

---

### 3. MessageSource (국제화)

```yaml
spring:
  messages:
    basename: messages
    encoding: UTF-8
    fallback-to-system-locale: false
```

**동작**:
- `basename: messages` → `messages_ko.properties`, `messages_en.properties` 로딩
- `encoding: UTF-8` → 한글 깨짐 방지
- `fallback-to-system-locale: false` → 서버 로케일에 의존하지 않음 (예측 가능성)

**상세 가이드**: [02_message-i18n-guide.md](./02_message-i18n-guide.md)

---

### 4. Validation

```yaml
spring:
  validation:
    fail-fast: false  # 모든 검증 에러 수집
```

**옵션**:
- `fail-fast: false` → 모든 필드 검증 후 에러 목록 반환 (권장)
- `fail-fast: true` → 첫 번째 에러에서 즉시 중단 (빠른 실패)

**예시**:
```java
// fail-fast: false
{
  "errors": [
    {"field": "email", "message": "Invalid email format"},
    {"field": "password", "message": "Password too short"}
  ]
}

// fail-fast: true
{
  "errors": [
    {"field": "email", "message": "Invalid email format"}
  ]
}
```

---

### 5. Actuator (모니터링)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator

  endpoint:
    health:
      show-details: when-authorized
```

**엔드포인트**:
- `/actuator/health` → 헬스 체크 (K8s Liveness/Readiness)
- `/actuator/metrics` → 메트릭 수집
- `/actuator/prometheus` → Prometheus 포맷 메트릭

**보안 설정**:
```yaml
# ✅ Good: 프로덕션에서는 최소 노출
management:
  endpoints:
    web:
      exposure:
        include: health,metrics  # info, env 제외

# ❌ Bad: 모든 엔드포인트 노출
management:
  endpoints:
    web:
      exposure:
        include: "*"  # 보안 위험
```

---

### 6. Logging

```yaml
logging:
  level:
    root: INFO
    com.ryuqq: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

**로깅 레벨 전략**:
- **로컬**: `TRACE` (모든 정보)
- **개발**: `DEBUG` (디버깅 정보)
- **프로덕션**: `WARN` (경고만)

**SQL 로깅**:
```yaml
org.hibernate.SQL: DEBUG                               # SQL 쿼리
org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # 바인딩 파라미터
```

**출력**:
```
Hibernate: select o1_0.id,o1_0.customer_id from orders o1_0 where o1_0.id=?
binding parameter [1] as [BIGINT] - [1]
```

---

## 🚨 Do / Don't

### ❌ Bad Examples

```yaml
# ❌ 프로덕션 설정 노출
spring:
  datasource:
    password: "prod_password_123"  # 절대 커밋 금지!

# ❌ 주석 없는 설정
server:
  tomcat:
    threads:
      max: 200

# ❌ 환경 구분 없음
logging:
  level:
    root: DEBUG  # 모든 환경에서 DEBUG (프로덕션 위험)

# ❌ 하드코딩된 값
api:
  external:
    url: http://192.168.1.100:8080  # IP 하드코딩
```

### ✅ Good Examples

```yaml
# ✅ 환경 변수 사용
spring:
  datasource:
    password: ${DB_PASSWORD}

# ✅ 명확한 주석
server:
  tomcat:
    threads:
      # 최대 스레드 수 (동시 처리 가능한 최대 요청 수)
      # CPU 코어 수 * 2 ~ 4 권장
      max: 200

# ✅ Profile별 환경 구분
---
spring:
  config:
    activate:
      on-profile: prod

logging:
  level:
    root: WARN  # 프로덕션: 경고만

# ✅ 외부 설정 참조
api:
  external:
    url: ${EXTERNAL_API_URL:http://localhost:8080}  # 환경 변수 + 기본값
```

---

## 📋 체크리스트

application.yml 작성 시:
- [ ] 모든 설정에 주석 작성 (목적, 권장값, 영향)
- [ ] Profile별 환경 구분 (local, dev, prod)
- [ ] 민감 정보는 환경 변수 사용 (`${...}`)
- [ ] 프로덕션 설정은 repository에 커밋하지 않음
- [ ] Jackson null 처리 설정 (`non_null`)
- [ ] Graceful Shutdown 활성화 (프로덕션)
- [ ] Actuator 엔드포인트 최소 노출 (프로덕션)
- [ ] SQL 로깅은 개발 환경만 활성화
- [ ] Tomcat 스레드풀 튜닝 (CPU 코어 * 2~4)
- [ ] MessageSource 인코딩 UTF-8 고정

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
