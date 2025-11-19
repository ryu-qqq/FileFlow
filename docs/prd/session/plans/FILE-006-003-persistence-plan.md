# FILE-006-003 TDD Plan

**Task**: Persistence Layer - 파일 메타데이터 영속화 및 Redis 세션 저장소 구현
**Layer**: Persistence Layer
**브랜치**: feature/FILE-006-003-persistence
**예상 소요 시간**: 375분 (25 사이클 × 15분)

---

## 📝 TDD 사이클 체크리스트

### 1️⃣ Flyway 마이그레이션 스크립트 작성 (Cycle 1)

#### 🔴 Red: 테스트 작성
- [ ] Flyway 마이그레이션 스크립트는 테스트 불필요
- [ ] 마이그레이션 검증은 Integration Test에서 수행
- [ ] 커밋: `test: Flyway 마이그레이션 검증 준비 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `V1__create_files_table.sql` 파일 생성
- [ ] files 테이블 정의 (15개 필드)
- [ ] 인덱스 3개 정의 (idx_file_id, idx_user_uploaded, idx_s3_path)
- [ ] 제약사항 추가 (file_id UNIQUE, file_size > 0, version >= 0)
- [ ] 커밋: `impl: Flyway 마이그레이션 스크립트 작성 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테이블 스키마 최적화
- [ ] 인덱스 최적화
- [ ] 커밋: `refactor: Flyway 마이그레이션 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: Flyway 마이그레이션 정리 (Tidy)`

---

### 2️⃣ FileJpaEntity 구현 (Cycle 2)

#### 🔴 Red: 테스트 작성
- [ ] `FileJpaEntityTest.java` 생성
- [ ] `shouldCreateEntity()` 테스트 작성
- [ ] 필드 검증 테스트 작성
- [ ] 커밋: `test: FileJpaEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileJpaEntity.java` 생성 (Plain Java)
- [ ] @Entity, @Table 어노테이션 추가
- [ ] 15개 필드 정의
- [ ] @Id, @GeneratedValue 추가
- [ ] @Version 추가 (Optimistic Lock)
- [ ] Long FK 전략 (관계 어노테이션 금지)
- [ ] Getter/Setter 작성 (Lombok 금지)
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileJpaEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 인덱스 어노테이션 추가 (@Table(indexes = ...))
- [ ] JPA Entity ArchUnit 테스트 통과
- [ ] 커밋: `refactor: FileJpaEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `FileJpaEntityFixture.java` 생성
- [ ] `FileJpaEntityFixture.create()` 메서드 작성
- [ ] 다양한 상태의 Fixture 메서드 추가
- [ ] 커밋: `test: FileJpaEntityFixture 정리 (Tidy)`

---

### 3️⃣ FileMapper 구현 (Part 1: toEntity) (Cycle 3)

#### 🔴 Red: 테스트 작성
- [ ] `FileMapperTest.java` 생성
- [ ] `shouldConvertDomainToEntity()` 테스트 작성
- [ ] Law of Demeter 준수 검증 (getFileIdValue() 사용)
- [ ] VO → 원시 타입 변환 검증
- [ ] 커밋: `test: FileMapper toEntity 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileMapper.java` 생성 (Static 메서드)
- [ ] `toEntity(File)` 메서드 구현
- [ ] Law of Demeter 준수 (file.getFileIdValue() 사용)
- [ ] VO → 원시 타입 변환 로직
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileMapper toEntity 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 변환 로직 명확화
- [ ] ArchUnit Mapper 테스트 통과
- [ ] 커밋: `refactor: FileMapper toEntity 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (Static Mapper)
- [ ] 커밋: `test: FileMapper toEntity 테스트 정리 (Tidy)`

---

### 4️⃣ FileMapper 구현 (Part 2: toDomain) (Cycle 4)

