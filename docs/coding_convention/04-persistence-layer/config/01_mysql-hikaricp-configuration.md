# MySQL HikariCP 엔터프라이즈급 설정 가이드

**목적**: Spring Boot 3.5.x + MySQL 8.0+ 환경에서 HikariCP Connection Pool 최적 설정

**필수 버전**: Spring Boot 3.0+, MySQL 8.0+, Java 21+

---

## 🎯 HikariCP를 선택한 이유

### Spring Boot 기본 커넥션 풀
- **별도 의존성 불필요**: Spring Boot에 기본 포함
- **세계에서 가장 빠른 커넥션 풀**: Tomcat JDBC, C3P0 대비 우수
- **Zero-Overhead**: 바이트코드 레벨 최적화
- **안정성**: Dead Connection 자동 감지 및 제거
- **경량**: 130KB JAR 파일

### 성능 벤치마크
- **HikariCP**: 1,000,000 ops/sec
- **Tomcat JDBC**: 700,000 ops/sec
- **C3P0**: 300,000 ops/sec

---

## 🔥 핵심 설정 값 설명

### 1. Connection Pool Size

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # 최대 커넥션 수
      minimum-idle: 10       # 최소 유휴 커넥션
```

#### HikariCP 개발자 권장 공식
```
connections = ((core_count * 2) + effective_spindle_count)
```

**예시**:
- 4 CPU 코어 + SSD(1) = 9개 커넥션
- 8 CPU 코어 + SSD(1) = 17개 커넥션

#### 실전 권장 범위
| 환경 | Pool Size | 이유 |
|------|-----------|------|
| **Local** | 5-10 | 단일 개발자, 낮은 부하 |
| **Dev** | 10-20 | 팀 개발, 통합 테스트 |
| **Stage** | 20-30 | 프로덕션 유사 환경 |
| **Prod** | 20-50 | CPU 코어 기반, 모니터링 후 조정 |

#### ⚠️ 주의사항
```yaml
# ❌ 나쁜 예
maximum-pool-size: 100  # 과다 설정, DB 부하 증가

# ✅ 좋은 예
maximum-pool-size: 20   # 적절한 범위
```

**과다 설정 시 문제점**:
- DB 서버 부하 증가
- Context Switching 오버헤드
- 메모리 낭비
- Connection 관리 비용 증가

**DB 최대 커넥션 확인**:
```sql
-- MySQL max_connections 확인
SHOW VARIABLES LIKE 'max_connections';

-- 권장: DB max_connections > (App instances * maximum-pool-size)
-- 예: 3개 인스턴스 * 20 = 60 < 151 (MySQL 기본값)
```

---

### 2. Connection Timeout

```yaml
spring:
  datasource:
    hikari:
      connection-timeout: 30000  # 30초 (밀리초)
```

**의미**: 커넥션 풀에서 커넥션을 얻기 위한 최대 대기 시간

**권장값**: 30초 (30000ms)

**설정 기준**:
- **너무 짧으면** (10초 이하): 순간적 부하에 TimeoutException 발생
- **너무 길면** (60초 이상): 장애 시 응답 지연

---

### 3. Idle Timeout

```yaml
spring:
  datasource:
    hikari:
      idle-timeout: 600000  # 10분 (밀리초)
```

**의미**: 유휴 커넥션이 풀에서 제거되기까지의 시간

**권장값**: 10분 (600000ms)

**동작 방식**:
- `minimum-idle`보다 적은 커넥션은 제거되지 않음
- 유휴 상태가 10분 이상 지속되면 자동 제거
- 트래픽이 낮은 시간대 리소스 절약

---

### 4. Max Lifetime

```yaml
spring:
  datasource:
    hikari:
      max-lifetime: 1800000  # 30분 (밀리초)
```

**의미**: 커넥션이 풀에서 유지되는 최대 시간

**권장값**: DB `wait_timeout`의 70-80%

**계산 방법**:
```sql
-- MySQL wait_timeout 확인
SHOW VARIABLES LIKE 'wait_timeout';
-- 기본값: 28800초 (8시간)

