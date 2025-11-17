# FILE-003 TDD Plan

**Task**: Persistence Layer 구현
**Layer**: Persistence Layer (Adapter-Out)
**브랜치**: feature/FILE-003-persistence
**예상 소요 시간**: 780분 (52 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### Phase 1: Flyway Migration (3 사이클)

---

### 1️⃣ files 테이블 마이그레이션 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] `adapter-out-persistence-mysql/src/test/java/.../migration/FlywayMigrationTest.java` 생성
- [ ] `shouldCreateFilesTable()` 테스트 작성 (TestContainers MySQL)
- [ ] 테이블 존재 확인, 컬럼 타입 확인, 인덱스 확인
- [ ] 커밋: `test: files 테이블 마이그레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `adapter-out-persistence-mysql/src/main/resources/db/migration/V1__create_files_table.sql` 생성
- [ ] files 테이블 DDL 작성
  - 컬럼 정의 (id, file_id, file_name, file_size, mime_type, status, s3_key, s3_bucket, cdn_url, uploader_id, category, tags, version, deleted_at, created_at, updated_at)
  - 인덱스 생성 (idx_uploader_status_created, idx_category)
  - CHECK 제약조건 (file_size > 0)
- [ ] 테스트 실행 → 통과 확인
- [ ] 커밋: `feat: files 테이블 마이그레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] SQL 포맷팅 확인
- [ ] 인덱스 이름 일관성 확인
- [ ] 커밋: `struct: files 테이블 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FlywayMigrationFixture.java` 생성 (Flyway 공통 테스트 설정)
- [ ] 커밋: `test: FlywayMigration Fixture 정리 (Tidy)`

---

### 2️⃣ file_processing_jobs 테이블 마이그레이션 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateFileProcessingJobsTable()` 테스트 작성
- [ ] 테이블 존재 확인, 컬럼 타입 확인, 인덱스 확인
- [ ] 커밋: `test: file_processing_jobs 테이블 마이그레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `V2__create_file_processing_jobs_table.sql` 생성
- [ ] file_processing_jobs 테이블 DDL 작성
  - 컬럼 정의 (id, job_id, file_id, job_type, status, retry_count, max_retry_count, input_s3_key, output_s3_key, error_message, created_at, processed_at)
  - 인덱스 생성 (idx_file_status)
- [ ] 커밋: `feat: file_processing_jobs 테이블 마이그레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] SQL 포맷팅 확인
- [ ] 커밋: `struct: file_processing_jobs 테이블 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FlywayMigrationFixture 업데이트
- [ ] 커밋: `test: file_processing_jobs 마이그레이션 Fixture 정리 (Tidy)`

---

### 3️⃣ message_outbox 테이블 마이그레이션 (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCreateMessageOutboxTable()` 테스트 작성
- [ ] 테이블 존재 확인, 컬럼 타입 확인, 인덱스 확인
- [ ] 커밋: `test: message_outbox 테이블 마이그레이션 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `V3__create_message_outbox_table.sql` 생성
- [ ] message_outbox 테이블 DDL 작성
  - 컬럼 정의 (id, event_type, aggregate_id, payload, status, retry_count, max_retry_count, created_at, processed_at)
  - 인덱스 생성 (idx_status_created)
- [ ] 커밋: `feat: message_outbox 테이블 마이그레이션 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] SQL 포맷팅 확인
- [ ] 커밋: `struct: message_outbox 테이블 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FlywayMigrationFixture 업데이트
- [ ] 커밋: `test: message_outbox 마이그레이션 Fixture 정리 (Tidy)`

---

### Phase 2: JPA Entity 구현 (12 사이클)

---

### 4️⃣ BaseAuditEntity 구현 (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `entity/BaseAuditEntityTest.java` 생성
- [ ] `shouldAutoSetCreatedAtAndUpdatedAt()` 테스트 작성
- [ ] 커밋: `test: BaseAuditEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `entity/BaseAuditEntity.java` 생성
- [ ] `@MappedSuperclass` 추가
- [ ] `@EntityListeners(AuditingEntityListener.class)` 추가
- [ ] createdAt, updatedAt 필드 정의 (@CreatedDate, @LastModifiedDate)
- [ ] 커밋: `feat: BaseAuditEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (모든 Entity는 BaseAuditEntity 상속)
- [ ] 커밋: `struct: BaseAuditEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `BaseAuditEntityFixture.java` 생성
- [ ] 커밋: `test: BaseAuditEntity Fixture 정리 (Tidy)`

---

### 5️⃣ FileJpaEntity - 기본 필드 구현 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `entity/FileJpaEntityTest.java` 생성
- [ ] `shouldCreateFileJpaEntity()` 테스트 작성
- [ ] 기본 필드 값 검증 (id, fileId, fileName, fileSize, mimeType, status, s3Key, s3Bucket, uploaderId)
- [ ] 커밋: `test: FileJpaEntity 기본 필드 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `entity/FileJpaEntity.java` 생성
- [ ] `@Entity`, `@Table(name = "files")` 추가
- [ ] BaseAuditEntity 상속
- [ ] 기본 필드 정의 (Plain Java, Lombok 금지)
- [ ] Getter/Setter 작성
- [ ] 커밋: `feat: FileJpaEntity 기본 필드 구현 (Green)`

#### ♻️ Refactor: 리팩토리ング
- [ ] ArchUnit 테스트 추가 (JPA 관계 어노테이션 금지, Lombok 금지)
- [ ] 커밋: `struct: FileJpaEntity 기본 필드 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileJpaEntityFixture.java` 생성 (Object Mother 패턴)
- [ ] `aFileJpaEntity()` 메서드 작성
- [ ] 커밋: `test: FileJpaEntity Fixture 정리 (Tidy)`

---

### 6️⃣ FileJpaEntity - Long FK 전략 (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `shouldNotHaveJpaRelationshipAnnotations()` 테스트 작성
- [ ] ArchUnit으로 @ManyToOne, @OneToMany 금지 검증
- [ ] 커밋: `test: FileJpaEntity Long FK 전략 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] uploaderId 필드에 `@Column(name = "uploader_id", nullable = false)` 추가
- [ ] JPA 관계 어노테이션 없음 확인
- [ ] 커밋: `feat: FileJpaEntity Long FK 전략 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 강화 (모든 Entity에 적용)
- [ ] 커밋: `struct: FileJpaEntity Long FK 전략 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileJpaEntityFixture 업데이트
- [ ] 커밋: `test: FileJpaEntity Long FK 전략 Fixture 정리 (Tidy)`

