# Phase 2B: External Download

**진행 상태**: ⏳ 대기 중 (0/6 - 0%)

## 개요

Phase 2B는 외부 URL에서 파일을 서버가 다운로드하여 S3에 저장하는 External Download 기능을 구현합니다.
클라이언트가 대역폭을 소비하지 않고, 서버 측에서 파일을 검증하고 정책을 적용할 수 있습니다.

**핵심 목표**: 서버 측 다운로드, 비동기 처리, 업로드 정책 적용

## External Download 흐름

```
1. Start → ExternalDownload 작업 등록 (PENDING 상태)
2. Worker가 비동기로 외부 URL에서 다운로드 (IN_PROGRESS)
3. S3에 업로드 (청크 단위 스트리밍)
4. UploadSession 생성 (COMPLETED)
5. 실패 시 재시도 (최대 3회)
```

## 태스크 목록

### ⏳ KAN-320: ExternalDownload Aggregate 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: ExternalDownload Aggregate 구현 (상태 머신: PENDING → IN_PROGRESS → COMPLETED/FAILED)

**구현 클래스**:
- `ExternalDownload.java` (Aggregate Root)
- `ExternalDownloadId.java` (Long Value Object)
- `ExternalDownloadStatus.java` (Enum)
- `RetryPolicy.java` (Value Object - maxRetries, retryCount)

**핵심 메서드**:
- `start()` - 다운로드 시작
- `markProgress(downloadedBytes)` - 진행 상황 업데이트
- `complete(uploadSessionId)` - 완료 처리
- `fail(reason)` - 실패 처리
- `canRetry()` - 재시도 가능 여부

**DoD**:
- [ ] Zero-Tolerance 규칙 준수
- [ ] 상태 전환 로직 구현
- [ ] Unit Test 작성 (재시도 로직 포함)

**도메인 모델 예시**:
```java
public class ExternalDownload {
    private ExternalDownloadId id;
    private String sourceUrl;
    private String targetBucketName;
    private String targetObjectKey;
    private ExternalDownloadStatus status;
    private long totalBytes;
    private long downloadedBytes;
    private RetryPolicy retryPolicy;
    private String uploadSessionId; // 완료 후 생성된 세션

    public void fail(String reason) {
        if (retryPolicy.canRetry()) {
            this.status = ExternalDownloadStatus.PENDING;
            retryPolicy.incrementRetry();
            registerEvent(new DownloadRetryScheduledEvent(id));
        } else {
            this.status = ExternalDownloadStatus.FAILED;
            registerEvent(new DownloadFailedEvent(id, reason));
        }
    }
}
```

---

### ⏳ KAN-321: UploadPolicy Aggregate 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: UploadPolicy Aggregate 구현 (업로드 방식 결정)

**구현 클래스**:
- `UploadPolicy.java` (Aggregate Root)
- `UploadType.java` (Enum: DIRECT/MULTIPART/EXTERNAL)
- `PolicyCondition.java` (Value Object)

**정책 규칙**:
```
1. externalUrl 제공 → EXTERNAL
2. fileSize >= 100MB → MULTIPART
3. else → DIRECT
```

**DoD**:
- [ ] 정책 평가 로직 구현
- [ ] Unit Test (정책 분기 테스트)

---

### ⏳ KAN-322: PolicyResolverService 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: PolicyResolverService 구현 (정책 평가 서비스)

**핵심 로직**:
- 요청 파라미터 기반 정책 결정
- UploadPolicy 조회 및 평가

**DoD**:
- [ ] 정책 결정 로직 구현
- [ ] Unit Test

---

### ⏳ KAN-323: StartExternalDownloadUseCase 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: StartExternalDownloadUseCase 구현

**세부 작업**:
- [ ] StartExternalDownloadCommand 생성
  - sourceUrl (String)
  - tenantId, orgId, userId (권한 컨텍스트)
  - targetBucketName, targetObjectKey
  - maxRetries (기본값: 3)

- [ ] ExternalDownload Aggregate 생성
- [ ] 비동기 Worker 트리거 (Spring Events or Message Queue)

**DoD**:
- [ ] Unit Test
- [ ] Integration Test
- [ ] 권한 검증

---

### ⏳ KAN-324: ExternalDownloadWorker 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: ExternalDownloadWorker 구현 (비동기 다운로드 워커)

**구현 방식**:
- `@Async` 메서드 또는 별도 스레드 풀
- 청크 단위 스트리밍 (메모리 효율성)
- 진행 상황 업데이트 (콜백)

**핵심 로직**:
```java
@Async
public void download(ExternalDownloadId downloadId) {
    ExternalDownload download = repository.findById(downloadId);

    try {
        download.start();

        // HTTP 다운로드 (청크 단위)
        InputStream inputStream = httpClient.get(download.getSourceUrl());

        // S3에 스트리밍 업로드
        s3Port.uploadStream(
            download.getTargetBucketName(),
            download.getTargetObjectKey(),
            inputStream,
            (bytesWritten) -> {
                download.markProgress(bytesWritten);
                repository.save(download);
            }
        );

        // UploadSession 생성
        UploadSession session = createUploadSession(download);
        download.complete(session.getId());

    } catch (Exception e) {
        download.fail(e.getMessage());
        repository.save(download);
    }
}
```

**DoD**:
- [ ] 청크 단위 스트리밍 구현
- [ ] 진행 상황 업데이트
- [ ] 예외 처리 및 재시도
- [ ] Integration Test (S3 Mock)

---

### ⏳ KAN-325: ExternalDownloadController 구현

**상태**: 해야 할 일
**우선순위**: Medium

**목표**: ExternalDownloadController 구현

**API 엔드포인트**:

#### POST /uploads/external
- Request: `StartExternalDownloadRequest`
  ```json
  {
    "sourceUrl": "https://example.com/large-file.zip",
    "filename": "large-file.zip",
    "mimeType": "application/zip"
  }
  ```
- Response: 202 Accepted + `ExternalDownloadResponse`
  ```json
  {
    "downloadId": "ext_download_001",
    "status": "PENDING",
    "estimatedCompletionAt": "2025-01-01T12:30:00Z"
  }
  ```

#### GET /uploads/external/{downloadId}
- Response: 200 OK + 진행 상황
  ```json
  {
    "downloadId": "ext_download_001",
    "status": "IN_PROGRESS",
    "totalBytes": 1073741824,
    "downloadedBytes": 536870912,
    "progress": 50
  }
  ```

**DoD**:
- [ ] OpenAPI 3.0 스펙 작성
- [ ] Controller 통합 테스트
- [ ] 권한 검증 (file.upload)

---

## 📊 Phase 2B 요약

### 아키텍처 구성
```
Domain Layer:
- ExternalDownload (Aggregate Root)
- UploadPolicy (Aggregate Root)
- RetryPolicy (Value Object)

Application Layer:
- StartExternalDownloadUseCase
- PolicyResolverService
- ExternalDownloadWorker

Adapter Layer:
- ExternalDownloadJpaAdapter
- ExternalDownloadController
- HttpClientAdapter (외부 URL 다운로드)
- S3StorageAdapter (S3 업로드)
```

### 성능 목표
- External Download 시작 P95 < 200ms
- 다운로드 처리량 > 10MB/s
- 동시 다운로드 제한 10개

### 다음 단계
Phase 2B 완료 후 Phase 2C (Events & Batch)로 진행
