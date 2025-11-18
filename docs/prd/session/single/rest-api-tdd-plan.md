# REST API Layer TDD Plan - Single Presigned URL Upload

**Kent Beck TDD + Tidy First 철학 적용**
- Red (test:) → Green (feat:) → Refactor (struct:) 패턴
- 작은 커밋, 한 번에 한 가지만 변경
- Zero-Tolerance 규칙 자동 준수

---

## Cycle 1: GeneratePresignedUrlRequest DTO

#### Red (test:)

**파일**: `adapter-in/rest-api/src/test/java/.../dto/request/GeneratePresignedUrlRequestTest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request;

import com.ryuqq.fileflow.support.fixture.SessionRequestFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GeneratePresignedUrlRequest 테스트")
class GeneratePresignedUrlRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("유효한 요청을 생성해야 한다")
    void shouldCreateValidRequest() {
        // given
        GeneratePresignedUrlRequest request = SessionRequestFixture.aGeneratePresignedUrlRequest();

        // when
        Set<ConstraintViolation<GeneratePresignedUrlRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
        assertThat(request.sessionId()).isNotBlank();
        assertThat(request.fileName()).isNotBlank();
        assertThat(request.fileSize()).isPositive();
        assertThat(request.mimeType()).isNotBlank();
        assertThat(request.category()).isNotBlank();
        assertThat(request.uploaderType()).isNotBlank();
    }

    @Test
    @DisplayName("sessionId가 null이면 검증 실패해야 한다")
    void shouldFailWhenSessionIdIsNull() {
        // given
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
            null, "test.jpg", 1024L, "image/jpeg", "banner", "ADMIN"
        );

        // when
        Set<ConstraintViolation<GeneratePresignedUrlRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("sessionId");
    }

    @Test
    @DisplayName("fileName이 빈 문자열이면 검증 실패해야 한다")
    void shouldFailWhenFileNameIsBlank() {
        // given
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
            "01JD8001-1234-5678-9abc-def012345678", "", 1024L, "image/jpeg", "banner", "ADMIN"
        );

        // when
        Set<ConstraintViolation<GeneratePresignedUrlRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("fileName");
    }

    @Test
    @DisplayName("fileSize가 0 이하이면 검증 실패해야 한다")
    void shouldFailWhenFileSizeIsNotPositive() {
        // given
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
            "01JD8001-1234-5678-9abc-def012345678", "test.jpg", 0L, "image/jpeg", "banner", "ADMIN"
        );

        // when
        Set<ConstraintViolation<GeneratePresignedUrlRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("fileSize");
    }

    @Test
    @DisplayName("mimeType이 null이면 검증 실패해야 한다")
    void shouldFailWhenMimeTypeIsNull() {
        // given
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
            "01JD8001-1234-5678-9abc-def012345678", "test.jpg", 1024L, null, "banner", "ADMIN"
        );

        // when
        Set<ConstraintViolation<GeneratePresignedUrlRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("mimeType");
    }
}
```

#### Green (feat:)

**파일**: `adapter-in/rest-api/src/main/java/.../dto/request/GeneratePresignedUrlRequest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Presigned URL 생성 요청 DTO
 *
 * @param sessionId 세션 ID (UUID v7, Idempotent Key)
 * @param fileName 파일명
 * @param fileSize 파일 크기 (bytes)
 * @param mimeType MIME 타입 (예: image/jpeg)
 * @param category 파일 카테고리 (예: banner)
 * @param uploaderType 업로더 타입 (ADMIN/CUSTOMER)
 */
public record GeneratePresignedUrlRequest(
    @NotBlank(message = "sessionId는 필수입니다")
    String sessionId,

    @NotBlank(message = "fileName은 필수입니다")
    String fileName,

    @Positive(message = "fileSize는 양수여야 합니다")
    long fileSize,

    @NotNull(message = "mimeType은 필수입니다")
    String mimeType,

    @NotBlank(message = "category는 필수입니다")
    String category,

    @NotBlank(message = "uploaderType은 필수입니다")
    String uploaderType
) {
}
```

**파일**: `adapter-in/rest-api/src/testFixtures/java/.../fixture/SessionRequestFixture.java`

```java
package com.ryuqq.fileflow.support.fixture;

import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.GeneratePresignedUrlRequest;

public class SessionRequestFixture {

    public static GeneratePresignedUrlRequest aGeneratePresignedUrlRequest() {
        return new GeneratePresignedUrlRequest(
            "01JD8001-1234-5678-9abc-def012345678",
            "example.jpg",
            1048576L,
            "image/jpeg",
            "banner",
            "ADMIN"
        );
    }

    public static GeneratePresignedUrlRequest withSessionId(String sessionId) {
        return new GeneratePresignedUrlRequest(
            sessionId,
            "example.jpg",
            1048576L,
            "image/jpeg",
            "banner",
            "ADMIN"
        );
    }
}
```

