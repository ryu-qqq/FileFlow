package com.ryuqq.fileflow.adapter.in.rest.common.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.in.rest.common.error.ErrorMapperRegistry;
import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * GlobalExceptionHandler 단위 테스트
 *
 * <p>각 예외 핸들러 메서드가 올바른 RFC 7807 ProblemDetail 응답을 생성하는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private ErrorMapperRegistry errorMapperRegistry;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler(errorMapperRegistry);
        when(request.getRequestURI()).thenReturn("/api/v1/test");
        when(request.getQueryString()).thenReturn(null);
    }

    @Nested
    @DisplayName("handleValidationException - MethodArgumentNotValidException")
    class HandleValidationException {

        @Mock
        private BindingResult bindingResult;

        @Test
        @DisplayName("필드 유효성 검증 실패 시 400 응답 반환")
        void shouldReturn400WhenValidationFails() {
            // given
            FieldError fieldError = new FieldError("request", "email", "이메일 형식이 올바르지 않습니다");
            when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

            MethodArgumentNotValidException exception =
                    new MethodArgumentNotValidException(null, bindingResult);

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleValidationException(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
            assertThat(response.getBody().getDetail()).isEqualTo("Validation failed for request");
            assertThat(response.getBody().getProperties()).containsKey("errors");
        }

        @Test
        @DisplayName("여러 필드 유효성 검증 실패 시 모든 에러 포함")
        void shouldIncludeAllFieldErrors() {
            // given
            FieldError fieldError1 = new FieldError("request", "email", "이메일 형식이 올바르지 않습니다");
            FieldError fieldError2 = new FieldError("request", "name", "이름은 필수입니다");
            when(bindingResult.getFieldErrors())
                    .thenReturn(java.util.List.of(fieldError1, fieldError2));

            MethodArgumentNotValidException exception =
                    new MethodArgumentNotValidException(null, bindingResult);

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleValidationException(exception, request);

            // then
            assertThat(response.getBody()).isNotNull();
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> errors =
                    (java.util.Map<String, String>) response.getBody().getProperties().get("errors");
            assertThat(errors).containsKeys("email", "name");
        }
    }

    @Nested
    @DisplayName("handleBindException - BindException")
    class HandleBindException {

        @Mock
        private BindingResult bindingResult;

        @Test
        @DisplayName("바인딩 실패 시 400 응답 반환")
        void shouldReturn400WhenBindingFails() {
            // given
            FieldError fieldError = new FieldError("request", "page", "숫자여야 합니다");
            when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

            BindException exception = new BindException(bindingResult);

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleBindException(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Bad Request");
        }
    }

    @Nested
    @DisplayName("handleConstraintViolation - ConstraintViolationException")
    class HandleConstraintViolation {

        @Mock
        private ConstraintViolation<?> violation;

        @Mock
        private Path propertyPath;

        @Test
        @DisplayName("제약조건 위반 시 400 응답 반환")
        void shouldReturn400WhenConstraintViolation() {
            // given
            when(violation.getPropertyPath()).thenReturn(propertyPath);
            when(propertyPath.toString()).thenReturn("userId");
            when(violation.getMessage()).thenReturn("양수여야 합니다");

            ConstraintViolationException exception =
                    new ConstraintViolationException(Set.of(violation));

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleConstraintViolation(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getProperties()).containsKey("errors");
        }

        @Test
        @DisplayName("propertyPath가 null인 경우 unknown으로 처리")
        void shouldHandleNullPropertyPath() {
            // given
            when(violation.getPropertyPath()).thenReturn(null);
            when(violation.getMessage()).thenReturn("유효하지 않습니다");

            ConstraintViolationException exception =
                    new ConstraintViolationException(Set.of(violation));

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleConstraintViolation(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> errors =
                    (java.util.Map<String, String>) response.getBody().getProperties().get("errors");
            assertThat(errors).containsKey("unknown");
        }
    }

    @Nested
    @DisplayName("handleIllegalArgumentException")
    class HandleIllegalArgumentException {

        @Test
        @DisplayName("잘못된 인자 시 400 응답 반환")
        void shouldReturn400WithMessage() {
            // given
            IllegalArgumentException exception =
                    new IllegalArgumentException("잘못된 파라미터입니다");

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleIllegalArgumentException(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail()).isEqualTo("잘못된 파라미터입니다");
        }

        @Test
        @DisplayName("메시지가 null인 경우 기본 메시지 반환")
        void shouldReturnDefaultMessageWhenNull() {
            // given
            IllegalArgumentException exception = new IllegalArgumentException((String) null);

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleIllegalArgumentException(exception, request);

            // then
            assertThat(response.getBody().getDetail()).isEqualTo("Invalid argument");
        }
    }

    @Nested
    @DisplayName("handleHttpMessageNotReadable")
    class HandleHttpMessageNotReadable {

        @Test
        @DisplayName("JSON 파싱 실패 시 400 응답 반환")
        void shouldReturn400WhenJsonParsingFails() {
            // given
            HttpMessageNotReadableException exception =
                    new HttpMessageNotReadableException(
                            "JSON parse error", new RuntimeException("Invalid JSON"));

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleHttpMessageNotReadable(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail())
                    .isEqualTo("잘못된 요청 형식입니다. JSON 형식을 확인해주세요.");
        }
    }

    @Nested
    @DisplayName("handleTypeMismatch - MethodArgumentTypeMismatchException")
    class HandleTypeMismatch {

        @Test
        @DisplayName("타입 불일치 시 400 응답 반환")
        void shouldReturn400WhenTypeMismatch() {
            // given
            MethodArgumentTypeMismatchException exception =
                    new MethodArgumentTypeMismatchException(
                            "abc", Long.class, "id", null, new NumberFormatException());

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleTypeMismatch(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail())
                    .contains("id")
                    .contains("abc")
                    .contains("Long");
        }

        @Test
        @DisplayName("requiredType이 null인 경우 기본 문자열 사용")
        void shouldHandleNullRequiredType() {
            // given
            MethodArgumentTypeMismatchException exception =
                    new MethodArgumentTypeMismatchException(
                            "abc", null, "id", null, new RuntimeException());

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleTypeMismatch(exception, request);

            // then
            assertThat(response.getBody().getDetail()).contains("required type");
        }
    }

    @Nested
    @DisplayName("handleMissingParam - MissingServletRequestParameterException")
    class HandleMissingParam {

        @Test
        @DisplayName("필수 파라미터 누락 시 400 응답 반환")
        void shouldReturn400WhenParamMissing() {
            // given
            MissingServletRequestParameterException exception =
                    new MissingServletRequestParameterException("page", "int");

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleMissingParam(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail()).contains("page");
        }
    }

    @Nested
    @DisplayName("handleNoResource - NoResourceFoundException")
    class HandleNoResource {

        @Test
        @DisplayName("리소스 없음 시 404 응답 반환")
        void shouldReturn404WhenResourceNotFound() {
            // given
            NoResourceFoundException exception =
                    new NoResourceFoundException(HttpMethod.GET, "/api/v1/unknown");

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleNoResource(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Not Found");
        }
    }

    @Nested
    @DisplayName("handleMethodNotAllowed - HttpRequestMethodNotSupportedException")
    class HandleMethodNotAllowed {

        @Test
        @DisplayName("지원하지 않는 메서드 시 405 응답 반환")
        void shouldReturn405WithSupportedMethods() {
            // given
            HttpRequestMethodNotSupportedException exception =
                    new HttpRequestMethodNotSupportedException(
                            "PUT", Set.of("GET", "POST"));

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleMethodNotAllowed(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getDetail())
                    .contains("PUT")
                    .contains("메서드는 지원하지 않습니다");
        }

        @Test
        @DisplayName("지원 메서드가 없는 경우 '없음' 표시")
        void shouldHandleEmptySupportedMethods() {
            // given
            HttpRequestMethodNotSupportedException exception =
                    new HttpRequestMethodNotSupportedException("DELETE");

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleMethodNotAllowed(exception, request);

            // then
            assertThat(response.getBody().getDetail()).contains("없음");
        }
    }

    @Nested
    @DisplayName("handleIllegalState - IllegalStateException")
    class HandleIllegalState {

        @Test
        @DisplayName("상태 충돌 시 409 응답 반환")
        void shouldReturn409WhenStateConflict() {
            // given
            IllegalStateException exception =
                    new IllegalStateException("이미 처리된 요청입니다");

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleIllegalState(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Conflict");
            assertThat(response.getBody().getDetail()).isEqualTo("이미 처리된 요청입니다");
        }

        @Test
        @DisplayName("메시지가 null인 경우 기본 메시지 반환")
        void shouldReturnDefaultMessageWhenNull() {
            // given
            IllegalStateException exception = new IllegalStateException((String) null);

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleIllegalState(exception, request);

            // then
            assertThat(response.getBody().getDetail()).isEqualTo("State conflict");
        }
    }

    @Nested
    @DisplayName("handleGlobal - Exception")
    class HandleGlobal {

        @Test
        @DisplayName("예상치 못한 예외 시 500 응답 반환")
        void shouldReturn500WhenUnexpectedError() {
            // given
            Exception exception = new RuntimeException("서버 내부 오류");

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleGlobal(exception, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Internal Server Error");
            assertThat(response.getBody().getDetail())
                    .isEqualTo("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    @Nested
    @DisplayName("handleDomain - DomainException")
    class HandleDomain {

        @Test
        @DisplayName("도메인 예외를 ErrorMapper로 매핑하여 응답")
        void shouldMapDomainExceptionUsingMapper() {
            // given
            DomainException exception = new TestDomainException(TestErrorCode.TEST_ERROR, "테스트 에러");
            ErrorMapper.MappedError mappedError =
                    new ErrorMapper.MappedError(
                            HttpStatus.NOT_FOUND,
                            "Not Found",
                            "리소스를 찾을 수 없습니다",
                            URI.create("about:blank"));

            when(errorMapperRegistry.map(any(DomainException.class), any(Locale.class)))
                    .thenReturn(Optional.of(mappedError));

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleDomain(exception, request, Locale.KOREA);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTitle()).isEqualTo("Not Found");
            assertThat(response.getBody().getDetail()).isEqualTo("리소스를 찾을 수 없습니다");
            assertThat(response.getBody().getProperties()).containsEntry("code", TestErrorCode.TEST_ERROR.getCode());
        }

        @Test
        @DisplayName("매핑되지 않은 도메인 예외는 기본 매핑 사용")
        void shouldUseDefaultMappingWhenNotMapped() {
            // given
            DomainException exception = new TestDomainException(TestErrorCode.UNKNOWN_ERROR, "알 수 없는 에러");
            ErrorMapper.MappedError defaultMapping =
                    new ErrorMapper.MappedError(
                            HttpStatus.BAD_REQUEST,
                            "Bad Request",
                            "알 수 없는 에러",
                            URI.create("about:blank"));

            when(errorMapperRegistry.map(any(DomainException.class), any(Locale.class)))
                    .thenReturn(Optional.empty());
            when(errorMapperRegistry.defaultMapping(any(DomainException.class)))
                    .thenReturn(defaultMapping);

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleDomain(exception, request, Locale.KOREA);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("5xx 에러는 ERROR 레벨로 로깅")
        void shouldLogErrorLevelFor5xxErrors() {
            // given
            DomainException exception = new TestDomainException(TestErrorCode.SERVER_ERROR, "서버 에러");
            ErrorMapper.MappedError mappedError =
                    new ErrorMapper.MappedError(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Internal Server Error",
                            "서버 에러가 발생했습니다",
                            URI.create("about:blank"));

            when(errorMapperRegistry.map(any(DomainException.class), any(Locale.class)))
                    .thenReturn(Optional.of(mappedError));

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleDomain(exception, request, Locale.KOREA);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("쿼리 스트링 처리")
    class QueryStringHandling {

        @Test
        @DisplayName("쿼리 스트링이 있는 경우 instance에 포함")
        void shouldIncludeQueryStringInInstance() {
            // given
            when(request.getQueryString()).thenReturn("page=1&size=10");
            IllegalArgumentException exception =
                    new IllegalArgumentException("잘못된 파라미터");

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleIllegalArgumentException(exception, request);

            // then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getInstance().toString())
                    .isEqualTo("/api/v1/test?page=1&size=10");
        }

        @Test
        @DisplayName("빈 쿼리 스트링은 포함하지 않음")
        void shouldNotIncludeBlankQueryString() {
            // given
            when(request.getQueryString()).thenReturn("   ");
            IllegalArgumentException exception =
                    new IllegalArgumentException("잘못된 파라미터");

            // when
            ResponseEntity<ProblemDetail> response =
                    exceptionHandler.handleIllegalArgumentException(exception, request);

            // then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getInstance().toString())
                    .isEqualTo("/api/v1/test");
        }
    }

    /**
     * 테스트용 ErrorCode 구현
     */
    private enum TestErrorCode implements com.ryuqq.fileflow.domain.common.exception.ErrorCode {
        TEST_ERROR("TEST_ERROR", 400, "테스트 에러"),
        UNKNOWN_ERROR("UNKNOWN_ERROR", 400, "알 수 없는 에러"),
        SERVER_ERROR("SERVER_ERROR", 500, "서버 에러");

        private final String code;
        private final int httpStatus;
        private final String message;

        TestErrorCode(String code, int httpStatus, String message) {
            this.code = code;
            this.httpStatus = httpStatus;
            this.message = message;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public int getHttpStatus() {
            return httpStatus;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    /**
     * 테스트용 DomainException 구현
     */
    private static class TestDomainException extends DomainException {

        TestDomainException(TestErrorCode errorCode, String message) {
            super(errorCode, message);
        }

        TestDomainException(TestErrorCode errorCode) {
            super(errorCode);
        }
    }
}
