# Redis Lettuce 엔터프라이즈급 설정 가이드

**목적**: Spring Boot 3.5.x + Redis 7.0+ 환경에서 Lettuce 클라이언트 최적 설정

**필수 버전**: Spring Boot 3.0+, Redis 7.0+, Java 21+

---

## 🎯 Redis 사용 사례

### 주요 용도
1. **Cache**: 조회 성능 향상 (조회 빈도 높은 데이터)
2. **Session Store**: 분산 세션 관리 (다중 서버 환경)
3. **Message Queue**: Pub/Sub, Streams (이벤트 기반 아키텍처)
4. **Rate Limiting**: API 요청 제한 (Sliding Window, Token Bucket)
5. **Distributed Lock**: 분산 환경 동기화

### 캐시 적용 기준
| 항목 | 조건 | 예시 |
|------|------|------|
| **읽기 빈도** | 조회가 쓰기보다 10배 이상 | 상품 목록, 카테고리 |
| **변경 빈도** | 데이터 업데이트가 드묾 | 설정, 코드 테이블 |
| **계산 비용** | 복잡한 집계, 조인 쿼리 | 대시보드 통계 |
| **실시간성** | 약간의 지연 허용 가능 | 조회수, 좋아요 수 |

### ❌ 캐시 부적합 사례
- **실시간성 필수**: 최신 데이터 필수 (주문 상태, 재고)
- **쓰기 빈도 높음**: 초당 수천 건 업데이트
- **대용량 데이터**: 단일 키가 10MB 이상

---

## 🚀 Lettuce vs Jedis

| 항목 | Lettuce (권장) | Jedis |
|------|----------------|-------|
| **방식** | Async/Reactive (Netty) | Synchronous (Blocking) |
| **Connection** | 단일 연결 공유 | Connection Pool 필요 |
| **Spring Boot** | 기본 클라이언트 (3.x) | 별도 의존성 추가 |
| **성능** | 높음 (비동기) | 보통 (동기) |
| **복잡도** | 간단 | 복잡 (Pool 관리) |
| **Reactive** | 지원 (Reactor) | 미지원 |

### ✅ Lettuce 선택 이유
- **Spring Boot 3.x 기본 클라이언트**
- **비동기/리액티브 지원** (Reactor, WebFlux 통합)
- **Connection Pool 불필요** (단일 연결 재사용, Thread-Safe)
- **성능 우수** (비동기 I/O, Netty 기반)
- **자동 재연결** (Connection 실패 시 자동 복구)

---

## 📦 의존성 추가

### build.gradle.kts
```kotlin
dependencies {
    // Redis (Lettuce 기본 포함)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Lettuce Connection Pool (선택, 권장)
    implementation("org.apache.commons:commons-pool2")
}
```

**Commons Pool2 추가 이유**:
- Lettuce는 기본적으로 단일 연결 재사용
- 고부하 환경에서는 Connection Pool 사용 권장
- Pool 미사용 시에도 문제 없음 (Lettuce 특성)

---

## 🔥 핵심 설정 값 설명

### 1. Connection Pool 설정

```yaml
spring:
  redis:
    lettuce:
      pool:
        enabled: true
        max-active: 8   # 최대 활성 커넥션
        max-idle: 8     # 최대 유휴 커넥션
        min-idle: 2     # 최소 유휴 커넥션
        max-wait: 3000ms  # 커넥션 대기 시간
```

#### max-active (최대 활성 커넥션)
**의미**: 동시에 사용 가능한 최대 커넥션 수

**권장값**:
- **Local**: 8
- **Prod**: 16

**설정 기준**:
- 일반적으로 8-16이 적절
- CPU 코어 수 * 2 정도
- 모니터링 후 조정

#### max-idle / min-idle (유휴 커넥션)
**의미**: 풀에 유지하는 유휴 커넥션 수

**권장값**:
- `max-idle` = `max-active` (풀 크기 고정)
- `min-idle` = `max-active / 4` (25% 항상 유지)

**이유**:
- 커넥션 생성 비용 절약
- 순간 부하 대응

#### max-wait (커넥션 대기 시간)
**의미**: 커넥션을 얻기 위한 최대 대기 시간

**권장값**: 3000ms (3초)

---

### 2. Timeout 설정

