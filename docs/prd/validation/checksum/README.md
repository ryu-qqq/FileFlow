# Checksum Validation Bounded Context

**Bounded Context**: `validation/checksum`
**Dependencies**: `session/single` (File Aggregate)
**예상 기간**: 2일
**우선순위**: Level 3 (Level 2 완료 후)

---

## 📋 개요

**목적**: 파일 무결성 검증을 위한 Checksum 계산 및 검증 기능을 제공합니다.

**핵심 문제 해결**:
- **파일 무결성**: 업로드 중 파일 손상 여부 확인
- **중복 파일 감지**: 동일 파일 재업로드 방지
- **보안**: 악성 파일 변조 감지

**사용 사례**:
- 클라이언트에서 계산한 Checksum과 서버 검증
- 동일 파일 업로드 시 기존 파일 재사용
- S3 업로드 후 무결성 검증

---

## 🎯 주요 기능

### In Scope
1. **ChecksumValidation Aggregate** - Checksum 검증 생명주기 관리
2. **Checksum 계산** - SHA-256, MD5 지원
3. **클라이언트 Checksum 검증** - 업로드 전 Checksum 비교
4. **S3 ETag 검증** - S3 업로드 후 무결성 확인
5. **중복 파일 감지** - Checksum 기반 중복 파일 조회

### Out of Scope (Future)
- 바이러스/악성코드 스캔
- 콘텐츠 유사도 검사 (Perceptual Hash)
- 블록체인 기반 무결성 보증

---

## 🏗️ Domain Layer

### Aggregates

#### 1. ChecksumValidation
**책임**: Checksum 검증 생명주기 관리

**주요 메서드**:
```java
public class ChecksumValidation {
    private ValidationId validationId;      // UUID v7
    private FileId fileId;
    private ChecksumType checksumType;      // SHA256, MD5
    private Checksum clientChecksum;        // 클라이언트 제공
    private Checksum serverChecksum;        // 서버 계산
    private ValidationStatus status;        // PENDING, VALID, INVALID
    private String failureReason;           // 검증 실패 사유

    public static ChecksumValidation create(
        FileId fileId,
        ChecksumType checksumType,
        Checksum clientChecksum,
        Clock clock
    );

    public void validateWithServerChecksum(Checksum serverChecksum);
    public boolean isValid();
}
```

### Value Objects

#### Checksum
```java
public record Checksum(String value) {
    private static final Pattern SHA256_PATTERN = Pattern.compile("^[a-f0-9]{64}$");
    private static final Pattern MD5_PATTERN = Pattern.compile("^[a-f0-9]{32}$");

    public Checksum {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Checksum은 null이거나 빈 값일 수 없습니다.");
        }
        if (!SHA256_PATTERN.matcher(value).matches() && !MD5_PATTERN.matcher(value).matches()) {
            throw new InvalidChecksumFormatException(value);
        }
    }

    public static Checksum sha256(String value) {
        return new Checksum(value);
    }

    public static Checksum md5(String value) {
        return new Checksum(value);
    }

    public static Checksum calculate(InputStream inputStream, ChecksumType type) throws IOException {
        MessageDigest digest = MessageDigest.getInstance(type.algorithm());
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        byte[] hash = digest.digest();
        return new Checksum(bytesToHex(hash));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
```

### Enums

#### ChecksumType
- `SHA256`: SHA-256 해시 (기본값, 권장)
- `MD5`: MD5 해시 (레거시 지원)

#### ValidationStatus
- `PENDING`: 검증 대기
- `VALID`: 검증 성공
- `INVALID`: 검증 실패

---

## 📦 Application Layer

### Use Cases

#### 1. ValidateChecksumUseCase (Command)
**책임**: 클라이언트 Checksum 검증

```java
@Component
public class ValidateChecksumFacade implements ValidateChecksumUseCase {

    @Override
    public ChecksumValidationResponse execute(ValidateChecksumCommand cmd) {
        // 1. 트랜잭션: ChecksumValidation 생성
        ChecksumValidation validation = checksumValidationManager.createValidation(
            cmd.fileId(),
            cmd.checksumType(),
            cmd.clientChecksum()
        );

        // 2. 트랜잭션 밖: S3에서 파일 다운로드
        File file = fileQueryPort.findById(cmd.fileId());
        InputStream fileStream = s3ClientPort.download(file.s3Bucket(), file.s3Key());

        // 3. 트랜잭션 밖: 서버 Checksum 계산
        Checksum serverChecksum = Checksum.calculate(fileStream, cmd.checksumType());

        // 4. 트랜잭션: 검증 결과 저장
        validation.validateWithServerChecksum(serverChecksum);
        checksumValidationPersistencePort.update(validation);

        return ChecksumValidationResponse.from(validation);
    }
}
```

#### 2. FindDuplicateFileUseCase (Query)
**책임**: Checksum 기반 중복 파일 조회

```java
@Component
public class FindDuplicateFileService implements FindDuplicateFileUseCase {

    @Override
    public DuplicateFileResponse execute(FindDuplicateFileQuery query) {
        // 1. Checksum으로 기존 파일 조회
        List<File> duplicates = fileQueryPort.findByChecksum(query.checksum());

        if (duplicates.isEmpty()) {
            return DuplicateFileResponse.noDuplicate();
        }

        // 2. 가장 최근 파일 반환
        File latestFile = duplicates.stream()
            .max(Comparator.comparing(File::createdAt))
            .orElseThrow();

        return DuplicateFileResponse.from(latestFile);
    }
}
```

