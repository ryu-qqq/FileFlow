# FILE-ASSET-PROCESSING Persistence Layer TDD Plan

> **Jira Issue**: [KAN-340](https://ryuqqq.atlassian.net/browse/KAN-340)
> **Epic**: [KAN-336](https://ryuqqq.atlassian.net/browse/KAN-336)

## Overview
- **PRD**: `docs/prd/file-asset-processing.md`
- **Layer**: Persistence (MySQL/JPA + QueryDSL)
- **Estimated Time**: 300 minutes (5 hours)
- **Total Cycles**: 20 TDD cycles

---

## Zero-Tolerance Rules (Persistence Layer)

### Must Follow
- [x] **Lombok 금지** - Plain Java 사용 (Getter 수동 작성)
- [x] **Long FK 전략** - JPA 관계 어노테이션 금지 (@ManyToOne, @OneToMany 금지)
- [x] **QueryDSL DTO Projection** - Entity 직접 반환 금지, Projections.constructor() 사용
- [x] **정적 팩토리 메서드** - `of()` 패턴 사용
- [x] **protected 기본 생성자** - JPA 요구사항
- [x] **N+1 방지** - fetch join 또는 DTO Projection 필수

### Test Requirements
- JPA Repository: `@DataJpaTest` + TestFixture
- QueryDSL Repository: `@DataJpaTest` + JPAQueryFactory 설정
- Mapper: 단위 테스트 (TestFixture 사용)
- Adapter: 통합 테스트 (실제 DB 연동)

---

## TDD Cycles

### Cycle 1: ProcessedFileAssetJpaEntity (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/entity/ProcessedFileAssetJpaEntityTest.java
[ ] 테스트: `of_정적팩토리_모든필드매핑()` - Domain → Entity 변환
[ ] 테스트: `of_parentAssetId_null허용()` - nullable 필드 처리
[ ] 테스트: `getter_모든필드반환()` - Getter 동작 검증
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ProcessedFileAssetJpaEntity 정적 팩토리 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Entity 파일 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/entity/ProcessedFileAssetJpaEntity.java
[ ] @Entity, @Table(name = "processed_file_assets") 추가
[ ] @Id @GeneratedValue(strategy = IDENTITY) 추가
[ ] 모든 필드 정의 (Lombok 금지!)
  - processedAssetId (String, UUID)
  - originalAssetId (String, Long FK 전략)
  - parentAssetId (String, nullable)
  - variant (ImageVariantType, @Enumerated)
  - format (ImageFormatType, @Enumerated)
  - fileName, fileSize, width, height
  - bucket, s3Key
  - userId, organizationId, tenantId (Long FK 전략)
  - createdAt
[ ] protected 기본 생성자 추가 (JPA용)
[ ] private 생성자 + of() 정적 팩토리 메서드
[ ] 모든 Getter 수동 작성
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ProcessedFileAssetJpaEntity 구현"
```

---

### Cycle 2: FileAssetStatusHistoryJpaEntity (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/entity/FileAssetStatusHistoryJpaEntityTest.java
[ ] 테스트: `of_정적팩토리_모든필드매핑()` - 전체 필드 변환
[ ] 테스트: `of_fromStatus_null허용()` - 최초 생성 시 fromStatus null
[ ] 테스트: `getter_모든필드반환()` - Getter 동작 검증
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileAssetStatusHistoryJpaEntity 정적 팩토리 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Entity 파일 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/entity/FileAssetStatusHistoryJpaEntity.java
[ ] @Entity, @Table(name = "file_asset_status_histories") 추가
[ ] 필드 정의:
  - historyId (String, UUID)
  - fileAssetId (Long, Long FK 전략!)
  - fromStatus (FileAssetStatusType, nullable)
  - toStatus (FileAssetStatusType)
  - message (String, nullable)
  - actor (String)
  - actorType (ActorType enum)
  - changedAt (LocalDateTime)
  - durationMillis (Long, nullable)
[ ] protected 기본 생성자 + of() 정적 팩토리
[ ] Getter 수동 작성
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetStatusHistoryJpaEntity 구현"
```

---

### Cycle 3: ActorType Enum (10분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/entity/ActorTypeTest.java
[ ] 테스트: `values_모든타입존재()` - SYSTEM, N8N, USER
[ ] 테스트: `valueOf_문자열변환()` - 문자열 → enum 변환
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ActorType enum 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Enum 파일 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/entity/ActorType.java
[ ] SYSTEM, N8N, USER 상수 정의
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ActorType enum 구현"
```

---

### Cycle 4: FileProcessingOutboxJpaEntity (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/entity/FileProcessingOutboxJpaEntityTest.java
[ ] 테스트: `of_정적팩토리_모든필드매핑()` - 전체 필드 변환
[ ] 테스트: `markAsSent_상태변경()` - SENT 상태 + processedAt 설정
[ ] 테스트: `markAsFailed_상태변경_재시도횟수증가()` - FAILED + retryCount++
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileProcessingOutboxJpaEntity 정적 팩토리 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Entity 파일 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/entity/FileProcessingOutboxJpaEntity.java
[ ] @Entity, @Table(name = "file_processing_outbox") 추가
[ ] 필드 정의:
  - outboxId (String, UUID)
  - fileAssetId (Long, Long FK 전략!)
  - eventType (String)
  - aggregateType (String)
  - payload (String, TEXT)
  - status (OutboxStatusType)
  - retryCount (int)
  - lastError (String, nullable)
  - createdAt, processedAt
[ ] markAsSent(), markAsFailed() 메서드 추가
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileProcessingOutboxJpaEntity 구현"
```

---

### Cycle 5: OutboxStatusType Enum (10분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/entity/OutboxStatusTypeTest.java
[ ] 테스트: `values_모든상태존재()` - PENDING, SENT, FAILED
[ ] 테스트: `valueOf_문자열변환()` - 문자열 → enum 변환
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: OutboxStatusType enum 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Enum 파일 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/entity/OutboxStatusType.java
[ ] PENDING, SENT, FAILED 상수 정의
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: OutboxStatusType enum 구현"
```

---

### Cycle 6: ProcessedFileAssetJpaRepository (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/repository/ProcessedFileAssetJpaRepositoryTest.java
[ ] @DataJpaTest + @Import(QueryDslConfig.class)
[ ] 테스트: `save_저장후조회()` - JPA 저장 동작 검증
[ ] 테스트: `findByProcessedAssetId_존재하는ID()` - 조회 메서드
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ProcessedFileAssetJpaRepository 저장/조회 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Repository 파일 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/repository/ProcessedFileAssetJpaRepository.java
[ ] JpaRepository<ProcessedFileAssetJpaEntity, Long> 상속
[ ] findByProcessedAssetId() 메서드 선언
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ProcessedFileAssetJpaRepository 구현"
```

---

### Cycle 7: ProcessedFileAssetQueryRepository (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/repository/ProcessedFileAssetQueryRepositoryTest.java
[ ] @DataJpaTest + JPAQueryFactory @Autowired
[ ] 테스트: `findByOriginalAssetId_DTO_Projection()` - QueryDSL DTO 반환
[ ] 테스트: `findByParentAssetId_HTML하위이미지조회()` - 부모 ID 기준 조회
[ ] 테스트: `findByOriginalAssetId_빈결과()` - 없는 ID 조회 시 빈 리스트
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ProcessedFileAssetQueryRepository DTO Projection 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] DTO 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/dto/ProcessedFileAssetDto.java
  - record 사용 (Lombok 금지!)
[ ] Repository 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/repository/ProcessedFileAssetQueryRepository.java
[ ] JPAQueryFactory 의존성 주입 (생성자)
[ ] findByOriginalAssetId() - Projections.constructor() 사용
[ ] findByParentAssetId() - DTO Projection
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ProcessedFileAssetQueryRepository QueryDSL DTO Projection 구현"
```

---

### Cycle 8: FileAssetStatusHistoryJpaRepository (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/repository/FileAssetStatusHistoryJpaRepositoryTest.java
[ ] @DataJpaTest
[ ] 테스트: `save_저장후조회()` - JPA 저장 동작
[ ] 테스트: `findByFileAssetIdOrderByChangedAtAsc_시간순정렬()` - 정렬 검증
[ ] 테스트: `findTopByFileAssetIdOrderByChangedAtDesc_최신이력()` - 최신 1건
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileAssetStatusHistoryJpaRepository 저장/조회 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Repository 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/repository/FileAssetStatusHistoryJpaRepository.java
[ ] JpaRepository<FileAssetStatusHistoryJpaEntity, Long> 상속
[ ] findByFileAssetIdOrderByChangedAtAsc() 선언
[ ] findTopByFileAssetIdOrderByChangedAtDesc() 선언
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetStatusHistoryJpaRepository 구현"
```

---

### Cycle 9: FileAssetStatusHistoryQueryRepository (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/repository/FileAssetStatusHistoryQueryRepositoryTest.java
[ ] @DataJpaTest + JPAQueryFactory
[ ] 테스트: `findExceedingSla_SLA초과이력조회()` - durationMillis > slaMillis
[ ] 테스트: `findExceedingSla_날짜필터()` - fromDate 이후만 조회
[ ] 테스트: `findExceedingSla_limit적용()` - 조회 개수 제한
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileAssetStatusHistoryQueryRepository SLA 조회 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] DTO 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/dto/StatusHistoryDto.java
  - record 사용
