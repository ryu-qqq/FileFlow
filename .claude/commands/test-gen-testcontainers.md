---
description: Testcontainers 설정 자동 생성 (MySQL, Redis, Kafka)
---

# Testcontainers 설정 자동 생성

**목적**: Testcontainers 기반 테스트 인프라 설정 자동 생성

**타겟**: Test Infrastructure - Testcontainers Configuration

**생성 내용**: MySQL, Redis, Kafka Container 설정 및 Base Test Class

---

## 🎯 사용법

```bash
# MySQL Testcontainer 설정 생성
/test-gen-testcontainers MySQL

# Redis Testcontainer 설정 생성
/test-gen-testcontainers Redis

# Kafka Testcontainer 설정 생성
/test-gen-testcontainers Kafka

# 전체 설정 생성
/test-gen-testcontainers All
```

---

## ✅ 자동 생성되는 설정

### 1. Base Test Configuration Class

```java
package com.ryuqq.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers 기본 설정
 *
 * <p>모든 통합 테스트에서 공유하는 컨테이너 설정을 제공합니다.</p>
 *
 * <ul>
 *   <li>MySQL 8.0 컨테이너</li>
 *   <li>Redis 7 컨테이너</li>
 *   <li>Kafka 7.5 컨테이너</li>
 * </ul>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    /**
     * MySQL 테스트 컨테이너 생성
     *
     * @return MySQL 컨테이너 인스턴스
     */
    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)  // 테스트 간 재사용
            .withCommand(
                "--character-set-server=utf8mb4",
                "--collation-server=utf8mb4_unicode_ci",
                "--max_connections=1000"
            );
    }

    /**
     * Redis 테스트 컨테이너 생성
     *
     * @return Redis 컨테이너 인스턴스
     */
    @Bean
    @ServiceConnection(name = "redis")
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true)
            .withCommand("redis-server", "--maxmemory", "256mb");
    }

    /**
     * Kafka 테스트 컨테이너 생성
     *
     * @return Kafka 컨테이너 인스턴스
     */
    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withReuse(true)
            .withKraft();  // KRaft 모드 (ZooKeeper 불필요)
    }
}
```

### 2. Abstract Base Test Class

```java
package com.ryuqq.test.base;

import com.ryuqq.test.config.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;

/**
 * 통합 테스트 기본 클래스
 *
 * <p>모든 통합 테스트는 이 클래스를 상속하여 Testcontainers 환경을 자동으로 구성합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>{@code
 * @DisplayName("OrderRepository 통합 테스트")
 * class OrderRepositoryIntegrationTest extends AbstractIntegrationTest {
 *
 *     @Autowired
 *     private OrderRepository orderRepository;
 *
 *     @Test
 *     void shouldSaveOrder() {
 *         // 테스트 코드
 *     }
 * }
 * }</pre>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MySQLContainer<?> mysqlContainer;

    @Autowired
    protected GenericContainer<?> redisContainer;

    @Autowired
    protected KafkaContainer kafkaContainer;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // MySQL 설정은 @ServiceConnection으로 자동 구성됨
        // 추가 속성이 필요한 경우 여기서 설정
    }

    @BeforeEach
    void verifyContainersRunning() {
        if (!mysqlContainer.isRunning()) {
            throw new IllegalStateException("MySQL container is not running");
        }
        if (!redisContainer.isRunning()) {
            throw new IllegalStateException("Redis container is not running");
        }
        if (!kafkaContainer.isRunning()) {
            throw new IllegalStateException("Kafka container is not running");
        }
    }
}
```

### 3. MySQL 전용 Base Test Class

```java
package com.ryuqq.test.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import jakarta.persistence.EntityManager;

/**
 * MySQL 통합 테스트 기본 클래스
 *
 * <p>MySQL만 필요한 Repository 테스트에 최적화된 기본 클래스입니다.</p>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@SpringBootTest
@Testcontainers
public abstract class AbstractMySQLIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true)
        .withCommand(
            "--character-set-server=utf8mb4",
            "--collation-server=utf8mb4_unicode_ci"
        );

    @Autowired
    protected EntityManager entityManager;

    /**
     * 영속성 컨텍스트 초기화
     *
     * <p>1차 캐시를 비워 DB에서 직접 조회하도록 강제합니다.</p>
     */
    protected void clearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }
}
```

### 4. Redis 전용 Base Test Class

```java
package com.ryuqq.test.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

/**
 * Redis 통합 테스트 기본 클래스
 *
 * <p>Redis 캐시 테스트에 최적화된 기본 클래스입니다.</p>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@SpringBootTest
@Testcontainers
public abstract class AbstractRedisIntegrationTest {

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379)
        .withReuse(true)
        .withCommand("redis-server", "--maxmemory", "256mb", "--maxmemory-policy", "allkeys-lru");

    @Autowired
    protected CacheManager cacheManager;

    /**
     * 모든 캐시 초기화
     */
    protected void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName ->
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear()
        );
    }

    /**
     * 특정 캐시 초기화
     *
     * @param cacheName 초기화할 캐시 이름
     */
    protected void clearCache(String cacheName) {
        Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
    }
}
```

### 5. Kafka 전용 Base Test Class

