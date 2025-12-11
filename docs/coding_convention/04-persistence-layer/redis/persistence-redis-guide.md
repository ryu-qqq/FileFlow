# Persistence Layer — **Redis (Lettuce + Redisson)**

> 이 문서는 `persistence-layer` 중 **Redis** 파트에 대한 **요약 가이드**
>
> **Lettuce (캐싱)** + **Redisson (분산락)** 이원화 전략을 기반으로 합니다.

---

## 1) 라이브러리 전략

### Lettuce vs Redisson 역할 분리

| 라이브러리 | 용도 | 이유 |
|-----------|------|------|
| **Lettuce** | 캐싱, 세션, 단순 K-V | Spring Boot 기본, 가벼움, 빠름 |
| **Redisson** | 분산락, 분산 자료구조 | Pub/Sub 기반, Watchdog, 검증됨 |

```
┌─────────────────────────────────────────────────────┐
│                    Redis Server                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│   [Lettuce]                    [Redisson]           │
│   Spring Boot 기본              별도 추가            │
│                                                     │
│   • @Cacheable                 • RLock (분산락)     │
│   • RedisTemplate              • RSemaphore         │
│   • Spring Session             • RCountDownLatch    │
│   • 단순 K-V 저장              • RAtomicLong        │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### 왜 분산락에 Redisson인가?

**Lettuce 분산락 문제점:**
- ❌ 스핀락 (Polling): Lock 해제까지 계속 Redis 조회 → CPU/네트워크 낭비
- ❌ 알림 없음: Lock 해제 시 대기 스레드에게 알림 불가
- ❌ Lua 스크립트 필요: 원자성 보장 위해 직접 구현
- ❌ Lock 연장 불가: 작업이 길어지면 Lock 만료 위험

**Redisson 분산락 장점:**
- ✅ Pub/Sub 기반: Lock 해제 시 이벤트로 대기 스레드 깨움
- ✅ Watchdog: 작업 중 Lock 자동 연장 (기본 30초마다)
- ✅ 다양한 Lock 타입: Fair, Read/Write, MultiLock
- ✅ 검증된 구현: 수많은 프로덕션 환경에서 사용

---

## 2) 핵심 원칙 (한눈에)

### 캐싱 (Lettuce)
* **어댑터=비즈니스 로직 금지**: Cache/Session 저장/조회만 수행
* **Cache-Aside 패턴 고정**: 조회=Cache → DB → Cache 저장, 수정=DB 저장 → Cache 무효화
* **TTL 전략 필수**: 모든 캐시는 **TTL 설정 필수**. Null 캐싱 금지
* **Key Naming Convention**: `{namespace}:{entity}:{id}` 패턴 고정
* **트랜잭션 없음**: `@Transactional` 내에서 Cache 무효화 금지

### 분산락 (Redisson)
* **Pub/Sub 기반**: 스핀락 금지, 이벤트 리스너 방식 사용
* **Watchdog 활용**: 장시간 작업 시 Lock 자동 연장
* **try-finally 필수**: Lock 획득 시 반드시 finally에서 해제
* **타임아웃 필수**: tryLock()에 waitTime, leaseTime 명시

### 금지사항

| 금지 | 이유 |
|------|------|
| Lombok | Plain Java 원칙 |
| 비즈니스 로직 (Adapter 내) | Application/Domain에서 처리 |
| `@Transactional` 내 Cache 무효화 | Rollback 시 불일치 |
| Null 캐싱 (`cache-null-values: true`) | 메모리 낭비 |
| Prod에서 `KEYS` 명령어 | Redis 블로킹 → SCAN 사용 |
| 대용량 데이터 단일 키 저장 (10MB 이상) | 네트워크 병목 |
| Lettuce로 분산락 구현 | 스핀락 문제 → Redisson 사용 |

---

## 3) 패키징 구조

```
persistence-redis/
├─ config/
│  ├─ LettuceConfig.java          # Lettuce 연결 설정
│  ├─ CacheConfig.java            # Spring Cache + RedisCacheManager
│  └─ RedissonConfig.java         # Redisson 연결 설정
│
├─ common/
│  ├─ serializer/
│  │  └─ RedisObjectSerializer.java
│  └─ key/
│     └─ RedisKeyGenerator.java
│
└─ {bc}/                          # ← Bounded Context
   ├─ adapter/
   │  ├─ {Bc}CacheAdapter.java    # ✅ 캐싱 (Lettuce)
   │  └─ {Bc}LockAdapter.java     # ✅ 분산락 (Redisson)
   └─ dto/                        # ← 선택
      └─ {Bc}CacheDto.java