[ ] Repository 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/repository/FileAssetStatusHistoryQueryRepository.java
[ ] findExceedingSla() - Projections.constructor() + where + orderBy + limit
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetStatusHistoryQueryRepository QueryDSL 구현"
```

---

### Cycle 10: FileProcessingOutboxJpaRepository (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/repository/FileProcessingOutboxJpaRepositoryTest.java
[ ] @DataJpaTest
[ ] 테스트: `save_저장후조회()` - JPA 저장 동작
[ ] 테스트: `findByStatusOrderByCreatedAtAsc_상태별조회()` - PENDING 조회
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileProcessingOutboxJpaRepository 저장/조회 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Repository 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/repository/FileProcessingOutboxJpaRepository.java
[ ] JpaRepository<FileProcessingOutboxJpaEntity, Long> 상속
[ ] findByStatusOrderByCreatedAtAsc(OutboxStatusType status, Pageable pageable) 선언
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileProcessingOutboxJpaRepository 구현"
```

---

### Cycle 11: FileProcessingOutboxQueryRepository (20분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/repository/FileProcessingOutboxQueryRepositoryTest.java
[ ] @DataJpaTest + JPAQueryFactory
[ ] 테스트: `findPendingEvents_PENDING상태조회()` - status = PENDING
[ ] 테스트: `findPendingEvents_생성순정렬()` - createdAt ASC
[ ] 테스트: `findRetryableFailedEvents_재시도가능조회()` - FAILED + retryCount < max
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileProcessingOutboxQueryRepository Outbox 조회 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Repository 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/repository/FileProcessingOutboxQueryRepository.java
[ ] findPendingEvents(int limit) - PENDING 조회 + 생성순 정렬
[ ] findRetryableFailedEvents(int maxRetryCount, int limit) - FAILED + retryCount 조건
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileProcessingOutboxQueryRepository QueryDSL 구현"
```

---

### Cycle 12: ProcessedFileAssetMapper (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/mapper/ProcessedFileAssetMapperTest.java
[ ] 테스트: `toEntity_Domain에서Entity변환()` - Domain → Entity
[ ] 테스트: `toDomain_Entity에서Domain변환()` - Entity → Domain
[ ] 테스트: `toEntity_parentAssetId_null처리()` - nullable 필드
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ProcessedFileAssetMapper Domain-Entity 변환 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Mapper 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/mapper/ProcessedFileAssetMapper.java
[ ] final class + private 생성자 (유틸리티 클래스)
[ ] toEntity(ProcessedFileAsset domain) - 정적 메서드
[ ] toDomain(ProcessedFileAssetJpaEntity entity) - 정적 메서드
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ProcessedFileAssetMapper 구현"
```