-- HikariCP max-lifetime 설정
-- 8시간 * 0.7 = 5.6시간 = 20160초 = 20160000ms
```

**이유**: DB가 커넥션을 닫기 전에 먼저 종료 (Dead Connection 방지)

---

### 5. Leak Detection Threshold

```yaml
spring:
  datasource:
    hikari:
      leak-detection-threshold: 60000  # 60초 (밀리초)
```

**의미**: 커넥션 누수 감지 시간 (커넥션을 반환하지 않은 경우)

**권장값**:
- **Prod**: 60000 (60초) - 누수 감지 활성화
- **Local/Dev**: 0 - 비활성화 (개발 편의)

**로그 예시**:
```
WARN HikariPool - Connection leak detection triggered for connection,
stack trace follows
```

**누수 원인**:
```java
// ❌ 나쁜 예 - 커넥션 반환 안 함
Connection conn = dataSource.getConnection();
// ... 작업 수행
// conn.close() 호출 안 함!

// ✅ 좋은 예 - try-with-resources
try (Connection conn = dataSource.getConnection()) {
    // ... 작업 수행
}  // 자동으로 close() 호출
```

---

### 6. Connection Test Query

```yaml
spring:
  datasource:
    hikari:
      # ❌ 설정하지 마세요!
      # connection-test-query: SELECT 1
```

**중요**: HikariCP는 JDBC4 `isValid()` 메서드 사용 (더 빠름)

**성능 차이**:
- `isValid()`: Native JDBC 메서드, 빠름
- `connection-test-query`: 실제 SQL 실행, 느림

**Validation Timeout**:
```yaml
validation-timeout: 5000  # 5초 (기본값)
```

---

## 📋 환경별 설정

### application-local.yml (개발 환경)

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/spring_standards?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
    username: root
    password: root1234

    hikari:
      # Pool Size
      maximum-pool-size: 10  # 작은 풀 크기
      minimum-idle: 5

      # Timeout
      connection-timeout: 20000  # 20초
      idle-timeout: 300000  # 5분
      max-lifetime: 600000  # 10분

      # Leak Detection (비활성화)
      leak-detection-threshold: 0

      # Pool Name
      pool-name: HikariPool-Local

      # MySQL 최적화
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        elideSetAutoCommits: true
        maintainTimeStats: false

  jpa:
    hibernate:
      ddl-auto: validate  # ✅ Flyway 사용 시 validate 필수
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        highlight_sql: true  # Local에서만 SQL 하이라이트
    show-sql: false  # ❌ System.out 비활성화

logging:
  level:
    com.ryuqq: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

---

### application-prod.yml (프로덕션 환경)

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:spring_standards}?useSSL=true&requireSSL=true&serverTimezone=Asia/Seoul&useUnicode=true&characterEncoding=UTF-8
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

    hikari:
      # Pool Size (CPU 코어 기반 조정)
      maximum-pool-size: 20
      minimum-idle: 10

      # Timeout
      connection-timeout: 30000  # 30초
      idle-timeout: 600000  # 10분
      max-lifetime: 1800000  # 30분

      # Leak Detection (활성화)
      leak-detection-threshold: 60000  # 60초

      # Pool Name
      pool-name: HikariPool-Prod

      # Connection Init SQL
      connection-init-sql: SELECT 1

      # MySQL 최적화
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        elideSetAutoCommits: true
        maintainTimeStats: false

      # Health Check
      health-check-properties:
        connectivityCheckTimeoutMs: 1000

  jpa:
    hibernate:
      ddl-auto: validate  # ✅ Flyway 사용, validate 필수
    properties:
      hibernate:
        format_sql: false  # Prod는 비활성화
        use_sql_comments: false
        jdbc:
          batch_size: 50
          fetch_size: 50
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
        query:
          plan_cache_max_size: 2048
          in_clause_parameter_padding: true
    show-sql: false

logging:
  level:
    com.ryuqq: INFO
    org.hibernate.SQL: WARN  # Prod는 WARN
```

---

## 🚀 JPA/Hibernate 최적화

### 1. OSIV (Open Session In View) 비활성화

```yaml
spring:
  jpa:
    open-in-view: false  # ❌ 필수!
```