---

### 7️⃣ FileJpaEntity - Optimistic Lock (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `shouldIncrementVersionOnUpdate()` 테스트 작성
- [ ] Optimistic Lock 동작 검증
- [ ] 커밋: `test: FileJpaEntity Optimistic Lock 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] version 필드에 `@Version` 추가
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: FileJpaEntity Optimistic Lock 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (모든 Entity는 @Version 필드 필수)
- [ ] 커밋: `struct: FileJpaEntity Optimistic Lock 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileJpaEntityFixture 업데이트
- [ ] 커밋: `test: FileJpaEntity Optimistic Lock Fixture 정리 (Tidy)`

---

### 8️⃣ FileJpaEntity - Soft Delete (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `shouldSoftDeleteFile()` 테스트 작성
- [ ] deletedAt 필드 검증
- [ ] 커밋: `test: FileJpaEntity Soft Delete 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] deletedAt 필드 추가 (LocalDateTime, Nullable)
- [ ] softDelete() 메서드 추가
- [ ] 커밋: `feat: FileJpaEntity Soft Delete 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Soft Delete 로직 개선
- [ ] 커밋: `struct: FileJpaEntity Soft Delete 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileJpaEntityFixture 업데이트 (softDeleted 버전 추가)
- [ ] 커밋: `test: FileJpaEntity Soft Delete Fixture 정리 (Tidy)`

---

### 9️⃣ FileProcessingJobJpaEntity 구현 (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingJobJpaEntityTest.java` 생성
- [ ] `shouldCreateFileProcessingJobJpaEntity()` 테스트 작성
- [ ] 커밋: `test: FileProcessingJobJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `entity/FileProcessingJobJpaEntity.java` 생성
- [ ] `@Entity`, `@Table(name = "file_processing_jobs")` 추가
- [ ] 모든 필드 정의 (id, jobId, fileId, jobType, status, retryCount, maxRetryCount, inputS3Key, outputS3Key, errorMessage, createdAt, processedAt)
- [ ] Long FK 전략 (fileId는 String, 관계 어노테이션 금지)
- [ ] 커밋: `feat: FileProcessingJobJpaEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileProcessingJobJpaEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingJobJpaEntityFixture.java` 생성
- [ ] 커밋: `test: FileProcessingJobJpaEntity Fixture 정리 (Tidy)`

---

### 🔟 FileProcessingJobJpaEntity - 재시도 로직 (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `shouldIncrementRetryCount()` 테스트 작성
- [ ] `canRetry()` 메서드 테스트 작성
- [ ] 커밋: `test: FileProcessingJobJpaEntity 재시도 로직 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `incrementRetryCount()` 메서드 추가
- [ ] `canRetry()` 메서드 추가 (retryCount < maxRetryCount)
- [ ] 커밋: `feat: FileProcessingJobJpaEntity 재시도 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 재시도 로직 개선
- [ ] 커밋: `struct: FileProcessingJobJpaEntity 재시도 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileProcessingJobJpaEntityFixture 업데이트
- [ ] 커밋: `test: FileProcessingJobJpaEntity 재시도 로직 Fixture 정리 (Tidy)`

---

### 1️⃣1️⃣ MessageOutboxJpaEntity 구현 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `MessageOutboxJpaEntityTest.java` 생성
- [ ] `shouldCreateMessageOutboxJpaEntity()` 테스트 작성
- [ ] 커밋: `test: MessageOutboxJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `entity/MessageOutboxJpaEntity.java` 생성
- [ ] `@Entity`, `@Table(name = "message_outbox")` 추가
- [ ] 모든 필드 정의 (id, eventType, aggregateId, payload, status, retryCount, maxRetryCount, createdAt, processedAt)
- [ ] 커밋: `feat: MessageOutboxJpaEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: MessageOutboxJpaEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `MessageOutboxJpaEntityFixture.java` 생성
- [ ] 커밋: `test: MessageOutboxJpaEntity Fixture 정리 (Tidy)`

---

