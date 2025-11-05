# Download Bounded Context 종합 분석 및 리팩토링 가이드

**작성일**: 2025-11-05
**분석 범위**: `download` Bounded Context 전체 레이어
**우선순위**: P0 (필수) / P1 (중요) / P2 (선택)

---

## 📋 목차

1. [분석 개요](#분석-개요)
2. [현재 상태 평가](#현재-상태-평가)
3. [레이어별 상세 분석](#레이어별-상세-분석)
   - [Application Layer (CQRS)](#1-application-layer-cqrs)
   - [Orchestrator Pattern](#2-orchestrator-pattern-managerfacade)
   - [Adapter-Out CQRS](#3-adapter-out-cqrs)
   - [Domain Exceptions](#4-domain-exceptions)
   - [REST API ErrorMapper](#5-rest-api-errormapper)
4. [리팩토링 우선순위](#리팩토링-우선순위)
5. [실행 계획](#실행-계획)

---

## 분석 개요

### 목표
Download Bounded Context의 모든 레이어에서 Spring Standards 코딩 컨벤션 준수 여부를 평가하고, 구체적인 리팩토링 작업을 도출합니다.

### 분석 범위
```
download/
├── domain/              # Domain Layer
│   ├── ExternalDownload.java (Aggregate Root)
│   ├── ErrorCode.java
│   └── ErrorMessage.java
│
├── application/         # Application Layer
│   ├── service/         # UseCase 구현
│   │   ├── StartExternalDownloadService.java (Command)
│   │   └── GetDownloadStatusService.java (Query)
│   ├── manager/         # StateManager 패턴
│   │   ├── ExternalDownloadManager.java
│   │   └── ExternalDownloadOutboxManager.java
│   └── port/
│       ├── in/          # Inbound Ports
│       └── out/         # Outbound Ports
│           ├── ExternalDownloadPort.java (⚠️ CQRS 미적용)
│           ├── ExternalDownloadOutboxCommandPort.java (✅ CQRS)
│           └── ExternalDownloadOutboxQueryPort.java (✅ CQRS)
│
└── adapter-out/         # Adapter-Out Layer
    └── persistence-mysql/
        ├── ExternalDownloadPersistenceAdapter.java (⚠️ CQRS 미적용)
        ├── ExternalDownloadOutboxCommandAdapter.java (✅ CQRS)
        └── ExternalDownloadOutboxQueryAdapter.java (✅ CQRS)
```

---

## 현재 상태 평가

### 종합 평가표

| 레이어 | 컨벤션 준수율 | 주요 이슈 | 우선순위 |
|--------|---------------|-----------|----------|
| **Domain Layer** | ⚠️ 75% | Domain Exception 미사용 | P0 |
| **Application Layer (Port)** | ⚠️ 50% | CQRS 부분 적용 (Outbox만) | P1 |
| **Application Layer (Manager)** | ⚠️ 60% | 역할 불명확 (Port 직접 주입) | P2 |
| **Adapter-Out** | ⚠️ 50% | CQRS 부분 적용 (Outbox만) | P1 |
| **REST API** | ⚠️ 40% | ErrorMapper 미구현 | P0 |

### 컨벤션 준수 현황

#### ✅ 잘 지켜진 규칙
- **Lombok 금지**: 모든 코드에서 Pure Java 사용
- **Law of Demeter**: Domain Layer에서 `getIdValue()`, `getSourceUrlString()` 등 Tell-Don't-Ask 패턴 준수
- **Long FK 전략**: JPA 관계 어노테이션 없음
- **Transaction 경계**: `@Transactional` 명시적 사용
- **Outbox Pattern CQRS**: ExternalDownloadOutbox는 Command/Query Port 완벽 분리

#### ❌ 개선 필요한 규칙
- **Domain Exception**: 표준 Java 예외 사용 (IllegalStateException, IllegalArgumentException)
- **CQRS Port 분리**: ExternalDownloadPort가 Command/Query 혼합
- **ErrorMapper**: Download Domain 전용 ErrorMapper 미구현
- **Manager 역할**: StateManager가 Port를 직접 주입 (Service 책임과 혼재)

---

## 레이어별 상세 분석

### 1. Application Layer (CQRS)

#### 1.1 현재 상태

##### ✅ Outbox Pattern: CQRS 완벽 적용

**CommandPort**:
```java
// ✅ GOOD: Command 전용 Port (쓰기만)
public interface ExternalDownloadOutboxCommandPort {
    ExternalDownloadOutbox save(ExternalDownloadOutbox outbox);
    void deleteById(Long outboxId);
    int deleteProcessedMessagesBefore(LocalDateTime beforeDate);
}
```

**QueryPort**:
```java
// ✅ GOOD: Query 전용 Port (읽기만)
public interface ExternalDownloadOutboxQueryPort {
    Optional<ExternalDownloadOutbox> findByIdempotencyKey(String idempotencyKey);
    Optional<ExternalDownloadOutbox> findById(Long outboxId);
    List<ExternalDownloadOutbox> findByStatus(OutboxStatus status, int limit);
    // ... 기타 조회 메서드
}
```

**UseCase 사용**:
```java
// ✅ GOOD: Command와 Query Port를 명확히 구분하여 사용
@Service
public class StartExternalDownloadService {
    private final ExternalDownloadOutboxCommandPort outboxCommandPort; // 쓰기
    private final ExternalDownloadOutboxQueryPort outboxQueryPort;     // 읽기
    // ...
}
```

##### ⚠️ ExternalDownloadPort: CQRS 미적용

**문제점**:
```java
// ❌ BAD: Command와 Query가 하나의 Port에 혼재
public interface ExternalDownloadPort {
    // Command 메서드
    ExternalDownload save(ExternalDownload download);
    void delete(Long id);

    // Query 메서드
    Optional<ExternalDownload> findById(Long id);
    Optional<ExternalDownload> findByUploadSessionId(Long uploadSessionId);
    List<ExternalDownload> findByStatus(ExternalDownloadStatus status);
    List<ExternalDownload> findRetryableDownloads(Integer maxRetry, LocalDateTime retryAfter);
}
```

**영향 범위**:
- `StartExternalDownloadService` (Command UseCase)
  - Line 46: `ExternalDownloadPort` 주입 (Command + Query 혼합)
  - 실제로는 `save()` (Command)만 사용
- `GetDownloadStatusService` (Query UseCase)
  - Line 36: `ExternalDownloadPort` 주입 (Command + Query 혼합)
  - 실제로는 `findById()` (Query)만 사용
- `ExternalDownloadManager`
  - Line 51: `ExternalDownloadPort` 직접 주입 (StateManager 책임 위반)

#### 1.2 리팩토링 계획

**목표**: `ExternalDownloadPort`를 Command/Query로 분리

**작업 내용**:

1. **ExternalDownloadCommandPort 생성**
   ```java
   /**
    * External Download Command Port (CQRS - Command Side)
    * 명령(생성/수정/삭제) 전용 Port 인터페이스
    */
   public interface ExternalDownloadCommandPort {
       ExternalDownload save(ExternalDownload download);
       void delete(Long id);
   }
   ```

2. **ExternalDownloadQueryPort 생성**
   ```java
   /**
    * External Download Query Port (CQRS - Query Side)
    * 조회 전용 Port 인터페이스
    */
   public interface ExternalDownloadQueryPort {
       Optional<ExternalDownload> findById(Long id);
       Optional<ExternalDownload> findByUploadSessionId(Long uploadSessionId);
       List<ExternalDownload> findByStatus(ExternalDownloadStatus status);
       List<ExternalDownload> findRetryableDownloads(Integer maxRetry, LocalDateTime retryAfter);
   }
   ```

3. **UseCase 수정**
   - `StartExternalDownloadService`: `ExternalDownloadCommandPort` 사용
   - `GetDownloadStatusService`: `ExternalDownloadQueryPort` 사용

4. **기존 Port 제거**
   - `ExternalDownloadPort` 인터페이스 삭제
   - 모든 참조를 Command/Query Port로 교체

---

### 2. Orchestrator Pattern (Manager/Facade)

#### 2.1 현재 상태

##### Manager의 역할

**ExternalDownloadManager**:
```java
@Component
public class ExternalDownloadManager {
    // ⚠️ ISSUE: StateManager가 Port를 직접 주입
    private final ExternalDownloadPort downloadPort;
    private final UploadSessionPort uploadSessionPort;
    private final FileCommandManager fileCommandManager;

    // StateManager 역할: 상태 변경 메서드
    @Transactional
    public ExternalDownload startDownloading(ExternalDownload download) { ... }

    @Transactional
    public ExternalDownload completeDownload(ExternalDownload download, long fileSize) { ... }

    @Transactional
    public ExternalDownload failWithRetry(ExternalDownload download, ErrorCode errorCode, String errorMessage) { ... }

    // ⚠️ ISSUE: 조회 메서드도 포함 (CQRS 위반)
    @Transactional(readOnly = true)
    public Optional<ExternalDownload> findById(Long downloadId) { ... }

    @Transactional(readOnly = true)
    public ExternalDownload getById(Long downloadId) { ... }

    // ⚠️ ISSUE: 복합 작업 (Facade 역할과 혼재)
    @Transactional
    public void markCompleted(
        ExternalDownload download,
        UploadSession session,
        DownloadResult result
    ) {
        // Download 완료 + UploadSession 업데이트 + FileAsset 생성
        // ⇒ 이것은 Facade 또는 Service 책임
    }
}
```

**ExternalDownloadOutboxManager**:
```java
@Component
public class ExternalDownloadOutboxManager {
    // ✅ GOOD: Command와 Query Port를 명확히 분리
    private final ExternalDownloadOutboxQueryPort queryPort;
    private final ExternalDownloadOutboxCommandPort commandPort;

    // StateManager 역할: 상태 변경만
    @Transactional
    public ExternalDownloadOutbox markProcessing(ExternalDownloadOutbox outbox) { ... }

    @Transactional
    public ExternalDownloadOutbox markProcessed(ExternalDownloadOutbox outbox) { ... }

    // 조회 메서드: QueryPort 위임
    @Transactional(readOnly = true)
    public List<ExternalDownloadOutbox> findNewMessages(int batchSize) {
        return queryPort.findByStatus(OutboxStatus.PENDING, batchSize);
    }
}
```

#### 2.2 분석 결과

##### ✅ 코딩 컨벤션 준수 사항
- **Spring Proxy 문제 해결**: Manager를 별도 Bean으로 분리하여 `@Transactional` 정상 작동
- **트랜잭션 경계 명확화**: 각 상태 변경 메서드에 `@Transactional` 명시
- **중앙화된 상태 관리**: 모든 상태 변경이 Manager를 통해 이루어짐

##### ⚠️ 개선 필요 사항
1. **Port 직접 주입**: StateManager가 Port를 직접 주입하는 것은 관심사 혼재
   - **문제**: StateManager의 원래 책임은 "상태 변경 로직 캡슐화"인데, Port 주입은 "영속화 관심사"
   - **해결**: Service가 Port 주입 → Manager는 Domain 객체만 받아서 상태 변경

2. **조회 메서드 포함**: StateManager에 조회 메서드가 있는 것은 CQRS 위반
   - **문제**: `findById()`, `getById()` 같은 조회 메서드는 Query 책임
   - **해결**: 조회는 Service가 QueryPort를 통해 직접 수행

3. **복합 작업 (Facade 역할)**: `markCompleted()`는 3가지 Aggregate 처리
   - **문제**: Download + UploadSession + FileAsset 처리는 Facade 또는 Orchestrator 책임
   - **해결**:
     - **Option A**: 별도 Facade 생성 (`DownloadCompletionFacade`)
     - **Option B**: Service에서 직접 처리 (작은 규모면 충분)

#### 2.3 리팩토링 계획

##### P2 (선택): Manager 역할 재정의

**Option 1: Manager 제거** (Service 흡수)
- Manager의 메서드를 Service로 이동
- Service가 Port를 직접 주입
- 장점: 단순화, 레이어 감소
- 단점: Service 코드 증가

**Option 2: Manager 순수화** (Domain 객체만 처리)
```java
// ✅ GOOD: StateManager는 Domain 객체만 받아서 상태 변경
@Component
public class ExternalDownloadStateManager {
    // Port 주입 제거!

    // Pure State Transition
    public ExternalDownload startDownloading(ExternalDownload download) {
        download.start();
        return download;
    }

    public ExternalDownload completeDownload(ExternalDownload download, long fileSize) {
        download.updateProgress(FileSize.of(fileSize), FileSize.of(fileSize));
        download.complete();
        return download;
    }
}

// Service에서 Port 주입 및 영속화
@Service
public class StartExternalDownloadService {
    private final ExternalDownloadCommandPort commandPort; // Port 주입
    private final ExternalDownloadStateManager stateManager; // 상태 변경만

    @Transactional
    public void start(Long downloadId) {
        ExternalDownload download = commandPort.findById(downloadId); // Port 사용
        ExternalDownload started = stateManager.startDownloading(download); // 상태 변경
        commandPort.save(started); // Port 사용
    }
}
```

**Option 3: 현재 유지** (Manager가 Port 주입)
- 현재 구조 유지
- 복합 작업 메서드(`markCompleted`)만 별도 Facade로 분리
- 장점: 변경 최소화
- 단점: 관심사 혼재 지속

**권장**: Option 3 (현재 유지) → 실용적인 선택

---

### 3. Adapter-Out CQRS

#### 3.1 현재 상태

##### ✅ Outbox Pattern: CQRS 완벽 적용

**CommandAdapter**:
```java
// ✅ GOOD: Command 전용 Adapter (쓰기만)
@Component
public class ExternalDownloadOutboxCommandAdapter
    implements ExternalDownloadOutboxCommandPort {

    private final ExternalDownloadOutboxJpaRepository repository;

    @Override
    @Transactional
    public ExternalDownloadOutbox save(ExternalDownloadOutbox outbox) {
        ExternalDownloadOutboxJpaEntity entity =
            ExternalDownloadOutboxEntityMapper.toEntity(outbox);
        ExternalDownloadOutboxJpaEntity saved = repository.save(entity);
        return ExternalDownloadOutboxEntityMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long outboxId) {
        repository.deleteById(outboxId);
    }
}
```

**QueryAdapter**:
```java
// ✅ GOOD: Query 전용 Adapter (읽기만, QueryDSL 사용)
@Component
@Transactional(readOnly = true)
public class ExternalDownloadOutboxQueryAdapter
    implements ExternalDownloadOutboxQueryPort {

    private final JPAQueryFactory queryFactory; // QueryDSL

    @Override
    public Optional<ExternalDownloadOutbox> findById(Long outboxId) {
        ExternalDownloadOutboxJpaEntity entity = queryFactory
            .selectFrom(externalDownloadOutboxJpaEntity)
            .where(externalDownloadOutboxJpaEntity.id.eq(outboxId))
            .fetchOne();
        return Optional.ofNullable(entity)
            .map(ExternalDownloadOutboxEntityMapper::toDomain);
    }

    // ... 기타 조회 메서드 (QueryDSL)
}
```

##### ⚠️ ExternalDownloadPersistenceAdapter: CQRS 미적용

**문제점**:
```java
// ❌ BAD: Command와 Query가 하나의 Adapter에 혼재
@Component
public class ExternalDownloadPersistenceAdapter implements ExternalDownloadPort {
    private final ExternalDownloadJpaRepository repository;

    // Command 메서드
    @Override
    public ExternalDownload save(ExternalDownload download) { ... }

    @Override
    public void delete(Long id) { ... }

    // Query 메서드
    @Override
    public Optional<ExternalDownload> findById(Long id) { ... }

    @Override
    public List<ExternalDownload> findByStatus(ExternalDownloadStatus status) { ... }

    @Override
    public List<ExternalDownload> findRetryableDownloads(Integer maxRetry, LocalDateTime retryAfter) { ... }
}
```

#### 3.2 리팩토링 계획

**목표**: `ExternalDownloadPersistenceAdapter`를 Command/Query Adapter로 분리

**작업 내용**:

1. **ExternalDownloadCommandAdapter 생성**
   ```java
   /**
    * External Download Command Adapter (CQRS - Command Side)
    * JPA Repository를 사용한 쓰기 전용 Adapter
    */
   @Component
   public class ExternalDownloadCommandAdapter
       implements ExternalDownloadCommandPort {

       private final ExternalDownloadJpaRepository repository;

       @Override
       @Transactional
       public ExternalDownload save(ExternalDownload download) {
           ExternalDownloadJpaEntity entity =
               ExternalDownloadEntityMapper.toEntity(download);
           ExternalDownloadJpaEntity saved = repository.save(entity);
           return ExternalDownloadEntityMapper.toDomain(saved);
       }

       @Override
       @Transactional
       public void delete(Long id) {
           repository.deleteById(id);
       }
   }
   ```

2. **ExternalDownloadQueryAdapter 생성**
   ```java
   /**
    * External Download Query Adapter (CQRS - Query Side)
    * QueryDSL을 사용한 읽기 전용 Adapter
    */
   @Component
   @Transactional(readOnly = true)
   public class ExternalDownloadQueryAdapter
       implements ExternalDownloadQueryPort {

       private final JPAQueryFactory queryFactory;

       @Override
       public Optional<ExternalDownload> findById(Long id) {
           ExternalDownloadJpaEntity entity = queryFactory
               .selectFrom(externalDownloadJpaEntity)
               .where(externalDownloadJpaEntity.id.eq(id))
               .fetchOne();
           return Optional.ofNullable(entity)
               .map(ExternalDownloadEntityMapper::toDomain);
       }

       @Override
       public List<ExternalDownload> findByStatus(ExternalDownloadStatus status) {
           List<ExternalDownloadJpaEntity> entities = queryFactory
               .selectFrom(externalDownloadJpaEntity)
               .where(externalDownloadJpaEntity.status.eq(status))
               .fetch();
           return entities.stream()
               .map(ExternalDownloadEntityMapper::toDomain)
               .collect(Collectors.toList());
       }

       @Override
       public List<ExternalDownload> findRetryableDownloads(
           Integer maxRetry,
           LocalDateTime retryAfter
       ) {
           List<ExternalDownloadJpaEntity> entities = queryFactory
               .selectFrom(externalDownloadJpaEntity)
               .where(
                   externalDownloadJpaEntity.status.eq(ExternalDownloadStatus.DOWNLOADING),
                   externalDownloadJpaEntity.retryCount.lt(maxRetry),
                   externalDownloadJpaEntity.lastRetriedAt.before(retryAfter)
               )
               .fetch();
           return entities.stream()
               .map(ExternalDownloadEntityMapper::toDomain)
               .collect(Collectors.toList());
       }
   }
   ```

3. **기존 Adapter 제거**
   - `ExternalDownloadPersistenceAdapter` 삭제

---

### 4. Domain Exceptions

#### 4.1 현재 상태

##### ❌ 표준 Java 예외 사용

**ExternalDownload.java**:
```java
// Line 262 - ❌ BAD
throw new IllegalStateException("Can only start from INIT state: " + status);

// Line 279 - ❌ BAD
throw new IllegalStateException("Can only complete from PROCESSING state: " + status);

// Line 402 - ❌ BAD
throw new IllegalArgumentException("유효하지 않은 URL입니다");
```

**ExternalDownloadManager.java**:
```java
// Line 86-88 - ❌ BAD
@Transactional(readOnly = true)
public ExternalDownload getById(Long downloadId) {
    return downloadPort.findById(downloadId)
        .orElseThrow(() -> new IllegalStateException(
            "ExternalDownload not found: " + downloadId
        ));
}
```

##### 문제점
1. **Domain 독립성 위반**: 표준 Java 예외는 기술적 예외, Domain 의미 없음
2. **ErrorMapper 불가**: REST API Layer에서 Domain 예외를 HTTP 응답으로 변환 불가
3. **에러 코드 없음**: 클라이언트가 에러를 구분할 방법 없음
4. **다국어 지원 불가**: 에러 메시지 하드코딩

#### 4.2 리팩토링 계획

**목표**: Domain Exception 계층 생성 및 적용

**작업 내용**:

1. **DownloadException (Sealed Interface) 생성**
   - 위치: `domain/src/main/java/com/ryuqq/fileflow/domain/download/exception/DownloadException.java`
   - DomainException 상속
   - Sealed Interface로 서브타입 제한

2. **구체적 Exception 생성**
   - `InvalidDownloadStateException`: 상태 전이 오류
   - `InvalidUrlException`: URL 검증 실패
   - `DownloadNotFoundException`: Download 조회 실패

3. **ExternalDownload.java 수정**
   - Line 262, 279: `InvalidDownloadStateException` 사용
   - Line 402: `InvalidUrlException` 사용

4. **ExternalDownloadManager.java 수정**
   - Line 86-88: `DownloadNotFoundException` 사용

---

### 5. REST API ErrorMapper

#### 5.1 현재 상태

**GlobalExceptionHandler.java**:
```java
// ✅ GOOD: ErrorMapperRegistry 통합
@ExceptionHandler(DomainException.class)
public ResponseEntity<ProblemDetail> handleDomain(
    DomainException ex,
    HttpServletRequest request,
    Locale locale
) {
    var mapped = errorMapperRegistry.map(ex, locale)
        .orElseGet(() -> errorMapperRegistry.defaultMapping(ex)); // ❌ Default로 fallback
    // ...
}
```

**문제점**:
- Download Domain 전용 ErrorMapper가 없음
- DownloadException 발생 시 Default ErrorMapper로 처리됨
- HTTP 상태 코드, 메시지가 일반적 (400 Bad Request)

#### 5.2 리팩토링 계획

**목표**: DownloadErrorMapper 구현

**작업 내용**:

1. **DownloadErrorMapper 생성**
   - 위치: `adapter-in/rest-api/.../download/mapper/DownloadErrorMapper.java`
   - ErrorMapper 인터페이스 구현
   - `@Component` 등록 (자동 ErrorMapperRegistry 통합)

2. **예외별 HTTP 매핑**
   - `InvalidDownloadStateException` → 400 Bad Request
   - `InvalidUrlException` → 400 Bad Request
   - `DownloadNotFoundException` → 404 Not Found

3. **RFC 7807 Problem Details 반환**
   - `type`: 에러 유형 URL
   - `title`: 에러 제목
   - `status`: HTTP 상태 코드
   - `detail`: 상세 메시지
   - `instance`: 요청 URI

---

## 리팩토링 우선순위

### P0 (필수 - 즉시 수행)

| 작업 | 예상 시간 | 이유 |
|------|----------|------|
| **Domain Exception 생성** | 1-2시간 | Domain 독립성 확보, ErrorMapper 전제 조건 |
| **DownloadErrorMapper 구현** | 30분-1시간 | 클라이언트 에러 처리 표준화 |

**합계**: **2-3시간**

---

### P1 (중요 - 1주일 내)

| 작업 | 예상 시간 | 이유 |
|------|----------|------|
| **Application Port CQRS 분리** | 2-3시간 | 아키텍처 표준 준수 |
| **Adapter-Out CQRS 분리** | 2-3시간 | 읽기/쓰기 최적화, 확장성 |

**합계**: **4-6시간**

---

### P2 (선택 - 시간 여유 시)

| 작업 | 예상 시간 | 이유 |
|------|----------|------|
| **Manager 역할 재정의** | 2-4시간 | 관심사 분리 (선택사항) |

**합계**: **2-4시간**

---

## 실행 계획

### Phase 1: P0 작업 (2-3시간)

#### Step 1: Domain Exception 생성 (1-2시간)

**작업 순서**:
1. `domain/.../exception/` 디렉토리 생성
2. `DownloadException.java` (Sealed Interface) 작성
3. `InvalidDownloadStateException.java` 작성
4. `InvalidUrlException.java` 작성
5. `DownloadNotFoundException.java` 작성
6. `ExternalDownload.java` 수정 (Line 262, 279, 402)
7. `ExternalDownloadManager.java` 수정 (Line 86-88)
8. 빌드 확인 (`./gradlew :domain:build`)

**검증**:
- [ ] 모든 클래스에 Javadoc (`@author`, `@since`)
- [ ] Lombok 사용 없음
- [ ] Sealed Interface 적용
- [ ] 빌드 성공

#### Step 2: DownloadErrorMapper 구현 (30분-1시간)

**작업 순서**:
1. `adapter-in/rest-api/.../download/mapper/` 디렉토리 생성
2. `DownloadErrorMapper.java` 작성
3. `ErrorMapper` 인터페이스 구현
4. `@Component` 어노테이션 추가
5. 각 Exception에 대한 HTTP 매핑 구현
6. ErrorMapperRegistry 자동 등록 확인

**검증**:
- [ ] `@Component` 있음
- [ ] `ErrorMapper` 구현
- [ ] 모든 DownloadException 처리
- [ ] RFC 7807 Problem Details 반환

#### Step 3: 통합 테스트 (30분)

**테스트 시나리오**:
1. Invalid State Exception (400 Bad Request)
2. Invalid URL Exception (400 Bad Request)
3. Not Found Exception (404 Not Found)

**검증**:
- [ ] HTTP 상태 코드 정확
- [ ] ErrorCode 필드 포함
- [ ] Message 필드 포함
- [ ] Detail 필드 포함

---

### Phase 2: P1 작업 (4-6시간)

#### Step 1: Application Port CQRS 분리 (2-3시간)

**작업 순서**:
1. `ExternalDownloadCommandPort.java` 생성
2. `ExternalDownloadQueryPort.java` 생성
3. `StartExternalDownloadService` 수정 (CommandPort 사용)
4. `GetDownloadStatusService` 수정 (QueryPort 사용)
5. `ExternalDownloadManager` 수정 (CommandPort, QueryPort 사용)
6. `ExternalDownloadPort.java` 삭제
7. 빌드 및 테스트 확인

**검증**:
- [ ] Command UseCase는 CommandPort만 사용
- [ ] Query UseCase는 QueryPort만 사용
- [ ] 기존 Port 제거됨
- [ ] 모든 테스트 통과

#### Step 2: Adapter-Out CQRS 분리 (2-3시간)

**작업 순서**:
1. `ExternalDownloadCommandAdapter.java` 생성
2. `ExternalDownloadQueryAdapter.java` 생성 (QueryDSL)
3. QueryDSL Q클래스 생성 확인
4. 모든 조회 메서드를 QueryDSL로 변환
5. `ExternalDownloadPersistenceAdapter.java` 삭제
6. 빌드 및 테스트 확인

**검증**:
- [ ] CommandAdapter는 JPA Repository 사용
- [ ] QueryAdapter는 JPAQueryFactory 사용
- [ ] `@Transactional(readOnly = true)` 적용
- [ ] 기존 Adapter 제거됨
- [ ] 모든 테스트 통과

---

### Phase 3: P2 작업 (선택, 2-4시간)

#### Manager 역할 재정의

**Option 3 (권장)**: 현재 유지 + 복합 작업 Facade 분리

**작업 순서**:
1. `DownloadCompletionFacade.java` 생성 (선택)
2. `markCompleted()` 메서드 이동
3. Service에서 Facade 호출로 변경
4. Manager는 단순 상태 변경만 유지

**검증**:
- [ ] Manager는 상태 변경만 처리
- [ ] 복합 작업은 Facade 처리
- [ ] Service는 Facade 호출

---

## 체크리스트

### P0 (필수)
- [ ] Domain Exception 계층 생성 (4개 클래스)
- [ ] ExternalDownload.java 예외 교체 (3곳)
- [ ] ExternalDownloadManager.java 예외 교체 (1곳)
- [ ] DownloadErrorMapper 구현
- [ ] 통합 테스트 시나리오 통과

### P1 (중요)
- [ ] ExternalDownloadCommandPort 생성
- [ ] ExternalDownloadQueryPort 생성
- [ ] UseCase Port 교체 (2개 Service)
- [ ] Manager Port 교체
- [ ] ExternalDownloadPort 삭제
- [ ] ExternalDownloadCommandAdapter 생성
- [ ] ExternalDownloadQueryAdapter 생성 (QueryDSL)
- [ ] ExternalDownloadPersistenceAdapter 삭제
- [ ] 모든 테스트 통과

### P2 (선택)
- [ ] DownloadCompletionFacade 생성 (선택)
- [ ] Manager 역할 정리

---

## 참고 자료

### 코딩 컨벤션
- [Domain Layer 규칙](../coding_convention/02-domain-layer/)
- [Application Layer 규칙](../coding_convention/03-application-layer/)
- [Persistence Layer 규칙](../coding_convention/04-persistence-layer/)
- [REST API Layer 규칙](../coding_convention/01-adapter-rest-api-layer/)
- [Error Handling 규칙](../coding_convention/08-error-handling/)

### 패턴 가이드
- [CQRS Pattern](../coding_convention/03-application-layer/usecase-design/02_cqrs-pattern.md)
- [Manager Pattern](../coding_convention/03-application-layer/transaction-management/)
- [Domain Exception Design](../coding_convention/08-error-handling/domain-exception-design/)
- [ErrorMapper Pattern](../coding_convention/01-adapter-rest-api-layer/exception-handling/)

---

**작성자**: Claude Code
**최종 수정**: 2025-11-05
**다음 작업**: P0 작업 시작 (Domain Exception 생성)
