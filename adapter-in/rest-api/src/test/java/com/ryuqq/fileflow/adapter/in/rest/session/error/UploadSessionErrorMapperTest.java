package com.ryuqq.fileflow.adapter.in.rest.session.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper.MappedError;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.session.exception.SessionErrorCode;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

@DisplayName("UploadSessionErrorMapper 단위 테스트")
class UploadSessionErrorMapperTest {

    private UploadSessionErrorMapper mapper;
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);
        mapper = new UploadSessionErrorMapper(messageSource);
    }

    @Nested
    @DisplayName("supports 메서드 테스트")
    class SupportsTest {

        @ParameterizedTest
        @EnumSource(SessionErrorCode.class)
        @DisplayName("모든 SessionErrorCode를 지원한다")
        void supports_AllSessionErrorCodes_ShouldReturnTrue(SessionErrorCode errorCode) {
            // when
            boolean result = mapper.supports(errorCode.getCode());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 도메인의 에러 코드는 지원하지 않는다")
        void supports_OtherDomainErrorCode_ShouldReturnFalse() {
            // when
            boolean result = mapper.supports("ASSET_NOT_FOUND");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("알 수 없는 에러 코드는 지원하지 않는다")
        void supports_UnknownErrorCode_ShouldReturnFalse() {
            // when
            boolean result = mapper.supports("UNKNOWN_ERROR");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트")
    class MapTest {

        @Test
        @DisplayName("SESSION_NOT_FOUND 에러를 404로 매핑한다")
        void map_SessionNotFound_ShouldReturnNotFound() {
            // given
            SessionErrorCode errorCode = SessionErrorCode.SESSION_NOT_FOUND;
            DomainException exception = createDomainException(errorCode);

            when(messageSource.getMessage(any(), any(), any(), any()))
                    .thenReturn(errorCode.getMessage());

            // when
            MappedError result = mapper.map(exception, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.type().toString()).contains("session-not-found");
        }

        @Test
        @DisplayName("SESSION_EXPIRED 에러를 410으로 매핑한다")
        void map_SessionExpired_ShouldReturnGone() {
            // given
            SessionErrorCode errorCode = SessionErrorCode.SESSION_EXPIRED;
            DomainException exception = createDomainException(errorCode);

            when(messageSource.getMessage(any(), any(), any(), any()))
                    .thenReturn(errorCode.getMessage());

            // when
            MappedError result = mapper.map(exception, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.GONE);
        }

        @Test
        @DisplayName("INVALID_SESSION_STATUS 에러를 409로 매핑한다")
        void map_InvalidSessionStatus_ShouldReturnConflict() {
            // given
            SessionErrorCode errorCode = SessionErrorCode.INVALID_SESSION_STATUS;
            DomainException exception = createDomainException(errorCode);

            when(messageSource.getMessage(any(), any(), any(), any()))
                    .thenReturn(errorCode.getMessage());

            // when
            MappedError result = mapper.map(exception, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("FILE_SIZE_EXCEEDED 에러를 400으로 매핑한다")
        void map_FileSizeExceeded_ShouldReturnBadRequest() {
            // given
            SessionErrorCode errorCode = SessionErrorCode.FILE_SIZE_EXCEEDED;
            DomainException exception = createDomainException(errorCode);

            when(messageSource.getMessage(any(), any(), any(), any()))
                    .thenReturn(errorCode.getMessage());

            // when
            MappedError result = mapper.map(exception, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("INCOMPLETE_PARTS 에러를 412로 매핑한다")
        void map_IncompleteParts_ShouldReturnPreconditionFailed() {
            // given
            SessionErrorCode errorCode = SessionErrorCode.INCOMPLETE_PARTS;
            DomainException exception = createDomainException(errorCode);

            when(messageSource.getMessage(any(), any(), any(), any()))
                    .thenReturn(errorCode.getMessage());

            // when
            MappedError result = mapper.map(exception, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.PRECONDITION_FAILED);
        }

        @Test
        @DisplayName("Type URI는 에러 코드를 소문자로 변환하여 포함한다")
        void map_TypeUri_ShouldContainNormalizedCode() {
            // given
            SessionErrorCode errorCode = SessionErrorCode.DUPLICATE_PART_NUMBER;
            DomainException exception = createDomainException(errorCode);

            when(messageSource.getMessage(any(), any(), any(), any()))
                    .thenReturn(errorCode.getMessage());

            // when
            MappedError result = mapper.map(exception, Locale.KOREAN);

            // then
            assertThat(result.type().toString())
                    .isEqualTo("https://api.fileflow.com/errors/session/duplicate-part-number");
        }

        @Test
        @DisplayName("메시지 소스에서 제목을 조회한다")
        void map_Title_ShouldBeRetrievedFromMessageSource() {
            // given
            SessionErrorCode errorCode = SessionErrorCode.SESSION_NOT_FOUND;
            DomainException exception = createDomainException(errorCode);

            when(messageSource.getMessage(
                            eq("error.session.session-not-found.title"),
                            any(),
                            eq(errorCode.getMessage()),
                            any()))
                    .thenReturn("세션을 찾을 수 없습니다");

            // when
            MappedError result = mapper.map(exception, Locale.KOREAN);

            // then
            assertThat(result.title()).isEqualTo("세션을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("메시지 소스에서 상세 메시지를 조회한다")
        void map_Detail_ShouldBeRetrievedFromMessageSource() {
            // given
            SessionErrorCode errorCode = SessionErrorCode.SESSION_NOT_FOUND;
            String exceptionMessage = "Session session-123 not found";
            DomainException exception =
                    new TestSessionException(errorCode.getCode(), exceptionMessage);

            when(messageSource.getMessage(
                            eq("error.session.session-not-found.detail"),
                            any(),
                            eq(exceptionMessage),
                            any()))
                    .thenReturn("요청한 세션을 찾을 수 없습니다.");

            when(messageSource.getMessage(
                            eq("error.session.session-not-found.title"), any(), any(), any()))
                    .thenReturn("Session Not Found");

            // when
            MappedError result = mapper.map(exception, Locale.KOREAN);

            // then
            assertThat(result.detail()).isEqualTo("요청한 세션을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("모든 SessionErrorCode 매핑 테스트")
    class AllErrorCodeMappingTest {

        @ParameterizedTest
        @EnumSource(SessionErrorCode.class)
        @DisplayName("모든 SessionErrorCode가 올바른 HTTP 상태로 매핑된다")
        void map_AllErrorCodes_ShouldReturnCorrectStatus(SessionErrorCode errorCode) {
            // given
            DomainException exception = createDomainException(errorCode);

            when(messageSource.getMessage(any(), any(), any(), any()))
                    .thenReturn(errorCode.getMessage());

            // when
            MappedError result = mapper.map(exception, Locale.KOREAN);

            // then
            assertThat(result.status().value()).isEqualTo(errorCode.getHttpStatus());
        }
    }

    private DomainException createDomainException(SessionErrorCode errorCode) {
        return new TestSessionException(errorCode.getCode(), errorCode.getMessage());
    }

    private static class TestSessionException extends DomainException {
        TestSessionException(String code, String message) {
            super(code, message);
        }
    }
}
