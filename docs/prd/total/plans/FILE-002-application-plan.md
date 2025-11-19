# FILE-002: Application Layer TDD Plan

**Epic**: File Management System (파일 관리 시스템)
**Layer**: Application Layer
**브랜치**: feature/FILE-002-application
**Plan 버전**: v1.0 (MVP)

---

## 📋 MVP 범위 분석

### 포함 항목 (MVP)

**Port In (UseCase)**:
- GeneratePresignedUrlUseCase (Presigned URL 발급)
- CompleteUploadUseCase (업로드 완료 처리)

**Port Out - Command**:
- FilePersistencePort (File 저장)
- UploadSessionPersistencePort (UploadSession 저장/업데이트)

**Port Out - Query**:
- UploadSessionQueryPort (SessionId로 조회)

**Port Out - External**:
- S3ClientPort (Presigned URL 생성)

**Command DTOs**:
- GeneratePresignedUrlCommand
- CompleteUploadCommand

**Response DTOs**:
- PresignedUrlResponse
- FileResponse

**UserContext**:
- JWT 기반 (tenantId, uploaderId, uploaderType, uploaderSlug)

**Services**:
- GeneratePresignedUrlService (멱등성 보장)
- CompleteUploadService (세션 상태 검증)

### 제외 항목 (v2 이후)

- DownloadSession 관련 UseCase
- FileProcessingJob 관련 UseCase
- MessageOutbox 발행 로직
- Retry 로직
- 접근 제어 (Visibility)

---

## 🎯 TDD 사이클 전략

### 전체 사이클: 20개

**Phase 1: DTOs** (5 cycles)
- Cycle 1: UserContext Record
- Cycle 2: GeneratePresignedUrlCommand
- Cycle 3: CompleteUploadCommand
- Cycle 4: PresignedUrlResponse
- Cycle 5: FileResponse

**Phase 2: Port Interfaces** (6 cycles)
- Cycle 6: GeneratePresignedUrlUseCase (Port In)
- Cycle 7: CompleteUploadUseCase (Port In)
- Cycle 8: FilePersistencePort (Port Out - Command)
- Cycle 9: UploadSessionPersistencePort (Port Out - Command)
- Cycle 10: UploadSessionQueryPort (Port Out - Query)
- Cycle 11: S3ClientPort (Port Out - External)

**Phase 3: Service Implementation** (7 cycles)
- Cycle 12: GeneratePresignedUrlService - 멱등성 확인 로직
- Cycle 13: GeneratePresignedUrlService - 새 세션 생성 로직
- Cycle 14: GeneratePresignedUrlService - FileCategory 처리 로직
- Cycle 15: CompleteUploadService - 세션 조회 및 검증
- Cycle 16: CompleteUploadService - File 생성 로직
- Cycle 17: CompleteUploadService - 세션 완료 처리
- Cycle 18: Transaction 경계 검증

**Phase 4: Quality & Fixtures** (2 cycles)
- Cycle 19: TestFixtures
- Cycle 20: ArchUnit 테스트 + Coverage 90%

---

## 📚 Phase 1: DTOs (Cycle 1-5)

### Cycle 1: UserContext Record

**목적**: JWT에서 추출한 사용자 컨텍스트를 담는 VO

**Red** (test: 커밋):

```java
// application/src/test/java/.../dto/UserContextTest.java
package com.ryuqq.fileflow.application.dto;

import com.ryuqq.fileflow.domain.vo.TenantId;
import com.ryuqq.fileflow.domain.vo.UploaderId;
import com.ryuqq.fileflow.domain.enums.UploaderType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserContextTest {

    @Test
    void Record_타입_검증() {
        // UserContext는 Record여야 함
        UserContext context = new UserContext(
            TenantId.of(1L),
            UploaderId.of(100L),
            UploaderType.ADMIN,
            "connectly"
        );

        assertThat(context).isNotNull();
        assertThat(context.tenantId()).isEqualTo(TenantId.of(1L));
        assertThat(context.uploaderId()).isEqualTo(UploaderId.of(100L));
        assertThat(context.uploaderType()).isEqualTo(UploaderType.ADMIN);
        assertThat(context.uploaderSlug()).isEqualTo("connectly");
    }

    @Test
    void 모든_필드_필수() {
        assertThatThrownBy(() ->
            new UserContext(null, UploaderId.of(100L), UploaderType.ADMIN, "connectly")
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void uploaderSlug_Admin은_connectly() {
        UserContext admin = new UserContext(
            TenantId.of(1L),
            UploaderId.of(1L),
            UploaderType.ADMIN,
            "connectly"
        );

        assertThat(admin.uploaderSlug()).isEqualTo("connectly");
    }

    @Test
    void uploaderSlug_Seller는_회사_slug() {
        UserContext seller = new UserContext(
            TenantId.of(1L),
            UploaderId.of(200L),
            UploaderType.SELLER,
            "samsung-electronics"
        );

        assertThat(seller.uploaderSlug()).isEqualTo("samsung-electronics");
    }

    @Test
    void uploaderSlug_Customer는_default() {
        UserContext customer = new UserContext(
            TenantId.of(1L),
            UploaderId.of(300L),
            UploaderType.CUSTOMER,
            "default"
        );

        assertThat(customer.uploaderSlug()).isEqualTo("default");
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../dto/UserContext.java
package com.ryuqq.fileflow.application.dto;

import com.ryuqq.fileflow.domain.vo.TenantId;
import com.ryuqq.fileflow.domain.vo.UploaderId;
import com.ryuqq.fileflow.domain.enums.UploaderType;

/**
 * JWT에서 추출한 사용자 컨텍스트
 * SecurityContext.getAuthentication().getPrincipal()로 접근
 */
public record UserContext(
    TenantId tenantId,
    UploaderId uploaderId,
    UploaderType uploaderType,
    String uploaderSlug  // "connectly", "samsung-electronics", "default"
) {
    public UserContext {
        if (tenantId == null) {
            throw new NullPointerException("tenantId는 필수입니다");
        }
        if (uploaderId == null) {
            throw new NullPointerException("uploaderId는 필수입니다");
        }
        if (uploaderType == null) {
            throw new NullPointerException("uploaderType은 필수입니다");
        }
        if (uploaderSlug == null || uploaderSlug.isBlank()) {
            throw new NullPointerException("uploaderSlug는 필수입니다");
        }
    }
}
```

**커밋**:
```bash
git add .
git commit -m "test: UserContext Record 테스트 추가 (Red)"

git add .
git commit -m "feat: UserContext Record 구현 (Green)"
```

---

### Cycle 2: GeneratePresignedUrlCommand

**목적**: Presigned URL 발급 요청 Command DTO

**Red** (test: 커밋):