#### 커밋

```bash
test: GeneratePresignedUrlRequest DTO 검증 테스트 추가
feat: GeneratePresignedUrlRequest DTO 구현 (6개 필드 검증)
```

---

## Cycle 2: CompleteUploadRequest DTO

#### Red (test:)

**파일**: `adapter-in/rest-api/src/test/java/.../dto/request/CompleteUploadRequestTest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request;

import com.ryuqq.fileflow.support.fixture.SessionRequestFixture;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CompleteUploadRequest 테스트")
class CompleteUploadRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("유효한 요청을 생성해야 한다")
    void shouldCreateValidRequest() {
        // given
        CompleteUploadRequest request = SessionRequestFixture.aCompleteUploadRequest();

        // when
        Set<ConstraintViolation<CompleteUploadRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
        assertThat(request.sessionId()).isNotBlank();
        assertThat(request.fileSize()).isPositive();
    }

    @Test
    @DisplayName("sessionId가 null이면 검증 실패해야 한다")
    void shouldFailWhenSessionIdIsNull() {
        // given
        CompleteUploadRequest request = new CompleteUploadRequest(null, 1024L);

        // when
        Set<ConstraintViolation<CompleteUploadRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("sessionId");
    }

    @Test
    @DisplayName("fileSize가 0 이하이면 검증 실패해야 한다")
    void shouldFailWhenFileSizeIsNotPositive() {
        // given
        CompleteUploadRequest request = new CompleteUploadRequest(
            "01JD8001-1234-5678-9abc-def012345678", 0L
        );

        // when
        Set<ConstraintViolation<CompleteUploadRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("fileSize");
    }
}
```

#### Green (feat:)

**파일**: `adapter-in/rest-api/src/main/java/.../dto/request/CompleteUploadRequest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * 업로드 완료 요청 DTO
 *
 * @param sessionId 세션 ID (UUID v7, Idempotent Key)
 * @param fileSize 실제 업로드된 파일 크기 (bytes)
 */
public record CompleteUploadRequest(
    @NotBlank(message = "sessionId는 필수입니다")
    String sessionId,

    @Positive(message = "fileSize는 양수여야 합니다")
    long fileSize
) {
}
```

**파일**: `adapter-in/rest-api/src/testFixtures/java/.../fixture/SessionRequestFixture.java` (추가)

```java
public static CompleteUploadRequest aCompleteUploadRequest() {
    return new CompleteUploadRequest(
        "01JD8001-1234-5678-9abc-def012345678",
        1048576L
    );
}

public static CompleteUploadRequest withSessionIdAndSize(String sessionId, long fileSize) {
    return new CompleteUploadRequest(sessionId, fileSize);
}
```

#### 커밋

```bash
test: CompleteUploadRequest DTO 검증 테스트 추가
feat: CompleteUploadRequest DTO 구현 (2개 필드 검증)
```

---

## Cycle 3: PresignedUrlResponse DTO

#### Red (test:)

**파일**: `adapter-in/rest-api/src/test/java/.../dto/response/PresignedUrlResponseTest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.dto.response;

import com.ryuqq.fileflow.support.fixture.SessionResponseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PresignedUrlResponse 테스트")
class PresignedUrlResponseTest {

    @Test
    @DisplayName("Response를 생성해야 한다")
    void shouldCreateResponse() {
        // given
        String presignedUrl = "https://s3.amazonaws.com/bucket/file.jpg?signature=xyz";
        ZonedDateTime expiresAt = ZonedDateTime.now().plusMinutes(5);

        // when
        PresignedUrlResponse response = new PresignedUrlResponse(presignedUrl, expiresAt);

        // then
        assertThat(response.presignedUrl()).isEqualTo(presignedUrl);
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("Fixture로 Response를 생성해야 한다")
    void shouldCreateResponseFromFixture() {
        // when
        PresignedUrlResponse response = SessionResponseFixture.aPresignedUrlResponse();

        // then
        assertThat(response.presignedUrl()).isNotBlank();
        assertThat(response.expiresAt()).isNotNull();
    }
}
```

#### Green (feat:)

**파일**: `adapter-in/rest-api/src/main/java/.../dto/response/PresignedUrlResponse.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.dto.response;

import java.time.ZonedDateTime;

/**
 * Presigned URL 응답 DTO
 *
 * @param presignedUrl S3 Presigned URL (5분 유효)
 * @param expiresAt URL 만료 시각 (UTC)
 */
public record PresignedUrlResponse(
    String presignedUrl,
    ZonedDateTime expiresAt
) {
}
```