```yaml
spring:
  redis:
    timeout: 3000ms  # 커맨드 타임아웃
    connect-timeout: 3000ms  # 커넥션 타임아웃 (선택)
```

#### timeout (커맨드 타임아웃)
**의미**: Redis 커맨드 실행 제한 시간

**권장값**: 3000ms (3초)

**설정 기준**:
- **너무 짧으면** (1초 이하): 정상 쿼리도 타임아웃
- **너무 길면** (10초 이상): 장애 시 응답 지연

---

### 3. Shutdown Timeout

```yaml
spring:
  redis:
    lettuce:
      shutdown-timeout: 100ms  # 종료 타임아웃
```

**의미**: 애플리케이션 종료 시 Lettuce 클라이언트 종료 대기 시간

**권장값**: 100ms (빠른 종료)

---

## 📋 환경별 설정

### application-local.yml (개발 환경)

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:  # 로컬은 비밀번호 없음
    database: 0  # DB 인덱스 (0-15)
    timeout: 3000ms

    lettuce:
      pool:
        enabled: true
        max-active: 8
        max-idle: 8
        min-idle: 2
        max-wait: 3000ms
        time-between-eviction-runs: 60s  # Eviction 실행 주기

      shutdown-timeout: 100ms

  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 기본 TTL: 1시간 (밀리초)
      cache-null-values: false  # Null 값 캐싱 금지

logging:
  level:
    io.lettuce.core: DEBUG  # Lettuce 로깅 (Local에서만)
```

---

### application-prod.yml (프로덕션 환경)

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}
    database: 0
    timeout: 3000ms

    lettuce:
      pool:
        enabled: true
        max-active: 16  # Prod는 더 큰 풀
        max-idle: 16
        min-idle: 4
        max-wait: 5000ms  # Prod는 더 긴 대기
        time-between-eviction-runs: 60s

      shutdown-timeout: 100ms

      # Cluster 설정 (선택)
      cluster:
        refresh:
          adaptive: true  # 클러스터 토폴로지 자동 갱신
          period: 60s  # 갱신 주기

  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 기본 TTL: 1시간
      cache-null-values: false  # Null 값 캐싱 금지

logging:
  level:
    io.lettuce.core: WARN  # Prod는 WARN
```

---

## 🛠️ RedisConfig.java 설정

### RedisConfig.java (엔터프라이즈급 설정)