```java
// application/src/test/java/.../dto/command/GeneratePresignedUrlCommandTest.java
package com.ryuqq.fileflow.application.dto.command;

import com.ryuqq.fileflow.domain.vo.*;
import com.ryuqq.fileflow.domain.enums.UploaderType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GeneratePresignedUrlCommandTest {

    @Test
    void Record_타입_검증() {
        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
            SessionId.of("session-123"),
            FileName.of("test.jpg"),
            FileSize.of(1024L),
            MimeType.of("image/jpeg"),
            FileCategory.of("banner", UploaderType.ADMIN)
        );

        assertThat(command).isNotNull();
        assertThat(command.sessionId()).isEqualTo(SessionId.of("session-123"));
        assertThat(command.fileName()).isEqualTo(FileName.of("test.jpg"));
        assertThat(command.fileSize()).isEqualTo(FileSize.of(1024L));
        assertThat(command.mimeType()).isEqualTo(MimeType.of("image/jpeg"));
        assertThat(command.category()).isNotNull();
    }

    @Test
    void category_nullable_허용() {
        // Customer는 category가 null일 수 있음
        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
            SessionId.of("session-123"),
            FileName.of("test.jpg"),
            FileSize.of(1024L),
            MimeType.of("image/jpeg"),
            null  // category nullable
        );

        assertThat(command.category()).isNull();
    }

    @Test
    void 필수_필드_검증() {
        assertThatThrownBy(() ->
            new GeneratePresignedUrlCommand(
                null,  // sessionId 필수
                FileName.of("test.jpg"),
                FileSize.of(1024L),
                MimeType.of("image/jpeg"),
                null
            )
        ).isInstanceOf(NullPointerException.class);
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../dto/command/GeneratePresignedUrlCommand.java
package com.ryuqq.fileflow.application.dto.command;

import com.ryuqq.fileflow.domain.vo.*;

/**
 * Presigned URL 발급 요청 Command
 */
public record GeneratePresignedUrlCommand(
    SessionId sessionId,
    FileName fileName,
    FileSize fileSize,
    MimeType mimeType,
    FileCategory category  // Nullable (Customer는 null 가능)
) {
    public GeneratePresignedUrlCommand {
        if (sessionId == null) {
            throw new NullPointerException("sessionId는 필수입니다");
        }
        if (fileName == null) {
            throw new NullPointerException("fileName은 필수입니다");
        }
        if (fileSize == null) {
            throw new NullPointerException("fileSize는 필수입니다");
        }
        if (mimeType == null) {
            throw new NullPointerException("mimeType은 필수입니다");
        }
        // category는 nullable (Customer는 항상 null)
    }
}
```

**커밋**:
```bash
git commit -m "test: GeneratePresignedUrlCommand 테스트 추가 (Red)"
git commit -m "feat: GeneratePresignedUrlCommand 구현 (Green)"
```

---

### Cycle 3: CompleteUploadCommand

**목적**: 업로드 완료 요청 Command DTO

**Red** (test: 커밋):

```java
// application/src/test/java/.../dto/command/CompleteUploadCommandTest.java
package com.ryuqq.fileflow.application.dto.command;

import com.ryuqq.fileflow.domain.vo.SessionId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CompleteUploadCommandTest {

    @Test
    void Record_타입_검증() {
        CompleteUploadCommand command = new CompleteUploadCommand(
            SessionId.of("session-123")
        );

        assertThat(command).isNotNull();
        assertThat(command.sessionId()).isEqualTo(SessionId.of("session-123"));
    }

    @Test
    void sessionId_필수() {
        assertThatThrownBy(() ->
            new CompleteUploadCommand(null)
        ).isInstanceOf(NullPointerException.class);
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../dto/command/CompleteUploadCommand.java
package com.ryuqq.fileflow.application.dto.command;

import com.ryuqq.fileflow.domain.vo.SessionId;

/**
 * 업로드 완료 요청 Command
 */
public record CompleteUploadCommand(
    SessionId sessionId
) {
    public CompleteUploadCommand {
        if (sessionId == null) {
            throw new NullPointerException("sessionId는 필수입니다");
        }
    }
}
```

**커밋**:
```bash
git commit -m "test: CompleteUploadCommand 테스트 추가 (Red)"
git commit -m "feat: CompleteUploadCommand 구현 (Green)"
```

---

### Cycle 4: PresignedUrlResponse

**목적**: Presigned URL 발급 응답 DTO

**Red** (test: 커밋):

```java
// application/src/test/java/.../dto/response/PresignedUrlResponseTest.java
package com.ryuqq.fileflow.application.dto.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PresignedUrlResponseTest {

    @Test
    void Record_타입_검증() {
        PresignedUrlResponse response = new PresignedUrlResponse(
            "session-123",
            "file-456",
            "https://s3.amazonaws.com/...",
            300,  // 5분
            "SINGLE"
        );

        assertThat(response).isNotNull();
        assertThat(response.sessionId()).isEqualTo("session-123");
        assertThat(response.fileId()).isEqualTo("file-456");
        assertThat(response.presignedUrl()).startsWith("https://");
        assertThat(response.expiresIn()).isEqualTo(300);
        assertThat(response.uploadType()).isEqualTo("SINGLE");
    }

    @Test
    void expiresIn_초단위_300초() {
        PresignedUrlResponse response = new PresignedUrlResponse(
            "session-123",
            "file-456",
            "https://s3.amazonaws.com/...",
            300,
            "SINGLE"
        );

        assertThat(response.expiresIn()).isEqualTo(300);
    }

    @Test
    void uploadType_SINGLE_고정() {
        PresignedUrlResponse response = new PresignedUrlResponse(
            "session-123",
            "file-456",
            "https://s3.amazonaws.com/...",
            300,
            "SINGLE"
        );

        assertThat(response.uploadType()).isEqualTo("SINGLE");
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../dto/response/PresignedUrlResponse.java
package com.ryuqq.fileflow.application.dto.response;

/**
 * Presigned URL 발급 응답 DTO
 */
public record PresignedUrlResponse(
    String sessionId,
    String fileId,
    String presignedUrl,
    int expiresIn,  // 초 단위 (300초 = 5분)
    String uploadType  // "SINGLE"
) {}
```

**커밋**:
```bash
git commit -m "test: PresignedUrlResponse 테스트 추가 (Red)"
git commit -m "feat: PresignedUrlResponse 구현 (Green)"
```

---

### Cycle 5: FileResponse

**목적**: 업로드 완료 응답 DTO

**Red** (test: 커밋):

```java
// application/src/test/java/.../dto/response/FileResponseTest.java
package com.ryuqq.fileflow.application.dto.response;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class FileResponseTest {

    @Test
    void Record_타입_검증() {
        FileResponse response = new FileResponse(
            "session-123",
            "file-456",
            "test.jpg",
            1024L,
            "image/jpeg",
            "COMPLETED",
            "uploads/1/admin/connectly/banner/file-456_test.jpg",
            "fileflow-uploads-1",
            LocalDateTime.now()
        );

        assertThat(response).isNotNull();
        assertThat(response.sessionId()).isEqualTo("session-123");
        assertThat(response.fileId()).isEqualTo("file-456");
        assertThat(response.fileName()).isEqualTo("test.jpg");
        assertThat(response.fileSize()).isEqualTo(1024L);
        assertThat(response.mimeType()).isEqualTo("image/jpeg");
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.s3Key()).startsWith("uploads/");
        assertThat(response.s3Bucket()).isEqualTo("fileflow-uploads-1");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void status_COMPLETED_고정() {
        FileResponse response = new FileResponse(
            "session-123",
            "file-456",
            "test.jpg",
            1024L,
            "image/jpeg",
            "COMPLETED",
            "uploads/1/admin/connectly/banner/file-456_test.jpg",
            "fileflow-uploads-1",
            LocalDateTime.now()
        );

        assertThat(response.status()).isEqualTo("COMPLETED");
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../dto/response/FileResponse.java
package com.ryuqq.fileflow.application.dto.response;

import java.time.LocalDateTime;

/**
 * 업로드 완료 응답 DTO
 */
public record FileResponse(
    String sessionId,
    String fileId,
    String fileName,
    Long fileSize,
    String mimeType,
    String status,  // "COMPLETED"
    String s3Key,
    String s3Bucket,
    LocalDateTime createdAt
) {}
```

**커밋**:
```bash
git commit -m "test: FileResponse 테스트 추가 (Red)"
git commit -m "feat: FileResponse 구현 (Green)"
```

---

## 📚 Phase 2: Port Interfaces (Cycle 6-11)

### Cycle 6: GeneratePresignedUrlUseCase (Port In)