**파일**: `adapter-in/rest-api/src/testFixtures/java/.../fixture/SessionResponseFixture.java`

```java
package com.ryuqq.fileflow.support.fixture;

import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.response.PresignedUrlResponse;

import java.time.ZonedDateTime;

public class SessionResponseFixture {

    public static PresignedUrlResponse aPresignedUrlResponse() {
        return new PresignedUrlResponse(
            "https://s3.amazonaws.com/bucket/example.jpg?X-Amz-Signature=abc123",
            ZonedDateTime.now().plusMinutes(5)
        );
    }

    public static PresignedUrlResponse withUrl(String presignedUrl) {
        return new PresignedUrlResponse(presignedUrl, ZonedDateTime.now().plusMinutes(5));
    }
}
```

#### 커밋

```bash
test: PresignedUrlResponse DTO 테스트 추가
feat: PresignedUrlResponse DTO 구현 (2개 필드)
```

---

## Cycle 4: FileResponse DTO

#### Red (test:)

**파일**: `adapter-in/rest-api/src/test/java/.../dto/response/FileResponseTest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.dto.response;

import com.ryuqq.fileflow.support.fixture.SessionResponseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FileResponse 테스트")
class FileResponseTest {

    @Test
    @DisplayName("Response를 생성해야 한다")
    void shouldCreateResponse() {
        // given
        String fileId = "01JD8001-1234-5678-9abc-def012345678";
        String fileName = "example.jpg";

        // when
        FileResponse response = new FileResponse(
            fileId, fileName, 1048576L, "image/jpeg",
            "banner", "ADMIN", "https://cdn.example.com/files/example.jpg",
            "ACTIVE", "01JD8002-2345-6789-abcd-ef0123456789"
        );

        // then
        assertThat(response.fileId()).isEqualTo(fileId);
        assertThat(response.fileName()).isEqualTo(fileName);
        assertThat(response.fileSize()).isEqualTo(1048576L);
        assertThat(response.mimeType()).isEqualTo("image/jpeg");
        assertThat(response.category()).isEqualTo("banner");
        assertThat(response.uploaderType()).isEqualTo("ADMIN");
        assertThat(response.fileUrl()).isEqualTo("https://cdn.example.com/files/example.jpg");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.uploaderId()).isEqualTo("01JD8002-2345-6789-abcd-ef0123456789");
    }

    @Test
    @DisplayName("Fixture로 Response를 생성해야 한다")
    void shouldCreateResponseFromFixture() {
        // when
        FileResponse response = SessionResponseFixture.aFileResponse();

        // then
        assertThat(response.fileId()).isNotBlank();
        assertThat(response.fileName()).isNotBlank();
        assertThat(response.fileSize()).isPositive();
    }
}
```

#### Green (feat:)

**파일**: `adapter-in/rest-api/src/main/java/.../dto/response/FileResponse.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.dto.response;

/**
 * 파일 응답 DTO
 *
 * @param fileId 파일 ID (UUID v7)
 * @param fileName 파일명
 * @param fileSize 파일 크기 (bytes)
 * @param mimeType MIME 타입
 * @param category 파일 카테고리
 * @param uploaderType 업로더 타입
 * @param fileUrl 파일 URL (CDN)
 * @param status 파일 상태 (ACTIVE)
 * @param uploaderId 업로더 ID (User UUID v7)
 */
public record FileResponse(
    String fileId,
    String fileName,
    long fileSize,
    String mimeType,
    String category,
    String uploaderType,
    String fileUrl,
    String status,
    String uploaderId
) {
}
```

**파일**: `adapter-in/rest-api/src/testFixtures/java/.../fixture/SessionResponseFixture.java` (추가)

```java
public static FileResponse aFileResponse() {
    return new FileResponse(
        "01JD8001-1234-5678-9abc-def012345678",
        "example.jpg",
        1048576L,
        "image/jpeg",
        "banner",
        "ADMIN",
        "https://cdn.example.com/files/example.jpg",
        "ACTIVE",
        "01JD8002-2345-6789-abcd-ef0123456789"
    );
}

public static FileResponse withFileId(String fileId) {
    return new FileResponse(
        fileId,
        "example.jpg",
        1048576L,
        "image/jpeg",
        "banner",
        "ADMIN",
        "https://cdn.example.com/files/example.jpg",
        "ACTIVE",
        "01JD8002-2345-6789-abcd-ef0123456789"
    );
}
```

#### 커밋

```bash
test: FileResponse DTO 테스트 추가
feat: FileResponse DTO 구현 (9개 필드)
```