```java
package com.ryuqq.adapter.out.persistence.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * RedisConfig - Redis 및 Cache 설정
 *
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li>RedisTemplate 빈 등록 (범용 객체 직렬화)</li>
 *   <li>GenericJackson2JsonRedisSerializer 사용 (타입 정보 포함)</li>
 *   <li>Spring Cache 활성화</li>
 *   <li>TTL 기반 캐시 전략</li>
 * </ul>
 *
 * @author Development Team
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * RedisTemplate<String, Object> 빈 등록
     *
     * <p><strong>Serializer 전략:</strong></p>
     * <ul>
     *   <li><strong>Key</strong>: StringRedisSerializer (항상 String)</li>
     *   <li><strong>Value</strong>: GenericJackson2JsonRedisSerializer (타입 정보 포함)</li>
     *   <li><strong>HashKey</strong>: StringRedisSerializer</li>
     *   <li><strong>HashValue</strong>: GenericJackson2JsonRedisSerializer</li>
     * </ul>
     *
     * <p><strong>GenericJackson2JsonRedisSerializer 선택 이유:</strong></p>
     * <ul>
     *   <li>Jackson의 {@code @class} 메타데이터 포함 → 타입 정보 보존</li>
     *   <li>다양한 타입의 객체를 하나의 RedisTemplate으로 처리 가능</li>
     *   <li>역직렬화 시 원본 타입으로 자동 변환</li>
     * </ul>
     *
     * <p><strong>vs Jackson2JsonRedisSerializer:</strong></p>
     * <ul>
     *   <li>Jackson2JsonRedisSerializer: 명시적 타입 필요 → 범용성 낮음</li>
     *   <li>GenericJackson2JsonRedisSerializer: 타입 추론 가능 → 범용성 높음</li>
     * </ul>
     *
     * <p><strong>사용 예시:</strong></p>
     * <pre>{@code
     * @Service
     * public class UserCacheService {
     *     private final RedisTemplate<String, Object> redisTemplate;
     *
     *     public void cacheUser(User user) {
     *         String key = "user:" + user.getId();
     *         redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(30));
     *     }
     *
     *     public User getUser(Long userId) {
     *         String key = "user:" + userId;
     *         return (User) redisTemplate.opsForValue().get(key);
     *     }
     * }
     * }</pre>
     *
     * @param connectionFactory Redis Connection Factory
     * @return RedisTemplate 인스턴스
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // ObjectMapper 커스터마이징
        ObjectMapper objectMapper = createObjectMapper();

        // Serializer 설정
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Key: String, Value: JSON
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);

        // Hash Key: String, Hash Value: JSON
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * RedisCacheManager 빈 등록
     *
     * <p><strong>TTL 전략:</strong></p>
     * <ul>
     *   <li>기본 TTL: 1시간</li>
     *   <li>Null 값 캐싱 금지</li>
     *   <li>Key Prefix: {@code cache::}</li>
     * </ul>
     *
     * <p><strong>캐시별 TTL 커스터마이징:</strong></p>
     * <pre>{@code
     * users: 30분 (자주 변경)
     * products: 1시간 (가끔 변경)
     * sessions: 30분 (활동 기반)
     * rate-limits: 1분 (시간 단위 제한)
     * }</pre>
     *
     * <p><strong>사용 예시:</strong></p>
     * <pre>{@code
     * @Service
     * public class UserService {
     *     @Cacheable(value = "users", key = "#userId")
     *     public User getUser(Long userId) {
     *         return userRepository.findById(userId).orElseThrow();
     *     }
     *
     *     @CacheEvict(value = "users", key = "#userId")
     *     public void deleteUser(Long userId) {
     *         userRepository.deleteById(userId);
     *     }
     *
     *     @CachePut(value = "users", key = "#user.id")
     *     public User updateUser(User user) {
     *         return userRepository.save(user);
     *     }
     * }
     * }</pre>
     *
     * @param connectionFactory Redis Connection Factory
     * @return RedisCacheManager 인스턴스
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // ObjectMapper 커스터마이징
        ObjectMapper objectMapper = createObjectMapper();
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))  // 기본 TTL: 1시간
            .disableCachingNullValues()  // Null 값 캐싱 금지
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer)
            )
            .computePrefixWith(cacheName -> "cache::" + cacheName + "::");  // Key Prefix

        // 캐시별 TTL 커스터마이징
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("sessions", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("rate-limits", defaultConfig.entryTtl(Duration.ofMinutes(1)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }

    /**
     * ObjectMapper 생성 및 커스터마이징
     *
     * <p><strong>주요 설정:</strong></p>
     * <ul>
     *   <li>JavaTimeModule: Java 8 Time API 지원 (LocalDateTime, ZonedDateTime 등)</li>
     *   <li>WRITE_DATES_AS_TIMESTAMPS: ISO-8601 포맷 사용 (타임스탬프 숫자 대신)</li>
     *   <li>activateDefaultTyping: 타입 정보 포함 ({@code @class} 메타데이터)</li>
     * </ul>
     *
     * @return 커스터마이징된 ObjectMapper
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 Time API 지원
        objectMapper.registerModule(new JavaTimeModule());

        // ISO-8601 포맷 사용 (타임스탬프 숫자 대신)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 타입 정보 포함 (Polymorphic Type Handling)
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)
            .build();

        objectMapper.activateDefaultTyping(
            ptv,
            ObjectMapper.DefaultTyping.NON_FINAL
        );

        return objectMapper;
    }
}
```

---

## 🔑 Redis Key Naming Convention

### 권장 패턴
```
{namespace}:{entity}:{id}

예시:
- cache::users::123
- session::user:456
- ratelimit::api:789::2024-01-01
```

### 구조 설명
- `{namespace}`: 용도 구분 (cache, session, lock, queue)
- `{entity}`: 엔티티 타입 (users, products, orders)
- `{id}`: 고유 식별자

### 장점
- **키 충돌 방지**: 네임스페이스로 명확히 구분
- **디버깅 용이**: 키만 봐도 용도 파악 가능
- **패턴 기반 삭제**: `KEYS cache::users::*` (주의: Prod에서는 SCAN 사용)
- **Redis Insight 가독성**: 트리 구조로 표시

