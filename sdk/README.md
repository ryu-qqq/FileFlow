# FileFlow SDK

FileFlow 서버와 통신하기 위한 Java/Spring Boot SDK입니다. 파일 업로드, 다운로드 URL 생성, 외부 URL 다운로드 요청 등의 기능을 간편하게 사용할 수 있습니다.

## 목차

- [주요 기능](#주요-기능)
- [요구 사항](#요구-사항)
- [설치](#설치)
- [빠른 시작](#빠른-시작)
- [상세 사용법](#상세-사용법)
  - [클라이언트 생성](#클라이언트-생성)
  - [인증 설정](#인증-설정)
  - [FileAsset API](#fileasset-api)
  - [UploadSession API](#uploadsession-api)
  - [ExternalDownload API](#externaldownload-api)
- [비동기 클라이언트](#비동기-클라이언트)
- [Spring Boot 연동](#spring-boot-연동)
- [예외 처리](#예외-처리)
- [설정 옵션](#설정-옵션)

---

## 주요 기능

| 기능 | 설명 |
|------|------|
| **파일 다운로드 URL 생성** | S3 Presigned URL을 생성하여 파일 다운로드 |
| **배치 다운로드 URL** | 여러 파일의 다운로드 URL을 한 번에 생성 |
| **파일 업로드 세션** | Presigned PUT URL을 통한 직접 S3 업로드 |
| **외부 URL 다운로드** | 외부 URL에서 파일을 다운로드하여 저장 |
| **파일 관리** | 파일 조회, 삭제, 재시도 등 |

### 지원 클라이언트

- **동기 클라이언트** (`FileFlowClient`): Spring RestClient 기반
- **비동기 클라이언트** (`FileFlowAsyncClient`): Spring WebClient + Project Reactor 기반

---

## 요구 사항

- Java 21+
- Spring Framework 6.2+ / Spring Boot 3.4+

---

## 설치

### Gradle (JitPack)

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

// build.gradle
dependencies {
    // Spring Boot Starter (권장 - Auto Configuration 포함)
    implementation 'com.github.ryu-qqq.fileflow:fileflow-sdk-spring-boot-starter:VERSION'

    // 또는 Core만 사용
    implementation 'com.github.ryu-qqq.fileflow:fileflow-sdk-core:VERSION'
}
```

### Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.ryu-qqq.fileflow</groupId>
    <artifactId>fileflow-sdk-spring-boot-starter</artifactId>
    <version>VERSION</version>
</dependency>
```

> **Note**: `VERSION`은 GitHub 릴리스 태그 또는 커밋 해시로 대체하세요.

---

## 빠른 시작

### 1. 클라이언트 생성

```java
import com.ryuqq.fileflow.sdk.client.FileFlowClient;

FileFlowClient client = FileFlowClient.builder()
    .baseUrl("https://fileflow.example.com")
    .serviceToken("your-service-token")
    .build();
```

### 2. 파일 다운로드 URL 생성

```java
import com.ryuqq.fileflow.sdk.model.asset.DownloadUrlResponse;

// 기본 만료 시간(1시간)으로 다운로드 URL 생성
DownloadUrlResponse response = client.fileAssets()
    .generateDownloadUrl("file-asset-id");

System.out.println("Download URL: " + response.getDownloadUrl());
System.out.println("Expires At: " + response.getExpiresAt());
```

### 3. 파일 업로드

```java
import com.ryuqq.fileflow.sdk.model.session.*;

// 1. 업로드 세션 초기화 (Presigned URL 발급)
InitSingleUploadRequest request = InitSingleUploadRequest.builder()
    .filename("document.pdf")
    .contentType("application/pdf")
    .fileSize(1024000L)
    .category("documents")
    .build();

InitSingleUploadResponse session = client.uploadSessions()
    .initSingle(request);

// 2. Presigned URL로 파일 직접 업로드 (HTTP PUT)
// uploadToS3(session.getPresignedUrl(), fileBytes);

// 3. 업로드 완료 처리
client.uploadSessions().completeSingle(session.getSessionId());
```

---

## 상세 사용법

### 클라이언트 생성

#### 기본 설정

```java
FileFlowClient client = FileFlowClient.builder()
    .baseUrl("https://fileflow.example.com")
    .serviceToken("your-service-token")
    .build();
```

#### 전체 설정

```java
import java.time.Duration;

FileFlowClient client = FileFlowClient.builder()
    .baseUrl("https://fileflow.example.com")
    .serviceToken("your-service-token")
    .connectTimeout(Duration.ofSeconds(10))   // 연결 타임아웃 (기본: 5초)
    .readTimeout(Duration.ofSeconds(60))      // 읽기 타임아웃 (기본: 30초)
    .writeTimeout(Duration.ofSeconds(60))     // 쓰기 타임아웃 (기본: 30초)
    .logRequests(true)                        // 요청 로깅 활성화
    .build();
```

---

### 인증 설정

SDK는 유연한 인증 방식을 지원합니다.

#### 1. Service Token (가장 간단)

서버 간 통신에서 고정된 서비스 토큰을 사용합니다.

```java
FileFlowClient client = FileFlowClient.builder()
    .baseUrl("https://fileflow.example.com")
    .serviceToken("your-service-token")  // ThreadLocal 우선, 없으면 이 토큰 사용
    .build();
```

#### 2. ThreadLocal 토큰 전파

사용자 요청의 토큰을 그대로 전파하고 싶을 때 사용합니다.

```java
import com.ryuqq.fileflow.sdk.auth.FileFlowTokenHolder;

// 요청 처리 시작 시 토큰 설정
FileFlowTokenHolder.setToken(userAccessToken);

try {
    // SDK 호출 시 자동으로 ThreadLocal 토큰 사용
    client.fileAssets().generateDownloadUrl("file-id");
} finally {
    // 요청 처리 완료 후 정리
    FileFlowTokenHolder.clear();
}
```

#### 3. ThreadLocal + Service Token 폴백 (권장)

ThreadLocal에 토큰이 있으면 사용하고, 없으면 Service Token으로 폴백합니다.

```java
import com.ryuqq.fileflow.sdk.auth.ChainTokenResolver;

FileFlowClient client = FileFlowClient.builder()
    .baseUrl("https://fileflow.example.com")
    .tokenResolver(ChainTokenResolver.withFallback("service-token"))
    .build();

// 사용자 요청 컨텍스트에서 호출 → 사용자 토큰 사용
FileFlowTokenHolder.setToken(userToken);
client.fileAssets().get("file-id");

// 백그라운드 작업에서 호출 → 서비스 토큰 사용
FileFlowTokenHolder.clear();
client.fileAssets().get("file-id");
```

> **Note**: `serviceToken()`을 사용하면 내부적으로 `ChainTokenResolver.withFallback()`이 자동 적용됩니다.

#### 4. 커스텀 TokenResolver

```java
import com.ryuqq.fileflow.sdk.auth.TokenResolver;

TokenResolver customResolver = () -> {
    // 커스텀 로직으로 토큰 조회
    String token = myTokenService.getToken();
    return Optional.ofNullable(token);
};

FileFlowClient client = FileFlowClient.builder()
    .baseUrl("https://fileflow.example.com")
    .tokenResolver(customResolver)
    .build();
```

---

### FileAsset API

파일 자산 관리를 위한 API입니다.

#### 파일 조회

```java
import com.ryuqq.fileflow.sdk.model.asset.FileAssetResponse;

// 단일 파일 조회
FileAssetResponse file = client.fileAssets().get("file-asset-id");

System.out.println("File ID: " + file.getFileAssetId());
System.out.println("Filename: " + file.getOriginalFilename());
System.out.println("Size: " + file.getFileSize());
System.out.println("Status: " + file.getStatus());
```

#### 파일 목록 조회 (페이징)

```java
import com.ryuqq.fileflow.sdk.model.common.PageResponse;

// 페이지네이션으로 파일 목록 조회
PageResponse<FileAssetResponse> page = client.fileAssets().list(0, 20);

System.out.println("Total: " + page.getTotalElements());
System.out.println("Total Pages: " + page.getTotalPages());

for (FileAssetResponse file : page.getContent()) {
    System.out.println("- " + file.getOriginalFilename());
}
```

#### 다운로드 URL 생성

```java
import com.ryuqq.fileflow.sdk.model.asset.DownloadUrlResponse;
import java.time.Duration;

// 기본 만료 시간 (1시간)
DownloadUrlResponse url = client.fileAssets()
    .generateDownloadUrl("file-asset-id");

// 커스텀 만료 시간 (10분)
DownloadUrlResponse url = client.fileAssets()
    .generateDownloadUrl("file-asset-id", Duration.ofMinutes(10));

System.out.println("URL: " + url.getDownloadUrl());
System.out.println("Expires: " + url.getExpiresAt());
```

#### 배치 다운로드 URL 생성

```java
import java.util.List;

List<String> fileIds = List.of("file-1", "file-2", "file-3");

// 여러 파일의 다운로드 URL 한 번에 생성 (최대 100개)
List<DownloadUrlResponse> urls = client.fileAssets()
    .batchGenerateDownloadUrl(fileIds);

// 커스텀 만료 시간
List<DownloadUrlResponse> urls = client.fileAssets()
    .batchGenerateDownloadUrl(fileIds, Duration.ofHours(2));
```

#### 파일 삭제

```java
// 단일 파일 삭제 (소프트 삭제)
client.fileAssets().delete("file-asset-id");

// 배치 삭제
List<String> fileIds = List.of("file-1", "file-2", "file-3");
client.fileAssets().batchDelete(fileIds);
```

#### 파일 처리 재시도

```java
// 실패한 파일 처리 재시도
client.fileAssets().retry("file-asset-id");
```

#### 파일 통계 조회

```java
// 파일 자산 통계 조회
FileAssetStatisticsResponse stats = client.fileAssets().getStatistics();

System.out.println("Total Files: " + stats.getTotalCount());
System.out.println("Total Size: " + stats.getTotalSize());
System.out.println("By Status: " + stats.getStatusCounts());
```

---

### UploadSession API

파일 업로드 세션 관리를 위한 API입니다.

#### 단일 파일 업로드

```java
import com.ryuqq.fileflow.sdk.model.session.*;

// 1. 업로드 세션 초기화
InitSingleUploadRequest request = InitSingleUploadRequest.builder()
    .filename("photo.jpg")
    .contentType("image/jpeg")
    .fileSize(2048000L)         // 2MB
    .category("images")
    .metadata(Map.of(
        "userId", "user-123",
        "description", "Profile photo"
    ))
    .build();

InitSingleUploadResponse session = client.uploadSessions().initSingle(request);

System.out.println("Session ID: " + session.getSessionId());
System.out.println("Presigned URL: " + session.getPresignedUrl());
System.out.println("Expires At: " + session.getExpiresAt());

// 2. Presigned URL로 S3에 직접 업로드 (SDK 외부에서 처리)
// HTTP PUT 요청으로 파일 바이트를 presignedUrl에 업로드
//
// 예시 (RestTemplate 사용):
// HttpHeaders headers = new HttpHeaders();
// headers.setContentType(MediaType.parseMediaType("image/jpeg"));
// HttpEntity<byte[]> entity = new HttpEntity<>(fileBytes, headers);
// restTemplate.put(session.getPresignedUrl(), entity);

// 3. 업로드 완료 알림
client.uploadSessions().completeSingle(session.getSessionId());
```

#### 멀티파트 업로드 (대용량 파일)

5MB 이상의 대용량 파일은 멀티파트 업로드를 사용합니다.

```java
import com.ryuqq.fileflow.sdk.model.session.*;

// 1. 멀티파트 업로드 세션 초기화
InitMultipartUploadRequest request = InitMultipartUploadRequest.builder()
    .fileName("large-video.mp4")
    .fileSize(500_000_000L)     // 500MB
    .contentType("video/mp4")
    .partSize(10_000_000L)      // 파트당 10MB
    .uploadCategory("videos")
    .build();

InitMultipartUploadResponse session = client.uploadSessions().initMultipart(request);

System.out.println("Session ID: " + session.getSessionId());
System.out.println("Upload ID: " + session.getUploadId());
System.out.println("Total Parts: " + session.getTotalParts());

// 2. 각 파트에 대한 Presigned URL로 업로드
for (var part : session.getParts()) {
    System.out.println("Part " + part.getPartNumber() + ": " + part.getPresignedUrl());
    // HTTP PUT으로 각 파트 업로드
}

// 3. 파트 업로드 완료 표시
MarkPartUploadedRequest partRequest = MarkPartUploadedRequest.builder()
    .partNumber(1)
    .etag("\"abc123def456\"")  // S3 응답의 ETag
    .size(10_000_000L)
    .build();

client.uploadSessions().markPartUploaded(session.getSessionId(), partRequest);

// 4. 멀티파트 업로드 완료
CompleteMultipartUploadResponse result = client.uploadSessions()
    .completeMultipart(session.getSessionId());

System.out.println("Status: " + result.getStatus());
System.out.println("Completed At: " + result.getCompletedAt());
```

#### 업로드 세션 조회

```java
// 단일 세션 상세 조회
UploadSessionDetailResponse detail = client.uploadSessions().get("session-id");

System.out.println("Session ID: " + detail.getSessionId());
System.out.println("Status: " + detail.getStatus());
System.out.println("Upload Type: " + detail.getUploadType());
System.out.println("File Name: " + detail.getFileName());

// 세션 목록 조회 (페이징)
UploadSessionSearchRequest searchRequest = UploadSessionSearchRequest.builder()
    .page(0)
    .size(20)
    .status(SessionStatus.PENDING)
    .build();

PageResponse<UploadSessionResponse> sessions = client.uploadSessions().list(searchRequest);

for (UploadSessionResponse session : sessions.getContent()) {
    System.out.println("- " + session.getSessionId() + ": " + session.getStatus());
}
```

#### 업로드 취소

```java
// 업로드 세션 취소
CancelUploadSessionResponse response = client.uploadSessions().cancel("session-id");

System.out.println("Cancelled Session: " + response.getSessionId());
System.out.println("Status: " + response.getStatus());
```

---

### ExternalDownload API

외부 URL에서 파일을 다운로드하여 저장하는 API입니다.

#### 외부 URL 다운로드 요청

```java
// 기본 요청
String downloadId = client.externalDownloads()
    .request(
        "550e8400-e29b-41d4-a716-446655440000",     // 멱등성 키 (UUID)
        "https://example.com/files/document.pdf"    // 소스 URL
    );

System.out.println("Download ID: " + downloadId);
```

#### Webhook 알림 포함

```java
// 다운로드 완료 시 webhook으로 알림 받기
String downloadId = client.externalDownloads()
    .request(
        "550e8400-e29b-41d4-a716-446655440001",            // 멱등성 키 (UUID)
        "https://example.com/files/video.mp4",             // 소스 URL
        "https://my-server.com/webhook/download-complete"  // Webhook URL
    );
```

#### 다운로드 요청 조회

```java
// 단일 다운로드 요청 상세 조회
ExternalDownloadDetailResponse detail = client.externalDownloads().get(downloadId);

System.out.println("Download ID: " + detail.getId());
System.out.println("Status: " + detail.getStatus());
System.out.println("Source URL: " + detail.getSourceUrl());
System.out.println("File Asset ID: " + detail.getFileAssetId());  // 완료 시

// 다운로드 요청 목록 조회 (페이징)
ExternalDownloadSearchRequest searchRequest = ExternalDownloadSearchRequest.builder()
    .page(0)
    .size(20)
    .status("COMPLETED")
    .build();

PageResponse<ExternalDownloadResponse> downloads = client.externalDownloads().list(searchRequest);

for (ExternalDownloadResponse download : downloads.getContent()) {
    System.out.println("- " + download.getId() + ": " + download.getStatus());
}
```

---

## 비동기 클라이언트

WebClient + Project Reactor 기반의 비동기 클라이언트를 제공합니다.

### 비동기 클라이언트 생성

```java
import com.ryuqq.fileflow.sdk.client.FileFlowAsyncClient;

FileFlowAsyncClient asyncClient = FileFlowClient.builder()
    .baseUrl("https://fileflow.example.com")
    .serviceToken("your-service-token")
    .buildAsync();  // buildAsync() 사용
```

### 비동기 API 사용 예시

```java
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

// 단일 파일 다운로드 URL 생성
Mono<DownloadUrlResponse> urlMono = asyncClient.fileAssets()
    .generateDownloadUrl("file-asset-id");

urlMono.subscribe(response -> {
    System.out.println("URL: " + response.getDownloadUrl());
});

// 배치 다운로드 URL 생성 (Flux 반환)
Flux<DownloadUrlResponse> urlFlux = asyncClient.fileAssets()
    .batchGenerateDownloadUrl(List.of("file-1", "file-2", "file-3"));

urlFlux.subscribe(response -> {
    System.out.println("URL: " + response.getDownloadUrl());
});

// 블로킹 방식으로 결과 받기
DownloadUrlResponse result = urlMono.block();
```

### WebFlux 컨트롤러에서 사용

```java
@RestController
public class FileController {

    private final FileFlowAsyncClient fileFlowClient;

    public FileController(FileFlowAsyncClient fileFlowClient) {
        this.fileFlowClient = fileFlowClient;
    }

    @GetMapping("/files/{id}/download-url")
    public Mono<DownloadUrlResponse> getDownloadUrl(@PathVariable String id) {
        return fileFlowClient.fileAssets().generateDownloadUrl(id);
    }

    @GetMapping("/files/{id}")
    public Mono<FileAssetResponse> getFile(@PathVariable String id) {
        return fileFlowClient.fileAssets().get(id);
    }
}
```

---

## Spring Boot 연동

### Auto Configuration

`fileflow-sdk-spring-boot-starter`를 사용하면 자동으로 클라이언트 빈이 생성됩니다.

#### application.yml 설정

```yaml
fileflow:
  base-url: https://fileflow.example.com
  service-token: ${FILEFLOW_SERVICE_TOKEN}

  # 선택 옵션
  connect-timeout: 5s      # 기본값: 5초
  read-timeout: 30s        # 기본값: 30초
  write-timeout: 30s       # 기본값: 30초
  log-requests: false      # 기본값: false
  async: false             # true면 FileFlowAsyncClient 빈 생성
```

#### 동기 클라이언트 주입

```java
@Service
public class FileService {

    private final FileFlowClient fileFlowClient;

    public FileService(FileFlowClient fileFlowClient) {
        this.fileFlowClient = fileFlowClient;
    }

    public String getDownloadUrl(String fileId) {
        return fileFlowClient.fileAssets()
            .generateDownloadUrl(fileId)
            .getDownloadUrl();
    }
}
```

#### 비동기 클라이언트 사용

```yaml
fileflow:
  base-url: https://fileflow.example.com
  service-token: ${FILEFLOW_SERVICE_TOKEN}
  async: true  # 비동기 클라이언트 활성화
```

```java
@Service
public class FileService {

    private final FileFlowAsyncClient fileFlowClient;

    public FileService(FileFlowAsyncClient fileFlowClient) {
        this.fileFlowClient = fileFlowClient;
    }

    public Mono<String> getDownloadUrl(String fileId) {
        return fileFlowClient.fileAssets()
            .generateDownloadUrl(fileId)
            .map(DownloadUrlResponse::getDownloadUrl);
    }
}
```

#### 커스텀 클라이언트 빈 등록

Auto Configuration을 오버라이드할 수 있습니다.

```java
@Configuration
public class FileFlowConfig {

    @Bean
    public FileFlowClient fileFlowClient() {
        return FileFlowClient.builder()
            .baseUrl("https://fileflow.example.com")
            .tokenResolver(customTokenResolver())
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    private TokenResolver customTokenResolver() {
        return () -> {
            // 커스텀 토큰 로직
            return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> auth.getCredentials().toString());
        };
    }
}
```

---

## 예외 처리

SDK는 HTTP 상태 코드에 따라 적절한 예외를 발생시킵니다.

### 예외 계층 구조

```
FileFlowException (base)
├── FileFlowBadRequestException (400)
├── FileFlowUnauthorizedException (401)
├── FileFlowForbiddenException (403)
├── FileFlowNotFoundException (404)
└── FileFlowServerException (500+)
```

### 예외 처리 예시

```java
import com.ryuqq.fileflow.sdk.exception.*;

try {
    FileAssetResponse file = client.fileAssets().get("non-existent-id");
} catch (FileFlowNotFoundException e) {
    System.out.println("File not found: " + e.getMessage());
    System.out.println("Error code: " + e.getErrorCode());
} catch (FileFlowUnauthorizedException e) {
    System.out.println("Authentication failed: " + e.getMessage());
} catch (FileFlowForbiddenException e) {
    System.out.println("Access denied: " + e.getMessage());
} catch (FileFlowBadRequestException e) {
    System.out.println("Bad request: " + e.getMessage());
} catch (FileFlowServerException e) {
    System.out.println("Server error (" + e.getStatusCode() + "): " + e.getMessage());
} catch (FileFlowException e) {
    System.out.println("Unknown error: " + e.getMessage());
}
```

### 예외 정보

```java
try {
    client.fileAssets().get("file-id");
} catch (FileFlowException e) {
    int statusCode = e.getStatusCode();      // HTTP 상태 코드
    String errorCode = e.getErrorCode();     // 에러 코드 (예: "FILE_NOT_FOUND")
    String message = e.getMessage();         // 상세 메시지
}
```

### Spring @ExceptionHandler와 함께 사용

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileFlowNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(FileFlowNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(FileFlowException.class)
    public ResponseEntity<ErrorResponse> handleFileFlowException(FileFlowException e) {
        return ResponseEntity.status(e.getStatusCode())
            .body(new ErrorResponse(e.getErrorCode(), e.getMessage()));
    }
}
```

---

## 설정 옵션

### FileFlowClientBuilder 옵션

| 옵션 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `baseUrl` | String | (필수) | FileFlow API 서버 URL |
| `serviceToken` | String | - | 서비스 인증 토큰 |
| `tokenResolver` | TokenResolver | - | 커스텀 토큰 리졸버 |
| `connectTimeout` | Duration | 5초 | 연결 타임아웃 |
| `readTimeout` | Duration | 30초 | 읽기 타임아웃 |
| `writeTimeout` | Duration | 30초 | 쓰기 타임아웃 |
| `logRequests` | boolean | false | HTTP 요청 로깅 활성화 |

### Spring Boot Properties

| 프로퍼티 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| `fileflow.base-url` | String | (필수) | FileFlow API 서버 URL |
| `fileflow.service-token` | String | (필수) | 서비스 인증 토큰 |
| `fileflow.connect-timeout` | Duration | 5s | 연결 타임아웃 |
| `fileflow.read-timeout` | Duration | 30s | 읽기 타임아웃 |
| `fileflow.write-timeout` | Duration | 30s | 쓰기 타임아웃 |
| `fileflow.log-requests` | boolean | false | HTTP 요청 로깅 활성화 |
| `fileflow.async` | boolean | false | 비동기 클라이언트 사용 |

---

## API 레퍼런스

### FileFlowClient

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `fileAssets()` | FileAssetApi | FileAsset API 접근 |
| `uploadSessions()` | UploadSessionApi | UploadSession API 접근 |
| `externalDownloads()` | ExternalDownloadApi | ExternalDownload API 접근 |

### FileAssetApi

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `get(fileAssetId)` | FileAssetResponse | 파일 정보 조회 |
| `list(page, size)` | PageResponse | 파일 목록 조회 |
| `generateDownloadUrl(fileAssetId)` | DownloadUrlResponse | 다운로드 URL 생성 |
| `generateDownloadUrl(fileAssetId, expiresIn)` | DownloadUrlResponse | 만료 시간 지정 URL 생성 |
| `batchGenerateDownloadUrl(fileAssetIds)` | List | 배치 URL 생성 |
| `batchGenerateDownloadUrl(fileAssetIds, expiresIn)` | List | 배치 URL 생성 (만료 지정) |
| `delete(fileAssetId)` | void | 파일 삭제 |
| `batchDelete(fileAssetIds)` | void | 배치 삭제 |
| `retry(fileAssetId)` | void | 처리 재시도 |
| `getStatistics()` | FileAssetStatisticsResponse | 파일 통계 조회 |

### UploadSessionApi

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `initSingle(request)` | InitSingleUploadResponse | 단일 업로드 세션 초기화 |
| `completeSingle(sessionId)` | void | 단일 업로드 완료 처리 |
| `initMultipart(request)` | InitMultipartUploadResponse | 멀티파트 업로드 세션 초기화 |
| `markPartUploaded(sessionId, request)` | MarkPartUploadedResponse | 파트 업로드 완료 표시 |
| `completeMultipart(sessionId)` | CompleteMultipartUploadResponse | 멀티파트 업로드 완료 |
| `get(sessionId)` | UploadSessionDetailResponse | 세션 상세 조회 |
| `list(request)` | PageResponse | 세션 목록 조회 |
| `cancel(sessionId)` | CancelUploadSessionResponse | 업로드 취소 |

### ExternalDownloadApi

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `request(idempotencyKey, sourceUrl)` | String | 외부 URL 다운로드 요청 |
| `request(idempotencyKey, sourceUrl, webhookUrl)` | String | Webhook 포함 다운로드 요청 |
| `get(downloadId)` | ExternalDownloadDetailResponse | 다운로드 상세 조회 |
| `list(request)` | PageResponse | 다운로드 목록 조회 |

---

## 라이선스

MIT License