#### 3. CalculateChecksumOnUploadComplete (Event Listener)
**책임**: 업로드 완료 시 자동으로 Checksum 계산

```java
@Component
public class FileUploadedEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onFileUploaded(FileUploadedEvent event) {
        // 1. 트랜잭션 밖: S3에서 파일 다운로드
        File file = fileQueryPort.findById(event.fileId());
        InputStream fileStream = s3ClientPort.download(file.s3Bucket(), file.s3Key());

        // 2. 트랜잭션 밖: SHA-256 계산
        Checksum checksum = Checksum.calculate(fileStream, ChecksumType.SHA256);

        // 3. 트랜잭션: File Aggregate에 Checksum 저장
        file.updateChecksum(checksum);
        filePersistencePort.update(file);
    }
}
```

---

## 🗄️ Persistence Layer

### File Entity 확장

#### FileJpaEntity에 checksum 컬럼 추가
```sql
ALTER TABLE files
ADD COLUMN checksum_sha256 VARCHAR(64) AFTER s3_bucket,
ADD INDEX idx_checksum (checksum_sha256);
```

### Flyway Migration

#### V8__create_checksum_validations_table.sql
```sql
CREATE TABLE checksum_validations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    validation_id VARCHAR(36) NOT NULL UNIQUE,
    file_id VARCHAR(36) NOT NULL,
    checksum_type VARCHAR(20) NOT NULL,
    client_checksum VARCHAR(64) NOT NULL,
    server_checksum VARCHAR(64),
    status VARCHAR(20) NOT NULL,
    failure_reason TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_validation_id (validation_id),
    INDEX idx_file_id (file_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 🌐 REST API Layer

### Endpoints

| Method | Path | Description | Status Code |
|--------|------|-------------|-------------|
| POST | /api/v1/files/{fileId}/validate-checksum | Checksum 검증 | 200 OK |
| GET | /api/v1/files/duplicate | 중복 파일 조회 | 200 OK |
| POST | /api/v1/files/presigned-url (확장) | Checksum 기반 중복 체크 | 200 OK (중복), 201 Created (신규) |

### Request Example

**POST /api/v1/files/{fileId}/validate-checksum**:
```json
{
  "checksumType": "SHA256",
  "clientChecksum": "a3c5d8f7b2e1c4d9a6b8f0e2c7d5a9b3f1e8c6d4a7b9f2e5c8d1a4b7f0e3c6d9"
}
```

### Response Example

**POST /api/v1/files/{fileId}/validate-checksum (200 OK)**:
```json
{
  "validationId": "01JDC000-1234-5678-9abc-def012345678",
  "fileId": "01JD8001-1234-5678-9abc-def012345678",
  "status": "VALID",
  "clientChecksum": "a3c5d8f7b2e1c4d9a6b8f0e2c7d5a9b3f1e8c6d4a7b9f2e5c8d1a4b7f0e3c6d9",
  "serverChecksum": "a3c5d8f7b2e1c4d9a6b8f0e2c7d5a9b3f1e8c6d4a7b9f2e5c8d1a4b7f0e3c6d9",
  "message": "파일 무결성이 확인되었습니다."
}
```

**GET /api/v1/files/duplicate?checksum=a3c5... (200 OK)**:
```json
{
  "isDuplicate": true,
  "existingFile": {
    "fileId": "01JD7000-1234-5678-9abc-def012345678",
    "fileName": "기존파일.jpg",
    "s3Key": "uploads/1/admin/connectly/banner/01JD7000_기존파일.jpg",
    "uploadedAt": "2025-11-17T15:30:00Z"
  },
  "message": "동일한 파일이 이미 존재합니다. 기존 파일을 재사용하시겠습니까?"
}
```

---

## 📊 Integration Points

### session/single 확장
**GeneratePresignedUrlUseCase**에 중복 체크 추가:
```java
@Override
public PresignedUrlResponse execute(GeneratePresignedUrlCommand cmd) {
    // 1. 클라이언트에서 Checksum 제공 시 중복 체크
    if (cmd.checksum() != null) {
        Optional<File> duplicate = fileQueryPort.findByChecksum(cmd.checksum());
        if (duplicate.isPresent()) {
            return PresignedUrlResponse.duplicateFile(duplicate.get());
        }
    }

    // 2. 중복 없으면 기존 로직 수행
    // ...
}
```

---

## ✅ Definition of Done

### 기능 요구사항
- [ ] SHA-256 Checksum 계산 (기본)
- [ ] MD5 Checksum 계산 (레거시 지원)
- [ ] 클라이언트 Checksum vs 서버 Checksum 검증
- [ ] 중복 파일 조회 (Checksum 기반)
- [ ] Presigned URL 발급 시 중복 체크 옵션

### 품질 요구사항
- [ ] Unit Test Coverage > 90%
- [ ] Integration Test (TestContainers + LocalStack S3)
- [ ] ArchUnit Test 통과

### 성능 요구사항
- [ ] 10MB 파일 Checksum 계산 < 2초 (P95)
- [ ] 중복 파일 조회 < 100ms (DB 인덱스)

---

## 🔗 의존성

### Upstream
- `session/single` - Presigned URL 발급 시 중복 체크

### Downstream
- S3 Download API
- MessageDigest (Java 표준 라이브러리)

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (validation/checksum Bounded Context)