---

## Cycle 5: FileApiController (MockMvc 테스트)

#### Red (test:)

**파일**: `adapter-in/rest-api/src/test/java/.../controller/FileApiControllerTest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.CompleteUploadRequest;
import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.GeneratePresignedUrlRequest;
import com.ryuqq.fileflow.application.session.dto.response.FileResponse;
import com.ryuqq.fileflow.application.session.dto.response.PresignedUrlResponse;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.GeneratePresignedUrlUseCase;
import com.ryuqq.fileflow.support.fixture.SessionRequestFixture;
import com.ryuqq.fileflow.support.fixture.SessionResponseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileApiController.class)
@DisplayName("FileApiController 테스트")
class FileApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GeneratePresignedUrlUseCase generatePresignedUrlUseCase;

    @MockBean
    private CompleteUploadUseCase completeUploadUseCase;

    @Test
    @DisplayName("POST /presigned-url - Presigned URL을 생성해야 한다")
    void shouldGeneratePresignedUrl() throws Exception {
        // given
        GeneratePresignedUrlRequest request = SessionRequestFixture.aGeneratePresignedUrlRequest();
        PresignedUrlResponse response = SessionResponseFixture.aPresignedUrlResponse();

        given(generatePresignedUrlUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.presignedUrl").isNotEmpty())
            .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /presigned-url - sessionId가 없으면 400 반환해야 한다")
    void shouldReturn400WhenSessionIdIsMissing() throws Exception {
        // given
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
            null, "test.jpg", 1024L, "image/jpeg", "banner", "ADMIN"
        );

        // when & then
        mockMvc.perform(post("/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /upload-complete - 업로드를 완료해야 한다")
    void shouldCompleteUpload() throws Exception {
        // given
        CompleteUploadRequest request = SessionRequestFixture.aCompleteUploadRequest();
        FileResponse response = SessionResponseFixture.aFileResponse();

        given(completeUploadUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/upload-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileId").isNotEmpty())
            .andExpect(jsonPath("$.fileName").isNotEmpty())
            .andExpect(jsonPath("$.fileUrl").isNotEmpty())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /upload-complete - sessionId가 없으면 400 반환해야 한다")
    void shouldReturn400WhenSessionIdIsMissingInCompleteUpload() throws Exception {
        // given
        CompleteUploadRequest request = new CompleteUploadRequest(null, 1024L);

        // when & then
        mockMvc.perform(post("/upload-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
```

#### Green (feat:)

**파일**: `adapter-in/rest-api/src/main/java/.../controller/FileApiController.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.controller;

import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.CompleteUploadRequest;
import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.GeneratePresignedUrlRequest;
import com.ryuqq.fileflow.adapter.in.rest.api.session.mapper.SessionApiMapper;
import com.ryuqq.fileflow.application.session.dto.response.FileResponse;
import com.ryuqq.fileflow.application.session.dto.response.PresignedUrlResponse;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.GeneratePresignedUrlUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileApiController {

    private final GeneratePresignedUrlUseCase generatePresignedUrlUseCase;
    private final CompleteUploadUseCase completeUploadUseCase;
    private final SessionApiMapper sessionApiMapper;

    public FileApiController(
        GeneratePresignedUrlUseCase generatePresignedUrlUseCase,
        CompleteUploadUseCase completeUploadUseCase,
        SessionApiMapper sessionApiMapper
    ) {
        this.generatePresignedUrlUseCase = generatePresignedUrlUseCase;
        this.completeUploadUseCase = completeUploadUseCase;
        this.sessionApiMapper = sessionApiMapper;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
        @Valid @RequestBody GeneratePresignedUrlRequest request
    ) {
        PresignedUrlResponse response = generatePresignedUrlUseCase.execute(
            sessionApiMapper.toCommand(request)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-complete")
    public ResponseEntity<FileResponse> completeUpload(
        @Valid @RequestBody CompleteUploadRequest request
    ) {
        FileResponse response = completeUploadUseCase.execute(
            sessionApiMapper.toCommand(request)
        );
        return ResponseEntity.ok(response);
    }
}
```

#### 커밋

```bash
test: FileApiController MockMvc 테스트 추가 (2개 엔드포인트)
feat: FileApiController 구현 (POST /presigned-url, POST /upload-complete)
```

---

## Cycle 6: SessionApiMapper

#### Red (test:)