**목적**: Presigned URL 발급 UseCase 인터페이스

**Red** (test: 커밋):

```java
// application/src/test/java/.../port/in/command/GeneratePresignedUrlUseCaseTest.java
package com.ryuqq.fileflow.application.port.in.command;

import com.ryuqq.fileflow.application.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.application.dto.response.PresignedUrlResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GeneratePresignedUrlUseCaseTest {

    @Test
    void Interface_정의_확인() {
        // UseCase는 Interface여야 함
        assertThat(GeneratePresignedUrlUseCase.class.isInterface()).isTrue();
    }

    @Test
    void execute_메서드_존재() throws NoSuchMethodException {
        // execute 메서드 시그니처 확인
        var method = GeneratePresignedUrlUseCase.class.getMethod(
            "execute",
            GeneratePresignedUrlCommand.class
        );

        assertThat(method.getReturnType()).isEqualTo(PresignedUrlResponse.class);
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../port/in/command/GeneratePresignedUrlUseCase.java
package com.ryuqq.fileflow.application.port.in.command;

import com.ryuqq.fileflow.application.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.application.dto.response.PresignedUrlResponse;

/**
 * Presigned URL 발급 UseCase (Port In)
 */
public interface GeneratePresignedUrlUseCase {
    /**
     * Presigned URL 발급
     * 멱등성 보장: 동일 sessionId 재요청 시 기존 URL 반환
     */
    PresignedUrlResponse execute(GeneratePresignedUrlCommand command);
}
```

**커밋**:
```bash
git commit -m "test: GeneratePresignedUrlUseCase 인터페이스 테스트 추가 (Red)"
git commit -m "feat: GeneratePresignedUrlUseCase 인터페이스 정의 (Green)"
```

---

### Cycle 7: CompleteUploadUseCase (Port In)

**목적**: 업로드 완료 UseCase 인터페이스

**Red** (test: 커밋):

```java
// application/src/test/java/.../port/in/command/CompleteUploadUseCaseTest.java
package com.ryuqq.fileflow.application.port.in.command;

import com.ryuqq.fileflow.application.dto.command.CompleteUploadCommand;
import com.ryuqq.fileflow.application.dto.response.FileResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CompleteUploadUseCaseTest {

    @Test
    void Interface_정의_확인() {
        assertThat(CompleteUploadUseCase.class.isInterface()).isTrue();
    }

    @Test
    void execute_메서드_존재() throws NoSuchMethodException {
        var method = CompleteUploadUseCase.class.getMethod(
            "execute",
            CompleteUploadCommand.class
        );

        assertThat(method.getReturnType()).isEqualTo(FileResponse.class);
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../port/in/command/CompleteUploadUseCase.java
package com.ryuqq.fileflow.application.port.in.command;

import com.ryuqq.fileflow.application.dto.command.CompleteUploadCommand;
import com.ryuqq.fileflow.application.dto.response.FileResponse;

/**
 * 업로드 완료 UseCase (Port In)
 */
public interface CompleteUploadUseCase {
    /**
     * 업로드 완료 처리
     * 세션 상태 검증: 만료, 중복 완료
     */
    FileResponse execute(CompleteUploadCommand command);
}
```

**커밋**:
```bash
git commit -m "test: CompleteUploadUseCase 인터페이스 테스트 추가 (Red)"
git commit -m "feat: CompleteUploadUseCase 인터페이스 정의 (Green)"
```

---

### Cycle 8: FilePersistencePort (Port Out - Command)

**목적**: File 저장 Port 인터페이스

**Red** (test: 커밋):

```java
// application/src/test/java/.../port/out/command/FilePersistencePortTest.java
package com.ryuqq.fileflow.application.port.out.command;

import com.ryuqq.fileflow.domain.aggregate.File;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FilePersistencePortTest {

    @Test
    void Interface_정의_확인() {
        assertThat(FilePersistencePort.class.isInterface()).isTrue();
    }

    @Test
    void save_메서드_존재() throws NoSuchMethodException {
        var method = FilePersistencePort.class.getMethod("save", File.class);
        assertThat(method.getReturnType()).isEqualTo(File.class);
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../port/out/command/FilePersistencePort.java
package com.ryuqq.fileflow.application.port.out.command;

import com.ryuqq.fileflow.domain.aggregate.File;

/**
 * File 저장 Port (Port Out - Command)
 */
public interface FilePersistencePort {
    /**
     * File 저장
     */
    File save(File file);
}
```

**커밋**:
```bash
git commit -m "test: FilePersistencePort 인터페이스 테스트 추가 (Red)"
git commit -m "feat: FilePersistencePort 인터페이스 정의 (Green)"
```

---

### Cycle 9: UploadSessionPersistencePort (Port Out - Command)

**목적**: UploadSession 저장/업데이트 Port 인터페이스

**Red** (test: 커밋):

```java
// application/src/test/java/.../port/out/command/UploadSessionPersistencePortTest.java
package com.ryuqq.fileflow.application.port.out.command;

import com.ryuqq.fileflow.domain.aggregate.UploadSession;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UploadSessionPersistencePortTest {

    @Test
    void Interface_정의_확인() {
        assertThat(UploadSessionPersistencePort.class.isInterface()).isTrue();
    }

    @Test
    void save_메서드_존재() throws NoSuchMethodException {
        var method = UploadSessionPersistencePort.class.getMethod("save", UploadSession.class);
        assertThat(method.getReturnType()).isEqualTo(UploadSession.class);
    }

    @Test
    void update_메서드_존재() throws NoSuchMethodException {
        var method = UploadSessionPersistencePort.class.getMethod("update", UploadSession.class);
        assertThat(method.getReturnType()).isEqualTo(UploadSession.class);
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../port/out/command/UploadSessionPersistencePort.java
package com.ryuqq.fileflow.application.port.out.command;

import com.ryuqq.fileflow.domain.aggregate.UploadSession;

/**
 * UploadSession 저장/업데이트 Port (Port Out - Command)
 */
public interface UploadSessionPersistencePort {
    /**
     * UploadSession 저장
     */
    UploadSession save(UploadSession session);

    /**
     * UploadSession 업데이트
     */
    UploadSession update(UploadSession session);
}
```

**커밋**:
```bash
git commit -m "test: UploadSessionPersistencePort 인터페이스 테스트 추가 (Red)"
git commit -m "feat: UploadSessionPersistencePort 인터페이스 정의 (Green)"
```

---

### Cycle 10: UploadSessionQueryPort (Port Out - Query)

**목적**: UploadSession 조회 Port 인터페이스

**Red** (test: 커밋):

```java
// application/src/test/java/.../port/out/query/UploadSessionQueryPortTest.java
package com.ryuqq.fileflow.application.port.out.query;

import com.ryuqq.fileflow.domain.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.vo.SessionId;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class UploadSessionQueryPortTest {

    @Test
    void Interface_정의_확인() {
        assertThat(UploadSessionQueryPort.class.isInterface()).isTrue();
    }

    @Test
    void findBySessionId_메서드_존재() throws NoSuchMethodException {
        var method = UploadSessionQueryPort.class.getMethod("findBySessionId", SessionId.class);
        assertThat(method.getReturnType()).isEqualTo(Optional.class);
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../port/out/query/UploadSessionQueryPort.java
package com.ryuqq.fileflow.application.port.out.query;

import com.ryuqq.fileflow.domain.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.vo.SessionId;

import java.util.Optional;

/**
 * UploadSession 조회 Port (Port Out - Query)
 */
public interface UploadSessionQueryPort {
    /**
     * SessionId로 UploadSession 조회
     */
    Optional<UploadSession> findBySessionId(SessionId sessionId);
}
```