```

---

## 4) 디렉터리별 상세 가이드 링크

### **config/**
* **Lettuce 설정**: [Lettuce Configuration](./config/lettuce-configuration.md) - Connection Pool 설정
* **Cache 설정**: [Cache Configuration](./config/cache-configuration.md) - Spring Cache + RedisCacheManager

### **adapter/** (캐싱)
* **Cache Adapter**: [Cache Adapter 가이드](./adapter/cache-adapter-guide.md) | [테스트](./adapter/cache-adapter-test-guide.md) | [ArchUnit](./adapter/cache-adapter-archunit.md)

### **lock/** (분산락)
* **분산락 개요**: [Distributed Lock 가이드](./lock/distributed-lock-guide.md) - Redisson 기반 분산락 전략
* **Lock Adapter**: [Lock Adapter 가이드](./lock/lock-adapter-guide.md) | [테스트](./lock/lock-adapter-test-guide.md) | [ArchUnit](./lock/lock-adapter-archunit.md)

---

## 5) 주요 패턴

### Cache-Aside (Lettuce)
```java
// 조회
public Order getOrder(Long orderId) {
    // 1. Cache 조회
    Order cached = cachePort.get(orderId).orElse(null);
    if (cached != null) return cached;

    // 2. DB 조회
    Order order = queryPort.findById(OrderId.of(orderId));

    // 3. Cache 저장
    cachePort.cache(orderId, order);
    return order;
}

// 수정
@Transactional
public void updateOrder(Order order) {
    persistPort.persist(order);
    // Cache 무효화는 @TransactionalEventListener에서 처리
}
```

### 분산락 (Redisson)
```java
public void processWithLock(Long orderId) {
    RLock lock = lockPort.getLock("order:" + orderId);

    try {
        // 최대 10초 대기, 30초 유지
        boolean acquired = lock.tryLock(10, 30, TimeUnit.SECONDS);

        if (!acquired) {
            throw new LockAcquisitionException("Lock 획득 실패");
        }

        // 비즈니스 로직
        processOrder(orderId);

    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new LockAcquisitionException("Lock 획득 중 인터럽트", e);
    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

### Key Naming
```java
// 캐싱: {namespace}:{entity}:{id}
"cache::orders::123"
"cache::users::456"

// 분산락: lock:{entity}:{id}
"lock:order:123"
"lock:stock:item:789"
```

---

## 6) 설정 체크리스트

### Lettuce (캐싱)
- [ ] Connection Pool 설정 (`max-active: 16`)
- [ ] `timeout: 3000ms` (Command Timeout)
- [ ] `GenericJackson2JsonRedisSerializer` 사용
- [ ] `cache-null-values: false` (Null 캐싱 금지)
- [ ] Key Naming Convention 준수
- [ ] TTL 전략 수립 (용도별 차등)
- [ ] 환경 변수로 비밀번호 관리

### Redisson (분산락)
- [ ] `lockWatchdogTimeout: 30000ms` (Watchdog 주기)
- [ ] `nettyThreads` 설정 (기본: 가용 CPU * 2)
- [ ] Cluster/Sentinel 모드 설정 (Prod)
- [ ] Lock Timeout 전략 수립

### 모니터링
- [ ] Lettuce 메트릭 수집 (Latency, Hit Rate)
- [ ] Redisson Lock 메트릭 (획득 시간, 실패율)
- [ ] Alert 설정 (Latency > 100ms, Lock 실패율 > 1%)

---

## 7) 의존성 설정

### build.gradle
```groovy
dependencies {
    // Lettuce (Spring Boot 기본 포함)
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'

    // Redisson (분산락용)
    implementation 'org.redisson:redisson-spring-boot-starter:3.27.0'
}
```

### application.yml
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 4
      timeout: 3000ms

# Redisson은 별도 설정 파일 또는 Java Config 사용
```

---

## 8) 참고 문서

* [Lettuce Documentation](https://lettuce.io/core/release/reference/)
* [Redisson Documentation](https://github.com/redisson/redisson/wiki)
* [Spring Data Redis](https://docs.spring.io/spring-data/redis/reference/)
* [Redis Best Practices](https://redis.io/docs/management/optimization/)

---

**작성자**: Development Team
**최종 수정일**: 2025-12-04
**버전**: 2.0.0