### 1️⃣2️⃣ MessageOutboxJpaEntity - 재시도 로직 (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `shouldIncrementRetryCount()` 테스트 작성
- [ ] `canRetry()` 메서드 테스트 작성
- [ ] 커밋: `test: MessageOutboxJpaEntity 재시도 로직 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `incrementRetryCount()` 메서드 추가
- [ ] `canRetry()` 메서드 추가
- [ ] 커밋: `feat: MessageOutboxJpaEntity 재시도 로직 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 재시도 로직 개선
- [ ] 커밋: `struct: MessageOutboxJpaEntity 재시도 로직 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] MessageOutboxJpaEntityFixture 업데이트
- [ ] 커밋: `test: MessageOutboxJpaEntity 재시도 로직 Fixture 정리 (Tidy)`

---

### 1️⃣3️⃣ BaseAuditEntity ArchUnit 검증 (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `architecture/EntityArchitectureTest.java` 생성
- [ ] `allEntitiesShouldExtendBaseAuditEntity()` 테스트 작성
- [ ] 커밋: `test: Entity ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성 (모든 *JpaEntity는 BaseAuditEntity 상속)
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Entity ArchUnit 검증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Entity ArchUnit 검증 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Entity ArchUnit 검증 Fixture 정리 (Tidy)`

---

### 1️⃣4️⃣ JPA 관계 어노테이션 금지 ArchUnit (Cycle 14)

#### 🔴 Red: 테스트 작성
- [ ] `noJpaRelationshipAnnotationsShouldBeUsed()` 테스트 작성
- [ ] @ManyToOne, @OneToMany, @OneToOne, @ManyToMany 금지 검증
- [ ] 커밋: `test: JPA 관계 어노테이션 금지 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: JPA 관계 어노테이션 금지 ArchUnit 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: JPA 관계 어노테이션 금지 ArchUnit 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: JPA 관계 어노테이션 금지 ArchUnit Fixture 정리 (Tidy)`

---

### 1️⃣5️⃣ Lombok 금지 ArchUnit (Cycle 15)

#### 🔴 Red: 테스트 작성
- [ ] `noLombokShouldBeUsedInEntities()` 테스트 작성
- [ ] @Data, @Getter, @Setter, @Builder 등 금지 검증
- [ ] 커밋: `test: Lombok 금지 ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Lombok 금지 ArchUnit 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Lombok 금지 ArchUnit 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Lombok 금지 ArchUnit Fixture 정리 (Tidy)`

---

### Phase 3: Mapper 구현 (12 사이클)

---

### 1️⃣6️⃣ FileMapper - toJpaEntity (Cycle 16)

#### 🔴 Red: 테스트 작성
- [ ] `mapper/FileMapperTest.java` 생성
- [ ] `shouldConvertDomainToJpaEntity()` 테스트 작성
- [ ] 모든 필드 매핑 검증
- [ ] 커밋: `test: FileMapper toJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `mapper/FileMapper.java` 생성
- [ ] `toJpaEntity(File domain)` 메서드 구현
- [ ] 모든 필드 매핑 (Plain Java, Lombok 금지)
- [ ] 커밋: `feat: FileMapper toJpaEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Null 체크 추가
- [ ] ArchUnit 테스트 추가 (Mapper는 Lombok 금지)
- [ ] 커밋: `struct: FileMapper toJpaEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileMapperFixture.java` 생성
- [ ] 커밋: `test: FileMapper toJpaEntity Fixture 정리 (Tidy)`

---

### 1️⃣7️⃣ FileMapper - toDomain (Cycle 17)

#### 🔴 Red: 테스트 작성
- [ ] `shouldConvertJpaEntityToDomain()` 테스트 작성
- [ ] 모든 필드 매핑 검증
- [ ] 커밋: `test: FileMapper toDomain 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `toDomain(FileJpaEntity entity)` 메서드 구현
- [ ] FileStatus Enum 변환
- [ ] 커밋: `feat: FileMapper toDomain 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Null 체크 추가
- [ ] 커밋: `struct: FileMapper toDomain 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileMapperFixture 업데이트
- [ ] 커밋: `test: FileMapper toDomain Fixture 정리 (Tidy)`

---

### 1️⃣8️⃣ FileMapper - List 변환 (Cycle 18)

#### 🔴 Red: 테스트 작성
- [ ] `shouldConvertListDomainToJpaEntities()` 테스트 작성
- [ ] `shouldConvertListJpaEntitiesToDomains()` 테스트 작성
- [ ] 커밋: `test: FileMapper List 변환 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `toJpaEntities(List<File> domains)` 메서드 구현
- [ ] `toDomains(List<FileJpaEntity> entities)` 메서드 구현
- [ ] 커밋: `feat: FileMapper List 변환 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Stream API 사용 개선
- [ ] 커밋: `struct: FileMapper List 변환 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileMapperFixture 업데이트
- [ ] 커밋: `test: FileMapper List 변환 Fixture 정리 (Tidy)`

---

### 1️⃣9️⃣ FileMapper - JSON 변환 (Cycle 19)

#### 🔴 Red: 테스트 작성
- [ ] `shouldConvertTagsToJson()` 테스트 작성
- [ ] `shouldConvertJsonToTags()` 테스트 작성
- [ ] 커밋: `test: FileMapper JSON 변환 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `toJson(List<String> tags)` 메서드 구현 (Jackson ObjectMapper)
- [ ] `fromJson(String tags)` 메서드 구현
- [ ] 커밋: `feat: FileMapper JSON 변환 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 예외 처리 개선 (JsonProcessingException)
- [ ] 커밋: `struct: FileMapper JSON 변환 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileMapperFixture 업데이트
- [ ] 커밋: `test: FileMapper JSON 변환 Fixture 정리 (Tidy)`

---

