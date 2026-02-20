# FileFlow SDK

FileFlow 서버와 통신하기 위한 Java SDK입니다.
파일 업로드(단일/멀티파트), 자산 관리, 외부 URL 다운로드, 이미지 변환 기능을 제공합니다.

## Modules

| 모듈 | 설명 |
|------|------|
| `fileflow-sdk-core` | 핵심 SDK (Java 21+, 외부 프레임워크 의존성 없음) |
| `fileflow-sdk-spring-boot-starter` | Spring Boot Auto-Configuration |

---

## Getting Started

### Spring Boot (권장)

**의존성 추가 (JitPack)**

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

// build.gradle
dependencies {
    implementation 'com.github.ryu-qqq.FileFlow:fileflow-sdk-spring-boot-starter:v1.0.2'
}
```

```xml
<!-- Maven -->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.ryu-qqq.FileFlow</groupId>
    <artifactId>fileflow-sdk-spring-boot-starter</artifactId>
    <version>v1.0.2</version>
</dependency>
```

**application.yml**

```yaml
fileflow:
  base-url: https://fileflow.example.com
  service-name: my-service
  service-token: ${FILEFLOW_SERVICE_TOKEN}
  timeout:
    connect: 5s    # 기본값: 5초
    read: 30s      # 기본값: 30초
```

**사용**

```java
@Service
public class FileUploadService {

    private final SingleUploadSessionApi uploadApi;

    public FileUploadService(SingleUploadSessionApi uploadApi) {
        this.uploadApi = uploadApi;
    }

    public String createUploadSession(String fileName, String contentType) {
        var request = new CreateSingleUploadSessionRequest(
            fileName, contentType, "PUBLIC", "PRODUCT_IMAGE", "WEB"
        );
        var response = uploadApi.create(request);
        return response.data().presignedUrl();
    }
}
```

### Programmatic (Spring 없이)

```java
FileFlowClient client = FileFlowClient.builder()
    .baseUrl("https://fileflow.example.com")
    .serviceName("my-service")
    .serviceToken("secret-token")
    .connectTimeout(Duration.ofSeconds(5))
    .readTimeout(Duration.ofSeconds(30))
    .build();
```

---

## API Reference

### Single Upload Session API

단일 파일 업로드를 위한 Presigned URL 기반 세션을 관리합니다.

```java
SingleUploadSessionApi api = client.singleUploadSession();

// 1. 세션 생성 → Presigned URL 발급
var response = api.create(new CreateSingleUploadSessionRequest(
    "photo.jpg",        // fileName
    "image/jpeg",       // contentType
    "PUBLIC",           // accessType (PUBLIC | PRIVATE)
    "PRODUCT_IMAGE",    // purpose
    "WEB"               // source
));
String presignedUrl = response.data().presignedUrl();
String sessionId = response.data().sessionId();

// 2. Presigned URL로 S3에 직접 업로드 (HTTP PUT)
// ...

// 3. 업로드 완료 처리
api.complete(sessionId, new CompleteSingleUploadSessionRequest(
    1024000L,           // fileSize (bytes)
    "\"etag-value\""    // etag (S3 응답에서 추출)
));

// 세션 조회
var session = api.get(sessionId);
```

### Multipart Upload Session API

대용량 파일을 분할 업로드합니다.

```java
MultipartUploadSessionApi api = client.multipartUploadSession();

// 1. 멀티파트 세션 생성
var response = api.create(new CreateMultipartUploadSessionRequest(
    "video.mp4",        // fileName
    "video/mp4",        // contentType
    "PRIVATE",          // accessType
    10_485_760L,        // partSize (10MB)
    "USER_UPLOAD",      // purpose
    "MOBILE"            // source
));
String sessionId = response.data().sessionId();

// 2. 파트별 Presigned URL 조회
var partUrl = api.getPresignedPartUrl(sessionId, 1);
// → partUrl.data().presignedUrl() 로 S3에 파트 업로드

// 3. 완료된 파트 등록
api.addCompletedPart(sessionId, new AddCompletedPartRequest(
    1,                  // partNumber (1-based)
    "\"part-etag\"",    // etag
    10_485_760L         // size
));

// 4. 전체 업로드 완료
api.complete(sessionId, new CompleteMultipartUploadSessionRequest(
    52_428_800L,        // totalFileSize
    "\"final-etag\""    // etag
));