---

### Cycle 13: FileAssetStatusHistoryMapper (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/mapper/FileAssetStatusHistoryMapperTest.java
[ ] 테스트: `toEntity_Domain에서Entity변환()` - Domain → Entity
[ ] 테스트: `toDomain_Entity에서Domain변환()` - Entity → Domain
[ ] 테스트: `toEntity_fromStatus_null처리()` - 최초 생성 시 null
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileAssetStatusHistoryMapper Domain-Entity 변환 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Mapper 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/mapper/FileAssetStatusHistoryMapper.java
[ ] final class + private 생성자
[ ] toEntity(FileAssetStatusHistory domain) - FileAssetStatus → FileAssetStatusType 변환
[ ] toDomain(FileAssetStatusHistoryJpaEntity entity) - 역변환
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetStatusHistoryMapper 구현"
```

---

### Cycle 14: FileProcessingOutboxMapper (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/mapper/FileProcessingOutboxMapperTest.java
[ ] 테스트: `toEntity_Domain에서Entity변환()` - Domain → Entity
[ ] 테스트: `toDomain_Entity에서Domain변환()` - Entity → Domain
[ ] 테스트: `toEntity_lastError_null처리()` - nullable 필드
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileProcessingOutboxMapper Domain-Entity 변환 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Mapper 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/mapper/FileProcessingOutboxMapper.java
[ ] final class + private 생성자
[ ] toEntity(FileProcessingOutbox domain) - OutboxStatus → OutboxStatusType 변환
[ ] toDomain(FileProcessingOutboxJpaEntity entity) - 역변환
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileProcessingOutboxMapper 구현"
```