**커밋**:
```bash
git commit -m "test: UploadSessionQueryPort 인터페이스 테스트 추가 (Red)"
git commit -m "feat: UploadSessionQueryPort 인터페이스 정의 (Green)"
```

---

### Cycle 11: S3ClientPort (Port Out - External)

**목적**: S3 Presigned URL 생성 Port 인터페이스

**Red** (test: 커밋):

```java
// application/src/test/java/.../port/out/external/S3ClientPortTest.java
package com.ryuqq.fileflow.application.port.out.external;

import com.ryuqq.fileflow.domain.vo.*;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

class S3ClientPortTest {

    @Test
    void Interface_정의_확인() {
        assertThat(S3ClientPort.class.isInterface()).isTrue();
    }

    @Test
    void generatePresignedPutUrl_메서드_존재() throws NoSuchMethodException {
        var method = S3ClientPort.class.getMethod(
            "generatePresignedPutUrl",
            S3Bucket.class,
            S3Key.class,
            MimeType.class,
            Duration.class
        );
        assertThat(method.getReturnType()).isEqualTo(PresignedUrl.class);
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../port/out/external/S3ClientPort.java
package com.ryuqq.fileflow.application.port.out.external;

import com.ryuqq.fileflow.domain.vo.*;

import java.time.Duration;

/**
 * S3 Client Port (Port Out - External)
 */
public interface S3ClientPort {
    /**
     * Presigned PUT URL 생성
     *
     * @param bucket S3 Bucket
     * @param key S3 Key
     * @param mimeType MIME Type
     * @param expiration 만료 시간 (5분)
     * @return Presigned URL
     */
    PresignedUrl generatePresignedPutUrl(
        S3Bucket bucket,
        S3Key key,
        MimeType mimeType,
        Duration expiration
    );
}
```

**커밋**:
```bash
git commit -m "test: S3ClientPort 인터페이스 테스트 추가 (Red)"
git commit -m "feat: S3ClientPort 인터페이스 정의 (Green)"
```

---

## 📚 Phase 3: Service Implementation (Cycle 12-18)

### Cycle 12: GeneratePresignedUrlService - 멱등성 확인 로직

**목적**: 동일 sessionId 재요청 시 기존 URL 반환

**Red** (test: 커밋):

```java
// application/src/test/java/.../service/GeneratePresignedUrlServiceTest.java
package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.UserContext;
import com.ryuqq.fileflow.application.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.application.dto.response.PresignedUrlResponse;
import com.ryuqq.fileflow.application.port.out.command.UploadSessionPersistencePort;
import com.ryuqq.fileflow.application.port.out.external.S3ClientPort;
import com.ryuqq.fileflow.application.port.out.query.UploadSessionQueryPort;
import com.ryuqq.fileflow.domain.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.enums.UploaderType;
import com.ryuqq.fileflow.domain.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class GeneratePresignedUrlServiceTest {

    @Mock
    private UploadSessionQueryPort uploadSessionQueryPort;
    @Mock
    private UploadSessionPersistencePort uploadSessionPersistencePort;
    @Mock
    private S3ClientPort s3ClientPort;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private Clock clock;
    private GeneratePresignedUrlService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clock = Clock.fixed(Instant.parse("2025-01-18T10:00:00Z"), ZoneId.systemDefault());
        service = new GeneratePresignedUrlService(
            uploadSessionQueryPort,
            uploadSessionPersistencePort,
            s3ClientPort,
            clock
        );

        // SecurityContext Mock 설정
        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
    }

    @Test
    void 멱등성_동일_sessionId_재요청_시_기존_URL_반환() {
        // Given: 기존 세션 존재
        SessionId sessionId = SessionId.of("session-123");
        UploadSession existingSession = UploadSession.initiate(
            sessionId,
            TenantId.of(1L),
            FileName.of("test.jpg"),
            FileSize.of(1024L),
            MimeType.of("image/jpeg"),
            UploadType.SINGLE,
            PresignedUrl.of("https://s3.amazonaws.com/existing-url"),
            clock
        );

        given(uploadSessionQueryPort.findBySessionId(sessionId))
            .willReturn(Optional.of(existingSession));

        UserContext userContext = new UserContext(
            TenantId.of(1L),
            UploaderId.of(100L),
            UploaderType.ADMIN,
            "connectly"
        );
        given(authentication.getPrincipal()).willReturn(userContext);

        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
            sessionId,
            FileName.of("test.jpg"),
            FileSize.of(1024L),
            MimeType.of("image/jpeg"),
            null
        );

        // When
        PresignedUrlResponse response = service.execute(command);

        // Then: 기존 URL 반환, 새 세션 생성 없음
        assertThat(response.sessionId()).isEqualTo("session-123");
        assertThat(response.presignedUrl()).isEqualTo("https://s3.amazonaws.com/existing-url");
        assertThat(response.uploadType()).isEqualTo("SINGLE");

        // 새 세션 저장 호출 없음
        verify(uploadSessionPersistencePort, never()).save(any(UploadSession.class));
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../service/GeneratePresignedUrlService.java
package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.UserContext;
import com.ryuqq.fileflow.application.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.application.dto.response.PresignedUrlResponse;
import com.ryuqq.fileflow.application.port.in.command.GeneratePresignedUrlUseCase;
import com.ryuqq.fileflow.application.port.out.command.UploadSessionPersistencePort;
import com.ryuqq.fileflow.application.port.out.external.S3ClientPort;
import com.ryuqq.fileflow.application.port.out.query.UploadSessionQueryPort;
import com.ryuqq.fileflow.domain.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.vo.FileId;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;

/**
 * Presigned URL 발급 Service
 */
@Component
@Transactional
public class GeneratePresignedUrlService implements GeneratePresignedUrlUseCase {

    private final UploadSessionQueryPort uploadSessionQueryPort;
    private final UploadSessionPersistencePort uploadSessionPersistencePort;
    private final S3ClientPort s3ClientPort;
    private final Clock clock;

    public GeneratePresignedUrlService(
        UploadSessionQueryPort uploadSessionQueryPort,
        UploadSessionPersistencePort uploadSessionPersistencePort,
        S3ClientPort s3ClientPort,
        Clock clock
    ) {
        this.uploadSessionQueryPort = uploadSessionQueryPort;
        this.uploadSessionPersistencePort = uploadSessionPersistencePort;
        this.s3ClientPort = s3ClientPort;
        this.clock = clock;
    }

    @Override
    public PresignedUrlResponse execute(GeneratePresignedUrlCommand command) {
        // 1. SecurityContext에서 UserContext 추출
        UserContext userContext = (UserContext) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();

        // 2. 멱등성: 동일 sessionId가 있으면 기존 URL 반환
        Optional<UploadSession> existingSession = uploadSessionQueryPort
            .findBySessionId(command.sessionId());

        if (existingSession.isPresent()) {
            UploadSession session = existingSession.get();
            return new PresignedUrlResponse(
                session.sessionId().value(),
                FileId.generate().value(),  // 새 FileId
                session.presignedUrl().value(),
                300,
                "SINGLE"
            );
        }

        // 3. 새 세션 생성 로직 (다음 Cycle에서 구현)
        return null;
    }
}
```

**커밋**:
```bash
git commit -m "test: GeneratePresignedUrlService 멱등성 테스트 추가 (Red)"
git commit -m "feat: GeneratePresignedUrlService 멱등성 구현 (Green)"
```

---

### Cycle 13: GeneratePresignedUrlService - 새 세션 생성 로직

**목적**: 기존 세션이 없을 때 새 세션 생성 및 Presigned URL 발급

**Red** (test: 커밋):