// 세션 취소
api.abort(sessionId);
```

### Asset API

업로드 완료된 파일 자산을 조회/삭제하거나, S3에 이미 존재하는 파일을 자산으로 등록합니다.

```java
AssetApi api = client.asset();

// S3에 이미 존재하는 파일을 Asset으로 등록 (데이터 이동 없음)
var registered = api.register(new RegisterAssetRequest(
    "public/2026/02/product.jpg",  // s3Key (필수)
    "fileflow-bucket",              // bucket (필수)
    "PUBLIC",                       // accessType (PUBLIC | PRIVATE)
    "product.jpg",                  // fileName
    "image/jpeg",                   // contentType
    "PRODUCT_IMAGE",                // purpose
    "product-service"               // source
));
String assetId = registered.data().assetId();

// 자산 조회
var asset = api.get(assetId);
// → assetId, s3Key, bucket, fileName, fileSize, contentType, extension, origin, ...

// 이미지 메타데이터 조회 (width, height)
var metadata = api.getMetadata(assetId);
// → metadataId, assetId, width, height, transformType

// 자산 삭제 (소프트 삭제)
api.delete(assetId, "WEB");
```

### Download Task API

외부 URL의 파일을 S3로 비동기 다운로드합니다.

```java
DownloadTaskApi api = client.downloadTask();

// 다운로드 작업 생성
var response = api.create(new CreateDownloadTaskRequest(
    "https://example.com/image.jpg",    // sourceUrl
    "downloads/image.jpg",              // s3Key
    "my-bucket",                        // bucket
    "PUBLIC",                           // accessType
    "EXTERNAL_IMAGE",                   // purpose
    "CRAWLER",                          // source
    "https://my-service.com/callback"   // callbackUrl (선택, null 가능)
));

// 작업 상태 조회
var task = api.get(downloadTaskId);
// → status: PENDING → RUNNING → COMPLETED / FAILED
// → retryCount, maxRetries, lastError, startedAt, completedAt
```

### Transform Request API

이미지 리사이즈, 포맷 변환, 압축, 썸네일 생성을 수행합니다.

```java
TransformRequestApi api = client.transformRequest();

// 이미지 변환 요청
var response = api.create(new CreateTransformRequestRequest(
    sourceAssetId,      // 원본 자산 ID
    "RESIZE",           // transformType: RESIZE | CONVERT | COMPRESS | THUMBNAIL
    800,                // width (선택)
    600,                // height (선택)
    85,                 // quality 0-100 (선택)
    "WEBP"              // targetFormat (선택)
));