---

### Cycle 15: ProcessedFileAssetPersistenceAdapter (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/adapter/ProcessedFileAssetPersistenceAdapterTest.java
[ ] @DataJpaTest 통합 테스트
[ ] 테스트: `save_Domain저장후반환()` - PersistencePort 구현
[ ] 테스트: `saveAll_배치저장()` - 여러 개 저장
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ProcessedFileAssetPersistenceAdapter 저장 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Adapter 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/adapter/ProcessedFileAssetPersistenceAdapter.java
[ ] @Repository 어노테이션
[ ] ProcessedFileAssetPersistencePort 구현
[ ] JpaRepository 의존성 주입 (생성자)
[ ] save() - Mapper.toEntity() → save() → Mapper.toDomain()
[ ] saveAll() - 배치 저장
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ProcessedFileAssetPersistenceAdapter 구현"
```

---

### Cycle 16: ProcessedFileAssetQueryAdapter (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/adapter/ProcessedFileAssetQueryAdapterTest.java
[ ] @DataJpaTest 통합 테스트
[ ] 테스트: `findByOriginalAssetId_Domain리스트반환()` - QueryPort 구현
[ ] 테스트: `findByParentAssetId_HTML하위이미지()` - 부모 ID 기준
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: ProcessedFileAssetQueryAdapter 조회 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Adapter 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/adapter/ProcessedFileAssetQueryAdapter.java
[ ] @Repository 어노테이션
[ ] ProcessedFileAssetQueryPort 구현
[ ] QueryRepository 의존성 주입
[ ] findByOriginalAssetId() - DTO → Domain 변환
[ ] findByParentAssetId() - DTO → Domain 변환
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: ProcessedFileAssetQueryAdapter 구현"
```

---

### Cycle 17: FileAssetStatusHistoryPersistenceAdapter (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/adapter/FileAssetStatusHistoryPersistenceAdapterTest.java
[ ] @DataJpaTest 통합 테스트
[ ] 테스트: `save_Domain저장후반환()` - PersistencePort 구현
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileAssetStatusHistoryPersistenceAdapter 저장 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Adapter 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/adapter/FileAssetStatusHistoryPersistenceAdapter.java
[ ] @Repository + FileAssetStatusHistoryPersistencePort 구현
[ ] save() - Mapper 사용
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetStatusHistoryPersistenceAdapter 구현"
```

---

### Cycle 18: FileAssetStatusHistoryQueryAdapter (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/adapter/FileAssetStatusHistoryQueryAdapterTest.java
[ ] @DataJpaTest 통합 테스트
[ ] 테스트: `findByFileAssetId_이력리스트반환()` - QueryPort 구현
[ ] 테스트: `findLatestByFileAssetId_최신이력()` - Optional 반환
[ ] 테스트: `findExceedingSla_SLA초과이력()` - 모니터링용
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileAssetStatusHistoryQueryAdapter 조회 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Adapter 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/adapter/FileAssetStatusHistoryQueryAdapter.java
[ ] @Repository + FileAssetStatusHistoryQueryPort 구현
[ ] findByFileAssetId() - JpaRepository + Mapper
[ ] findLatestByFileAssetId() - Optional + Mapper
[ ] findExceedingSla() - QueryRepository + Domain 변환
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileAssetStatusHistoryQueryAdapter 구현"
```

---

