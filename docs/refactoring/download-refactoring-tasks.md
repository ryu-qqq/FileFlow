# Download Bounded Context 리팩토링 작업 목록

## 📋 작업 개요

**날짜**: 2025-11-05
**우선순위**: P0 (필수 작업)
**예상 시간**: 3-4시간

---

## 🎯 Task 1: Domain Exception 계층 생성

### 목적
표준 Java 예외(`IllegalStateException`, `IllegalArgumentException`) 대신 Domain 계층의 커스텀 예외를 사용하여 도메인 레이어의 독립성 확보

### 파일 위치
```
domain/src/main/java/com/ryuqq/fileflow/domain/download/exception/
```

### 생성할 파일들

#### 1. DownloadException.java (Sealed Interface)
```java
package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

/**
 * Download Domain의 최상위 예외 인터페이스.
 *
 * @author system
 * @since 1.0
 */
public sealed interface DownloadException extends DomainException
    permits InvalidDownloadStateException,
            InvalidUrlException,
            DownloadNotFoundException {

    @Override
    String getErrorCode();

    @Override
    String getMessage();
}
```

#### 2. InvalidDownloadStateException.java
```java
package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.download.DownloadStatus;

/**
 * 다운로드 상태 전이가 유효하지 않을 때 발생하는 예외.
 *
 * <p>예시:
 * <ul>
 *   <li>INIT 상태가 아닌데 start() 호출</li>
 *   <li>COMPLETED 상태에서 다시 start() 호출</li>
 * </ul>
 *
 * @author system
 * @since 1.0
 */
public final class InvalidDownloadStateException implements DownloadException {

    private static final String ERROR_CODE = "DOWNLOAD_INVALID_STATE";

    private final String message;
    private final DownloadStatus currentState;
    private final String attemptedAction;

    /**
     * InvalidDownloadStateException 생성자.
     *
     * @param currentState 현재 다운로드 상태
     * @param attemptedAction 시도한 작업 (예: "start", "complete")
     */
    public InvalidDownloadStateException(DownloadStatus currentState, String attemptedAction) {
        this.currentState = currentState;
        this.attemptedAction = attemptedAction;
        this.message = String.format(
            "Cannot %s download in %s state",
            attemptedAction,
            currentState
        );
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public DownloadStatus getCurrentState() {
        return currentState;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}
```

#### 3. InvalidUrlException.java
```java
package com.ryuqq.fileflow.domain.download.exception;

/**
 * 유효하지 않은 URL이 제공되었을 때 발생하는 예외.
 *
 * <p>예시:
 * <ul>
 *   <li>빈 URL</li>
 *   <li>잘못된 형식의 URL</li>
 * </ul>
 *
 * @author system
 * @since 1.0
 */
public final class InvalidUrlException implements DownloadException {

    private static final String ERROR_CODE = "DOWNLOAD_INVALID_URL";

    private final String message;
    private final String invalidUrl;

    /**
     * InvalidUrlException 생성자.
     *
     * @param invalidUrl 유효하지 않은 URL 문자열
     */
    public InvalidUrlException(String invalidUrl) {
        this.invalidUrl = invalidUrl;
        this.message = String.format("유효하지 않은 URL입니다: %s", invalidUrl);
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getInvalidUrl() {
        return invalidUrl;
    }
}
```

#### 4. DownloadNotFoundException.java
```java
package com.ryuqq.fileflow.domain.download.exception;

import com.ryuqq.fileflow.domain.download.ExternalDownloadId;

/**
 * 다운로드를 찾을 수 없을 때 발생하는 예외.
 *
 * @author system
 * @since 1.0
 */
public final class DownloadNotFoundException implements DownloadException {

    private static final String ERROR_CODE = "DOWNLOAD_NOT_FOUND";

    private final String message;
    private final Long downloadId;

    /**
     * DownloadNotFoundException 생성자.
     *
     * @param downloadId 찾을 수 없는 다운로드 ID
     */
    public DownloadNotFoundException(ExternalDownloadId downloadId) {
        this.downloadId = downloadId.value();
        this.message = String.format("Download not found: %d", this.downloadId);
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Long getDownloadId() {
        return downloadId;
    }
}
```

### 체크리스트
- [ ] `domain/src/main/java/com/ryuqq/fileflow/domain/download/exception/` 디렉토리 생성
- [ ] `DownloadException.java` 생성 (Sealed Interface)
- [ ] `InvalidDownloadStateException.java` 생성
- [ ] `InvalidUrlException.java` 생성
- [ ] `DownloadNotFoundException.java` 생성
- [ ] 모든 클래스에 Javadoc 추가 (`@author`, `@since` 포함)
- [ ] Lombok 사용 여부 확인 (❌ 금지)

---