#### 🔴 Red: 테스트 작성
- [ ] `shouldConvertEntityToDomain()` 테스트 작성
- [ ] File.reconstitute() 호출 검증
- [ ] 원시 타입 → VO 변환 검증
- [ ] Clock 주입 검증
- [ ] 커밋: `test: FileMapper toDomain 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `toDomain(FileJpaEntity)` 메서드 구현
- [ ] 원시 타입 → VO 변환 로직
- [ ] File.reconstitute() 호출
- [ ] Clock.systemDefaultZone() 주입
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileMapper toDomain 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] S3Path 추출 로직 메서드 분리
- [ ] extractSellerName() 메서드 추가
- [ ] extractCustomPath() 메서드 추가
- [ ] Javadoc 추가
- [ ] 커밋: `refactor: FileMapper toDomain 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: FileMapper toDomain 테스트 정리 (Tidy)`

---

### 5️⃣ FileJpaRepository 구현 (Cycle 5)

#### 🔴 Red: 테스트 작성
- [ ] `FileJpaRepositoryTest.java` 생성 (@DataJpaTest)
- [ ] `shouldSaveFile()` 테스트 작성
- [ ] `shouldFindById()` 테스트 작성
- [ ] TestContainers MySQL 설정
- [ ] 커밋: `test: FileJpaRepository 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileJpaRepository.java` 생성 (인터페이스)
- [ ] JpaRepository<FileJpaEntity, Long> 상속
- [ ] Query Method 금지 (jpa-repository-guide.md)
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileJpaRepository 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] JPA Repository ArchUnit 테스트 통과
- [ ] 커밋: `refactor: FileJpaRepository 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (Repository는 Entity Fixture 사용)
- [ ] 커밋: `test: FileJpaRepository 테스트 정리 (Tidy)`

---

### 6️⃣ FileJpaRepository 통합 테스트 (Unique Constraint) (Cycle 6)

#### 🔴 Red: 테스트 작성
- [ ] `shouldThrowExceptionWhenDuplicateFileId()` 테스트 작성
- [ ] Unique Constraint 검증
- [ ] 커밋: `test: FileJpaRepository Unique 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 테스트 통과 (스키마에 UNIQUE 제약사항 이미 추가됨)
- [ ] 커밋: `impl: FileJpaRepository Unique 제약사항 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 코드 개선
- [ ] 커밋: `refactor: FileJpaRepository Unique 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: FileJpaRepository Unique 테스트 정리 (Tidy)`

---

### 7️⃣ FileQueryDslRepository 구현 (Part 1: findById) (Cycle 7)

#### 🔴 Red: 테스트 작성
- [ ] `FileQueryDslRepositoryTest.java` 생성 (@DataJpaTest)
- [ ] `shouldFindById()` 테스트 작성
- [ ] `shouldReturnEmptyWhenFileIdNotFound()` 테스트 작성
- [ ] `shouldReturnEmptyWhenDeleted()` 테스트 작성
- [ ] 커밋: `test: FileQueryDslRepository findById 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileQueryDslRepositoryImpl.java` 생성
- [ ] `findById(String fileId)` 메서드 구현
- [ ] QueryDSL 사용
- [ ] deleted = false 조건 추가
- [ ] FileMapper.toDomain() 사용
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileQueryDslRepository findById 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] QueryDSL Repository ArchUnit 테스트 통과
- [ ] 커밋: `refactor: FileQueryDslRepository findById 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: FileQueryDslRepository findById 테스트 정리 (Tidy)`

---

### 8️⃣ FileQueryDslRepository 구현 (Part 2: findAllByUserId) (Cycle 8)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFindAllByUserId()` 테스트 작성
- [ ] Pagination 검증 (offset, limit)
- [ ] 정렬 검증 (uploadedAt DESC)
- [ ] deleted = false 조건 검증
- [ ] 커밋: `test: FileQueryDslRepository findAllByUserId 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `findAllByUserId(Long userId, Pageable pageable)` 메서드 구현
- [ ] QueryDSL 사용
- [ ] Pagination 로직 (offset, limit)
- [ ] 정렬 로직 (uploadedAt DESC)
- [ ] FileMapper.toDomain() 사용
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileQueryDslRepository findAllByUserId 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Pagination 로직 최적화
- [ ] 커밋: `refactor: FileQueryDslRepository findAllByUserId 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: FileQueryDslRepository findAllByUserId 테스트 정리 (Tidy)`

---

### 9️⃣ FileQueryDslRepository 구현 (Part 3: countByUserId) (Cycle 9)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCountByUserId()` 테스트 작성
- [ ] deleted = false 조건 검증
- [ ] 커밋: `test: FileQueryDslRepository countByUserId 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `countByUserId(Long userId)` 메서드 구현
- [ ] QueryDSL count() 사용
- [ ] deleted = false 조건 추가
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileQueryDslRepository countByUserId 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 커밋: `refactor: FileQueryDslRepository countByUserId 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: FileQueryDslRepository countByUserId 테스트 정리 (Tidy)`

---

### 🔟 FileQueryDslRepository 구현 (Part 4: existsByFileId) (Cycle 10)

#### 🔴 Red: 테스트 작성
- [ ] `shouldCheckExistsByFileId()` 테스트 작성
- [ ] 존재하는 경우 true 반환 검증
- [ ] 존재하지 않는 경우 false 반환 검증
- [ ] deleted = true인 경우 false 반환 검증
- [ ] 커밋: `test: FileQueryDslRepository existsByFileId 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `existsByFileId(String fileId)` 메서드 구현
- [ ] QueryDSL selectOne() + fetchFirst() 사용
- [ ] deleted = false 조건 추가
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileQueryDslRepository existsByFileId 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 커밋: `refactor: FileQueryDslRepository existsByFileId 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: FileQueryDslRepository existsByFileId 테스트 정리 (Tidy)`

