package com.ryuqq.fileflow.adapter.in.rest.common.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/**
 * ErrorMapperRegistry 단위 테스트
 *
 * <p>도메인 예외를 HTTP 응답으로 매핑하는 레지스트리의 동작을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ErrorMapperRegistry 테스트")
class ErrorMapperRegistryTest {

    @Mock
    private ErrorMapper mapper1;

    @Mock
    private ErrorMapper mapper2;

    @Nested
    @DisplayName("map - 도메인 예외 매핑")
    class Map {

        @Test
        @DisplayName("지원하는 매퍼가 있으면 매핑 결과 반환")
        void shouldReturnMappedErrorWhenMapperSupports() {
            // given
            DomainException exception = new TestDomainException(TestErrorCode.TEST_ERROR, "테스트 에러");
            ErrorMapper.MappedError expectedMapping =
                    new ErrorMapper.MappedError(
                            HttpStatus.NOT_FOUND,
                            "Not Found",
                            "테스트 리소스를 찾을 수 없습니다",
                            URI.create("about:blank"));

            when(mapper1.supports("TEST_ERROR")).thenReturn(true);
            when(mapper1.map(any(DomainException.class), any(Locale.class)))
                    .thenReturn(expectedMapping);

            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1, mapper2));

            // when
            Optional<ErrorMapper.MappedError> result = registry.map(exception, Locale.KOREA);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.get().title()).isEqualTo("Not Found");
            assertThat(result.get().detail()).isEqualTo("테스트 리소스를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("지원하는 매퍼가 없으면 빈 Optional 반환")
        void shouldReturnEmptyWhenNoMapperSupports() {
            // given
            DomainException exception = new TestDomainException(TestErrorCode.UNKNOWN_ERROR, "알 수 없는 에러");

            when(mapper1.supports("UNKNOWN_ERROR")).thenReturn(false);
            when(mapper2.supports("UNKNOWN_ERROR")).thenReturn(false);

            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1, mapper2));

            // when
            Optional<ErrorMapper.MappedError> result = registry.map(exception, Locale.KOREA);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("첫 번째로 지원하는 매퍼 사용")
        void shouldUseFirstSupportingMapper() {
            // given
            DomainException exception = new TestDomainException(TestErrorCode.SHARED_ERROR, "공유 에러");
            ErrorMapper.MappedError mapping1 =
                    new ErrorMapper.MappedError(
                            HttpStatus.BAD_REQUEST,
                            "Bad Request",
                            "첫 번째 매퍼 결과",
                            URI.create("about:blank"));
            ErrorMapper.MappedError mapping2 =
                    new ErrorMapper.MappedError(
                            HttpStatus.CONFLICT,
                            "Conflict",
                            "두 번째 매퍼 결과",
                            URI.create("about:blank"));

            when(mapper1.supports("SHARED_ERROR")).thenReturn(true);
            when(mapper1.map(any(DomainException.class), any(Locale.class)))
                    .thenReturn(mapping1);
            // mapper2는 첫 번째 매퍼가 지원하므로 호출되지 않음

            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1, mapper2));

            // when
            Optional<ErrorMapper.MappedError> result = registry.map(exception, Locale.KOREA);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().detail()).isEqualTo("첫 번째 매퍼 결과");
        }

        @Test
        @DisplayName("빈 매퍼 리스트인 경우 빈 Optional 반환")
        void shouldReturnEmptyWhenNoMappers() {
            // given
            DomainException exception = new TestDomainException(TestErrorCode.TEST_ERROR, "테스트 에러");
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of());

            // when
            Optional<ErrorMapper.MappedError> result = registry.map(exception, Locale.KOREA);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("로케일에 따른 매핑")
        void shouldPassLocaleToMapper() {
            // given
            DomainException exception = new TestDomainException(TestErrorCode.TEST_ERROR, "테스트 에러");
            ErrorMapper.MappedError koreanMapping =
                    new ErrorMapper.MappedError(
                            HttpStatus.NOT_FOUND,
                            "찾을 수 없음",
                            "리소스를 찾을 수 없습니다",
                            URI.create("about:blank"));

            when(mapper1.supports("TEST_ERROR")).thenReturn(true);
            when(mapper1.map(any(DomainException.class), any(Locale.class)))
                    .thenReturn(koreanMapping);

            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1));

            // when
            Optional<ErrorMapper.MappedError> result = registry.map(exception, Locale.KOREA);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().title()).isEqualTo("찾을 수 없음");
        }
    }

    @Nested
    @DisplayName("defaultMapping - 기본 매핑")
    class DefaultMapping {

        @Test
        @DisplayName("기본 매핑은 400 Bad Request 반환")
        void shouldReturn400BadRequest() {
            // given
            DomainException exception = new TestDomainException(TestErrorCode.TEST_ERROR, "테스트 에러");
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of());

            // when
            ErrorMapper.MappedError result = registry.defaultMapping(exception);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.title()).isEqualTo("Bad Request");
            assertThat(result.type()).isEqualTo(URI.create("about:blank"));
        }

        @Test
        @DisplayName("예외 메시지가 있으면 detail에 포함")
        void shouldIncludeExceptionMessage() {
            // given
            DomainException exception = new TestDomainException(TestErrorCode.TEST_ERROR, "커스텀 에러 메시지");
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of());

            // when
            ErrorMapper.MappedError result = registry.defaultMapping(exception);

            // then
            assertThat(result.detail()).isEqualTo("커스텀 에러 메시지");
        }

        @Test
        @DisplayName("예외 메시지가 null이면 기본 메시지 반환")
        void shouldReturnDefaultMessageWhenNull() {
            // given
            DomainException exception = new TestDomainException(TestErrorCode.NULL_MESSAGE);
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of());

            // when
            ErrorMapper.MappedError result = registry.defaultMapping(exception);

            // then
            assertThat(result.detail()).isEqualTo("Invalid request");
        }
    }

    @Nested
    @DisplayName("실제 시나리오")
    class RealScenarios {

        @Test
        @DisplayName("세션 관련 에러 매핑")
        void shouldMapSessionError() {
            // given
            DomainException exception =
                    new TestDomainException(TestErrorCode.SESSION_EXPIRED, "세션이 만료되었습니다");
            ErrorMapper.MappedError sessionMapping =
                    new ErrorMapper.MappedError(
                            HttpStatus.GONE,
                            "Gone",
                            "업로드 세션이 만료되었습니다",
                            URI.create("about:blank"));

            when(mapper1.supports("SESSION_EXPIRED")).thenReturn(true);
            when(mapper1.map(any(DomainException.class), any(Locale.class)))
                    .thenReturn(sessionMapping);

            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1));

            // when
            Optional<ErrorMapper.MappedError> result = registry.map(exception, Locale.KOREA);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().status()).isEqualTo(HttpStatus.GONE);
        }

        @Test
        @DisplayName("자산 관련 에러 매핑")
        void shouldMapAssetError() {
            // given
            DomainException exception =
                    new TestDomainException(TestErrorCode.ASSET_NOT_FOUND, "자산을 찾을 수 없습니다");
            ErrorMapper.MappedError assetMapping =
                    new ErrorMapper.MappedError(
                            HttpStatus.NOT_FOUND,
                            "Not Found",
                            "파일 자산을 찾을 수 없습니다",
                            URI.create("about:blank"));

            when(mapper1.supports("ASSET_NOT_FOUND")).thenReturn(true);
            when(mapper1.map(any(DomainException.class), any(Locale.class)))
                    .thenReturn(assetMapping);

            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1));

            // when
            Optional<ErrorMapper.MappedError> result = registry.map(exception, Locale.KOREA);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().status()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 테스트용 ErrorCode 구현
     */
    private enum TestErrorCode implements com.ryuqq.fileflow.domain.common.exception.ErrorCode {
        TEST_ERROR("TEST_ERROR", 400, "테스트 에러"),
        UNKNOWN_ERROR("UNKNOWN_ERROR", 400, "알 수 없는 에러"),
        SHARED_ERROR("SHARED_ERROR", 400, "공유 에러"),
        SESSION_EXPIRED("SESSION_EXPIRED", 410, "세션이 만료되었습니다"),
        ASSET_NOT_FOUND("ASSET_NOT_FOUND", 404, "자산을 찾을 수 없습니다"),
        NULL_MESSAGE("NULL_MESSAGE", 400, null);

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