### 2️⃣0️⃣ FileProcessingJobMapper 구현 (Cycle 20)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingJobMapperTest.java` 생성
- [ ] `shouldConvertDomainToJpaEntity()` 테스트 작성
- [ ] `shouldConvertJpaEntityToDomain()` 테스트 작성
- [ ] 커밋: `test: FileProcessingJobMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `mapper/FileProcessingJobMapper.java` 생성
- [ ] `toJpaEntity(FileProcessingJob domain)` 메서드 구현
- [ ] `toDomain(FileProcessingJobJpaEntity entity)` 메서드 구현
- [ ] 커밋: `feat: FileProcessingJobMapper 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileProcessingJobMapper 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingJobMapperFixture.java` 생성
- [ ] 커밋: `test: FileProcessingJobMapper Fixture 정리 (Tidy)`

---

### 2️⃣1️⃣ MessageOutboxMapper 구현 (Cycle 21)

#### 🔴 Red: 테스트 작성
- [ ] `MessageOutboxMapperTest.java` 생성
- [ ] `shouldConvertDomainToJpaEntity()` 테스트 작성
- [ ] `shouldConvertJpaEntityToDomain()` 테스트 작성
- [ ] 커밋: `test: MessageOutboxMapper 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `mapper/MessageOutboxMapper.java` 생성
- [ ] `toJpaEntity(MessageOutbox domain)` 메서드 구현
- [ ] `toDomain(MessageOutboxJpaEntity entity)` 메서드 구현
- [ ] 커밋: `feat: MessageOutboxMapper 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: MessageOutboxMapper 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `MessageOutboxMapperFixture.java` 생성
- [ ] 커밋: `test: MessageOutboxMapper Fixture 정리 (Tidy)`

---

### 2️⃣2️⃣ Mapper ArchUnit 검증 (Cycle 22)

#### 🔴 Red: 테스트 작성
- [ ] `architecture/MapperArchitectureTest.java` 생성
- [ ] `mappersShouldNotUseLombok()` 테스트 작성
- [ ] 커밋: `test: Mapper ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성 (Mapper는 Lombok 금지)
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Mapper ArchUnit 검증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Mapper ArchUnit 검증 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Mapper ArchUnit 검증 Fixture 정리 (Tidy)`

---

### Phase 4: JpaRepository 구현 (12 사이클)

---

### 2️⃣3️⃣ FileJpaRepository - findByFileId (Cycle 23)

#### 🔴 Red: 테스트 작성
- [ ] `repository/FileJpaRepositoryTest.java` 생성 (@DataJpaTest, TestContainers)
- [ ] `shouldFindByFileId()` 테스트 작성
- [ ] 커밋: `test: FileJpaRepository findByFileId 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `repository/FileJpaRepository.java` 인터페이스 생성
- [ ] `JpaRepository<FileJpaEntity, Long>` 상속
- [ ] `findByFileId(String fileId): Optional<FileJpaEntity>` 메서드 추가
- [ ] 커밋: `feat: FileJpaRepository findByFileId 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Repository 규칙)
- [ ] 커밋: `struct: FileJpaRepository findByFileId 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileJpaRepositoryFixture.java` 생성
- [ ] 커밋: `test: FileJpaRepository findByFileId Fixture 정리 (Tidy)`

---

### 2️⃣4️⃣ FileProcessingJobJpaRepository - findByFileId (Cycle 24)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingJobJpaRepositoryTest.java` 생성
- [ ] `shouldFindByFileId()` 테스트 작성
- [ ] 커밋: `test: FileProcessingJobJpaRepository findByFileId 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `repository/FileProcessingJobJpaRepository.java` 인터페이스 생성
- [ ] `findByFileId(String fileId): List<FileProcessingJobJpaEntity>` 메서드 추가
- [ ] 커밋: `feat: FileProcessingJobJpaRepository findByFileId 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileProcessingJobJpaRepository findByFileId 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingJobJpaRepositoryFixture.java` 생성
- [ ] 커밋: `test: FileProcessingJobJpaRepository findByFileId Fixture 정리 (Tidy)`

---

### 2️⃣5️⃣ FileProcessingJobJpaRepository - findByJobId (Cycle 25)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFindByJobId()` 테스트 작성
- [ ] 커밋: `test: FileProcessingJobJpaRepository findByJobId 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `findByJobId(String jobId): Optional<FileProcessingJobJpaEntity>` 메서드 추가
- [ ] 커밋: `feat: FileProcessingJobJpaRepository findByJobId 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커밋: `struct: FileProcessingJobJpaRepository findByJobId 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileProcessingJobJpaRepositoryFixture 업데이트
- [ ] 커밋: `test: FileProcessingJobJpaRepository findByJobId Fixture 정리 (Tidy)`

---

### 2️⃣6️⃣ MessageOutboxJpaRepository - findPendingMessages (Cycle 26)

#### 🔴 Red: 테스트 작성
- [ ] `MessageOutboxJpaRepositoryTest.java` 생성
- [ ] `shouldFindPendingMessages()` 테스트 작성
- [ ] 커밋: `test: MessageOutboxJpaRepository findPendingMessages 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `repository/MessageOutboxJpaRepository.java` 인터페이스 생성
- [ ] `@Query` 사용하여 PENDING 상태 조회
- [ ] `findPendingMessages(LocalDateTime threshold, Pageable): List<MessageOutboxJpaEntity>` 메서드 추가
- [ ] 커밋: `feat: MessageOutboxJpaRepository findPendingMessages 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 쿼리 최적화 (인덱스 활용 확인)
- [ ] 커밋: `struct: MessageOutboxJpaRepository findPendingMessages 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `MessageOutboxJpaRepositoryFixture.java` 생성
- [ ] 커밋: `test: MessageOutboxJpaRepository findPendingMessages Fixture 정리 (Tidy)`

---

### 2️⃣7️⃣ FileQueryDslRepository 인터페이스 정의 (Cycle 27)