**파일**: `adapter-in/rest-api/src/test/java/.../mapper/SessionApiMapperTest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.mapper;

import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.CompleteUploadRequest;
import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.GeneratePresignedUrlRequest;
import com.ryuqq.fileflow.application.session.dto.command.CompleteUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.support.fixture.SessionRequestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SessionApiMapper 테스트")
class SessionApiMapperTest {

    private final SessionApiMapper mapper = new SessionApiMapper();

    @Test
    @DisplayName("GeneratePresignedUrlRequest를 Command로 변환해야 한다")
    void shouldMapGeneratePresignedUrlRequestToCommand() {
        // given
        GeneratePresignedUrlRequest request = SessionRequestFixture.aGeneratePresignedUrlRequest();

        // when
        GeneratePresignedUrlCommand command = mapper.toCommand(request);

        // then
        assertThat(command.sessionId().value()).isEqualTo(request.sessionId());
        assertThat(command.fileName().value()).isEqualTo(request.fileName());
        assertThat(command.fileSize().value()).isEqualTo(request.fileSize());
        assertThat(command.mimeType().value()).isEqualTo(request.mimeType());
        assertThat(command.category().category()).isEqualTo(request.category());
        assertThat(command.category().uploaderType().name()).isEqualTo(request.uploaderType());
    }

    @Test
    @DisplayName("CompleteUploadRequest를 Command로 변환해야 한다")
    void shouldMapCompleteUploadRequestToCommand() {
        // given
        CompleteUploadRequest request = SessionRequestFixture.aCompleteUploadRequest();

        // when
        CompleteUploadCommand command = mapper.toCommand(request);

        // then
        assertThat(command.sessionId().value()).isEqualTo(request.sessionId());
        assertThat(command.fileSize().value()).isEqualTo(request.fileSize());
    }
}
```

#### Green (feat:)

**파일**: `adapter-in/rest-api/src/main/java/.../mapper/SessionApiMapper.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.session.mapper;

import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.CompleteUploadRequest;
import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.GeneratePresignedUrlRequest;
import com.ryuqq.fileflow.application.session.dto.command.CompleteUploadCommand;
import com.ryuqq.fileflow.application.session.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.domain.session.vo.*;
import org.springframework.stereotype.Component;

@Component
public class SessionApiMapper {

    public GeneratePresignedUrlCommand toCommand(GeneratePresignedUrlRequest request) {
        return new GeneratePresignedUrlCommand(
            SessionId.of(request.sessionId()),
            FileName.of(request.fileName()),
            FileSize.of(request.fileSize()),
            MimeType.of(request.mimeType()),
            FileCategory.of(request.category(), UploaderType.valueOf(request.uploaderType()))
        );
    }

    public CompleteUploadCommand toCommand(CompleteUploadRequest request) {
        return new CompleteUploadCommand(
            SessionId.of(request.sessionId()),
            FileSize.of(request.fileSize())
        );
    }
}
```

#### 커밋

```bash
test: SessionApiMapper 테스트 추가 (Request → Command 변환)
feat: SessionApiMapper 구현 (VO 변환 로직)
```

---

## Cycle 7: GlobalExceptionHandler (Domain 예외 매핑)

#### Red (test:)

**파일**: `adapter-in/rest-api/src/test/java/.../exception/GlobalExceptionHandlerTest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.exception;

import com.ryuqq.fileflow.domain.session.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("InvalidSessionIdException을 400으로 매핑해야 한다")
    void shouldMapInvalidSessionIdExceptionTo400() {
        // given
        InvalidSessionIdException ex = new InvalidSessionIdException("Invalid session ID format");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleInvalidSessionIdException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("Invalid session ID");
    }

    @Test
    @DisplayName("SessionNotFoundException을 404로 매핑해야 한다")
    void shouldMapSessionNotFoundExceptionTo404() {
        // given
        SessionNotFoundException ex = new SessionNotFoundException("Session not found");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleSessionNotFoundException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).contains("Session not found");
    }

    @Test
    @DisplayName("SessionExpiredException을 410으로 매핑해야 한다")
    void shouldMapSessionExpiredExceptionTo410() {
        // given
        SessionExpiredException ex = new SessionExpiredException("Session expired");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleSessionExpiredException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat(response.getBody().message()).contains("Session expired");
    }

    @Test
    @DisplayName("FileSizeMismatchException을 400으로 매핑해야 한다")
    void shouldMapFileSizeMismatchExceptionTo400() {
        // given
        FileSizeMismatchException ex = new FileSizeMismatchException("File size mismatch");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleFileSizeMismatchException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("File size mismatch");
    }

    @Test
    @DisplayName("InvalidFileStateException을 409로 매핑해야 한다")
    void shouldMapInvalidFileStateExceptionTo409() {
        // given
        InvalidFileStateException ex = new InvalidFileStateException("Invalid file state");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleInvalidFileStateException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).contains("Invalid file state");
    }

    @Test
    @DisplayName("FileAlreadyExistsException을 409로 매핑해야 한다")
    void shouldMapFileAlreadyExistsExceptionTo409() {
        // given
        FileAlreadyExistsException ex = new FileAlreadyExistsException("File already exists");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleFileAlreadyExistsException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).contains("File already exists");
    }
}
```