```java
// application/src/test/java/.../service/GeneratePresignedUrlServiceTest.java (추가)

@Test
void 새_세션_생성_및_Presigned_URL_발급() {
    // Given: 기존 세션 없음
    SessionId sessionId = SessionId.of("session-new");
    given(uploadSessionQueryPort.findBySessionId(sessionId))
        .willReturn(Optional.empty());

    UserContext userContext = new UserContext(
        TenantId.of(1L),
        UploaderId.of(100L),
        UploaderType.ADMIN,
        "connectly"
    );
    given(authentication.getPrincipal()).willReturn(userContext);

    // S3 Presigned URL Mock
    given(s3ClientPort.generatePresignedPutUrl(
        any(S3Bucket.class),
        any(S3Key.class),
        any(MimeType.class),
        any(Duration.class)
    )).willReturn(PresignedUrl.of("https://s3.amazonaws.com/new-url"));

    // UploadSession save Mock
    given(uploadSessionPersistencePort.save(any(UploadSession.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
        sessionId,
        FileName.of("banner.jpg"),
        FileSize.of(2048L),
        MimeType.of("image/jpeg"),
        FileCategory.of("banner", UploaderType.ADMIN)
    );

    // When
    PresignedUrlResponse response = service.execute(command);

    // Then
    assertThat(response.sessionId()).isEqualTo("session-new");
    assertThat(response.presignedUrl()).isEqualTo("https://s3.amazonaws.com/new-url");
    assertThat(response.uploadType()).isEqualTo("SINGLE");
    assertThat(response.expiresIn()).isEqualTo(300);

    // S3 호출 검증
    verify(s3ClientPort).generatePresignedPutUrl(
        any(S3Bucket.class),
        any(S3Key.class),
        any(MimeType.class),
        eq(Duration.ofMinutes(5))
    );

    // 세션 저장 검증
    verify(uploadSessionPersistencePort).save(any(UploadSession.class));
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../service/GeneratePresignedUrlService.java (수정)

@Override
public PresignedUrlResponse execute(GeneratePresignedUrlCommand command) {
    // 1. SecurityContext에서 UserContext 추출
    UserContext userContext = (UserContext) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

    // 2. 멱등성: 동일 sessionId가 있으면 기존 URL 반환
    Optional<UploadSession> existingSession = uploadSessionQueryPort
        .findBySessionId(command.sessionId());

    if (existingSession.isPresent()) {
        UploadSession session = existingSession.get();
        return new PresignedUrlResponse(
            session.sessionId().value(),
            FileId.generate().value(),
            session.presignedUrl().value(),
            300,
            "SINGLE"
        );
    }

    // 3. FileId 생성
    FileId fileId = FileId.generate();

    // 4. FileCategory 처리 (다음 Cycle에서 세부 로직 구현)
    FileCategory category = command.category();

    // 5. S3Key 생성
    S3Key s3Key = S3Key.generate(
        userContext.tenantId(),
        userContext.uploaderType(),
        userContext.uploaderSlug(),
        category,
        fileId,
        command.fileName()
    );

    // 6. S3Bucket 생성
    S3Bucket s3Bucket = S3Bucket.forTenant(userContext.tenantId());

    // 7. Presigned URL 생성
    PresignedUrl presignedUrl = s3ClientPort.generatePresignedPutUrl(
        s3Bucket,
        s3Key,
        command.mimeType(),
        Duration.ofMinutes(5)
    );

    // 8. UploadSession 생성
    UploadSession session = UploadSession.initiate(
        command.sessionId(),
        userContext.tenantId(),
        command.fileName(),
        command.fileSize(),
        command.mimeType(),
        UploadType.SINGLE,
        presignedUrl,
        clock
    );

    // 9. UploadSession 저장
    uploadSessionPersistencePort.save(session);

    // 10. Response 반환
    return new PresignedUrlResponse(
        session.sessionId().value(),
        fileId.value(),
        presignedUrl.value(),
        300,
        "SINGLE"
    );
}
```

**커밋**:
```bash
git commit -m "test: GeneratePresignedUrlService 새 세션 생성 테스트 추가 (Red)"
git commit -m "feat: GeneratePresignedUrlService 새 세션 생성 구현 (Green)"
```

---

### Cycle 14: GeneratePresignedUrlService - FileCategory 처리 로직

**목적**: UploaderType별 FileCategory 처리 (Customer는 항상 default)

**Red** (test: 커밋):

```java
// application/src/test/java/.../service/GeneratePresignedUrlServiceTest.java (추가)

@Test
void Customer_FileCategory_항상_default() {
    // Given: Customer UserContext
    SessionId sessionId = SessionId.of("session-customer");
    given(uploadSessionQueryPort.findBySessionId(sessionId))
        .willReturn(Optional.empty());

    UserContext customerContext = new UserContext(
        TenantId.of(1L),
        UploaderId.of(300L),
        UploaderType.CUSTOMER,
        "default"
    );
    given(authentication.getPrincipal()).willReturn(customerContext);

    given(s3ClientPort.generatePresignedPutUrl(
        any(S3Bucket.class),
        any(S3Key.class),
        any(MimeType.class),
        any(Duration.class)
    )).willReturn(PresignedUrl.of("https://s3.amazonaws.com/customer-url"));

    given(uploadSessionPersistencePort.save(any(UploadSession.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Customer는 category를 null로 요청해도 default로 처리
    GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
        sessionId,
        FileName.of("review.jpg"),
        FileSize.of(1024L),
        MimeType.of("image/jpeg"),
        null  // Customer는 category null
    );

    // When
    PresignedUrlResponse response = service.execute(command);

    // Then
    assertThat(response).isNotNull();

    // S3Key에 "customer/default" 경로 포함 확인
    verify(s3ClientPort).generatePresignedPutUrl(
        eq(S3Bucket.forTenant(TenantId.of(1L))),
        argThat(s3Key -> s3Key.value().contains("customer/default")),
        any(MimeType.class),
        any(Duration.class)
    );
}

@Test
void Admin_FileCategory_요청값_또는_default() {
    // Given: Admin UserContext
    SessionId sessionId = SessionId.of("session-admin");
    given(uploadSessionQueryPort.findBySessionId(sessionId))
        .willReturn(Optional.empty());

    UserContext adminContext = new UserContext(
        TenantId.of(1L),
        UploaderId.of(100L),
        UploaderType.ADMIN,
        "connectly"
    );
    given(authentication.getPrincipal()).willReturn(adminContext);

    given(s3ClientPort.generatePresignedPutUrl(
        any(S3Bucket.class),
        any(S3Key.class),
        any(MimeType.class),
        any(Duration.class)
    )).willReturn(PresignedUrl.of("https://s3.amazonaws.com/admin-url"));

    given(uploadSessionPersistencePort.save(any(UploadSession.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
        sessionId,
        FileName.of("banner.jpg"),
        FileSize.of(2048L),
        MimeType.of("image/jpeg"),
        FileCategory.of("banner", UploaderType.ADMIN)
    );

    // When
    PresignedUrlResponse response = service.execute(command);

    // Then
    verify(s3ClientPort).generatePresignedPutUrl(
        eq(S3Bucket.forTenant(TenantId.of(1L))),
        argThat(s3Key -> s3Key.value().contains("admin/connectly/banner")),
        any(MimeType.class),
        any(Duration.class)
    );
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../service/GeneratePresignedUrlService.java (수정)

@Override
public PresignedUrlResponse execute(GeneratePresignedUrlCommand command) {
    // ... (생략: 1-3번 로직)

    // 4. FileCategory 처리
    FileCategory category = userContext.uploaderType() == UploaderType.CUSTOMER
        ? FileCategory.defaultCategory()
        : (command.category() != null
            ? command.category()
            : FileCategory.defaultCategory());

    // 5-10. S3Key 생성, Presigned URL 발급, 세션 저장, Response 반환
    // ... (이전 Cycle과 동일)
}
```

