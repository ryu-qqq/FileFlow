# Phase 2A: Multipart Upload 태스크 상세 가이드

## 📋 Phase 2A 개요
- **목표**: 대용량 파일을 위한 Multipart Upload 기능 구현
- **태스크 수**: 10개 (KAN-310 ~ KAN-319)
- **예상 기간**: 2주
- **핵심 기술**: AWS S3 Multipart Upload API, Spring Data Domain Events

---

## KAN-310: MultipartUpload Aggregate 구현

### 📌 작업 내용
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/upload/MultipartUpload.java

/**
 * Multipart Upload Aggregate Root
 * 대용량 파일 업로드를 위한 상태 관리
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class MultipartUpload {
    // 필드 정의 (NO Lombok!)
    private final Long id;
    private final Long uploadSessionId;  // Long FK Strategy
    private String providerUploadId;      // S3 UploadId
    private MultipartStatus status;
    private Integer totalParts;
    private final List<UploadPart> uploadedParts;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // 생성자는 private
    private MultipartUpload(Long uploadSessionId) {
        this.id = null;  // DB에서 생성
        this.uploadSessionId = uploadSessionId;
        this.status = MultipartStatus.INIT;
        this.uploadedParts = new ArrayList<>();
        this.startedAt = LocalDateTime.now();
    }

    // Static Factory Method
    public static MultipartUpload create(Long uploadSessionId) {
        return new MultipartUpload(uploadSessionId);
    }

    // 비즈니스 메서드
    public void initiate(String providerUploadId, Integer totalParts) {
        if (this.status != MultipartStatus.INIT) {
            throw new IllegalStateException("Already initiated");
        }
        this.providerUploadId = providerUploadId;
        this.totalParts = totalParts;
        this.status = MultipartStatus.IN_PROGRESS;
    }

    public void addPart(UploadPart part) {
        validatePartAddition(part);
        this.uploadedParts.add(part);
    }

    // Tell, Don't Ask 패턴
    public boolean canComplete() {
        return status == MultipartStatus.IN_PROGRESS
            && uploadedParts.size() == totalParts
            && hasAllPartsInSequence();
    }

    private boolean hasAllPartsInSequence() {
        Set<Integer> partNumbers = uploadedParts.stream()
            .map(UploadPart::getPartNumber)
            .collect(Collectors.toSet());

        for (int i = 1; i <= totalParts; i++) {
            if (!partNumbers.contains(i)) {
                return false;
            }
        }
        return true;
    }

    // Getter (필요한 것만)
    public Long getId() { return id; }
    public Long getUploadSessionId() { return uploadSessionId; }
    public String getProviderUploadId() { return providerUploadId; }
    public MultipartStatus getStatus() { return status; }

    // 방어적 복사
    public List<UploadPart> getUploadedParts() {
        return Collections.unmodifiableList(uploadedParts);
    }
}

// 상태 Enum
public enum MultipartStatus {
    INIT,        // 초기화 전
    IN_PROGRESS, // 업로드 진행 중
    COMPLETED,   // 완료
    ABORTED,     // 중단
    FAILED       // 실패
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **NO Lombok**: 모든 getter/setter 수동 작성
- ✅ **Tell, Don't Ask**: `canComplete()` 메서드로 상태 확인
- ✅ **Static Factory Method**: `create()` 사용
- ✅ **Immutable List**: `getUploadedParts()`에서 unmodifiable 반환
- ✅ **Javadoc**: 클래스와 public 메서드 문서화

### 🧪 테스트 시나리오
```java
@Test
void multipart_upload_상태_전환_테스트() {
    // given
    MultipartUpload upload = MultipartUpload.create(1L);

    // when
    upload.initiate("s3-upload-id", 3);

    // then
    assertThat(upload.getStatus()).isEqualTo(MultipartStatus.IN_PROGRESS);
    assertThat(upload.canComplete()).isFalse();
}

@Test
void 모든_파트_업로드_후_완료_가능() {
    // given
    MultipartUpload upload = MultipartUpload.create(1L);
    upload.initiate("s3-upload-id", 2);

    // when
    upload.addPart(UploadPart.of(1, "etag1", 5242880L));
    upload.addPart(UploadPart.of(2, "etag2", 3000000L));

    // then
    assertThat(upload.canComplete()).isTrue();
}
```

---

## KAN-311: UploadPart Value Object 구현

### 📌 작업 내용
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/upload/UploadPart.java

/**
 * Upload Part Value Object
 * 불변 객체로 구현
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadPart {
    private final Integer partNumber;
    private final String etag;
    private final Long size;
    private final String checksum;
    private final LocalDateTime uploadedAt;

    // Private 생성자
    private UploadPart(Integer partNumber, String etag, Long size) {
        this.partNumber = validatePartNumber(partNumber);
        this.etag = validateEtag(etag);
        this.size = validateSize(size, partNumber);
        this.checksum = null;  // Optional
        this.uploadedAt = LocalDateTime.now();
    }

    // Static Factory Method
    public static UploadPart of(Integer partNumber, String etag, Long size) {
        return new UploadPart(partNumber, etag, size);
    }

    // 검증 메서드
    private static Integer validatePartNumber(Integer partNumber) {
        if (partNumber == null || partNumber < 1 || partNumber > 10000) {
            throw new IllegalArgumentException(
                "Part number must be between 1 and 10000: " + partNumber
            );
        }
        return partNumber;
    }

    private static String validateEtag(String etag) {
        if (etag == null || etag.isBlank()) {
            throw new IllegalArgumentException("ETag cannot be empty");
        }
        return etag;
    }

    private static Long validateSize(Long size, Integer partNumber) {
        if (size == null || size < 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        // 마지막 파트가 아닌 경우 최소 5MB
        // 실제로는 마지막 파트 여부를 알 수 없으므로 여기서는 체크 안함
        return size;
    }

    // Value Object 필수: equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UploadPart)) return false;
        UploadPart that = (UploadPart) o;
        return Objects.equals(partNumber, that.partNumber) &&
               Objects.equals(etag, that.etag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partNumber, etag);
    }

    @Override
    public String toString() {
        return String.format(
            "UploadPart{partNumber=%d, etag='%s', size=%d}",
            partNumber, etag, size
        );
    }

    // Getter (NO Setter!)
    public Integer getPartNumber() { return partNumber; }
    public String getEtag() { return etag; }
    public Long getSize() { return size; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **완전 불변**: 모든 필드 final, setter 없음
- ✅ **검증 로직**: 생성 시점 검증
- ✅ **Value Object 패턴**: equals/hashCode 구현
- ✅ **Static Factory**: `of()` 메서드 제공

---

## KAN-313: MultipartUploadJpaAdapter 구현

### 📌 작업 내용
```java
// Entity - 위치: adapter-out/persistence-mysql/src/main/java/.../MultipartUploadEntity.java

/**
 * Multipart Upload JPA Entity
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Entity
@Table(name = "upload_multipart")
public class MultipartUploadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ❌ 금지: @ManyToOne 사용 안함!
    @Column(name = "upload_session_id", nullable = false)
    private Long uploadSessionId;  // Long FK Strategy

    @Column(name = "provider_upload_id", length = 500)
    private String providerUploadId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MultipartStatus status;

    @Column(name = "total_parts")
    private Integer totalParts;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Getter/Setter (NO Lombok!)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUploadSessionId() {
        return uploadSessionId;
    }

    public void setUploadSessionId(Long uploadSessionId) {
        this.uploadSessionId = uploadSessionId;
    }

    // ... 나머지 getter/setter
}

// Adapter - 위치: adapter-out/persistence-mysql/src/main/java/.../MultipartUploadJpaAdapter.java

/**
 * Multipart Upload 영속성 Adapter
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor  // 이것만 허용 (생성자 주입용)
public class MultipartUploadJpaAdapter implements MultipartUploadPort {

    private final MultipartUploadJpaRepository repository;
    private final MultipartUploadMapper mapper;
    private final UploadPartJpaRepository partRepository;

    @Override
    public MultipartUpload save(MultipartUpload multipart) {
        // 1. Aggregate → Entity 변환
        MultipartUploadEntity entity = mapper.toEntity(multipart);

        // 2. 저장
        MultipartUploadEntity saved = repository.save(entity);

        // 3. Parts 저장 (별도 테이블)
        if (multipart.getUploadedParts() != null) {
            saveUploadParts(saved.getId(), multipart.getUploadedParts());
        }

        // 4. Entity → Domain 변환
        return mapper.toDomain(saved, loadUploadParts(saved.getId()));
    }

    @Override
    public Optional<MultipartUpload> findById(Long id) {
        return repository.findById(id)
            .map(entity -> mapper.toDomain(
                entity,
                loadUploadParts(entity.getId())
            ));
    }

    @Override
    public Optional<MultipartUpload> findByUploadSessionId(Long sessionId) {
        return repository.findByUploadSessionId(sessionId)
            .map(entity -> mapper.toDomain(
                entity,
                loadUploadParts(entity.getId())
            ));
    }

    private List<UploadPart> loadUploadParts(Long multipartId) {
        return partRepository.findByMultipartUploadId(multipartId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    private void saveUploadParts(Long multipartId, List<UploadPart> parts) {
        List<UploadPartEntity> entities = parts.stream()
            .map(part -> mapper.toEntity(part, multipartId))
            .collect(Collectors.toList());
        partRepository.saveAll(entities);
    }
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **Long FK Strategy**: `@ManyToOne` 사용 안함
- ✅ **NO Lombok**: Entity getter/setter 수동 구현
- ✅ **명시적 매핑**: Mapper 클래스 별도 구현
- ✅ **Repository Pattern**: Port & Adapter 분리

---

## KAN-315: InitMultipartUploadUseCase 구현

### 📌 작업 내용
```java
// 위치: application/src/main/java/com/ryuqq/fileflow/application/upload/InitMultipartUploadUseCase.java

/**
 * Multipart Upload 초기화 UseCase
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class InitMultipartUploadUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final MultipartUploadPort multipartUploadPort;
    private final S3StoragePort s3StoragePort;
    private final PolicyResolverService policyResolver;

    /**
     * Multipart 업로드 초기화
     *
     * @param command 초기화 명령
     * @return 초기화 응답
     */
    public InitMultipartResponse execute(InitMultipartCommand command) {
        // 1. 정책 확인
        FileMetadata metadata = FileMetadata.of(
            command.getFileName(),
            command.getFileSize(),
            command.getContentType()
        );

        UploadPolicy policy = policyResolver.resolvePolicy(
            command.getTenantId(),
            metadata
        );

        PolicyEvaluationResult evaluation = policy.evaluate(metadata);
        if (!evaluation.isPassed()) {
            throw new PolicyViolationException(evaluation.getReason());
        }

        // 2. S3 Multipart 초기화 (트랜잭션 밖)
        S3InitResult s3Result = initializeS3Multipart(command);

        try {
            // 3. 도메인 객체 생성 및 저장 (트랜잭션 내)
            UploadSession session = createSession(command, s3Result);
            return buildResponse(session, s3Result);

        } catch (Exception e) {
            // 실패 시 S3 정리
            abortS3Multipart(s3Result);
            throw e;
        }
    }

    private S3InitResult initializeS3Multipart(InitMultipartCommand command) {
        String key = generateS3Key(command);

        InitiateMultipartUploadRequest request = InitiateMultipartUploadRequest.builder()
            .bucket(determineBucket(command.getTenantId()))
            .key(key)
            .contentType(command.getContentType())
            .build();

        InitiateMultipartUploadResponse response = s3StoragePort.initiateMultipartUpload(request);

        return new S3InitResult(
            response.uploadId(),
            key,
            calculatePartCount(command.getFileSize())
        );
    }

    @Transactional
    protected UploadSession createSession(
        InitMultipartCommand command,
        S3InitResult s3Result
    ) {
        // 1. 업로드 세션 생성
        UploadSession session = UploadSession.createForMultipart(
            command.getTenantId(),
            command.getFileName(),
            command.getFileSize()
        );

        // 2. Multipart 정보 생성
        MultipartUpload multipart = MultipartUpload.create(session.getId());
        multipart.initiate(s3Result.getUploadId(), s3Result.getPartCount());

        // 3. 세션에 Multipart 연결
        session.attachMultipart(multipart);

        // 4. 저장
        UploadSession savedSession = uploadSessionPort.save(session);
        multipartUploadPort.save(multipart);

        return savedSession;
    }

    private int calculatePartCount(Long fileSize) {
        // 파트 크기: 100MB (AWS 권장)
        long partSize = 100 * 1024 * 1024L;
        return (int) Math.ceil((double) fileSize / partSize);
    }

    private void abortS3Multipart(S3InitResult s3Result) {
        try {
            s3StoragePort.abortMultipartUpload(
                s3Result.getUploadId(),
                s3Result.getKey()
            );
        } catch (Exception e) {
            log.error("Failed to abort S3 multipart: {}", s3Result, e);
        }
    }
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **트랜잭션 분리**: S3 호출은 트랜잭션 밖
- ✅ **실패 처리**: S3 리소스 정리
- ✅ **정책 평가**: 업로드 전 정책 확인
- ✅ **Command Pattern**: Command DTO 사용