```java
package com.ryuqq.test.base;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka 통합 테스트 기본 클래스
 *
 * <p>Kafka 이벤트 테스트에 최적화된 기본 클래스입니다.</p>
 *
 * @author Claude Code
 * @since 2025-10-30
 */
@SpringBootTest
@Testcontainers
public abstract class AbstractKafkaIntegrationTest {

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
        .withReuse(true)
        .withKraft();

    /**
     * 테스트용 Kafka Consumer 생성
     *
     * @param topic 구독할 토픽
     * @return Consumer 인스턴스
     */
    protected Consumer<String, String> createConsumer(String topic) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(props).createConsumer();
        consumer.subscribe(List.of(topic));
        return consumer;
    }

    /**
     * 테스트용 Kafka Producer 생성
     *
     * @return Producer 인스턴스
     */
    protected Producer<String, String> createProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<String, String>(props).createProducer();
    }

    /**
     * 메시지 폴링 (최대 10초 대기)
     *
     * @param consumer Consumer 인스턴스
     * @return 수신된 메시지 레코드
     */
    protected ConsumerRecords<String, String> pollMessages(Consumer<String, String> consumer) {
        return consumer.poll(Duration.ofSeconds(10));
    }
}
```

### 6. Gradle 빌드 설정

```groovy
dependencies {
    // Testcontainers
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
    testImplementation 'org.testcontainers:mysql:1.19.3'
    testImplementation 'org.testcontainers:kafka:1.19.3'

    // 테스트 유틸
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
}

test {
    useJUnitPlatform()

    // Testcontainers 재사용 활성화
    systemProperty 'testcontainers.reuse.enable', 'true'

    // Docker Desktop 사용 (Mac/Windows)
    systemProperty 'testcontainers.docker.client.strategy', 'org.testcontainers.dockerclient.DockerMachineClientProviderStrategy'

    // 병렬 테스트 설정
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1
}
```

### 7. .testcontainers.properties (프로젝트 루트)

```properties
# Testcontainers 재사용 활성화
testcontainers.reuse.enable=true

# Docker 데몬 설정 (Mac/Linux)
docker.client.strategy=org.testcontainers.dockerclient.UnixSocketClientProviderStrategy

# 로깅 레벨
logging.level.org.testcontainers=INFO
logging.level.com.github.dockerjava=WARN
```

---

## 🔧 생성 규칙

### 1. 파일 위치
```
src/test/java/com/ryuqq/test/
├── config/
│   └── TestcontainersConfiguration.java
└── base/
    ├── AbstractIntegrationTest.java
    ├── AbstractMySQLIntegrationTest.java
    ├── AbstractRedisIntegrationTest.java
    └── AbstractKafkaIntegrationTest.java

.testcontainers.properties (프로젝트 루트)
```

### 2. 사용 예시
```java
// MySQL만 필요한 경우
@DisplayName("OrderRepository 통합 테스트")
class OrderRepositoryIntegrationTest extends AbstractMySQLIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldSaveOrder() {
        OrderEntity order = OrderEntity.builder()
            .orderId(1L)
            .customerId(100L)
            .build();

        orderRepository.save(order);
        clearPersistenceContext();

        assertThat(orderRepository.findById(1L)).isPresent();
    }
}

// Redis 캐시 테스트
@DisplayName("OrderQueryService 캐시 테스트")
class OrderQueryServiceCacheTest extends AbstractRedisIntegrationTest {

    @Autowired
    private OrderQueryService orderQueryService;

    @BeforeEach
    void setUp() {
        clearAllCaches();
    }

    @Test
    void shouldUseCacheOnSecondQuery() {
        // 캐시 테스트 코드
    }
}

// Kafka 이벤트 테스트
@DisplayName("OrderEventPublisher 테스트")
class OrderEventPublisherTest extends AbstractKafkaIntegrationTest {

    @Test
    void shouldPublishOrderCreatedEvent() {
        Consumer<String, String> consumer = createConsumer("order-events");

        // 이벤트 발행
        orderService.createOrder(/*...*/);

        // 메시지 수신 검증
        ConsumerRecords<String, String> records = pollMessages(consumer);
        assertThat(records.count()).isEqualTo(1);
    }
}
```

### 3. Zero-Tolerance 규칙 준수

- ✅ **Container Reuse**: 테스트 간 컨테이너 재사용
- ✅ **@ServiceConnection**: Spring Boot 3.1+ 자동 구성
- ✅ **Base Classes**: 공통 설정 상속
- ✅ **Isolation**: 각 테스트 독립 실행
- ✅ **Performance**: 병렬 테스트 지원

---

## 📊 성능 최적화

| 항목 | 설정 | 효과 |
|------|------|------|
| Container Reuse | `withReuse(true)` | 90% 시간 절감 |
| Parallel Tests | `maxParallelForks` | 50% 시간 절감 |
| KRaft Mode | Kafka `.withKraft()` | ZooKeeper 불필요 |
| Memory Limit | Redis `--maxmemory 256mb` | 리소스 최적화 |

---

## 💡 Claude Code 활용 팁

### 1. 기본 설정 생성
```
"Generate Testcontainers base configuration with MySQL, Redis, and Kafka"
```

### 2. 특정 Container 설정
```
"Add PostgreSQL Testcontainer configuration to AbstractIntegrationTest"
```

### 3. 성능 최적화
```
"Optimize Testcontainers configuration for faster test execution"
```

### 4. 커스텀 Base Class
```
"Create custom base test class for MongoDB Testcontainers"
```

---

## 🎯 기대 효과

1. **실제 환경 테스트**: Docker 기반 실제 DB/캐시 사용
2. **빠른 실행**: Container 재사용으로 90% 시간 절감
3. **격리된 테스트**: 각 테스트 독립 실행 보장
4. **간편한 사용**: Base Class 상속만으로 설정 완료

---

**✅ 이 명령어는 Claude Code가 Testcontainers 기반 테스트 인프라를 자동 생성하는 데 사용됩니다.**

**💡 핵심**: 모든 통합 테스트는 Testcontainers Base Class를 상속하여 실제 DB/캐시 환경에서 실행!
