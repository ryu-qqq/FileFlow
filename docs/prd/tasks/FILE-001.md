# FILE-001: Domain Layer 구현

**Epic**: File Management System
**Layer**: Domain Layer
**브랜치**: feature/FILE-001-domain
**Jira URL**: (sync-to-jira 후 추가)

---

## 📝 목적

파일 관리 시스템의 핵심 도메인 개념을 구현합니다. 파일 업로드, 가공, 메시지 전송의 비즈니스 규칙과 불변식을 Domain Aggregate로 표현합니다.

---

## 🎯 요구사항

### Aggregate Root: File

- [ ] **File Aggregate 설계**
  - `fileId`: String (UUID v7 - 날짜 포함, 시간 순서 정렬 가능)
  - `fileName`: String (원본 파일명)
  - `fileSize`: Long (바이트 단위)
  - `mimeType`: String (예: `image/jpeg`, `text/html`)
  - `status`: FileStatus (Enum)
  - `s3Key`: String (S3 Object Key)
  - `s3Bucket`: String (S3 Bucket Name)
  - `cdnUrl`: String (Nullable, CDN URL)
  - `uploaderId`: Long (Long FK 전략)
  - `category`: String (상품, 전시영역, 외부몰 연동 문서 등)
  - `tags`: List<String> (파일 태그)
  - `version`: Integer (파일 버전)
  - `deletedAt`: LocalDateTime (Nullable, Soft Delete)
  - `createdAt`: LocalDateTime
  - `updatedAt`: LocalDateTime

- [ ] **File 비즈니스 메서드**
  - `File.create()`: 파일 생성 (UUID v7 생성, PENDING 상태)
  - `markAsUploading()`: 상태를 UPLOADING으로 변경
  - `markAsCompleted()`: 상태를 COMPLETED로 변경
  - `markAsFailed()`: 상태를 FAILED로 변경
  - `markAsProcessing()`: 상태를 PROCESSING으로 변경
  - `incrementRetryCount()`: 재시도 횟수 증가
  - `softDelete()`: deletedAt 설정
  - `canUploadComplete()`: 업로드 완료 가능 여부 검증 (PENDING 또는 UPLOADING만 허용)

### Aggregate Root: FileProcessingJob

- [ ] **FileProcessingJob Aggregate 설계**
  - `jobId`: String (UUID v7)
  - `fileId`: String (FK, File UUID)
  - `jobType`: JobType (Enum)
  - `status`: JobStatus (Enum)
  - `retryCount`: Integer
  - `maxRetryCount`: Integer (기본값: 2)
  - `inputS3Key`: String
  - `outputS3Key`: String (Nullable)
  - `errorMessage`: String (Nullable)
  - `createdAt`: LocalDateTime
  - `processedAt`: LocalDateTime (Nullable)

- [ ] **FileProcessingJob 비즈니스 메서드**
  - `FileProcessingJob.create()`: 가공 작업 생성 (UUID v7, PENDING 상태)
  - `markAsProcessing()`: 상태를 PROCESSING으로 변경
  - `markAsCompleted(outputS3Key)`: 상태를 COMPLETED로 변경, outputS3Key 저장
  - `markAsFailed(errorMessage)`: 상태를 FAILED로 변경, 에러 메시지 저장
  - `incrementRetryCount()`: 재시도 횟수 증가
  - `canRetry()`: 재시도 가능 여부 검증 (retryCount < maxRetryCount)

### Aggregate Root: MessageOutbox

- [ ] **MessageOutbox Aggregate 설계**
  - `id`: Long (PK, Auto Increment)
  - `eventType`: String (이벤트 타입)
  - `aggregateId`: String (File UUID 또는 FileProcessingJob UUID)
  - `payload`: String (JSON)
  - `status`: OutboxStatus (Enum)
  - `retryCount`: Integer
  - `maxRetryCount`: Integer (기본값: 3)
  - `createdAt`: LocalDateTime
  - `processedAt`: LocalDateTime (Nullable)

- [ ] **MessageOutbox 비즈니스 메서드**
  - `MessageOutbox.create()`: 메시지 생성 (PENDING 상태)
  - `markAsSent()`: 상태를 SENT로 변경, processedAt 설정
  - `markAsFailed()`: 상태를 FAILED로 변경
  - `incrementRetryCount()`: 재시도 횟수 증가
  - `canRetry()`: 재시도 가능 여부 검증
  - `isExpired()`: TTL 만료 여부 (성공 7일, 실패 30일)

### Value Objects

- [ ] **FileStatus Enum**
  - PENDING, UPLOADING, COMPLETED, FAILED, RETRY_PENDING, PROCESSING