### Cycle 19: FileProcessingOutboxPersistenceAdapter (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/adapter/FileProcessingOutboxPersistenceAdapterTest.java
[ ] @DataJpaTest 통합 테스트
[ ] 테스트: `save_Domain저장후반환()` - PersistencePort 구현
[ ] 테스트: `saveAll_배치저장()` - 여러 개 저장
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileProcessingOutboxPersistenceAdapter 저장 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Adapter 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/adapter/FileProcessingOutboxPersistenceAdapter.java
[ ] @Repository + FileProcessingOutboxPersistencePort 구현
[ ] save() - Mapper 사용
[ ] saveAll() - 배치 저장
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileProcessingOutboxPersistenceAdapter 구현"
```

---

### Cycle 20: FileProcessingOutboxQueryAdapter (15분)

**Red Phase** - `test:`
```
[ ] 테스트 파일 생성: persistence-mysql/src/test/java/com/fileflow/fileasset/persistence/mysql/adapter/FileProcessingOutboxQueryAdapterTest.java
[ ] @DataJpaTest 통합 테스트
[ ] 테스트: `findPendingEvents_PENDING상태Domain반환()` - QueryPort 구현
[ ] 테스트: `findRetryableFailedEvents_재시도가능Domain반환()` - FAILED 조건
[ ] 테스트 실행 → 컴파일 에러 확인
[ ] Commit: "test: FileProcessingOutboxQueryAdapter 조회 테스트 추가"
```

**Green Phase** - `feat:`
```
[ ] Adapter 생성: persistence-mysql/src/main/java/com/fileflow/fileasset/persistence/mysql/adapter/FileProcessingOutboxQueryAdapter.java
[ ] @Repository + FileProcessingOutboxQueryPort 구현
[ ] findPendingEvents() - QueryRepository + Mapper
[ ] findRetryableFailedEvents() - QueryRepository + Mapper
[ ] 테스트 실행 → 통과 확인
[ ] Commit: "feat: FileProcessingOutboxQueryAdapter 구현"
```

---

## Tidy Phase: TestFixture 생성 (전체 완료 후)

**Refactor Phase** - `struct:`
```
[ ] ProcessedFileAssetJpaEntityTestFixture 생성
  - withDefaults() - 기본 테스트 데이터
  - withOriginalAssetId(String) - 특정 원본 ID
  - withVariant(ImageVariantType) - 특정 variant
[ ] FileAssetStatusHistoryJpaEntityTestFixture 생성
  - withDefaults()
  - withFileAssetId(Long)
  - withTransition(from, to)
[ ] FileProcessingOutboxJpaEntityTestFixture 생성
  - withDefaults()
  - withPendingStatus()
  - withFailedStatus()
[ ] 기존 테스트에 TestFixture 적용
[ ] Commit: "struct: Persistence TestFixture 생성 및 적용"
```

---

## Flyway Migration (별도 작업)

**Migration 파일 생성**:
```
[ ] V[버전]__create_processed_file_assets_table.sql
  - CREATE TABLE processed_file_assets
  - 인덱스: idx_original_asset_id, idx_parent_asset_id, idx_tenant_created

[ ] V[버전]__create_file_asset_status_histories_table.sql
  - CREATE TABLE file_asset_status_histories
  - 인덱스: idx_history_file_asset, idx_history_changed_at, idx_history_to_status

[ ] V[버전]__create_file_processing_outbox_table.sql
  - CREATE TABLE file_processing_outbox
  - 인덱스: idx_outbox_status, idx_outbox_status_retry, idx_outbox_created_at

[ ] V[버전]__alter_file_assets_add_processing_columns.sql
  - ALTER TABLE file_assets ADD processed_at DATETIME
  - ALTER TABLE file_assets ADD status_message VARCHAR(500)
  - 인덱스: idx_status_created, idx_category_status
```

---

## Summary

| Phase | Cycles | Estimated Time |
|-------|--------|----------------|
| Entity (1-5) | 5 | 65분 |
| Repository (6-11) | 6 | 105분 |
| Mapper (12-14) | 3 | 45분 |
| Adapter (15-20) | 6 | 90분 |
| TestFixture | 1 | 30분 |
| **Total** | **20+1** | **335분 (약 5.5시간)** |

---

## Checklist Before Starting

- [ ] Domain Layer Plan 완료 확인
- [ ] Application Layer Plan 완료 확인
- [ ] Domain Aggregate 및 VO 구현 완료
- [ ] Persistence Port 인터페이스 정의 완료
- [ ] Flyway 설정 확인
- [ ] QueryDSL 설정 확인 (Q클래스 생성)