#### 🔴 Red: 테스트 작성
- [ ] `repository/querydsl/FileQueryDslRepositoryTest.java` 생성
- [ ] `shouldFindByUploaderIdAndStatusWithCursor()` 테스트 작성
- [ ] 커밋: `test: FileQueryDslRepository 인터페이스 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `repository/querydsl/FileQueryDslRepository.java` 인터페이스 생성
- [ ] `findByUploaderIdAndStatusAndCategoryWithCursor(...)` 메서드 시그니처 추가
- [ ] 커밋: `feat: FileQueryDslRepository 인터페이스 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 커밋: `struct: FileQueryDslRepository 인터페이스 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileQueryDslRepositoryFixture.java` 생성
- [ ] 커밋: `test: FileQueryDslRepository 인터페이스 Fixture 정리 (Tidy)`

---

### 2️⃣8️⃣ FileQueryDslRepositoryImpl - Cursor Pagination (Cycle 28)

#### 🔴 Red: 테스트 작성
- [ ] `shouldImplementCursorPagination()` 테스트 작성
- [ ] hasNext, nextCursor 검증
- [ ] 커밋: `test: FileQueryDslRepository Cursor Pagination 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `repository/querydsl/FileQueryDslRepositoryImpl.java` 생성
- [ ] QueryDSL 사용하여 Cursor Pagination 구현
- [ ] createdAt 기준 정렬, cursor < createdAt 조건
- [ ] limit(size + 1) 사용하여 hasNext 확인
- [ ] 커밋: `feat: FileQueryDslRepository Cursor Pagination 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] DTO Projection 최적화 (N+1 방지)
- [ ] 커밋: `struct: FileQueryDslRepository Cursor Pagination 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileQueryDslRepositoryFixture 업데이트
- [ ] 커밋: `test: FileQueryDslRepository Cursor Pagination Fixture 정리 (Tidy)`

---

### 2️⃣9️⃣ FileQueryDslRepositoryImpl - 필터링 (Cycle 29)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFilterByUploaderIdAndStatusAndCategory()` 테스트 작성
- [ ] 커밋: `test: FileQueryDslRepository 필터링 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] uploaderId, status, category 필터 조건 추가
- [ ] Soft Delete 제외 (deletedAt.isNull())
- [ ] 커밋: `feat: FileQueryDslRepository 필터링 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] BooleanBuilder 사용하여 동적 쿼리 개선
- [ ] 커밋: `struct: FileQueryDslRepository 필터링 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileQueryDslRepositoryFixture 업데이트
- [ ] 커밋: `test: FileQueryDslRepository 필터링 Fixture 정리 (Tidy)`

---

### 3️⃣0️⃣ FileProcessingJobQueryDslRepository 구현 (Cycle 30)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingJobQueryDslRepositoryTest.java` 생성
- [ ] `shouldFindByFileIdWithDetails()` 테스트 작성
- [ ] 커밋: `test: FileProcessingJobQueryDslRepository 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `repository/querydsl/FileProcessingJobQueryDslRepository.java` 인터페이스 생성
- [ ] `FileProcessingJobQueryDslRepositoryImpl.java` 구현
- [ ] `findByFileIdWithDetails(String fileId)` 메서드 구현
- [ ] 커밋: `feat: FileProcessingJobQueryDslRepository 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] DTO Projection 최적화
- [ ] 커밋: `struct: FileProcessingJobQueryDslRepository 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingJobQueryDslRepositoryFixture.java` 생성
- [ ] 커밋: `test: FileProcessingJobQueryDslRepository Fixture 정리 (Tidy)`

---

### 3️⃣1️⃣ QueryDSL ArchUnit 검증 (Cycle 31)

#### 🔴 Red: 테스트 작성
- [ ] `architecture/QueryDslArchitectureTest.java` 생성
- [ ] `queryDslRepositoriesShouldUseDTO()` 테스트 작성
- [ ] 커밋: `test: QueryDSL ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성 (QueryDSL Repository는 DTO Projection 사용)
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: QueryDSL ArchUnit 검증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: QueryDSL ArchUnit 검증 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: QueryDSL ArchUnit 검증 Fixture 정리 (Tidy)`

---

### Phase 5: Command Adapter 구현 (12 사이클)

---

### 3️⃣2️⃣ FileCommandAdapter - save (Cycle 32)

#### 🔴 Red: 테스트 작성
- [ ] `adapter/command/FileCommandAdapterTest.java` 생성 (@DataJpaTest)
- [ ] `shouldSaveFile()` 테스트 작성
- [ ] Mock FileMapper, FileJpaRepository 준비
- [ ] 커밋: `test: FileCommandAdapter save 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `adapter/command/FileCommandAdapter.java` 생성
- [ ] FileCommandPort 구현
- [ ] `save(File file)` 메서드 구현
  - Domain → JpaEntity 변환 (Mapper)
  - JpaRepository.save()
  - JpaEntity → Domain 변환
- [ ] 커밋: `feat: FileCommandAdapter save 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가 (Adapter는 Port 인터페이스 구현)
- [ ] 커밋: `struct: FileCommandAdapter save 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileCommandAdapterFixture.java` 생성
- [ ] 커밋: `test: FileCommandAdapter save Fixture 정리 (Tidy)`

---

### 3️⃣3️⃣ FileCommandAdapter - saveAll (Cycle 33)