---

### 1️⃣1️⃣ FileCommandAdapter 구현 (Cycle 11)

#### 🔴 Red: 테스트 작성
- [ ] `FileCommandAdapterTest.java` 생성
- [ ] `shouldSaveFile()` 테스트 작성
- [ ] FileMapper 사용 검증
- [ ] 커밋: `test: FileCommandAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileCommandAdapter.java` 생성 (@Component)
- [ ] SaveFilePort 구현
- [ ] FileJpaRepository 주입
- [ ] `save(File)` 메서드 구현
- [ ] FileMapper.toEntity() 사용
- [ ] FileMapper.toDomain() 사용
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileCommandAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Command Adapter ArchUnit 테스트 통과
- [ ] 커밋: `refactor: FileCommandAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (Adapter는 Mock 사용)
- [ ] 커밋: `test: FileCommandAdapter 테스트 정리 (Tidy)`

---

### 1️⃣2️⃣ FileCommandAdapter 통합 테스트 (Optimistic Lock) (Cycle 12)

#### 🔴 Red: 테스트 작성
- [ ] `shouldThrowOptimisticLockException_whenConcurrentUpdate()` 테스트 작성
- [ ] 동시 업데이트 시나리오 작성
- [ ] OptimisticLockException 검증
- [ ] 커밋: `test: FileCommandAdapter Optimistic Lock 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 테스트 통과 (@Version 어노테이션으로 자동 처리)
- [ ] 커밋: `impl: FileCommandAdapter Optimistic Lock 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 코드 개선
- [ ] 멀티스레드 테스트 안정성 개선
- [ ] 커밋: `refactor: FileCommandAdapter Optimistic Lock 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: FileCommandAdapter Optimistic Lock 테스트 정리 (Tidy)`

---

### 1️⃣3️⃣ FileQueryAdapter 구현 (Cycle 13)

#### 🔴 Red: 테스트 작성
- [ ] `FileQueryAdapterTest.java` 생성
- [ ] `shouldFindByIdAndNotDeleted()` 테스트 작성
- [ ] `shouldFindByUserId()` 테스트 작성
- [ ] 커밋: `test: FileQueryAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `FileQueryAdapter.java` 생성 (@Component)
- [ ] LoadFilePort 구현
- [ ] FileQueryDslRepository 주입
- [ ] `findByIdAndNotDeleted(String fileId)` 메서드 구현
- [ ] `findByUserId(Long userId, Pageable pageable)` 메서드 구현
- [ ] 테스트 통과
- [ ] 커밋: `impl: FileQueryAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Query Adapter ArchUnit 테스트 통과
- [ ] 커밋: `refactor: FileQueryAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: FileQueryAdapter 테스트 정리 (Tidy)`

---

### 1️⃣4️⃣ RedisUploadSession DTO 구현 (Cycle 14)

#### 🔴 Red: 테스트 작성
- [ ] `RedisUploadSessionTest.java` 생성
- [ ] `shouldCreateRedisUploadSession()` 테스트 작성
- [ ] Record 필드 검증
- [ ] 커밋: `test: RedisUploadSession DTO 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RedisUploadSession.java` 생성 (Record)
- [ ] 16개 필드 정의
- [ ] 테스트 통과
- [ ] 커밋: `impl: RedisUploadSession DTO 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit DTO 테스트 통과 (Record 사용)
- [ ] 커밋: `refactor: RedisUploadSession DTO 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] `RedisUploadSessionFixture.java` 생성
- [ ] `RedisUploadSessionFixture.create()` 메서드 작성
- [ ] 커밋: `test: RedisUploadSessionFixture 정리 (Tidy)`

---

### 1️⃣5️⃣ RedisSessionMapper 구현 (Part 1: toRedis) (Cycle 15)

#### 🔴 Red: 테스트 작성
- [ ] `RedisSessionMapperTest.java` 생성
- [ ] `shouldConvertDomainToRedis()` 테스트 작성
- [ ] VO → 원시 타입 변환 검증
- [ ] 커밋: `test: RedisSessionMapper toRedis 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RedisSessionMapper.java` 생성 (Static 메서드)
- [ ] `toRedis(UploadSession)` 메서드 구현
- [ ] VO → 원시 타입 변환 로직
- [ ] 테스트 통과
- [ ] 커밋: `impl: RedisSessionMapper toRedis 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] ArchUnit Mapper 테스트 통과
- [ ] 커밋: `refactor: RedisSessionMapper toRedis 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (Static Mapper)
- [ ] 커밋: `test: RedisSessionMapper toRedis 테스트 정리 (Tidy)`

---

### 1️⃣6️⃣ RedisSessionMapper 구현 (Part 2: toDomain) (Cycle 16)

#### 🔴 Red: 테스트 작성
- [ ] `shouldConvertRedisToDomain()` 테스트 작성
- [ ] UploadSession.restore() 호출 검증
- [ ] 원시 타입 → VO 변환 검증
- [ ] 커밋: `test: RedisSessionMapper toDomain 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `toDomain(RedisUploadSession)` 메서드 구현
- [ ] 원시 타입 → VO 변환 로직
- [ ] UploadSession.restore() 호출
- [ ] 테스트 통과
- [ ] 커밋: `impl: RedisSessionMapper toDomain 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 변환 로직 명확화
- [ ] 커밋: `refactor: RedisSessionMapper toDomain 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: RedisSessionMapper toDomain 테스트 정리 (Tidy)`

---

### 1️⃣7️⃣ RedisConfig 구현 (Cycle 17)

#### 🔴 Red: 테스트 작성
- [ ] `RedisConfigTest.java` 생성
- [ ] RedisTemplate Bean 생성 검증
- [ ] RedisMessageListenerContainer Bean 생성 검증
- [ ] 커밋: `test: RedisConfig 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RedisConfig.java` 생성 (@Configuration)
- [ ] @EnableRedisRepositories 추가
- [ ] RedisTemplate Bean 정의
- [ ] StringRedisSerializer 설정
- [ ] RedisMessageListenerContainer Bean 정의
- [ ] 테스트 통과
- [ ] 커밋: `impl: RedisConfig 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Serializer 설정 최적화
- [ ] 커밋: `refactor: RedisConfig 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: RedisConfig 테스트 정리 (Tidy)`