// 변환 상태 조회
var result = api.get(transformRequestId);
// → status: PENDING → PROCESSING → COMPLETED / FAILED
// → resultAssetId: 변환 완료 시 새 자산 ID
```

---

## 상품 이미지 업로드 시나리오

상품 등록 시 이미지 URL이 요청에 포함되는 경우, **URL의 유형에 따라 처리 방식이 다릅니다.**

### 전체 흐름

```
상품 등록 요청 (이미지 URL 포함)
    │
    ├─ 케이스 1: 내부 S3 URL (presigned URL로 업로드된 파일)
    │   → AssetApi.register() — 데이터 이동 없음, 즉시 처리
    │
    ├─ 케이스 2: 외부 도메인 URL (https://other-cdn.com/...)
    │   → DownloadTaskApi.create() → FileFlow 서버가 비동기 다운로드
    │
    └─ 케이스 3: 프론트엔드 직접 업로드
        → SingleUploadSessionApi → presigned URL 발급 → 업로드 → complete
    │
    ▼
Asset 확보 (assetId) → 상품 데이터에 연결
```

### 케이스 1: S3에 이미 업로드된 파일 등록 (register)

> **사용 시점**: 프론트엔드가 presigned URL로 S3에 업로드는 했지만 `complete()`를 호출하지 않은 경우,
> 또는 이미 S3에 존재하는 파일을 Asset으로 등록하고 싶은 경우

```java
/**
 * S3 URL에서 s3Key를 추출하여 Asset으로 등록한다.
 * S3에서 다시 다운로드하지 않으므로 네트워크 비용이 들지 않는다.
 */
public String registerExistingS3File(String s3Url) {
    String s3Key = extractS3Key(s3Url);
    String bucket = extractBucket(s3Url);

    var response = assetApi.register(new RegisterAssetRequest(
            s3Key, bucket, "PUBLIC",
            extractFileName(s3Key), guessContentType(s3Key),
            "PRODUCT_IMAGE", "product-service"
    ));

    return response.data().assetId();
}
```

**s3Key 추출 예시**:

```java
// https://fileflow-bucket.s3.ap-northeast-2.amazonaws.com/public/2026/02/image.jpg
// → s3Key: public/2026/02/image.jpg
// → bucket: fileflow-bucket

private String extractS3Key(String s3Url) {
    URI uri = URI.create(s3Url);
    String path = uri.getPath();
    return path.startsWith("/") ? path.substring(1) : path;
}

private String extractBucket(String s3Url) {
    URI uri = URI.create(s3Url);
    return uri.getHost().split("\\.")[0];
}
```

### 케이스 2: 외부 도메인 URL 다운로드

> **사용 시점**: 외부 이미지 서버 URL이 상품 등록 요청에 포함된 경우

```java
/**
 * 외부 URL의 이미지를 FileFlow 서버가 비동기로 다운로드하여 S3에 저장한다.
 * 완료 시 callback URL로 통지하거나 폴링으로 확인할 수 있다.
 */
public String downloadExternalImage(String externalUrl) {
    var response = downloadTaskApi.create(new CreateDownloadTaskRequest(
            externalUrl,                   // 다운로드할 외부 URL
            null,                          // s3Key (null이면 자동 생성)
            "fileflow-bucket",             // 저장할 S3 버킷
            "PRIVATE",                     // 접근 타입
            "PRODUCT_IMAGE",               // 용도
            "product-service",             // 호출 서비스
            "https://my-api.com/callback"  // 완료 시 콜백 URL (선택)
    ));

    return response.data().downloadTaskId();
}

// 비동기 완료 확인 (폴링 방식)
public DownloadTaskResponse waitForCompletion(String taskId) {
    while (true) {
        var task = downloadTaskApi.get(taskId).data();
        switch (task.status()) {
            case "COMPLETED" -> { return task; }
            case "FAILED"    -> throw new RuntimeException("다운로드 실패: " + task.lastError());
            default          -> sleep(1000);  // PENDING, PROCESSING 상태면 대기
        }
    }
}
```

### 케이스 3: 프론트엔드 직접 업로드 (presigned URL)

> **사용 시점**: 프론트엔드가 이미지 파일을 직접 S3에 업로드하는 정상 흐름

```java
// Step 1: 백엔드 — 세션 생성 및 presigned URL 발급
var session = singleUploadSessionApi.create(new CreateSingleUploadSessionRequest(
        fileName, contentType, "PUBLIC", "PRODUCT_IMAGE", "product-service"
));
// 프론트에 내려줄 값: session.data().presignedUrl(), session.data().sessionId()

// Step 2: 프론트엔드 — presigned URL로 S3에 PUT 업로드
// const response = await fetch(presignedUrl, { method: 'PUT', body: file });
// const etag = response.headers.get('ETag');

// Step 3: 백엔드 — 업로드 완료 처리 (프론트가 etag, fileSize를 전달)
singleUploadSessionApi.complete(sessionId,
        new CompleteSingleUploadSessionRequest(fileSize, etag)
);
// 완료되면 자동으로 Asset 생성됨
```

### 통합 처리: 상품 서비스에서의 이미지 URL 분기

```java
@Service
public class ProductImageService {

    private final AssetApi assetApi;
    private final DownloadTaskApi downloadTaskApi;

    public ProductImageService(AssetApi assetApi, DownloadTaskApi downloadTaskApi) {
        this.assetApi = assetApi;
        this.downloadTaskApi = downloadTaskApi;
    }

    /**
     * 상품 등록 시 이미지 URL을 받아서 적절한 방식으로 Asset을 확보한다.
     */
    public String processProductImage(String imageUrl) {
        if (isInternalS3Url(imageUrl)) {
            // 이미 S3에 존재 → register로 즉시 Asset 등록 (데이터 이동 없음)
            String s3Key = extractS3Key(imageUrl);
            String bucket = extractBucket(imageUrl);
            var response = assetApi.register(new RegisterAssetRequest(
                    s3Key, bucket, "PUBLIC",
                    extractFileName(s3Key), guessContentType(s3Key),
                    "PRODUCT_IMAGE", "product-service"
            ));
            return response.data().assetId();

        } else {
            // 외부 URL → DownloadTask로 비동기 다운로드
            var response = downloadTaskApi.create(new CreateDownloadTaskRequest(
                    imageUrl, null, "fileflow-bucket",
                    "PRIVATE", "PRODUCT_IMAGE",
                    "product-service", null
            ));
            return response.data().downloadTaskId();
            // 비동기 처리: callback 또는 폴링으로 완료 확인 필요
        }
    }

    private boolean isInternalS3Url(String url) {
        return url.contains(".s3.") && url.contains("amazonaws.com");
    }

    private String extractS3Key(String s3Url) {
        URI uri = URI.create(s3Url);
        String path = uri.getPath();
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private String extractBucket(String s3Url) {
        return URI.create(s3Url).getHost().split("\\.")[0];
    }

    private String extractFileName(String s3Key) {
        int lastSlash = s3Key.lastIndexOf('/');
        return lastSlash >= 0 ? s3Key.substring(lastSlash + 1) : s3Key;
    }

    private String guessContentType(String s3Key) {
        if (s3Key.endsWith(".jpg") || s3Key.endsWith(".jpeg")) return "image/jpeg";
        if (s3Key.endsWith(".png")) return "image/png";
        if (s3Key.endsWith(".webp")) return "image/webp";
        if (s3Key.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}
```

---

## Error Handling

SDK는 HTTP 상태 코드에 따라 구체적인 예외를 발생시킵니다.

```
FileFlowException (base)
├── FileFlowBadRequestException      (400)
├── FileFlowUnauthorizedException    (401)
├── FileFlowForbiddenException       (403)
├── FileFlowNotFoundException        (404)
├── FileFlowConflictException        (409)
└── FileFlowServerException          (5xx)
```

```java
try {
    var response = api.create(request);
} catch (FileFlowBadRequestException e) {
    log.warn("잘못된 요청: code={}, message={}", e.getErrorCode(), e.getErrorMessage());
} catch (FileFlowNotFoundException e) {
    log.warn("리소스 없음: {}", e.getErrorMessage());
} catch (FileFlowServerException e) {
    log.error("서버 에러: status={}", e.getStatusCode());
} catch (FileFlowException e) {
    log.error("기타 에러: {}", e.getMessage());
}
```

모든 예외는 `getStatusCode()`, `getErrorCode()`, `getErrorMessage()` 메서드를 제공합니다.

---

## Authentication

FileFlow는 Service-to-Service 토큰 인증을 사용합니다.
모든 요청에 다음 헤더가 자동 포함됩니다:

```
X-Service-Name: {serviceName}
X-Service-Token: {serviceToken}
```

---

## Auto-Configured Beans (Spring Boot)

`fileflow.base-url`이 설정되면 다음 빈이 자동 등록됩니다:

| Bean | 타입 | 설명 |
|------|------|------|
| `fileFlowClient` | `FileFlowClient` | 메인 클라이언트 |
| `singleUploadSessionApi` | `SingleUploadSessionApi` | 단일 업로드 세션 |
| `multipartUploadSessionApi` | `MultipartUploadSessionApi` | 멀티파트 업로드 세션 |
| `assetApi` | `AssetApi` | 자산 관리 |
| `downloadTaskApi` | `DownloadTaskApi` | 외부 다운로드 |
| `transformRequestApi` | `TransformRequestApi` | 이미지 변환 |

각 API 인터페이스를 직접 주입받아 사용할 수 있습니다.

---

## Configuration Options

### FileFlowClientBuilder

| 옵션 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `baseUrl` | String | (필수) | FileFlow API 서버 URL |
| `serviceName` | String | (필수) | 서비스 이름 |
| `serviceToken` | String | (필수) | 서비스 인증 토큰 |
| `connectTimeout` | Duration | 5초 | 연결 타임아웃 |
| `readTimeout` | Duration | 30초 | 읽기 타임아웃 |

### Spring Boot Properties

| 프로퍼티 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| `fileflow.base-url` | String | (필수) | FileFlow API 서버 URL |
| `fileflow.service-name` | String | (필수) | 서비스 이름 |
| `fileflow.service-token` | String | (필수) | 서비스 인증 토큰 |
| `fileflow.timeout.connect` | Duration | 5s | 연결 타임아웃 |
| `fileflow.timeout.read` | Duration | 30s | 읽기 타임아웃 |

---

## Requirements

- Java 21+
- Spring Boot 3.x (starter 사용 시)

## License

MIT License
