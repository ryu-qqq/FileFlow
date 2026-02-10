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

업로드 완료된 파일 자산을 조회/삭제합니다.

```java
AssetApi api = client.asset();

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