---

### 1️⃣8️⃣ RedisUploadSessionRepository 구현 (Part 1: save) (Cycle 18)

#### 🔴 Red: 테스트 작성
- [ ] `RedisUploadSessionRepositoryTest.java` 생성
- [ ] `shouldSaveSessionWithTTL()` 테스트 작성
- [ ] Embedded Redis 설정
- [ ] TTL 검증
- [ ] 커밋: `test: RedisUploadSessionRepository save 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `RedisUploadSessionRepositoryImpl.java` 생성 (@Repository)
- [ ] RedisUploadSessionRepository 구현
- [ ] RedisTemplate 주입
- [ ] `save(RedisUploadSession, Duration)` 메서드 구현
- [ ] JSON Serialization (Jackson)
- [ ] 테스트 통과
- [ ] 커밋: `impl: RedisUploadSessionRepository save 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] KEY_PREFIX 상수화 ("upload:session:")
- [ ] 커밋: `refactor: RedisUploadSessionRepository save 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (Repository는 DTO Fixture 사용)
- [ ] 커밋: `test: RedisUploadSessionRepository save 테스트 정리 (Tidy)`

---

### 1️⃣9️⃣ RedisUploadSessionRepository 구현 (Part 2: findById) (Cycle 19)

#### 🔴 Red: 테스트 작성
- [ ] `shouldFindById()` 테스트 작성
- [ ] `shouldReturnEmptyWhenNotFound()` 테스트 작성
- [ ] JSON Deserialization 검증
- [ ] 커밋: `test: RedisUploadSessionRepository findById 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `findById(String sessionId)` 메서드 구현
- [ ] RedisTemplate.opsForValue().get() 사용
- [ ] JSON Deserialization (Jackson)
- [ ] 테스트 통과
- [ ] 커밋: `impl: RedisUploadSessionRepository findById 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 예외 처리 개선
- [ ] 커밋: `refactor: RedisUploadSessionRepository findById 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: RedisUploadSessionRepository findById 테스트 정리 (Tidy)`

---

### 2️⃣0️⃣ RedisUploadSessionRepository 구현 (Part 3: deleteById) (Cycle 20)

#### 🔴 Red: 테스트 작성
- [ ] `shouldDeleteById()` 테스트 작성
- [ ] 삭제 후 조회 시 Empty 반환 검증
- [ ] 커밋: `test: RedisUploadSessionRepository deleteById 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `deleteById(String sessionId)` 메서드 구현
- [ ] RedisTemplate.delete() 사용
- [ ] 테스트 통과
- [ ] 커밋: `impl: RedisUploadSessionRepository deleteById 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] 커밋: `refactor: RedisUploadSessionRepository deleteById 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: RedisUploadSessionRepository deleteById 테스트 정리 (Tidy)`

---

### 2️⃣1️⃣ RedisUploadSessionRepository 통합 테스트 (TTL) (Cycle 21)

#### 🔴 Red: 테스트 작성
- [ ] `shouldExpireAfterTTL()` 테스트 작성
- [ ] TTL 만료 후 조회 시 Empty 반환 검증
- [ ] 커밋: `test: RedisUploadSessionRepository TTL 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 테스트 통과 (RedisTemplate.set(key, value, ttl)로 자동 처리)
- [ ] 커밋: `impl: RedisUploadSessionRepository TTL 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 코드 개선
- [ ] Thread.sleep() 대신 Awaitility 사용 고려
- [ ] 커밋: `refactor: RedisUploadSessionRepository TTL 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: RedisUploadSessionRepository TTL 테스트 정리 (Tidy)`

---

### 2️⃣2️⃣ RedisUploadSessionRepository 통합 테스트 (Keyspace Notification) (Cycle 22)

#### 🔴 Red: 테스트 작성
- [ ] `shouldReceiveExpiredEvent_whenSessionExpires()` 테스트 작성
- [ ] RedisMessageListenerContainer 사용
- [ ] PatternTopic("__keyevent@0__:expired") 구독
- [ ] CountDownLatch로 이벤트 수신 검증
- [ ] 커밋: `test: Redis Keyspace Notification 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 테스트 통과
- [ ] RedisConfig에서 RedisMessageListenerContainer Bean 정의 완료
- [ ] 커밋: `impl: Redis Keyspace Notification 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 테스트 코드 개선
- [ ] Timeout 설정 최적화
- [ ] 커밋: `refactor: Redis Keyspace Notification 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: Redis Keyspace Notification 테스트 정리 (Tidy)`

---

### 2️⃣3️⃣ UploadSessionRedisAdapter 구현 (Cycle 23)

#### 🔴 Red: 테스트 작성
- [ ] `UploadSessionRedisAdapterTest.java` 생성
- [ ] `shouldFindById()` 테스트 작성
- [ ] `shouldSave()` 테스트 작성
- [ ] `shouldDelete()` 테스트 작성
- [ ] RedisSessionMapper 사용 검증
- [ ] 커밋: `test: UploadSessionRedisAdapter 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] `UploadSessionRedisAdapter.java` 생성 (@Component)
- [ ] LoadUploadSessionPort, SaveUploadSessionPort, DeleteUploadSessionPort 구현
- [ ] RedisUploadSessionRepository 주입
- [ ] `findById(String sessionId)` 메서드 구현
- [ ] `save(UploadSession, Duration)` 메서드 구현
- [ ] `delete(String sessionId)` 메서드 구현
- [ ] RedisSessionMapper 사용
- [ ] 테스트 통과
- [ ] 커밋: `impl: UploadSessionRedisAdapter 구현 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] Javadoc 추가
- [ ] Redis Adapter ArchUnit 테스트 통과
- [ ] 커밋: `refactor: UploadSessionRedisAdapter 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요 (Adapter는 Mock 사용)
- [ ] 커밋: `test: UploadSessionRedisAdapter 테스트 정리 (Tidy)`