### 예시
```java
@Service
public class UserCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "cache::users::";

    public void cacheUser(User user) {
        String key = CACHE_PREFIX + user.getId();
        redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(30));
    }

    public User getUser(Long userId) {
        String key = CACHE_PREFIX + userId;
        return (User) redisTemplate.opsForValue().get(key);
    }
}
```

---

## ⏰ TTL (Time To Live) 전략

### 용도별 TTL 권장값

| 캐시 타입 | TTL | 이유 | 예시 |
|-----------|-----|------|------|
| **Static Data** | 24시간 | 거의 변경되지 않음 | 코드 테이블, 설정 |
| **Reference Data** | 1시간 | 가끔 변경 | 카테고리, 상품 목록 |
| **User Data** | 10-30분 | 자주 변경 | 프로필, 설정 |
| **Session** | 30분 | 활동 기반 갱신 | 로그인 세션 |
| **Rate Limit** | 1분-1시간 | 시간 단위 제한 | API 요청 제한 |
| **Temporary** | 5분 | 임시 데이터 | OTP, 인증 토큰 |

### TTL 설정 방법

#### 1. Spring Cache (어노테이션)
```java
@Service
public class ProductService {

    // TTL: 1시간 (RedisCacheManager 설정)
    @Cacheable(value = "products", key = "#productId")
    public Product getProduct(Long productId) {
        return productRepository.findById(productId).orElseThrow();
    }
}
```

#### 2. RedisTemplate (명시적)
```java
@Service
public class UserCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void cacheUser(User user) {
        String key = "cache::users::" + user.getId();
        // TTL: 30분
        redisTemplate.opsForValue().set(key, user, Duration.ofMinutes(30));
    }
}
```

#### 3. RedisCacheManager (캐시별)
```java
// RedisConfig.java
Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofHours(1)));
```

---

## 🎯 Spring Cache 어노테이션

### @Cacheable (캐시 조회)
```java
@Service
public class UserService {

    /**
     * 캐시에 있으면 캐시 반환, 없으면 메서드 실행 후 캐시 저장
     */
    @Cacheable(value = "users", key = "#userId")
    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    /**
     * 조건부 캐싱 (활성 사용자만)
     */
    @Cacheable(value = "users", key = "#userId", condition = "#result.status == 'ACTIVE'")
    public User getUserWithCondition(Long userId) {
        return userRepository.findById(userId).orElseThrow();
    }
}
```

### @CachePut (캐시 업데이트)
```java
@Service
public class UserService {

    /**
     * 메서드 실행 후 결과를 캐시에 저장 (항상 실행)
     */
    @CachePut(value = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

### @CacheEvict (캐시 삭제)
```java
@Service
public class UserService {

    /**
     * 단일 키 삭제
     */
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * 전체 캐시 삭제
     */
    @CacheEvict(value = "users", allEntries = true)
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}
```

### @Caching (복합 캐시 작업)
```java
@Service
public class UserService {

    /**
     * 여러 캐시 동시 삭제
     */
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#userId"),
        @CacheEvict(value = "userProfiles", key = "#userId")
    })
    public void deleteUserAndProfile(Long userId) {
        userRepository.deleteById(userId);
        profileRepository.deleteByUserId(userId);
    }
}
```

---

## 📊 모니터링 및 메트릭

### 1. Lettuce 메트릭 (Micrometer)

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
- `lettuce.command.latency`: 커맨드 지연 시간 (ms)
- `lettuce.connections.active`: 활성 커넥션 수
- `lettuce.command.completion`: 완료된 커맨드 수

**Alert 기준**:
```yaml
# Prometheus Alert Rule
groups:
  - name: redis
    rules:
      - alert: RedisHighLatency
        expr: histogram_quantile(0.95, lettuce_command_latency_seconds) > 0.1
        for: 5m
        annotations:
          summary: "Redis 명령 지연 시간 100ms 초과"

      - alert: RedisConnectionFailure
        expr: rate(lettuce_connection_error_total[5m]) > 10
        for: 1m
        annotations:
          summary: "Redis 연결 실패 발생"
```

---

### 2. Redis 서버 모니터링

#### Redis INFO 명령어
```bash
# 메모리 사용량
redis-cli INFO memory

# 연결 수
redis-cli INFO clients