**OSIV의 문제점**:
- Transaction 범위 밖에서 Lazy Loading 허용 → N+1 문제 발생
- DB 커넥션을 HTTP 요청 전체 기간 점유 → 커넥션 부족
- 성능 저하의 주범

**대안**:
```java
@Service
@Transactional(readOnly = true)
public class GetOrderWithUserService implements GetOrderWithUserUseCase {

    @Override
    public OrderWithUserResponse execute(GetOrderQuery query) {
        // ✅ Transaction 내에서 Fetch Join으로 로딩
        Order order = loadOrderPort.loadWithUser(query.orderId());
        return OrderWithUserResponse.of(order);
    }
}
```

---

### 2. DDL Auto 전략

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # ✅ Flyway 사용 시 validate 필수
```

**옵션 설명**:
| 옵션 | 동작 | 권장 환경 |
|------|------|-----------|
| `none` | 아무것도 하지 않음 | - |
| `validate` | 엔티티와 테이블 매핑 검증만 | ✅ **Flyway 사용 시 (권장)** |
| `update` | 스키마 변경 시 자동 ALTER | ❌ 위험 (데이터 손실 가능) |
| `create` | 시작 시 DROP + CREATE | ❌ 프로덕션 절대 금지 |
| `create-drop` | 종료 시 DROP | ❌ 테스트 전용 |

---

### 3. Batch Processing

```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50  # Batch Insert/Update 크기
          fetch_size: 50  # Fetch 크기
        order_inserts: true  # Insert 정렬 (Batch 효율)
        order_updates: true  # Update 정렬
        batch_versioned_data: true  # @Version과 Batch 호환
```

**효과**:
```java
// 50개 INSERT를 1번의 네트워크 라운드트립으로 처리
for (int i = 0; i < 1000; i++) {
    Order order = Order.create(...);
    orderRepository.save(order);
}
// Without Batch: 1000번 네트워크 왕복
// With Batch: 20번 네트워크 왕복 (50개씩)
```

---

### 4. Query Plan Cache

```yaml
spring:
  jpa:
    properties:
      hibernate:
        query:
          plan_cache_max_size: 2048  # Query Plan 캐시 크기
          in_clause_parameter_padding: true  # IN 절 파라미터 패딩
```

**Query Plan Cache**:
- JPQL → SQL 변환 결과를 캐시
- 동일한 쿼리 재사용 시 변환 생략

**IN Clause Parameter Padding**:
```sql
-- Without Padding
WHERE id IN (?, ?, ?)  -- 3개 파라미터
WHERE id IN (?, ?, ?, ?, ?)  -- 5개 파라미터 (새 Plan)

-- With Padding (2의 제곱수로 패딩)
WHERE id IN (?, ?, ?, ?)  -- 4개 (2^2)
WHERE id IN (?, ?, ?, ?, ?, ?, ?, ?)  -- 8개 (2^3)
```

---

### 5. SQL 포맷팅 및 주석

```yaml
spring:
  jpa:
    properties:
      hibernate:
        format_sql: true  # SQL 포맷팅 (Local/Dev)
        use_sql_comments: true  # JPQL → SQL 주석 포함
    show-sql: false  # ❌ Logback 사용, System.out 비활성화
```

**SQL 주석 예시**:
```sql
/* load com.ryuqq.domain.Order */
SELECT o.id, o.user_id, o.status
FROM orders o
WHERE o.id = ?
```

---

## 📊 모니터링 및 메트릭

### 1. HikariCP 메트릭 (Micrometer)

```yaml
# application-prod.yml
management:
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
```

**주요 메트릭**:
- `hikaricp.connections.active`: 활성 커넥션 수
- `hikaricp.connections.idle`: 유휴 커넥션 수
- `hikaricp.connections.pending`: 대기 중인 스레드 수
- `hikaricp.connections.timeout`: 타임아웃 발생 횟수
- `hikaricp.connections.usage`: 커넥션 사용 시간 (ms)
- `hikaricp.connections.creation`: 커넥션 생성 시간 (ms)

**Alert 기준**:
```yaml
# Prometheus Alert Rule
groups:
  - name: hikaricp
    rules:
      - alert: HikariCPHighUsage
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.8
        for: 5m
        annotations:
          summary: "HikariCP Pool 사용률 80% 초과"

      - alert: HikariCPTimeout
        expr: rate(hikaricp_connections_timeout_total[5m]) > 10
        for: 1m
        annotations:
          summary: "HikariCP Timeout 발생"