- [ ] **JobType Enum**
  - 이미지: THUMBNAIL_GENERATION, IMAGE_RESIZE, IMAGE_FORMAT_CONVERSION, OCR
  - HTML: HTML_PARSING, HTML_IMAGE_UPLOAD, HTML_TEXT_ANALYSIS
  - 문서: DOCUMENT_TEXT_EXTRACTION, DOCUMENT_FORMAT_CONVERSION
  - 엑셀: EXCEL_CSV_CONVERSION, EXCEL_DATA_EXTRACTION

- [ ] **JobStatus Enum**
  - PENDING, PROCESSING, COMPLETED, FAILED, RETRY_PENDING

- [ ] **OutboxStatus Enum**
  - PENDING, SENT, FAILED

### 도메인 규칙 (Invariants)

- [ ] **파일 크기 검증**
  - 최대 파일 크기: 1GB (1,073,741,824 bytes)
  - 파일 크기 0 이하 불가

- [ ] **MIME 타입 검증**
  - 허용 목록: `image/*`, `text/html`, `application/pdf`, `application/vnd.ms-excel`, `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

- [ ] **상태 전환 규칙**
  - File: PENDING → UPLOADING → COMPLETED → PROCESSING
  - PENDING/UPLOADING에서만 FAILED로 전환 가능
  - COMPLETED에서만 PROCESSING으로 전환 가능

- [ ] **파일 버전 관리**
  - 같은 파일명 재업로드 시 version 증가
  - 이전 버전은 Soft Delete (deletedAt 설정)

- [ ] **UUID v7 생성**
  - 날짜 포함, 시간 순서 정렬 가능
  - S3 Key와 동일하게 사용 (예: `{fileId}.jpg`)

---

## ⚠️ 제약사항

### Zero-Tolerance 규칙

- [ ] **Lombok 금지**
  - Pure Java 또는 Record 사용
  - Getter/Setter 직접 작성

- [ ] **Law of Demeter 준수**
  - Getter 체이닝 금지
  - `file.getS3Url()` (O) / `file.getS3().getUrl()` (X)

- [ ] **Long FK 전략**
  - JPA 관계 어노테이션 금지
  - `private Long uploaderId;` (O)
  - `@ManyToOne private User user;` (X)

- [ ] **Tell Don't Ask 원칙**
  - 상태를 묻지 말고 행동을 지시
  - `file.canUploadComplete()` 후 `file.markAsCompleted()` 호출

### 테스트 규칙

- [ ] **ArchUnit 테스트 필수**
  - Domain Layer는 다른 Layer에 의존 금지
  - Lombok 사용 금지 검증
  - Law of Demeter 위반 검증

- [ ] **TestFixture 사용 필수**
  - `FileTestFixture.aFile()` 패턴 사용
  - 테스트 데이터 중앙 관리

- [ ] **테스트 커버리지 > 80%**
  - 비즈니스 메서드 모두 테스트
  - 상태 전환 테스트

---

## ✅ 완료 조건

- [ ] 3개 Aggregate Root 구현 완료 (File, FileProcessingJob, MessageOutbox)
- [ ] 4개 Value Object 구현 완료 (FileStatus, JobType, JobStatus, OutboxStatus)
- [ ] 모든 비즈니스 메서드 구현 완료
- [ ] 도메인 규칙 (Invariants) 모두 구현
- [ ] Unit Test 커버리지 > 80%
- [ ] ArchUnit 테스트 통과
- [ ] Zero-Tolerance 규칙 준수 검증
- [ ] 코드 리뷰 승인
- [ ] PR 머지 완료

---

## 🔗 관련 문서

- **PRD**: docs/prd/file-management-system.md
- **Plan**: docs/prd/plans/FILE-001-domain-plan.md (create-plan 후 생성)
- **Jira**: (sync-to-jira 후 추가)
- **컨벤션**: docs/coding_convention/02-domain-layer/

---

## 📝 참고사항

### UUID v7 생성 예시
```java
public class UuidV7Generator {
    public static String generate() {
        // UUID v7 생성 로직
        // 시간 기반 정렬 가능
    }
}
```

### 상태 전환 예시
```java
public class File {
    public void markAsCompleted() {
        if (!canUploadComplete()) {
            throw new IllegalStateException("업로드 완료 불가능한 상태입니다");
        }
        this.status = FileStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    private boolean canUploadComplete() {
        return status == FileStatus.PENDING || status == FileStatus.UPLOADING;
    }
}
```

### TestFixture 예시
```java
public class FileTestFixture {
    public static File aFile() {
        return File.create(
            "example.jpg",
            1024L,
            "image/jpeg",
            1L,
            "상품",
            List.of("이미지")
        );
    }

    public static File aCompletedFile() {
        File file = aFile();
        file.markAsUploading();
        file.markAsCompleted();
        return file;
    }
}
```