#### Green (feat:)

**파일**: `adapter-in/rest-api/src/main/java/.../exception/GlobalExceptionHandler.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.exception;

import com.ryuqq.fileflow.domain.session.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidSessionIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSessionIdException(InvalidSessionIdException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_SESSION_ID", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotFoundException(SessionNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("SESSION_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ErrorResponse> handleSessionExpiredException(SessionExpiredException ex) {
        ErrorResponse error = new ErrorResponse("SESSION_EXPIRED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GONE).body(error);
    }

    @ExceptionHandler(FileSizeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleFileSizeMismatchException(FileSizeMismatchException ex) {
        ErrorResponse error = new ErrorResponse("FILE_SIZE_MISMATCH", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidFileStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileStateException(InvalidFileStateException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_FILE_STATE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleFileAlreadyExistsException(FileAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse("FILE_ALREADY_EXISTS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
```

**파일**: `adapter-in/rest-api/src/main/java/.../exception/ErrorResponse.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.exception;

/**
 * 에러 응답 DTO
 *
 * @param code 에러 코드
 * @param message 에러 메시지
 */
public record ErrorResponse(
    String code,
    String message
) {
}
```

#### 커밋

```bash
test: GlobalExceptionHandler 테스트 추가 (6개 예외 매핑)
feat: GlobalExceptionHandler 구현 (Domain 예외 → HTTP 상태 코드)
```

---

## Cycle 8: E2E 통합 테스트 (Presigned URL 생성)

#### Red (test:)

**파일**: `adapter-in/rest-api/src/test/java/.../integration/GeneratePresignedUrlE2ETest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.GeneratePresignedUrlRequest;
import com.ryuqq.fileflow.support.fixture.SessionRequestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Presigned URL 생성 E2E 테스트")
class GeneratePresignedUrlE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("E2E - Presigned URL을 생성해야 한다")
    void shouldGeneratePresignedUrlE2E() throws Exception {
        // given
        GeneratePresignedUrlRequest request = SessionRequestFixture.aGeneratePresignedUrlRequest();

        // when & then
        mockMvc.perform(post("/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.presignedUrl").isNotEmpty())
            .andExpect(jsonPath("$.presignedUrl").value(org.hamcrest.Matchers.startsWith("https://")))
            .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    @DisplayName("E2E - 중복 sessionId로 요청 시 동일한 URL을 반환해야 한다 (Idempotency)")
    void shouldReturnSameUrlForDuplicateSessionId() throws Exception {
        // given
        String sessionId = "01JD8001-1234-5678-9abc-def012345678";
        GeneratePresignedUrlRequest request = SessionRequestFixture.withSessionId(sessionId);

        // when - 첫 번째 요청
        String firstResponse = mockMvc.perform(post("/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // when - 두 번째 요청 (동일한 sessionId)
        String secondResponse = mockMvc.perform(post("/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // then - 동일한 응답 (Idempotent)
        assertThat(firstResponse).isEqualTo(secondResponse);
    }
}
```

#### Green (feat:)

통합 테스트는 기존 구현으로 통과해야 합니다. 만약 실패한다면:
1. S3 Mock 설정 확인
2. Database Testcontainers 설정 확인
3. Transaction 경계 검증

#### 커밋

```bash
test: Presigned URL 생성 E2E 테스트 추가 (Idempotency 검증 포함)
```

---

## Cycle 9: E2E 통합 테스트 (업로드 완료)

#### Red (test:)