---

### 2️⃣4️⃣ Persistence Layer ArchUnit 테스트 (Cycle 24)

#### 🔴 Red: 테스트 작성
- [ ] `PersistenceLayerArchitectureTest.java` 생성
- [ ] Persistence Layer는 Domain, Application에만 의존 테스트
- [ ] Long FK 전략 준수 검증 (JPA 관계 어노테이션 금지)
- [ ] Lombok 사용 금지 검증
- [ ] Entity는 Plain Java 검증
- [ ] DTO는 Record 검증
- [ ] 커밋: `test: Persistence Layer ArchUnit 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] ArchUnit 테스트 구현 완료
- [ ] 테스트 통과
- [ ] 커밋: `impl: Persistence Layer ArchUnit 테스트 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] ArchUnit 규칙 명확화
- [ ] 커밋: `refactor: Persistence Layer ArchUnit 테스트 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] TestFixture 불필요
- [ ] 커밋: `test: Persistence Layer ArchUnit 테스트 정리 (Tidy)`

---

### 2️⃣5️⃣ 최종 통합 검증 (Cycle 25)

#### 🔴 Red: 테스트 작성
- [ ] MySQL + Redis 통합 시나리오 테스트 작성
- [ ] File 저장 → Redis 세션 저장 → Redis 세션 조회 → File 조회
- [ ] Optimistic Lock 충돌 시나리오 재검증
- [ ] 커밋: `test: 최종 통합 검증 테스트 추가 (Red)`

#### 🟢 Green: 최소 구현
- [ ] 통합 테스트 통과
- [ ] 커밋: `impl: 최종 통합 검증 통과 (Green)`

#### ♻️ Refactor: 리팩토링
- [ ] 전체 코드 리뷰 및 개선
- [ ] 중복 코드 제거
- [ ] Javadoc 보완
- [ ] 커밋: `refactor: Persistence Layer 최종 개선 (Refactor)`

#### 🧹 Tidy: TestFixture 정리
- [ ] 모든 TestFixture 최종 정리
- [ ] 사용하지 않는 Fixture 메서드 제거
- [ ] 커밋: `test: Persistence Layer Fixture 최종 정리 (Tidy)`

---

## ✅ 완료 조건

- [ ] 모든 TDD 사이클 완료 (25 사이클 × 4단계 = 100 체크박스)
- [ ] 모든 테스트 통과
- [ ] ArchUnit 테스트 통과
  - Persistence Layer 의존성 규칙
  - Long FK 전략 규칙
  - Lombok 금지 규칙
  - Entity/DTO 타입 규칙
- [ ] Zero-Tolerance 규칙 준수
  - Long FK 전략 (JPA 관계 어노테이션 금지)
  - Optimistic Lock (@Version)
  - QueryDSL DTO Projection
  - Lombok 금지
- [ ] 테스트 커버리지 > 80%
- [ ] Flyway 마이그레이션 성공

---

## 🔗 관련 문서

- Task: docs/prd/session/FILE-006-003.md
- PRD: /Users/sangwon-ryu/fileflow/docs/prd/presigned-url-upload.md
- Persistence Layer 규칙: docs/coding_convention/04-persistence-layer/

---

## 📊 사이클 요약

**총 사이클 수**: 25
**예상 소요 시간**: 375분 (6.25시간)
**Red 단계**: 25개
**Green 단계**: 25개
**Refactor 단계**: 25개
**Tidy 단계**: 25개

**레이어별 분류**:
- Flyway 마이그레이션: 1 사이클
- MySQL (Entity, Mapper, Repository): 9 사이클
- MySQL (Adapter): 3 사이클
- Redis (DTO, Mapper, Config, Repository): 9 사이클
- Redis (Adapter): 1 사이클
- ArchUnit 테스트: 1 사이클
- 최종 통합 검증: 1 사이클

---

## 🔧 다음 단계

1. `/kb/persistence/go` - TDD 사이클 시작 (자동으로 다음 체크박스 진행)
2. 각 사이클마다 4단계 커밋 (test: → impl: → refactor: → test:)
3. 모든 사이클 완료 후 FILE-006-004 (REST API Layer) 시작
