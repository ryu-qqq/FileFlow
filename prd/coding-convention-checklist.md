# 파일 업로드 시스템 코딩 컨벤션 체크리스트

## 🚨 Zero-Tolerance 규칙 체크리스트 (모든 태스크 공통)

### ✅ 필수 검증 항목

#### 1. Lombok 금지
```bash
# 검증 스크립트
grep -r "@Data\|@Getter\|@Setter\|@Builder" domain/ application/
# 결과가 없어야 함
```

- [ ] `@Data` 사용하지 않음
- [ ] `@Getter` 사용하지 않음
- [ ] `@Setter` 사용하지 않음
- [ ] `@Builder` 사용하지 않음
- [ ] 모든 getter/setter 수동 작성
- [ ] toString() 메서드 수동 구현

#### 2. Law of Demeter 준수
```bash
# Anti-pattern 검증
grep -r "\.\w\+()\.get" domain/ application/
# Getter 체이닝 없어야 함
```

- [ ] Getter 체이닝 없음 (`a.getB().getC()` ❌)
- [ ] Tell, Don't Ask 패턴 적용
- [ ] 각 클래스는 자신의 작업만 수행
- [ ] 메서드 파라미터 3개 이하
- [ ] 직접 소통하는 객체만 호출

#### 3. Long FK 전략
```bash
# JPA 관계 어노테이션 검증
grep -r "@ManyToOne\|@OneToMany\|@OneToOne\|@ManyToMany" adapter-out/persistence/
# 결과가 없어야 함
```

- [ ] JPA 관계 어노테이션 미사용
- [ ] 모든 FK는 Long 타입으로 관리
- [ ] Lazy Loading 문제 없음
- [ ] N+1 쿼리 문제 방지

#### 4. Transaction 경계
```bash
# Transaction 내 외부 호출 검증
python3 .claude/hooks/scripts/validate-transaction-boundary.py
```

- [ ] `@Transactional` 내 외부 API 호출 없음
- [ ] 트랜잭션은 Application Layer에서만
- [ ] Private 메서드에 `@Transactional` 없음
- [ ] Final 메서드에 `@Transactional` 없음

---

## Phase 2A: Multipart Upload 태스크별 체크리스트

### Task 1: MultipartUpload Aggregate 설계
```java
// 검증 포인트
public class MultipartUpload {
    // ✅ NO Lombok
    private Long id;
    private Long uploadSessionId; // ✅ Long FK

    // ✅ Static Factory Method
    public static MultipartUpload create() { }

    // ✅ Tell, Don't Ask
    public void addPart() { }
    public boolean canComplete() { }
}
```

**체크리스트:**
- [ ] Aggregate Root 식별
- [ ] Value Object 분리
- [ ] 불변성 보장
- [ ] Static Factory Method 사용
- [ ] Domain Event 정의

### Task 2: UploadPart Value Object
```java
// 검증 포인트
public class UploadPart {
    // ✅ 불변 필드
    private final int partNumber;
    private final String etag;

    // ✅ Private 생성자
    private UploadPart() { }

    // ✅ equals/hashCode 구현
}
```

**체크리스트:**
- [ ] final 필드 사용
- [ ] Private 생성자
- [ ] equals/hashCode 구현
- [ ] 유효성 검증 포함

### Task 3: MultipartStatus 상태 관리
```java
// 검증 포인트
public enum MultipartStatus {
    INITIATED, IN_PROGRESS, COMPLETING, COMPLETED, FAILED;

    // ✅ 상태 전이 검증
    public boolean canTransitionTo(MultipartStatus newStatus) {
        // 비즈니스 룰 구현
    }
}
```

**체크리스트:**
- [ ] 모든 상태 정의
- [ ] 상태 전이 규칙 구현
- [ ] 불가능한 전이 방지

### Task 4-10: Application/Adapter Layer
**공통 체크리스트:**
- [ ] UseCase 단일 책임
- [ ] Command/Query 분리
- [ ] Port 인터페이스 정의
- [ ] Anti-Corruption Layer 구현
- [ ] 통합 테스트 작성

---

## Phase 2B: External Download & Policy 태스크별 체크리스트

### Task 11: ExternalDownload Aggregate
```java
// 검증 포인트
public class ExternalDownload {
    private Long id;
    private Long uploadSessionId; // ✅ Long FK

    // ✅ 재시도 로직 캡슐화
    public boolean shouldRetry() {
        return retryCount < maxRetries
            && canRetryForStatus(status);
    }
}
```

**체크리스트:**
- [ ] 재시도 정책 캡슐화
- [ ] 상태 관리 구현
- [ ] 실패 처리 로직
- [ ] Domain Event 발행

### Task 12: DownloadStatus 상태 관리
**체크리스트:**
- [ ] 모든 상태 정의
- [ ] 재시도 가능 상태 구분
- [ ] 최종 상태 식별

### Task 13: UploadPolicy Aggregate
```java
// 검증 포인트
public class UploadPolicy {
    // ✅ Value Object 사용
    private final PolicyType type;
    private final PolicyConstraints constraints;

    // ✅ 정책 검증 캡슐화
    public PolicyViolation validate(UploadRequest request) {
        // 비즈니스 룰
    }
}
```

**체크리스트:**
- [ ] 정책 타입 정의
- [ ] 제약사항 캡슐화
- [ ] 검증 로직 구현
- [ ] 위반 사항 명확한 반환

