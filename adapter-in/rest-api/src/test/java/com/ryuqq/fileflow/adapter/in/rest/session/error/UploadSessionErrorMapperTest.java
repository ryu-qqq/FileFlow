package com.ryuqq.fileflow.adapter.in.rest.session.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper.MappedError;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.session.exception.*;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.net.URI;
import java.time.Instant;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

/**
 * UploadSessionErrorMapper 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UploadSessionErrorMapper 테스트")
class UploadSessionErrorMapperTest {

    @Mock private MessageSource messageSource;

    private UploadSessionErrorMapper errorMapper;

    @BeforeEach
    void setUp() {
        errorMapper = new UploadSessionErrorMapper(messageSource);
    }

    @Nested
    @DisplayName("supports 메서드 테스트")
    class SupportsTests {

        @Test
        @DisplayName("FILE-SIZE-EXCEEDED 코드를 지원해야 한다")
        void supports_fileSizeExceeded() {
            assertThat(errorMapper.supports("FILE-SIZE-EXCEEDED")).isTrue();
        }

        @Test
        @DisplayName("UNSUPPORTED-FILE-TYPE 코드를 지원해야 한다")
        void supports_unsupportedFileType() {
            assertThat(errorMapper.supports("UNSUPPORTED-FILE-TYPE")).isTrue();
        }

        @Test
        @DisplayName("INVALID-SESSION-STATUS 코드를 지원해야 한다")
        void supports_invalidSessionStatus() {
            assertThat(errorMapper.supports("INVALID-SESSION-STATUS")).isTrue();
        }

        @Test
        @DisplayName("SESSION-EXPIRED 코드를 지원해야 한다")
        void supports_sessionExpired() {
            assertThat(errorMapper.supports("SESSION-EXPIRED")).isTrue();
        }

        @Test
        @DisplayName("DUPLICATE-PART-NUMBER 코드를 지원해야 한다")
        void supports_duplicatePartNumber() {
            assertThat(errorMapper.supports("DUPLICATE-PART-NUMBER")).isTrue();
        }

        @Test
        @DisplayName("INVALID-PART-NUMBER 코드를 지원해야 한다")
        void supports_invalidPartNumber() {
            assertThat(errorMapper.supports("INVALID-PART-NUMBER")).isTrue();
        }

        @Test
        @DisplayName("INCOMPLETE-PARTS 코드를 지원해야 한다")
        void supports_incompleteParts() {
            assertThat(errorMapper.supports("INCOMPLETE-PARTS")).isTrue();
        }

        @Test
        @DisplayName("SESSION-NOT-FOUND 코드를 지원해야 한다")
        void supports_sessionNotFound() {
            assertThat(errorMapper.supports("SESSION-NOT-FOUND")).isTrue();
        }

        @Test
        @DisplayName("PART-NOT-FOUND 코드를 지원해야 한다")
        void supports_partNotFound() {
            assertThat(errorMapper.supports("PART-NOT-FOUND")).isTrue();
        }

        @Test
        @DisplayName("ETAG-MISMATCH 코드를 지원해야 한다")
        void supports_etagMismatch() {
            assertThat(errorMapper.supports("ETAG-MISMATCH")).isTrue();
        }

        @Test
        @DisplayName("지원하지 않는 코드는 false를 반환해야 한다")
        void supports_unsupportedCode_returnsFalse() {
            assertThat(errorMapper.supports("UNKNOWN-ERROR")).isFalse();
            assertThat(errorMapper.supports("ASSET-NOT-FOUND")).isFalse();
            assertThat(errorMapper.supports("")).isFalse();
        }

        @Test
        @DisplayName("모든 SessionErrorCode를 지원해야 한다")
        void supports_allSessionErrorCodes() {
            for (SessionErrorCode code : SessionErrorCode.values()) {
                assertThat(errorMapper.supports(code.getCode()))
                        .as("SessionErrorCode %s should be supported", code.name())
                        .isTrue();
            }
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - FILE_SIZE_EXCEEDED")
    class MapFileSizeExceededTests {

        @Test
        @DisplayName("FILE_SIZE_EXCEEDED 예외를 400 Bad Request로 매핑해야 한다")
        void map_fileSizeExceeded_returns400() {
            // given
            DomainException ex = new FileSizeExceededException(10_000_000L, 5_000_000L);
            setupMessageSourceMock("file-size-exceeded");

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.type())
                    .isEqualTo(URI.create("https://api.fileflow.com/errors/session/file-size-exceeded"));
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - UNSUPPORTED_FILE_TYPE")
    class MapUnsupportedFileTypeTests {

        @Test
        @DisplayName("UNSUPPORTED_FILE_TYPE 예외를 400 Bad Request로 매핑해야 한다")
        void map_unsupportedFileType_returns400() {
            // given
            DomainException ex = new UnsupportedFileTypeException("application/x-executable");
            setupMessageSourceMock("unsupported-file-type");

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create("https://api.fileflow.com/errors/session/unsupported-file-type"));
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - INVALID_SESSION_STATUS")
    class MapInvalidSessionStatusTests {

        @Test
        @DisplayName("INVALID_SESSION_STATUS 예외를 409 Conflict로 매핑해야 한다")
        void map_invalidSessionStatus_returns409() {
            // given
            DomainException ex =
                    new InvalidSessionStatusException(SessionStatus.PREPARING, SessionStatus.COMPLETED);
            setupMessageSourceMock("invalid-session-status");

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create(
                                    "https://api.fileflow.com/errors/session/invalid-session-status"));
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - SESSION_EXPIRED")
    class MapSessionExpiredTests {

        @Test
        @DisplayName("SESSION_EXPIRED 예외를 410 Gone으로 매핑해야 한다")
        void map_sessionExpired_returns410() {
            // given
            DomainException ex = new SessionExpiredException(Instant.now().minusSeconds(3600));
            setupMessageSourceMock("session-expired");

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.GONE);
            assertThat(result.type())
                    .isEqualTo(URI.create("https://api.fileflow.com/errors/session/session-expired"));
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - DUPLICATE_PART_NUMBER")
    class MapDuplicatePartNumberTests {

        @Test
        @DisplayName("DUPLICATE_PART_NUMBER 예외를 409 Conflict로 매핑해야 한다")
        void map_duplicatePartNumber_returns409() {
            // given
            DomainException ex = new DuplicatePartNumberException(5);
            setupMessageSourceMock("duplicate-part-number");

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create(
                                    "https://api.fileflow.com/errors/session/duplicate-part-number"));
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - INVALID_PART_NUMBER")
    class MapInvalidPartNumberTests {

        @Test
        @DisplayName("INVALID_PART_NUMBER 예외를 400 Bad Request로 매핑해야 한다")
        void map_invalidPartNumber_returns400() {
            // given
            DomainException ex = new InvalidPartNumberException(0, 10);
            setupMessageSourceMock("invalid-part-number");

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create("https://api.fileflow.com/errors/session/invalid-part-number"));
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - INCOMPLETE_PARTS")
    class MapIncompletePartsTests {

        @Test
        @DisplayName("INCOMPLETE_PARTS 예외를 412 Precondition Failed로 매핑해야 한다")
        void map_incompleteParts_returns412() {
            // given
            DomainException ex = new IncompletePartsException(5, 10);
            setupMessageSourceMock("incomplete-parts");

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.PRECONDITION_FAILED);
            assertThat(result.type())
                    .isEqualTo(URI.create("https://api.fileflow.com/errors/session/incomplete-parts"));
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - SESSION_NOT_FOUND")
    class MapSessionNotFoundTests {

        @Test
        @DisplayName("SESSION_NOT_FOUND 예외를 404 Not Found로 매핑해야 한다")
        void map_sessionNotFound_returns404() {
            // given
            DomainException ex = new SessionNotFoundException("session-123");
            setupMessageSourceMock("session-not-found");

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.type())
                    .isEqualTo(
                            URI.create("https://api.fileflow.com/errors/session/session-not-found"));
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - ETAG_MISMATCH")
    class MapEtagMismatchTests {

        @Test
        @DisplayName("ETAG_MISMATCH 예외를 409 Conflict로 매핑해야 한다")
        void map_etagMismatch_returns409() {
            // given
            DomainException ex = new ETagMismatchException("expected-etag", "actual-etag");
            setupMessageSourceMock("etag-mismatch");

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(result.type())
                    .isEqualTo(URI.create("https://api.fileflow.com/errors/session/etag-mismatch"));
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - 메시지 해석")
    class MapMessageResolutionTests {

        @Test
        @DisplayName("MessageSource에서 제목을 해석해야 한다")
        void map_resolvesTitle_fromMessageSource() {
            // given
            DomainException ex = new SessionNotFoundException("session-123");
            String expectedTitle = "세션을 찾을 수 없음";
            given(messageSource.getMessage(
                            eq("error.session.session-not-found.title"),
                            isNull(),
                            anyString(),
                            eq(Locale.KOREAN)))
                    .willReturn(expectedTitle);
            given(messageSource.getMessage(
                            eq("error.session.session-not-found.detail"),
                            isNull(),
                            anyString(),
                            eq(Locale.KOREAN)))
                    .willReturn(ex.getMessage());

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.title()).isEqualTo(expectedTitle);
        }

        @Test
        @DisplayName("MessageSource에서 상세 메시지를 해석해야 한다")
        void map_resolvesDetail_fromMessageSource() {
            // given
            DomainException ex = new SessionNotFoundException("session-123");
            String expectedDetail = "요청하신 세션을 찾을 수 없습니다";
            given(messageSource.getMessage(
                            eq("error.session.session-not-found.title"),
                            isNull(),
                            anyString(),
                            eq(Locale.KOREAN)))
                    .willReturn("세션 없음");
            given(messageSource.getMessage(
                            eq("error.session.session-not-found.detail"),
                            isNull(),
                            anyString(),
                            eq(Locale.KOREAN)))
                    .willReturn(expectedDetail);

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.detail()).isEqualTo(expectedDetail);
        }

        @Test
        @DisplayName("MessageSource에 메시지가 없으면 기본값을 사용해야 한다")
        void map_usesDefaultMessage_whenMessageSourceHasNoValue() {
            // given
            DomainException ex = new SessionNotFoundException("session-123");
            String titleKey = "error.session.session-not-found.title";
            String detailKey = "error.session.session-not-found.detail";

            // MessageSource가 기본값을 그대로 반환하도록 설정
            given(messageSource.getMessage(eq(titleKey), isNull(), anyString(), any(Locale.class)))
                    .willAnswer(inv -> inv.getArgument(2)); // 기본값 반환
            given(messageSource.getMessage(eq(detailKey), isNull(), anyString(), any(Locale.class)))
                    .willAnswer(inv -> inv.getArgument(2)); // 기본값 반환

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.title()).isEqualTo(SessionErrorCode.SESSION_NOT_FOUND.getMessage());
            assertThat(result.detail()).isEqualTo(ex.getMessage());
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - Type URI 생성")
    class MapTypeUriTests {

        @Test
        @DisplayName("Type URI가 올바른 형식으로 생성되어야 한다")
        void map_generatesCorrectTypeUri() {
            // given
            DomainException ex = new FileSizeExceededException(10_000_000L, 5_000_000L);
            setupMessageSourceMock("file-size-exceeded");

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.type().toString())
                    .startsWith("https://api.fileflow.com/errors/session/")
                    .endsWith("file-size-exceeded");
        }

        @Test
        @DisplayName("에러 코드가 소문자로 정규화되어야 한다")
        void map_normalizesCodeToLowerCase() {
            // given
            DomainException ex =
                    new InvalidSessionStatusException(SessionStatus.PREPARING, SessionStatus.COMPLETED);
            setupMessageSourceMock("invalid-session-status");

            // when
            MappedError result = errorMapper.map(ex, Locale.KOREAN);

            // then
            assertThat(result.type().toString()).doesNotContain("INVALID").contains("invalid");
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - 로케일 처리")
    class MapLocaleTests {

        @Test
        @DisplayName("영어 로케일로 메시지를 해석해야 한다")
        void map_withEnglishLocale() {
            // given
            DomainException ex = new SessionNotFoundException("session-123");
            String englishTitle = "Session Not Found";
            String englishDetail = "The requested session could not be found";

            given(messageSource.getMessage(
                            eq("error.session.session-not-found.title"),
                            isNull(),
                            anyString(),
                            eq(Locale.ENGLISH)))
                    .willReturn(englishTitle);
            given(messageSource.getMessage(
                            eq("error.session.session-not-found.detail"),
                            isNull(),
                            anyString(),
                            eq(Locale.ENGLISH)))
                    .willReturn(englishDetail);

            // when
            MappedError result = errorMapper.map(ex, Locale.ENGLISH);

            // then
            assertThat(result.title()).isEqualTo(englishTitle);
            assertThat(result.detail()).isEqualTo(englishDetail);
        }
    }

    /**
     * MessageSource Mock 설정 헬퍼 메서드
     *
     * @param normalizedCode 정규화된 에러 코드 (소문자)
     */
    private void setupMessageSourceMock(String normalizedCode) {
        String titleKey = "error.session." + normalizedCode + ".title";
        String detailKey = "error.session." + normalizedCode + ".detail";

        given(messageSource.getMessage(eq(titleKey), isNull(), anyString(), any(Locale.class)))
                .willAnswer(inv -> inv.getArgument(2));
        given(messageSource.getMessage(eq(detailKey), isNull(), anyString(), any(Locale.class)))
                .willAnswer(inv -> inv.getArgument(2));
    }
}