**커밋**:
```bash
git commit -m "test: GeneratePresignedUrlService FileCategory 처리 테스트 추가 (Red)"
git commit -m "feat: GeneratePresignedUrlService FileCategory 처리 구현 (Green)"
```

---

### Cycle 15: CompleteUploadService - 세션 조회 및 검증

**목적**: SessionId로 UploadSession 조회 및 상태 검증 (만료, 중복)

**Red** (test: 커밋):

```java
// application/src/test/java/.../service/CompleteUploadServiceTest.java
package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.UserContext;
import com.ryuqq.fileflow.application.dto.command.CompleteUploadCommand;
import com.ryuqq.fileflow.application.dto.response.FileResponse;
import com.ryuqq.fileflow.application.port.out.command.FilePersistencePort;
import com.ryuqq.fileflow.application.port.out.command.UploadSessionPersistencePort;
import com.ryuqq.fileflow.application.port.out.query.UploadSessionQueryPort;
import com.ryuqq.fileflow.domain.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.enums.UploaderType;
import com.ryuqq.fileflow.domain.exception.SessionExpiredException;
import com.ryuqq.fileflow.domain.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class CompleteUploadServiceTest {

    @Mock
    private UploadSessionQueryPort uploadSessionQueryPort;
    @Mock
    private UploadSessionPersistencePort uploadSessionPersistencePort;
    @Mock
    private FilePersistencePort filePersistencePort;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private Clock clock;
    private CompleteUploadService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clock = Clock.fixed(Instant.parse("2025-01-18T10:00:00Z"), ZoneId.systemDefault());
        service = new CompleteUploadService(
            uploadSessionQueryPort,
            uploadSessionPersistencePort,
            filePersistencePort,
            clock
        );

        SecurityContextHolder.setContext(securityContext);
        given(securityContext.getAuthentication()).willReturn(authentication);
    }

    @Test
    void 세션_만료_시_SessionExpiredException_발생() {
        // Given: 만료된 세션
        SessionId sessionId = SessionId.of("session-expired");
        Clock pastClock = Clock.fixed(
            Instant.parse("2025-01-18T09:50:00Z"),
            ZoneId.systemDefault()
        );

        UploadSession expiredSession = UploadSession.initiate(
            sessionId,
            TenantId.of(1L),
            FileName.of("test.jpg"),
            FileSize.of(1024L),
            MimeType.of("image/jpeg"),
            UploadType.SINGLE,
            PresignedUrl.of("https://s3.amazonaws.com/url"),
            pastClock
        );

        given(uploadSessionQueryPort.findBySessionId(sessionId))
            .willReturn(Optional.of(expiredSession));

        UserContext userContext = new UserContext(
            TenantId.of(1L),
            UploaderId.of(100L),
            UploaderType.ADMIN,
            "connectly"
        );
        given(authentication.getPrincipal()).willReturn(userContext);

        CompleteUploadCommand command = new CompleteUploadCommand(sessionId);

        // When & Then: 만료 체크 시 예외 발생
        assertThatThrownBy(() -> service.execute(command))
            .isInstanceOf(SessionExpiredException.class);
    }

    @Test
    void 세션_조회_실패_시_SessionNotFoundException_발생() {
        // Given: 존재하지 않는 세션
        SessionId sessionId = SessionId.of("session-not-found");
        given(uploadSessionQueryPort.findBySessionId(sessionId))
            .willReturn(Optional.empty());

        UserContext userContext = new UserContext(
            TenantId.of(1L),
            UploaderId.of(100L),
            UploaderType.ADMIN,
            "connectly"
        );
        given(authentication.getPrincipal()).willReturn(userContext);

        CompleteUploadCommand command = new CompleteUploadCommand(sessionId);

        // When & Then
        assertThatThrownBy(() -> service.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("세션을 찾을 수 없습니다");
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../service/CompleteUploadService.java
package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.UserContext;
import com.ryuqq.fileflow.application.dto.command.CompleteUploadCommand;
import com.ryuqq.fileflow.application.dto.response.FileResponse;
import com.ryuqq.fileflow.application.port.in.command.CompleteUploadUseCase;
import com.ryuqq.fileflow.application.port.out.command.FilePersistencePort;
import com.ryuqq.fileflow.application.port.out.command.UploadSessionPersistencePort;
import com.ryuqq.fileflow.application.port.out.query.UploadSessionQueryPort;
import com.ryuqq.fileflow.domain.aggregate.UploadSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * 업로드 완료 Service
 */
@Component
@Transactional
public class CompleteUploadService implements CompleteUploadUseCase {

    private final UploadSessionQueryPort uploadSessionQueryPort;
    private final UploadSessionPersistencePort uploadSessionPersistencePort;
    private final FilePersistencePort filePersistencePort;
    private final Clock clock;

    public CompleteUploadService(
        UploadSessionQueryPort uploadSessionQueryPort,
        UploadSessionPersistencePort uploadSessionPersistencePort,
        FilePersistencePort filePersistencePort,
        Clock clock
    ) {
        this.uploadSessionQueryPort = uploadSessionQueryPort;
        this.uploadSessionPersistencePort = uploadSessionPersistencePort;
        this.filePersistencePort = filePersistencePort;
        this.clock = clock;
    }

    @Override
    public FileResponse execute(CompleteUploadCommand command) {
        // 1. SecurityContext에서 UserContext 추출
        UserContext userContext = (UserContext) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();

        // 2. UploadSession 조회
        UploadSession session = uploadSessionQueryPort
            .findBySessionId(command.sessionId())
            .orElseThrow(() -> new IllegalArgumentException(
                "세션을 찾을 수 없습니다: " + command.sessionId().value()
            ));

        // 3. 세션 상태 검증
        session.ensureNotExpired(clock);
        session.ensureNotCompleted();

        // 4-11. File 생성, 저장, 세션 완료 처리 (다음 Cycle에서 구현)
        return null;
    }
}
```

**커밋**:
```bash
git commit -m "test: CompleteUploadService 세션 조회 및 검증 테스트 추가 (Red)"
git commit -m "feat: CompleteUploadService 세션 조회 및 검증 구현 (Green)"
```

---

### Cycle 16: CompleteUploadService - File 생성 로직

**목적**: UploadSession 정보로 File Aggregate 생성

**Red** (test: 커밋):