## 🎯 Task 2: ExternalDownload.java 리팩토링

### 목적
표준 Java 예외를 Domain Exception으로 교체

### 파일 경로
```
domain/src/main/java/com/ryuqq/fileflow/domain/download/ExternalDownload.java
```

### 수정 내용

#### 1. Import 추가
```java
import com.ryuqq.fileflow.domain.download.exception.InvalidDownloadStateException;
import com.ryuqq.fileflow.domain.download.exception.InvalidUrlException;
```

#### 2. Line 262 수정
**변경 전**:
```java
throw new IllegalStateException("Can only start from INIT state: " + status);
```

**변경 후**:
```java
throw new InvalidDownloadStateException(status, "start");
```

#### 3. Line 279 수정
**변경 전**:
```java
throw new IllegalStateException("Can only complete from PROCESSING state: " + status);
```

**변경 후**:
```java
throw new InvalidDownloadStateException(status, "complete");
```

#### 4. Line 402 수정
**변경 전**:
```java
throw new IllegalArgumentException("유효하지 않은 URL입니다");
```

**변경 후**:
```java
throw new InvalidUrlException(url);
```

### 체크리스트
- [ ] Import 문 추가
- [ ] Line 262 예외 교체 (`start()` 메서드)
- [ ] Line 279 예외 교체 (`complete()` 메서드)
- [ ] Line 402 예외 교체 (URL 검증 로직)
- [ ] 빌드 성공 확인 (`./gradlew :domain:build`)

---

## 🎯 Task 3: DownloadErrorMapper 구현

### 목적
Download Domain Exception을 HTTP 응답으로 변환하는 ErrorMapper 구현

### 파일 위치
```
adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/download/mapper/
```

### 생성할 파일

#### DownloadErrorMapper.java
```java
package com.ryuqq.fileflow.adapter.rest.download.mapper;

import com.ryuqq.fileflow.adapter.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.adapter.rest.common.mapper.ErrorResponse;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.download.exception.DownloadException;
import com.ryuqq.fileflow.domain.download.exception.InvalidDownloadStateException;
import com.ryuqq.fileflow.domain.download.exception.InvalidUrlException;
import com.ryuqq.fileflow.domain.download.exception.DownloadNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Download Domain Exception을 HTTP 응답으로 변환하는 ErrorMapper.
 *
 * <p>지원하는 예외:
 * <ul>
 *   <li>{@link InvalidDownloadStateException} → 400 Bad Request</li>
 *   <li>{@link InvalidUrlException} → 400 Bad Request</li>
 *   <li>{@link DownloadNotFoundException} → 404 Not Found</li>
 * </ul>
 *
 * @author system
 * @since 1.0
 */
@Component
public class DownloadErrorMapper implements ErrorMapper {

    @Override
    public boolean supports(DomainException exception) {
        return exception instanceof DownloadException;
    }

    @Override
    public ErrorResponse map(DomainException exception, Locale locale) {
        if (exception instanceof InvalidDownloadStateException ex) {
            return mapInvalidStateException(ex, locale);
        }

        if (exception instanceof InvalidUrlException ex) {
            return mapInvalidUrlException(ex, locale);
        }

        if (exception instanceof DownloadNotFoundException ex) {
            return mapNotFoundException(ex, locale);
        }

        // Fallback (should not happen due to sealed interface)
        return ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            exception.getErrorCode(),
            exception.getMessage()
        );
    }

    /**
     * InvalidDownloadStateException을 400 Bad Request로 매핑.
     *
     * @param ex InvalidDownloadStateException
     * @param locale 로케일
     * @return ErrorResponse
     */
    private ErrorResponse mapInvalidStateException(
        InvalidDownloadStateException ex,
        Locale locale
    ) {
        return ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            String.format(
                "Current state: %s, Attempted action: %s",
                ex.getCurrentState(),
                ex.getAttemptedAction()
            )
        );
    }

    /**
     * InvalidUrlException을 400 Bad Request로 매핑.
     *
     * @param ex InvalidUrlException
     * @param locale 로케일
     * @return ErrorResponse
     */
    private ErrorResponse mapInvalidUrlException(
        InvalidUrlException ex,
        Locale locale
    ) {
        return ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            String.format("Invalid URL: %s", ex.getInvalidUrl())
        );
    }

    /**
     * DownloadNotFoundException을 404 Not Found로 매핑.
     *
     * @param ex DownloadNotFoundException
     * @param locale 로케일
     * @return ErrorResponse
     */
    private ErrorResponse mapNotFoundException(
        DownloadNotFoundException ex,
        Locale locale
    ) {
        return ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            String.format("Download ID: %d", ex.getDownloadId())
        );
    }
}
```