#### 🔴 Red: 테스트 작성
- [ ] `shouldSaveAllFiles()` 테스트 작성
- [ ] 커밋: `test: FileCommandAdapter saveAll 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `saveAll(List<File> files)` 메서드 구현
- [ ] Mapper.toJpaEntities() + Repository.saveAll()
- [ ] 커밋: `feat: FileCommandAdapter saveAll 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Batch 처리 최적화
- [ ] 커밋: `struct: FileCommandAdapter saveAll 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileCommandAdapterFixture 업데이트
- [ ] 커밋: `test: FileCommandAdapter saveAll Fixture 정리 (Tidy)`

---

### 3️⃣4️⃣ FileCommandAdapter - updateStatus (Cycle 34)

#### 🔴 Red: 테스트 작성
- [ ] `shouldUpdateStatus()` 테스트 작성
- [ ] 커밋: `test: FileCommandAdapter updateStatus 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `updateStatus(String fileId, FileStatus status)` 메서드 구현
- [ ] Repository.findByFileId() → setStatus() → save()
- [ ] 커밋: `feat: FileCommandAdapter updateStatus 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 예외 처리 개선 (FileNotFoundException)
- [ ] 커밋: `struct: FileCommandAdapter updateStatus 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileCommandAdapterFixture 업데이트
- [ ] 커밋: `test: FileCommandAdapter updateStatus Fixture 정리 (Tidy)`

---

### 3️⃣5️⃣ FileCommandAdapter - softDelete (Cycle 35)

#### 🔴 Red: 테스트 작성
- [ ] `shouldSoftDeleteFile()` 테스트 작성
- [ ] 커밋: `test: FileCommandAdapter softDelete 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `softDelete(String fileId)` 메서드 구현
- [ ] Repository.findByFileId() → softDelete() → save()
- [ ] 커밋: `feat: FileCommandAdapter softDelete 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 예외 처리 개선
- [ ] 커밋: `struct: FileCommandAdapter softDelete 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileCommandAdapterFixture 업데이트
- [ ] 커밋: `test: FileCommandAdapter softDelete Fixture 정리 (Tidy)`

---

### 3️⃣6️⃣ FileProcessingJobCommandAdapter 구현 (Cycle 36)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingJobCommandAdapterTest.java` 생성
- [ ] `shouldSaveFileProcessingJob()` 테스트 작성
- [ ] 커밋: `test: FileProcessingJobCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `adapter/command/FileProcessingJobCommandAdapter.java` 생성
- [ ] FileProcessingJobCommandPort 구현
- [ ] `save()`, `saveAll()`, `updateStatus()` 메서드 구현
- [ ] 커밋: `feat: FileProcessingJobCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileProcessingJobCommandAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingJobCommandAdapterFixture.java` 생성
- [ ] 커밋: `test: FileProcessingJobCommandAdapter Fixture 정리 (Tidy)`

---

### 3️⃣7️⃣ MessageOutboxCommandAdapter 구현 (Cycle 37)

#### 🔴 Red: 테스트 작성
- [ ] `MessageOutboxCommandAdapterTest.java` 생성
- [ ] `shouldSaveMessageOutbox()` 테스트 작성
- [ ] 커밋: `test: MessageOutboxCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `adapter/command/MessageOutboxCommandAdapter.java` 생성
- [ ] MessageOutboxCommandPort 구현
- [ ] `save()` 메서드 구현
- [ ] 커밋: `feat: MessageOutboxCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: MessageOutboxCommandAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `MessageOutboxCommandAdapterFixture.java` 생성
- [ ] 커밋: `test: MessageOutboxCommandAdapter Fixture 정리 (Tidy)`

---

### Phase 6: Query Adapter 구현 (12 사이클)

---

### 3️⃣8️⃣ FileQueryAdapter - findById (Cycle 38)

#### 🔴 Red: 테스트 작성
- [ ] `adapter/query/FileQueryAdapterTest.java` 생성 (@DataJpaTest)
- [ ] `shouldFindById()` 테스트 작성
- [ ] 커밋: `test: FileQueryAdapter findById 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `adapter/query/FileQueryAdapter.java` 생성
- [ ] FileQueryPort 구현
- [ ] `findById(String fileId)` 메서드 구현
  - Repository.findByFileId()
  - JpaEntity → Domain 변환
- [ ] 커밋: `feat: FileQueryAdapter findById 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileQueryAdapter findById 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileQueryAdapterFixture.java` 생성
- [ ] 커밋: `test: FileQueryAdapter findById Fixture 정리 (Tidy)`

---

### 3️⃣9️⃣ FileQueryAdapter - findByIdWithLock (Cycle 39)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFindByIdWithOptimisticLock()` 테스트 작성
- [ ] 커밋: `test: FileQueryAdapter findByIdWithLock 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `findByIdWithLock(String fileId)` 메서드 구현
- [ ] @Lock(LockModeType.OPTIMISTIC) 사용
- [ ] 커밋: `feat: FileQueryAdapter findByIdWithLock 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Optimistic Lock 동작 확인
- [ ] 커밋: `struct: FileQueryAdapter findByIdWithLock 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileQueryAdapterFixture 업데이트
- [ ] 커밋: `test: FileQueryAdapter findByIdWithLock Fixture 정리 (Tidy)`

---

### 4️⃣0️⃣ FileQueryAdapter - findByUploaderIdAndStatusWithCursor (Cycle 40)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFindByUploaderIdAndStatusWithCursor()` 테스트 작성
- [ ] Cursor Pagination 검증 (hasNext, nextCursor)
- [ ] 커밋: `test: FileQueryAdapter Cursor Pagination 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `findByUploaderIdAndStatusWithCursor(...)` 메서드 구현
- [ ] QueryDslRepository 호출
- [ ] CursorPageResponse 반환
- [ ] 커밋: `feat: FileQueryAdapter Cursor Pagination 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] DTO Projection 최적화 확인
- [ ] 커밋: `struct: FileQueryAdapter Cursor Pagination 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] FileQueryAdapterFixture 업데이트
- [ ] 커밋: `test: FileQueryAdapter Cursor Pagination Fixture 정리 (Tidy)`

---

### 4️⃣1️⃣ FileProcessingJobQueryAdapter 구현 (Cycle 41)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingJobQueryAdapterTest.java` 생성
- [ ] `shouldFindByFileId()` 테스트 작성
- [ ] `shouldFindById()` 테스트 작성
- [ ] 커밋: `test: FileProcessingJobQueryAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `adapter/query/FileProcessingJobQueryAdapter.java` 생성
- [ ] FileProcessingJobQueryPort 구현
- [ ] `findByFileId()`, `findById()` 메서드 구현
- [ ] 커밋: `feat: FileProcessingJobQueryAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: FileProcessingJobQueryAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileProcessingJobQueryAdapterFixture.java` 생성
- [ ] 커밋: `test: FileProcessingJobQueryAdapter Fixture 정리 (Tidy)`

---

### 4️⃣2️⃣ MessageOutboxQueryAdapter 구현 (Cycle 42)

#### 🔴 Red: 테스트 작성
- [ ] `MessageOutboxQueryAdapterTest.java` 생성
- [ ] `shouldFindPendingMessages()` 테스트 작성
- [ ] 커밋: `test: MessageOutboxQueryAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `adapter/query/MessageOutboxQueryAdapter.java` 생성
- [ ] MessageOutboxQueryPort 구현
- [ ] `findPendingMessages(int limit)` 메서드 구현
- [ ] 커밋: `feat: MessageOutboxQueryAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 테스트 추가
- [ ] 커밋: `struct: MessageOutboxQueryAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `MessageOutboxQueryAdapterFixture.java` 생성
- [ ] 커밋: `test: MessageOutboxQueryAdapter Fixture 정리 (Tidy)`