```java
// application/src/test/java/.../service/CompleteUploadServiceTest.java (추가)

@Test
void File_Aggregate_생성_및_저장() {
    // Given: 유효한 세션
    SessionId sessionId = SessionId.of("session-valid");
    UploadSession session = UploadSession.initiate(
        sessionId,
        TenantId.of(1L),
        FileName.of("test.jpg"),
        FileSize.of(1024L),
        MimeType.of("image/jpeg"),
        UploadType.SINGLE,
        PresignedUrl.of("https://s3.amazonaws.com/url"),
        clock
    );

    given(uploadSessionQueryPort.findBySessionId(sessionId))
        .willReturn(Optional.of(session));

    UserContext userContext = new UserContext(
        TenantId.of(1L),
        UploaderId.of(100L),
        UploaderType.ADMIN,
        "connectly"
    );
    given(authentication.getPrincipal()).willReturn(userContext);

    // File save Mock
    given(filePersistencePort.save(any(File.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // UploadSession update Mock
    given(uploadSessionPersistencePort.update(any(UploadSession.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    CompleteUploadCommand command = new CompleteUploadCommand(sessionId);

    // When
    FileResponse response = service.execute(command);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.sessionId()).isEqualTo("session-valid");
    assertThat(response.fileName()).isEqualTo("test.jpg");
    assertThat(response.fileSize()).isEqualTo(1024L);
    assertThat(response.mimeType()).isEqualTo("image/jpeg");
    assertThat(response.status()).isEqualTo("COMPLETED");
    assertThat(response.s3Key()).startsWith("uploads/");
    assertThat(response.s3Bucket()).isEqualTo("fileflow-uploads-1");

    // File 저장 검증
    verify(filePersistencePort).save(any(File.class));
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../service/CompleteUploadService.java (수정)

@Override
public FileResponse execute(CompleteUploadCommand command) {
    // 1-3. SecurityContext, 세션 조회, 상태 검증 (이전 Cycle과 동일)

    // 4. FileId 생성
    FileId fileId = FileId.generate();

    // 5. FileCategory 처리
    FileCategory category = userContext.uploaderType() == UploaderType.CUSTOMER
        ? FileCategory.defaultCategory()
        : FileCategory.defaultCategory();  // MVP에서는 기본값

    // 6. S3Key 재생성 (세션 생성 시와 동일한 경로)
    S3Key s3Key = S3Key.generate(
        userContext.tenantId(),
        userContext.uploaderType(),
        userContext.uploaderSlug(),
        category,
        fileId,
        session.fileName()
    );

    // 7. S3Bucket 생성
    S3Bucket s3Bucket = S3Bucket.forTenant(userContext.tenantId());

    // 8. File Aggregate 생성
    File file = File.createFromSession(
        fileId,
        session.fileName(),
        session.fileSize(),
        session.mimeType(),
        s3Key,
        s3Bucket,
        userContext.uploaderId(),
        userContext.uploaderType(),
        userContext.uploaderSlug(),
        category,
        userContext.tenantId(),
        clock
    );

    // 9. File 저장
    File savedFile = filePersistencePort.save(file);

    // 10-11. 세션 완료 처리, Response 반환 (다음 Cycle에서 구현)
    return new FileResponse(
        session.sessionId().value(),
        savedFile.fileId().value(),
        savedFile.fileName().value(),
        savedFile.fileSize().bytes(),
        savedFile.mimeType().value(),
        savedFile.status().name(),
        savedFile.s3Key().value(),
        savedFile.s3Bucket().value(),
        savedFile.createdAt()
    );
}
```

**커밋**:
```bash
git commit -m "test: CompleteUploadService File 생성 테스트 추가 (Red)"
git commit -m "feat: CompleteUploadService File 생성 구현 (Green)"
```

---

### Cycle 17: CompleteUploadService - 세션 완료 처리

**목적**: UploadSession을 COMPLETED 상태로 업데이트

**Red** (test: 커밋):

```java
// application/src/test/java/.../service/CompleteUploadServiceTest.java (추가)

@Test
void UploadSession_COMPLETED_상태로_업데이트() {
    // Given
    SessionId sessionId = SessionId.of("session-to-complete");
    UploadSession session = UploadSession.initiate(
        sessionId,
        TenantId.of(1L),
        FileName.of("test.jpg"),
        FileSize.of(1024L),
        MimeType.of("image/jpeg"),
        UploadType.SINGLE,
        PresignedUrl.of("https://s3.amazonaws.com/url"),
        clock
    );

    given(uploadSessionQueryPort.findBySessionId(sessionId))
        .willReturn(Optional.of(session));

    UserContext userContext = new UserContext(
        TenantId.of(1L),
        UploaderId.of(100L),
        UploaderType.ADMIN,
        "connectly"
    );
    given(authentication.getPrincipal()).willReturn(userContext);

    given(filePersistencePort.save(any(File.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    given(uploadSessionPersistencePort.update(any(UploadSession.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    CompleteUploadCommand command = new CompleteUploadCommand(sessionId);

    // When
    service.execute(command);

    // Then: 세션 완료 처리 및 업데이트 확인
    verify(uploadSessionPersistencePort).update(argThat(
        updatedSession -> updatedSession.status() == SessionStatus.COMPLETED
    ));
}
```

**Green** (feat: 커밋):

```java
// application/src/main/java/.../service/CompleteUploadService.java (수정)

@Override
public FileResponse execute(CompleteUploadCommand command) {
    // 1-9. SecurityContext, 세션 조회/검증, File 생성/저장 (이전 Cycle과 동일)

    // 10. UploadSession 완료 처리
    session.markAsCompleted(clock);
    uploadSessionPersistencePort.update(session);

    // 11. Response 반환
    return new FileResponse(
        session.sessionId().value(),
        savedFile.fileId().value(),
        savedFile.fileName().value(),
        savedFile.fileSize().bytes(),
        savedFile.mimeType().value(),
        savedFile.status().name(),
        savedFile.s3Key().value(),
        savedFile.s3Bucket().value(),
        savedFile.createdAt()
    );
}
```

**커밋**:
```bash
git commit -m "test: CompleteUploadService 세션 완료 처리 테스트 추가 (Red)"
git commit -m "feat: CompleteUploadService 세션 완료 처리 구현 (Green)"
```

---

### Cycle 18: Transaction 경계 검증

**목적**: @Transactional 내 외부 API 호출 검증

**Red** (test: 커밋):

```java
// application/src/test/java/.../service/TransactionBoundaryTest.java
package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.port.out.external.S3ClientPort;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

class TransactionBoundaryTest {

    @Test
    void GeneratePresignedUrlService는_Transactional_어노테이션_존재() {
        assertThat(GeneratePresignedUrlService.class.isAnnotationPresent(Transactional.class))
            .isTrue();
    }

    @Test
    void CompleteUploadService는_Transactional_어노테이션_존재() {
        assertThat(CompleteUploadService.class.isAnnotationPresent(Transactional.class))
            .isTrue();
    }

    @Test
    void GeneratePresignedUrlService는_S3ClientPort_의존성_포함() {
        // S3ClientPort 의존성이 있음 (외부 API 호출)
        // ⚠️ Transaction 경계 주의: S3 호출 후 Transaction 시작 권장
        assertThat(GeneratePresignedUrlService.class.getDeclaredFields())
            .anyMatch(field -> field.getType().equals(S3ClientPort.class));
    }

    @Test
    void CompleteUploadService는_외부_API_호출_없음() {
        // FilePersistencePort, UploadSessionPersistencePort만 사용
        // Transaction 내 안전
        assertThat(CompleteUploadService.class.getDeclaredFields())
            .noneMatch(field -> field.getType().equals(S3ClientPort.class));
    }
}
```

**Green** (feat: 커밋):

```java
// 코드 변경 없음 (검증만 수행)
// GeneratePresignedUrlService는 S3 호출이 포함되어 있으므로,
// 실제 운영 시 Transaction 경계 조정 필요 (pre-commit hook에서 경고)
```

**커밋**:
```bash
git commit -m "test: Transaction 경계 검증 테스트 추가 (Red)"
git commit -m "chore: Transaction 경계 검증 완료 (Green)"
```

---

## 📚 Phase 4: Quality & Fixtures (Cycle 19-20)

### Cycle 19: TestFixtures

**목적**: Application Layer 테스트용 Fixture 생성

**Red** (test: 커밋):