---

## 테스트 가이드

### 단위 테스트 예시
```java
@ExtendWith(MockitoExtension.class)
class MultipartUploadTest {

    @Test
    @DisplayName("파트 추가 시 중복 검증")
    void should_reject_duplicate_part() {
        // given
        MultipartUpload upload = MultipartUpload.create(1L);
        upload.initiate("upload-id", 3);
        UploadPart part1 = UploadPart.of(1, "etag1", 5242880L);

        // when
        upload.addPart(part1);

        // then
        assertThatThrownBy(() -> upload.addPart(part1))
            .isInstanceOf(DuplicatePartException.class);
    }
}
```

### 통합 테스트 예시
```java
@SpringBootTest
@AutoConfigureMockMvc
class MultipartUploadIntegrationTest {

    @MockBean
    private S3StoragePort s3StoragePort;

    @Test
    void complete_multipart_upload_flow() {
        // given
        given(s3StoragePort.initiateMultipartUpload(any()))
            .willReturn(new InitResponse("upload-id"));

        // when & then
        // 전체 플로우 테스트
    }
}
```

---

## 다음 태스크

- **KAN-316**: GeneratePartPresignedUrlUseCase
- **KAN-317**: MarkPartUploadedUseCase
- **KAN-318**: CompleteMultipartUploadUseCase
- **KAN-319**: UploadController 확장

각 태스크는 동일한 코딩 컨벤션 원칙을 따라야 하며, 특히 트랜잭션 경계와 외부 API 호출 분리에 주의해야 합니다.