**파일**: `adapter-in/rest-api/src/test/java/.../integration/CompleteUploadE2ETest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.CompleteUploadRequest;
import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.GeneratePresignedUrlRequest;
import com.ryuqq.fileflow.support.fixture.SessionRequestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("업로드 완료 E2E 테스트")
class CompleteUploadE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("E2E - 업로드를 완료해야 한다")
    void shouldCompleteUploadE2E() throws Exception {
        // given - 먼저 Presigned URL 생성
        String sessionId = "01JD8001-1234-5678-9abc-def012345678";
        GeneratePresignedUrlRequest generateRequest = SessionRequestFixture.withSessionId(sessionId);

        mockMvc.perform(post("/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(generateRequest)))
            .andExpect(status().isOk());

        // when - 업로드 완료 요청
        CompleteUploadRequest completeRequest = SessionRequestFixture.withSessionIdAndSize(
            sessionId, 1048576L
        );

        // then
        mockMvc.perform(post("/upload-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileId").isNotEmpty())
            .andExpect(jsonPath("$.fileName").value("example.jpg"))
            .andExpect(jsonPath("$.fileSize").value(1048576))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.fileUrl").value(org.hamcrest.Matchers.startsWith("https://")));
    }

    @Test
    @DisplayName("E2E - 존재하지 않는 sessionId로 완료 시 404 반환해야 한다")
    void shouldReturn404WhenSessionNotFound() throws Exception {
        // given
        CompleteUploadRequest request = new CompleteUploadRequest(
            "01JD9999-9999-9999-9999-999999999999", 1024L
        );

        // when & then
        mockMvc.perform(post("/upload-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("SESSION_NOT_FOUND"));
    }

    @Test
    @DisplayName("E2E - 파일 크기 불일치 시 400 반환해야 한다")
    void shouldReturn400WhenFileSizeMismatch() throws Exception {
        // given - Presigned URL 생성 (fileSize: 1048576)
        String sessionId = "01JD8002-2345-6789-abcd-ef0123456789";
        GeneratePresignedUrlRequest generateRequest = SessionRequestFixture.withSessionId(sessionId);

        mockMvc.perform(post("/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(generateRequest)))
            .andExpect(status().isOk());

        // when - 다른 크기로 완료 시도
        CompleteUploadRequest completeRequest = new CompleteUploadRequest(sessionId, 999999L);

        // then
        mockMvc.perform(post("/upload-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FILE_SIZE_MISMATCH"));
    }
}
```

#### Green (feat:)

통합 테스트는 기존 구현으로 통과해야 합니다. 만약 실패한다면:
1. Session 상태 전이 검증
2. FileSize 일치 검증 로직 확인
3. Exception Handler 매핑 확인

#### 커밋

```bash
test: 업로드 완료 E2E 테스트 추가 (오류 시나리오 포함)
```

---

## Cycle 10: Spring REST Docs 통합

#### Red (test:)

**파일**: `adapter-in/rest-api/src/test/java/.../docs/FileApiDocumentationTest.java`

```java
package com.ryuqq.fileflow.adapter.in.rest.api.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.CompleteUploadRequest;
import com.ryuqq.fileflow.adapter.in.rest.api.session.dto.request.GeneratePresignedUrlRequest;
import com.ryuqq.fileflow.application.session.dto.response.FileResponse;
import com.ryuqq.fileflow.application.session.dto.response.PresignedUrlResponse;
import com.ryuqq.fileflow.application.session.port.in.command.CompleteUploadUseCase;
import com.ryuqq.fileflow.application.session.port.in.command.GeneratePresignedUrlUseCase;
import com.ryuqq.fileflow.support.fixture.SessionRequestFixture;
import com.ryuqq.fileflow.support.fixture.SessionResponseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureRestDocs
@DisplayName("File API 문서화 테스트")
class FileApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GeneratePresignedUrlUseCase generatePresignedUrlUseCase;

    @MockBean
    private CompleteUploadUseCase completeUploadUseCase;

    @Test
    @DisplayName("Presigned URL 생성 API 문서화")
    void documentGeneratePresignedUrl() throws Exception {
        // given
        GeneratePresignedUrlRequest request = SessionRequestFixture.aGeneratePresignedUrlRequest();
        PresignedUrlResponse response = SessionResponseFixture.aPresignedUrlResponse();

        given(generatePresignedUrlUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(document("generate-presigned-url",
                requestFields(
                    fieldWithPath("sessionId").type(JsonFieldType.STRING)
                        .description("세션 ID (UUID v7, Idempotent Key)"),
                    fieldWithPath("fileName").type(JsonFieldType.STRING)
                        .description("파일명"),
                    fieldWithPath("fileSize").type(JsonFieldType.NUMBER)
                        .description("파일 크기 (bytes)"),
                    fieldWithPath("mimeType").type(JsonFieldType.STRING)
                        .description("MIME 타입 (예: image/jpeg)"),
                    fieldWithPath("category").type(JsonFieldType.STRING)
                        .description("파일 카테고리 (예: banner)"),
                    fieldWithPath("uploaderType").type(JsonFieldType.STRING)
                        .description("업로더 타입 (ADMIN/CUSTOMER)")
                ),
                responseFields(
                    fieldWithPath("presignedUrl").type(JsonFieldType.STRING)
                        .description("S3 Presigned URL (5분 유효)"),
                    fieldWithPath("expiresAt").type(JsonFieldType.STRING)
                        .description("URL 만료 시각 (UTC)")
                )
            ));
    }

    @Test
    @DisplayName("업로드 완료 API 문서화")
    void documentCompleteUpload() throws Exception {
        // given
        CompleteUploadRequest request = SessionRequestFixture.aCompleteUploadRequest();
        FileResponse response = SessionResponseFixture.aFileResponse();

        given(completeUploadUseCase.execute(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/upload-complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andDo(document("complete-upload",
                requestFields(
                    fieldWithPath("sessionId").type(JsonFieldType.STRING)
                        .description("세션 ID (UUID v7, Idempotent Key)"),
                    fieldWithPath("fileSize").type(JsonFieldType.NUMBER)
                        .description("실제 업로드된 파일 크기 (bytes)")
                ),
                responseFields(
                    fieldWithPath("fileId").type(JsonFieldType.STRING)
                        .description("파일 ID (UUID v7)"),
                    fieldWithPath("fileName").type(JsonFieldType.STRING)
                        .description("파일명"),
                    fieldWithPath("fileSize").type(JsonFieldType.NUMBER)
                        .description("파일 크기 (bytes)"),
                    fieldWithPath("mimeType").type(JsonFieldType.STRING)
                        .description("MIME 타입"),
                    fieldWithPath("category").type(JsonFieldType.STRING)
                        .description("파일 카테고리"),
                    fieldWithPath("uploaderType").type(JsonFieldType.STRING)
                        .description("업로더 타입"),
                    fieldWithPath("fileUrl").type(JsonFieldType.STRING)
                        .description("파일 URL (CDN)"),
                    fieldWithPath("status").type(JsonFieldType.STRING)
                        .description("파일 상태 (ACTIVE)"),
                    fieldWithPath("uploaderId").type(JsonFieldType.STRING)
                        .description("업로더 ID (User UUID v7)")
                )
            ));
    }
}
```