# 커맨드 통계
redis-cli INFO stats
```

#### 주요 메트릭
- `used_memory`: 현재 메모리 사용량
- `used_memory_peak`: 최대 메모리 사용량
- `connected_clients`: 연결된 클라이언트 수
- `evicted_keys`: 메모리 부족으로 삭제된 키 수
- `keyspace_hits`: 캐시 히트 수
- `keyspace_misses`: 캐시 미스 수

**캐시 히트율 계산**:
```
hit_rate = keyspace_hits / (keyspace_hits + keyspace_misses) * 100
```

**권장 히트율**: 80% 이상

---

## 🔐 보안 설정

### 1. 환경 변수 사용

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}
```

**환경 변수 설정** (예: Docker Compose):
```yaml
services:
  app:
    environment:
      REDIS_HOST: redis-server
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}  # .env 파일에서 로드
```

---

### 2. Redis ACL (Access Control List)

**Redis 7.0+ ACL 설정**:
```bash
# Redis CLI에서 ACL 사용자 생성
ACL SETUSER app_user on >app_password ~cache::* +@read +@write -@dangerous

# 설명:
# on: 사용자 활성화
# >app_password: 비밀번호 설정
# ~cache::*: cache:: 네임스페이스만 접근 허용
# +@read +@write: 읽기/쓰기 허용
# -@dangerous: 위험한 명령어 금지 (FLUSHALL, KEYS 등)
```

**Spring Boot 설정**:
```yaml
spring:
  redis:
    host: redis-server
    port: 6379
    username: app_user  # Redis 6.0+
    password: app_password
```

---

## ✅ 체크리스트

### 필수 설정
- [ ] Lettuce Connection Pool 활성화 (`pool.enabled: true`)
- [ ] `max-active` 설정 (Local: 8, Prod: 16)
- [ ] `timeout` 3초 설정
- [ ] `GenericJackson2JsonRedisSerializer` 사용
- [ ] TTL 전략 수립 (용도별 차등 적용)
- [ ] Key Naming Convention 준수
- [ ] Null 값 캐싱 금지 (`cache-null-values: false`)
- [ ] 환경 변수로 민감 정보 관리

### 최적화 설정
- [ ] ObjectMapper 커스터마이징 (JavaTimeModule, ISO-8601)
- [ ] 캐시별 TTL 커스터마이징 (users, products 등)
- [ ] Key Prefix 설정 (`cache::{cacheName}::`)

### 모니터링
- [ ] Actuator health, metrics 엔드포인트 활성화
- [ ] Prometheus 메트릭 노출
- [ ] Lettuce 메트릭 수집
- [ ] Redis INFO 모니터링
- [ ] Alert 설정 (Latency > 100ms, Hit Rate < 80%)

---

## 📚 참고 자료

### Lettuce
- [Lettuce Documentation](https://lettuce.io/core/release/reference/)
- [Lettuce GitHub](https://github.com/lettuce-io/lettuce-core)

### Spring Data Redis
- [Spring Data Redis Reference](https://docs.spring.io/spring-data/redis/reference/)
- [Spring Boot Redis Properties](https://docs.spring.io/spring-boot/appendix/application-properties/index.html#application-properties.data.spring.data.redis)

### Redis
- [Redis Documentation](https://redis.io/docs/)
- [Redis Best Practices](https://redis.io/docs/management/optimization/)
- [Redis ACL](https://redis.io/docs/management/security/acl/)

---

## 🎯 요약

### 핵심 설정 (Prod 기준)
| 설정 | 값 | 이유 |
|------|-----|------|
| `max-active` | 16 | 고부하 환경 대응 |
| `max-idle` | 16 | Pool 크기 고정 |
| `min-idle` | 4 | 25% 항상 유지 |
| `timeout` | 3000ms (3초) | 적절한 대기 시간 |
| `TTL` | 용도별 차등 | Session 30분, Cache 1시간 |

### Serializer 선택
- **Key**: `StringRedisSerializer` (항상 String)
- **Value**: `GenericJackson2JsonRedisSerializer` (타입 정보 포함)

### Key Naming
- 패턴: `{namespace}:{entity}:{id}`
- 예시: `cache::users::123`

### 절대 금지
- ❌ `cache-null-values: true` (Null 캐싱)
- ❌ 비밀번호 하드코딩
- ❌ Prod에서 `KEYS` 명령어 사용 (SCAN 사용)
- ❌ 대용량 데이터 단일 키 저장 (10MB 이상)

---

**작성자**: Development Team
**최종 수정일**: 2025-11-04
**버전**: 1.0.0