---

### Phase 7: ArchUnit 전체 검증 (8 사이클)

---

### 4️⃣3️⃣ Adapter Port 구현 검증 (Cycle 43)

#### 🔴 Red: 테스트 작성
- [ ] `architecture/AdapterArchitectureTest.java` 생성
- [ ] `adaptersShouldImplementPort()` 테스트 작성
- [ ] 커밋: `test: Adapter Port 구현 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성 (Adapter는 Port 인터페이스 구현)
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Adapter Port 구현 검증 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Adapter Port 구현 검증 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Adapter Port 구현 검증 Fixture 정리 (Tidy)`

---

### 4️⃣4️⃣ Persistence Layer 의존성 규칙 (Cycle 44)

#### 🔴 Red: 테스트 작성
- [ ] `persistenceLayerShouldNotDependOnApplication()` 테스트 작성
- [ ] Persistence Layer는 Application/REST API에 의존 금지 검증
- [ ] 커밋: `test: Persistence Layer 의존성 규칙 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성 (LayeredArchitecture)
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Persistence Layer 의존성 규칙 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Persistence Layer 의존성 규칙 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Persistence Layer 의존성 규칙 Fixture 정리 (Tidy)`

---

### 4️⃣5️⃣ Repository 네이밍 규칙 (Cycle 45)

#### 🔴 Red: 테스트 작성
- [ ] `repositoriesShouldFollowNamingConvention()` 테스트 작성
- [ ] JpaRepository는 *JpaRepository 네이밍
- [ ] QueryDsl Repository는 *QueryDslRepository 네이밍
- [ ] 커밋: `test: Repository 네이밍 규칙 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Repository 네이밍 규칙 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Repository 네이밍 규칙 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Repository 네이밍 규칙 Fixture 정리 (Tidy)`

---

### 4️⃣6️⃣ Mapper 네이밍 규칙 (Cycle 46)

#### 🔴 Red: 테스트 작성
- [ ] `mappersShouldFollowNamingConvention()` 테스트 작성
- [ ] Mapper는 *Mapper 네이밍
- [ ] 커밋: `test: Mapper 네이밍 규칙 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 규칙 작성
- [ ] 테스트 통과 확인
- [ ] 커밋: `feat: Mapper 네이밍 규칙 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 규칙 설명 추가
- [ ] 커밋: `struct: Mapper 네이밍 규칙 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Mapper 네이밍 규칙 Fixture 정리 (Tidy)`

---

### Phase 8: Integration Test (8 사이클)

---

### 4️⃣7️⃣ FileCommandAdapter Integration Test (Cycle 47)

#### 🔴 Red: 테스트 작성
- [ ] `integration/FileCommandAdapterIntegrationTest.java` 생성 (@SpringBootTest, TestContainers)
- [ ] `shouldSaveAndRetrieveFile()` E2E 테스트 작성
- [ ] 커밋: `test: FileCommandAdapter Integration 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] TestContainers MySQL 설정
- [ ] Flyway 마이그레이션 자동 실행
- [ ] save() → findById() E2E 검증
- [ ] 커밋: `feat: FileCommandAdapter Integration 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 격리 확인 (@DirtiesContext)
- [ ] 커밋: `struct: FileCommandAdapter Integration 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileCommandAdapterIntegrationFixture.java` 생성
- [ ] 커밋: `test: FileCommandAdapter Integration Fixture 정리 (Tidy)`

---

### 4️⃣8️⃣ FileQueryAdapter Cursor Pagination Integration Test (Cycle 48)

#### 🔴 Red: 테스트 작성
- [ ] `FileQueryAdapterCursorPaginationIntegrationTest.java` 생성
- [ ] `shouldPaginateWithCursor()` E2E 테스트 작성
- [ ] hasNext, nextCursor 검증
- [ ] 커밋: `test: Cursor Pagination Integration 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 10개 파일 생성 → Cursor Pagination 조회 (size=3)
- [ ] 첫 페이지, 두 번째 페이지, 세 번째 페이지 검증
- [ ] 커밋: `feat: Cursor Pagination Integration 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 격리 확인
- [ ] 커밋: `struct: Cursor Pagination Integration 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Cursor Pagination Integration Fixture 정리 (Tidy)`