#### Green (feat:)

**파일**: `build.gradle` (rest-api 모듈)

```gradle
plugins {
    id 'org.asciidoctor.jvm.convert' version '3.3.2'
}

dependencies {
    testImplementation 'org.springframework.restdocs:spring-restdocs-mockmvc'
}

ext {
    snippetsDir = file('build/generated-snippets')
}

test {
    outputs.dir snippetsDir
}

asciidoctor {
    inputs.dir snippetsDir
    dependsOn test
}

bootJar {
    dependsOn asciidoctor
    from ("${asciidoctor.outputDir}") {
        into 'static/docs'
    }
}
```

#### 커밋

```bash
test: Spring REST Docs 문서화 테스트 추가 (2개 API)
feat: Spring REST Docs 설정 추가 (Gradle + Asciidoctor)
```

---

## 완료 체크리스트

### Request/Response DTOs
- [x] Cycle 1: GeneratePresignedUrlRequest
- [x] Cycle 2: CompleteUploadRequest
- [x] Cycle 3: PresignedUrlResponse
- [x] Cycle 4: FileResponse

### Controller & Mapper
- [x] Cycle 5: FileApiController (MockMvc 테스트)
- [x] Cycle 6: SessionApiMapper

### Exception Handling
- [x] Cycle 7: GlobalExceptionHandler (6개 예외 매핑)

### 통합 테스트
- [x] Cycle 8: Presigned URL 생성 E2E 테스트
- [x] Cycle 9: 업로드 완료 E2E 테스트

### API 문서화
- [x] Cycle 10: Spring REST Docs 통합

---

## Zero-Tolerance 규칙 준수 사항

### REST API Layer 규칙
1. **DTO는 Record 사용** (Immutable)
2. **@Valid 필수** (Request DTO 검증)
3. **MockMvc 테스트** (WebMvcTest + TestRestTemplate E2E)
4. **Exception Handler** (Domain 예외 → HTTP 상태 코드)
5. **RESTful 설계** (POST /presigned-url, POST /upload-complete)
6. **API 문서화** (Spring REST Docs)

### 테스트 전략
- **Unit Tests**: DTO 검증, Mapper 변환, Exception Handler
- **Integration Tests**: MockMvc (WebMvcTest)
- **E2E Tests**: TestRestTemplate (SpringBootTest + Testcontainers)
- **Documentation**: REST Docs (asciidoc 생성)

---

## 다음 단계

REST API Layer TDD Plan 완료 후:

1. **Domain Layer 구현** (domain-tdd-plan.md 기준)
2. **Application Layer 구현** (application-tdd-plan.md 기준)
3. **Persistence Layer 구현** (persistence-tdd-plan.md 기준)
4. **REST API Layer 구현** (rest-api-tdd-plan.md 기준)

각 레이어는 독립적으로 TDD 사이클 실행 가능합니다.