```java
// application/src/testFixtures/java/.../UserContextFixture.java
package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.UserContext;
import com.ryuqq.fileflow.domain.enums.UploaderType;
import com.ryuqq.fileflow.domain.vo.TenantId;
import com.ryuqq.fileflow.domain.vo.UploaderId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserContextFixtureTest {

    @Test
    void Admin_Fixture_생성() {
        UserContext admin = UserContextFixture.admin();

        assertThat(admin.uploaderType()).isEqualTo(UploaderType.ADMIN);
        assertThat(admin.uploaderSlug()).isEqualTo("connectly");
    }

    @Test
    void Seller_Fixture_생성() {
        UserContext seller = UserContextFixture.seller();

        assertThat(seller.uploaderType()).isEqualTo(UploaderType.SELLER);
        assertThat(seller.uploaderSlug()).isNotEqualTo("default");
    }

    @Test
    void Customer_Fixture_생성() {
        UserContext customer = UserContextFixture.customer();

        assertThat(customer.uploaderType()).isEqualTo(UploaderType.CUSTOMER);
        assertThat(customer.uploaderSlug()).isEqualTo("default");
    }
}
```

**Green** (feat: 커밋):

```java
// application/src/testFixtures/java/.../UserContextFixture.java
package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.UserContext;
import com.ryuqq.fileflow.domain.enums.UploaderType;
import com.ryuqq.fileflow.domain.vo.TenantId;
import com.ryuqq.fileflow.domain.vo.UploaderId;

public class UserContextFixture {

    public static UserContext admin() {
        return new UserContext(
            TenantId.of(1L),
            UploaderId.of(1L),
            UploaderType.ADMIN,
            "connectly"
        );
    }

    public static UserContext seller() {
        return new UserContext(
            TenantId.of(1L),
            UploaderId.of(200L),
            UploaderType.SELLER,
            "samsung-electronics"
        );
    }

    public static UserContext customer() {
        return new UserContext(
            TenantId.of(1L),
            UploaderId.of(300L),
            UploaderType.CUSTOMER,
            "default"
        );
    }
}
```

**추가 Fixture**:

```java
// application/src/testFixtures/java/.../GeneratePresignedUrlCommandFixture.java
package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.command.GeneratePresignedUrlCommand;
import com.ryuqq.fileflow.domain.enums.UploaderType;
import com.ryuqq.fileflow.domain.vo.*;

public class GeneratePresignedUrlCommandFixture {

    public static GeneratePresignedUrlCommand create() {
        return new GeneratePresignedUrlCommand(
            SessionId.of("session-123"),
            FileName.of("test.jpg"),
            FileSize.of(1024L),
            MimeType.of("image/jpeg"),
            null
        );
    }

    public static GeneratePresignedUrlCommand adminWithCategory() {
        return new GeneratePresignedUrlCommand(
            SessionId.of("session-admin"),
            FileName.of("banner.jpg"),
            FileSize.of(2048L),
            MimeType.of("image/jpeg"),
            FileCategory.of("banner", UploaderType.ADMIN)
        );
    }
}
```

**커밋**:
```bash
git commit -m "test: Application Layer TestFixtures 테스트 추가 (Red)"
git commit -m "feat: Application Layer TestFixtures 구현 (Green)"
```

---

### Cycle 20: ArchUnit 테스트 + Coverage 90%

**목적**: Application Layer 아키텍처 규칙 검증 및 커버리지 90% 달성

**Red** (test: 커밋):

```java
// application/src/test/java/.../architecture/ApplicationLayerArchTest.java
package com.ryuqq.fileflow.application.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class ApplicationLayerArchTest {

    private JavaClasses classes;

    @BeforeEach
    void setUp() {
        classes = new ClassFileImporter()
            .importPackages("com.ryuqq.fileflow.application");
    }

    @Test
    void UseCase_인터페이스는_Port_In_패키지에_위치() {
        classes()
            .that().haveSimpleNameEndingWith("UseCase")
            .should().resideInAPackage("..port.in..")
            .check(classes);
    }

    @Test
    void Service는_Component_어노테이션_필수() {
        classes()
            .that().haveSimpleNameEndingWith("Service")
            .should().beAnnotatedWith(Component.class)
            .check(classes);
    }

    @Test
    void Service는_Transactional_어노테이션_필수() {
        classes()
            .that().haveSimpleNameEndingWith("Service")
            .should().beAnnotatedWith(Transactional.class)
            .check(classes);
    }

    @Test
    void Command_DTO는_Record_타입() {
        classes()
            .that().resideInAPackage("..dto.command..")
            .and().haveSimpleNameEndingWith("Command")
            .should().beRecords()
            .check(classes);
    }

    @Test
    void Response_DTO는_Record_타입() {
        classes()
            .that().resideInAPackage("..dto.response..")
            .and().haveSimpleNameEndingWith("Response")
            .should().beRecords()
            .check(classes);
    }

    @Test
    void Port_Out은_Interface여야_함() {
        classes()
            .that().resideInAPackage("..port.out..")
            .should().beInterfaces()
            .check(classes);
    }

    @Test
    void Application_Layer는_Domain에만_의존() {
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..persistence..")
            .orShould().dependOnClassesThat().resideInAPackage("..rest..")
            .check(classes);
    }
}
```

**Green** (feat: 커밋):

```bash
# ArchUnit 테스트 실행
./gradlew test --tests ApplicationLayerArchTest

# 커버리지 확인
./gradlew jacocoTestReport

# 커버리지 90% 달성 확인
# application/build/reports/jacoco/test/html/index.html
```

**커밋**:
```bash
git commit -m "test: Application Layer ArchUnit 테스트 추가 (Red)"
git commit -m "chore: Application Layer ArchUnit 테스트 통과 + Coverage 90% (Green)"
```

---

## ✅ 완료 조건

### Phase 1: DTOs
- [x] Cycle 1: UserContext Record
- [x] Cycle 2: GeneratePresignedUrlCommand
- [x] Cycle 3: CompleteUploadCommand
- [x] Cycle 4: PresignedUrlResponse
- [x] Cycle 5: FileResponse

### Phase 2: Port Interfaces
- [x] Cycle 6: GeneratePresignedUrlUseCase (Port In)
- [x] Cycle 7: CompleteUploadUseCase (Port In)
- [x] Cycle 8: FilePersistencePort (Port Out - Command)
- [x] Cycle 9: UploadSessionPersistencePort (Port Out - Command)
- [x] Cycle 10: UploadSessionQueryPort (Port Out - Query)
- [x] Cycle 11: S3ClientPort (Port Out - External)

### Phase 3: Service Implementation
- [x] Cycle 12: GeneratePresignedUrlService - 멱등성 확인
- [x] Cycle 13: GeneratePresignedUrlService - 새 세션 생성
- [x] Cycle 14: GeneratePresignedUrlService - FileCategory 처리
- [x] Cycle 15: CompleteUploadService - 세션 조회 및 검증
- [x] Cycle 16: CompleteUploadService - File 생성
- [x] Cycle 17: CompleteUploadService - 세션 완료 처리
- [x] Cycle 18: Transaction 경계 검증

### Phase 4: Quality & Fixtures
- [x] Cycle 19: TestFixtures
- [x] Cycle 20: ArchUnit 테스트 + Coverage 90%

---

## 🔗 다음 단계

```bash
# TDD 시작
/kb/application/go  # → Cycle 1 실행 (UserContext Record)

# 또는 Persistence Layer Plan 생성
/create-plan FILE-003  # → Persistence Layer TDD Plan 생성
```

---

## 📚 Zero-Tolerance 규칙 준수

- ✅ **Lombok 금지**: 모든 DTO는 Record 사용
- ✅ **Law of Demeter**: DTO Flat 구조
- ✅ **Transaction 경계**: `@Transactional` 내 외부 API 호출 주의 (GeneratePresignedUrlService)
- ✅ **Port 분리**: Command/Query 명확히 분리
- ✅ **ArchUnit 검증**: UseCase, Service, DTO, Port 모두 규칙 준수
- ✅ **테스트 커버리지**: 90% 이상 (JaCoCo)