### 체크리스트
- [ ] `adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/download/mapper/` 디렉토리 생성
- [ ] `DownloadErrorMapper.java` 생성
- [ ] `@Component` 어노테이션 추가 (자동 스캔)
- [ ] `ErrorMapper` 인터페이스 구현
- [ ] 모든 DownloadException 서브타입 처리
- [ ] Javadoc 추가
- [ ] Lombok 사용 여부 확인 (❌ 금지)

---

## 🎯 Task 4: ErrorMapperRegistry 자동 등록 확인

### 목적
DownloadErrorMapper가 ErrorMapperRegistry에 자동으로 등록되는지 확인

### 파일 경로
```
adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/common/mapper/ErrorMapperRegistry.java
```

### 확인 사항

#### 생성자 확인
```java
public ErrorMapperRegistry(List<ErrorMapper> mappers) {
    this.mappers = mappers; // Spring이 자동으로 모든 ErrorMapper 빈 주입
}
```

**✅ 예상 동작**: `DownloadErrorMapper`에 `@Component`가 있으므로 자동으로 `List<ErrorMapper>`에 포함됨

### 체크리스트
- [ ] `ErrorMapperRegistry` 생성자에 `List<ErrorMapper>` 주입 확인
- [ ] `DownloadErrorMapper`에 `@Component` 있는지 확인
- [ ] 컴포넌트 스캔 범위에 포함되는지 확인

---

## 🎯 Task 5: 통합 테스트

### 목적
Domain Exception → ErrorMapper → HTTP 응답 전체 플로우 검증

### 테스트 시나리오

#### 1. Invalid State Exception 테스트
```bash
# INIT 상태가 아닌 다운로드를 start 시도
curl -X POST http://localhost:8080/api/downloads/{id}/start
```

**예상 응답**:
```json
{
  "status": 400,
  "errorCode": "DOWNLOAD_INVALID_STATE",
  "message": "Cannot start download in PROCESSING state",
  "detail": "Current state: PROCESSING, Attempted action: start"
}
```

#### 2. Invalid URL Exception 테스트
```bash
# 잘못된 URL로 다운로드 생성
curl -X POST http://localhost:8080/api/downloads \
  -H "Content-Type: application/json" \
  -d '{"url": ""}'
```

**예상 응답**:
```json
{
  "status": 400,
  "errorCode": "DOWNLOAD_INVALID_URL",
  "message": "유효하지 않은 URL입니다: ",
  "detail": "Invalid URL: "
}
```

#### 3. Not Found Exception 테스트
```bash
# 존재하지 않는 다운로드 조회
curl http://localhost:8080/api/downloads/99999
```

**예상 응답**:
```json
{
  "status": 404,
  "errorCode": "DOWNLOAD_NOT_FOUND",
  "message": "Download not found: 99999",
  "detail": "Download ID: 99999"
}
```

### 체크리스트
- [ ] Invalid State 시나리오 테스트
- [ ] Invalid URL 시나리오 테스트
- [ ] Not Found 시나리오 테스트
- [ ] HTTP 상태 코드 확인 (400, 404)
- [ ] ErrorCode 필드 확인
- [ ] Message 필드 확인
- [ ] Detail 필드 확인

---

## 📊 전체 진행 상황

### 우선순위별 작업
- **P0 (필수)**: Task 1-5
- **P1 (중요)**: CQRS Port 분리 (별도 문서)
- **P2 (선택)**: Manager 레이어 구조 정리 (별도 문서)

### 예상 소요 시간
| Task | 예상 시간 |
|------|----------|
| Task 1: Domain Exception 생성 | 1시간 |
| Task 2: ExternalDownload 리팩토링 | 30분 |
| Task 3: DownloadErrorMapper 구현 | 1시간 |
| Task 4: ErrorMapperRegistry 확인 | 15분 |
| Task 5: 통합 테스트 | 30분 |
| **Total** | **3-4시간** |

---

## ✅ 완료 기준

### 기술적 기준
- [ ] 모든 표준 Java 예외가 Domain Exception으로 교체됨
- [ ] DownloadErrorMapper가 ErrorMapperRegistry에 등록됨
- [ ] 모든 통합 테스트 시나리오 통과
- [ ] 빌드 성공 (`./gradlew clean build`)

### 코딩 컨벤션 기준
- [ ] Lombok 사용 없음 (Pure Java)
- [ ] 모든 public 클래스/메서드에 Javadoc
- [ ] `@author`, `@since` 포함
- [ ] Law of Demeter 준수

### 문서화 기준
- [ ] 이 문서의 모든 체크리스트 완료
- [ ] 코드 리뷰 통과
- [ ] PR 생성 및 머지

---

**작성자**: Claude Code
**날짜**: 2025-11-05
**다음 작업**: P1 (CQRS Port 분리) 문서 작성