```

---

### 2. Grafana 대시보드

**주요 패널**:
1. **Active Connections** (실시간)
2. **Idle Connections** (실시간)
3. **Pending Threads** (실시간)
4. **Connection Timeout Rate** (5분 평균)
5. **Connection Acquisition Time** (p50, p95, p99)

---

## 🔐 보안 설정

### 1. 환경 변수 사용

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

**환경 변수 설정** (예: Docker Compose):
```yaml
services:
  app:
    environment:
      DB_HOST: mysql-server
      DB_PORT: 3306
      DB_NAME: spring_standards
      DB_USERNAME: app_user
      DB_PASSWORD: ${DB_PASSWORD}  # .env 파일에서 로드
```

---

### 2. AWS Secrets Manager 통합

```java
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(SecretsManagerClient secretsClient) {
        // AWS Secrets Manager에서 DB 자격증명 로드
        String secretName = "prod/db/credentials";
        GetSecretValueRequest request = GetSecretValueRequest.builder()
            .secretId(secretName)
            .build();

        GetSecretValueResponse response = secretsClient.getSecretValue(request);
        JsonNode secret = objectMapper.readTree(response.secretString());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(secret.get("url").asText());
        config.setUsername(secret.get("username").asText());
        config.setPassword(secret.get("password").asText());

        return new HikariDataSource(config);
    }
}
```

---

## ✅ 체크리스트

### 필수 설정
- [ ] `maximum-pool-size` 설정 (Local: 10, Prod: 20-50)
- [ ] `connection-timeout` 30초 설정
- [ ] `max-lifetime` DB `wait_timeout`의 70-80%
- [ ] `leak-detection-threshold` Prod에서만 활성화 (60초)
- [ ] `open-in-view: false` 설정
- [ ] `ddl-auto: validate` 설정
- [ ] 환경 변수로 민감 정보 관리

### 최적화 설정
- [ ] Batch Processing 활성화 (`batch_size: 50`)
- [ ] Query Plan Cache 설정 (`plan_cache_max_size: 2048`)
- [ ] MySQL 최적화 속성 설정 (`cachePrepStmts`, `rewriteBatchedStatements`)

### 모니터링
- [ ] Actuator health, metrics 엔드포인트 활성화
- [ ] Prometheus 메트릭 노출
- [ ] HikariCP 메트릭 수집
- [ ] Grafana 대시보드 구성
- [ ] Alert 설정 (Pool 사용률 80% 초과 시)

---

## 📚 참고 자료

### HikariCP
- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [About Pool Sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)

### MySQL
- [MySQL Connector/J Configuration](https://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties.html)
- [MySQL Performance Tuning](https://dev.mysql.com/doc/refman/8.0/en/optimization.html)

### Spring Boot
- [Spring Boot Data Properties](https://docs.spring.io/spring-boot/appendix/application-properties/index.html#application-properties.data)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/)

---

## 🎯 요약

### 핵심 설정 (Prod 기준)
| 설정 | 값 | 이유 |
|------|-----|------|
| `maximum-pool-size` | 20-50 | CPU 코어 기반, 모니터링 후 조정 |
| `minimum-idle` | 10 | Pool의 50% 유지 |
| `connection-timeout` | 30000 (30초) | 순간 부하 대응 |
| `idle-timeout` | 600000 (10분) | 유휴 커넥션 제거 |
| `max-lifetime` | 1800000 (30분) | DB timeout의 70-80% |
| `leak-detection-threshold` | 60000 (60초) | 커넥션 누수 감지 |

### 절대 금지
- ❌ `open-in-view: true` (성능 저하)
- ❌ `ddl-auto: create` 또는 `update` (Prod)
- ❌ `maximum-pool-size: 100+` (과다 설정)
- ❌ DB 자격증명 하드코딩

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