---

### 4️⃣9️⃣ FileProcessingJobAdapter Integration Test (Cycle 49)

#### 🔴 Red: 테스트 작성
- [ ] `FileProcessingJobAdapterIntegrationTest.java` 생성
- [ ] `shouldSaveAndRetrieveFileProcessingJob()` E2E 테스트 작성
- [ ] 커밋: `test: FileProcessingJobAdapter Integration 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] save() → findByFileId() E2E 검증
- [ ] 커밋: `feat: FileProcessingJobAdapter Integration 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 격리 확인
- [ ] 커밋: `struct: FileProcessingJobAdapter Integration 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: FileProcessingJobAdapter Integration Fixture 정리 (Tidy)`

---

### 5️⃣0️⃣ MessageOutboxAdapter Integration Test (Cycle 50)

#### 🔴 Red: 테스트 작성
- [ ] `MessageOutboxAdapterIntegrationTest.java` 생성
- [ ] `shouldFindPendingMessages()` E2E 테스트 작성
- [ ] 커밋: `test: MessageOutboxAdapter Integration 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] save() → findPendingMessages() E2E 검증
- [ ] 커밋: `feat: MessageOutboxAdapter Integration 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 격리 확인
- [ ] 커밋: `struct: MessageOutboxAdapter Integration 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: MessageOutboxAdapter Integration Fixture 정리 (Tidy)`

---

### 5️⃣1️⃣ Flyway Migration 롤백 테스트 (Cycle 51)

#### 🔴 Red: 테스트 작성
- [ ] `FlywayMigrationRollbackTest.java` 생성
- [ ] `shouldRollbackMigrationOnError()` 테스트 작성
- [ ] 커밋: `test: Flyway Migration 롤백 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 잘못된 마이그레이션 SQL 실행 → 롤백 확인
- [ ] 커밋: `feat: Flyway Migration 롤백 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 격리 확인
- [ ] 커밋: `struct: Flyway Migration 롤백 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 커밋: `test: Flyway Migration 롤백 Fixture 정리 (Tidy)`

---

### 5️⃣2️⃣ 테스트 커버리지 검증 (Cycle 52)

#### 🔴 Red: 테스트 작성
- [ ] JaCoCo 플러그인 설정
- [ ] `shouldHaveTestCoverageAbove80Percent()` 테스트 작성
- [ ] 커밋: `test: 테스트 커버리지 검증 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 커버리지 > 80% 확인
- [ ] 커밋: `feat: 테스트 커버리지 검증 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 커버리지 리포트 생성 설정
- [ ] 커밋: `struct: 테스트 커버리지 검증 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 모든 Fixture 최종 검토
- [ ] 커밋: `test: 모든 Fixture 최종 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (52 사이클, 체크박스 모두 ✅)
- [ ] 3개 JPA Entity 구현 완료
- [ ] 3개 JpaRepository 구현 완료
- [ ] 2개 QueryDSL Repository 구현 완료
- [ ] 3개 Command Adapter 구현 완료
- [ ] 3개 Query Adapter 구현 완료
- [ ] 3개 Mapper 구현 완료
- [ ] 3개 Flyway Migration SQL 작성
- [ ] Integration Test (TestContainers) 통과
- [ ] ArchUnit 테스트 통과 (Long FK 전략, Lombok 금지, Port 구현)
- [ ] 테스트 커버리지 > 80%
- [ ] Zero-Tolerance 규칙 준수
- [ ] TestFixture 모두 정리 (Object Mother 패턴)
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **Task**: docs/prd/tasks/FILE-003.md
- **PRD**: docs/prd/file-management-system.md
- **컨벤션**: docs/coding_convention/04-persistence-layer/

---

## 📊 사이클 요약

| Phase | 사이클 수 | 예상 소요 시간 |
|-------|----------|---------------|
| Phase 1: Flyway Migration | 3 | 45분 |
| Phase 2: JPA Entity 구현 | 12 | 180분 |
| Phase 3: Mapper 구현 | 7 | 105분 |
| Phase 4: JpaRepository 구현 | 9 | 135분 |
| Phase 5: Command Adapter 구현 | 6 | 90분 |
| Phase 6: Query Adapter 구현 | 5 | 75분 |
| Phase 7: ArchUnit 전체 검증 | 4 | 60분 |
| Phase 8: Integration Test | 6 | 90분 |
| **합계** | **52** | **780분 (13시간)** |

---

## 🎯 핵심 원칙

1. **작은 단위**: 각 사이클은 5-15분 내 완료
2. **4단계 필수**: Red → Green → Refactor → Tidy 모두 수행
3. **TestFixture 필수**: Tidy 단계에서 Object Mother 패턴 적용
4. **Zero-Tolerance**: Long FK 전략, Lombok 금지, QueryDSL 최적화 엄격 준수
5. **체크박스 추적**: `/kb/persistence/go` 명령이 Plan 파일을 읽고 진행 상황 추적
6. **Long FK 전략**: JPA 관계 어노테이션 절대 금지
7. **QueryDSL DTO Projection**: N+1 방지 필수
8. **ArchUnit 검증**: 각 Refactor 단계에서 ArchUnit 규칙 검증 필수
9. **TestContainers**: Integration Test는 실제 MySQL 사용

---

## 🚀 다음 단계

```bash
# Plan 파일 생성 완료
/kb/persistence/go

# 또는 개별 Phase 실행
/kb/persistence/red      # Red Phase만
/kb/persistence/green    # Green Phase만
/kb/persistence/refactor # Refactor Phase만
```