### Task 14-16: Application/Adapter Layer
**체크리스트:**
- [ ] Retry Template 구현
- [ ] Circuit Breaker 패턴
- [ ] 정책 캐싱 구현
- [ ] 외부 API Anti-Corruption

---

## Phase 2C: Event & Integration 태스크별 체크리스트

### Task 17: Domain Event 정의
```java
// 검증 포인트
public class UploadCompletedEvent {
    // ✅ 불변 이벤트
    private final Long uploadSessionId;
    private final Instant occurredAt;

    // ✅ Static Factory
    public static UploadCompletedEvent of() { }
}
```

**체크리스트:**
- [ ] 모든 이벤트 불변성
- [ ] 이벤트 이름 명확성
- [ ] 필수 정보만 포함
- [ ] 타임스탬프 포함

### Task 18: AbstractAggregateRoot 확장
```java
// 검증 포인트
public class UploadSession extends AbstractAggregateRoot<UploadSession> {
    public void complete() {
        // ✅ 상태 변경
        this.status = COMPLETED;
        // ✅ 이벤트 등록
        registerEvent(UploadCompletedEvent.of(this.id));
    }
}
```

**체크리스트:**
- [ ] AbstractAggregateRoot 상속
- [ ] registerEvent() 사용
- [ ] 이벤트와 상태 동기화
- [ ] @DomainEvents 자동 처리

### Task 19: Event Publisher 구현
**체크리스트:**
- [ ] ApplicationEventPublisher 주입
- [ ] 비동기 처리 설정
- [ ] 에러 핸들링
- [ ] 이벤트 로깅

### Task 20: Idempotency 구현
```java
// 검증 포인트
@Component
public class IdempotencyMiddleware {
    // ✅ Redis 분산 락
    private final RedisTemplate<String, String> redis;

    public boolean acquireLock(String key) {
        // ✅ SetNX with TTL
        return redis.opsForValue()
            .setIfAbsent(key, "locked", Duration.ofMinutes(5));
    }
}
```

**체크리스트:**
- [ ] 멱등키 생성 전략
- [ ] 분산 락 구현
- [ ] TTL 설정
- [ ] 동시성 처리

### Task 21-26: Integration & Monitoring
**체크리스트:**
- [ ] SQS 메시지 구조 정의
- [ ] Dead Letter Queue 설정
- [ ] 메트릭 수집 구현
- [ ] 알림 임계값 설정
- [ ] 통합 테스트 시나리오

---

## 🔧 검증 도구 실행

### 1. 자동 검증 (Claude Hooks)
```bash
# Cache 빌드
python3 .claude/hooks/scripts/build-rule-cache.py

# 도메인 레이어 검증
/validate-domain domain/src/main/java/com/ryuqq/fileflow/domain/upload/

# 전체 아키텍처 검증
/validate-architecture
```

### 2. ArchUnit 테스트
```bash
# ArchUnit 테스트 실행
./gradlew test --tests "*ArchitectureTest"
```

### 3. Git Pre-commit Hook
```bash
# Pre-commit hook 설치
cp hooks/pre-commit .git/hooks/
chmod +x .git/hooks/pre-commit
```

### 4. 수동 검증 체크리스트
```bash
# Lombok 사용 검사
find . -name "*.java" -exec grep -l "@Data\|@Getter\|@Setter" {} \;

# Getter 체이닝 검사
find . -name "*.java" -exec grep -l "\.get.*()\.get" {} \;

# JPA 관계 검사
find . -name "*Entity.java" -exec grep -l "@ManyToOne\|@OneToMany" {} \;

# Transaction 경계 검사
grep -r "@Transactional" --include="*.java" | grep -v "application/src"
```

---

## 📋 태스크 완료 기준

### 각 태스크 완료 전 확인사항:

1. **코드 리뷰 체크리스트**
   - [ ] Zero-Tolerance 규칙 모두 통과
   - [ ] 단위 테스트 작성 완료
   - [ ] 통합 테스트 작성 완료
   - [ ] Javadoc 작성 완료

2. **성능 검증**
   - [ ] 대용량 파일 테스트 (>5GB)
   - [ ] 동시성 테스트 (100개 동시 업로드)
   - [ ] 메모리 프로파일링

3. **보안 검증**
   - [ ] 입력 유효성 검증
   - [ ] 권한 체크 구현
   - [ ] 민감 정보 로깅 방지

4. **문서화**
   - [ ] API 문서 업데이트
   - [ ] 시퀀스 다이어그램 작성
   - [ ] 에러 코드 정의

---

## 🚀 Phase별 진행 순서

### Phase 2A (1주차)
1. Domain 모델 설계 및 구현
2. Application UseCase 구현
3. Adapter 구현
4. 통합 테스트

### Phase 2B (2주차)
1. External Download 구현
2. Policy 시스템 구현
3. 재시도 로직 구현
4. 성능 최적화

### Phase 2C (3주차)
1. Event 시스템 구현
2. Idempotency 구현
3. 모니터링 구현
4. 전체 통합 테스트

---

## ⚠️ 주의사항

1. **절대 Lombok 사용 금지** - 발견 시 즉시 PR reject
2. **Getter 체이닝 금지** - Tell, Don't Ask 원칙 준수
3. **JPA 관계 어노테이션 금지** - Long FK만 사용
4. **Transaction 내 외부 호출 금지** - 트랜잭션 경계 준수
5. **모든 public 메서드 Javadoc 필수** - @author, @since 포함

이 체크리스트를 각 태스크 시작 전에 확인하고, 완료 후 다시 한 번 검증하세요.