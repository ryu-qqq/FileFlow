package com.ryuqq.fileflow.adapter.in.rest.common.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper.MappedError;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("ErrorMapperRegistry 단위 테스트")
class ErrorMapperRegistryTest {

    @Nested
    @DisplayName("map 메서드 테스트")
    class MapTest {

        @Test
        @DisplayName("지원하는 에러 코드를 가진 매퍼가 있으면 매핑된 에러를 반환한다")
        void map_WithSupportingMapper_ShouldReturnMappedError() {
            // given
            ErrorMapper mapper1 = mock(ErrorMapper.class);
            ErrorMapper mapper2 = mock(ErrorMapper.class);

            DomainException exception = new TestDomainException("SESSION-001", "테스트 에러");
            MappedError expectedError =
                    new MappedError(
                            HttpStatus.NOT_FOUND,
                            "Session Not Found",
                            "세션을 찾을 수 없습니다",
                            URI.create("https://api.test.com/errors/session-001"));

            when(mapper1.supports("SESSION-001")).thenReturn(false);
            when(mapper2.supports("SESSION-001")).thenReturn(true);
            when(mapper2.map(exception, Locale.KOREAN)).thenReturn(expectedError);

            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1, mapper2));

            // when
            Optional<MappedError> result = registry.map(exception, Locale.KOREAN);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.get().title()).isEqualTo("Session Not Found");
            assertThat(result.get().detail()).isEqualTo("세션을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("지원하는 매퍼가 없으면 빈 Optional을 반환한다")
        void map_WithNoSupportingMapper_ShouldReturnEmpty() {
            // given
            ErrorMapper mapper = mock(ErrorMapper.class);
            when(mapper.supports("UNKNOWN-001")).thenReturn(false);

            DomainException exception = new TestDomainException("UNKNOWN-001", "알 수 없는 에러");
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper));

            // when
            Optional<MappedError> result = registry.map(exception, Locale.KOREAN);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 매퍼 목록이면 빈 Optional을 반환한다")
        void map_WithEmptyMapperList_ShouldReturnEmpty() {
            // given
            DomainException exception = new TestDomainException("ANY-001", "에러");
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of());

            // when
            Optional<MappedError> result = registry.map(exception, Locale.KOREAN);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("첫 번째 지원하는 매퍼가 사용된다")
        void map_WithMultipleSupportingMappers_ShouldUseFirst() {
            // given
            ErrorMapper mapper1 = mock(ErrorMapper.class);
            ErrorMapper mapper2 = mock(ErrorMapper.class);

            DomainException exception = new TestDomainException("ERROR-001", "에러");
            MappedError error1 =
                    new MappedError(
                            HttpStatus.BAD_REQUEST,
                            "Error 1",
                            "첫 번째 매퍼",
                            URI.create("about:blank"));
            MappedError error2 =
                    new MappedError(
                            HttpStatus.NOT_FOUND, "Error 2", "두 번째 매퍼", URI.create("about:blank"));

            when(mapper1.supports("ERROR-001")).thenReturn(true);
            when(mapper2.supports("ERROR-001")).thenReturn(true);
            when(mapper1.map(exception, Locale.KOREAN)).thenReturn(error1);
            when(mapper2.map(exception, Locale.KOREAN)).thenReturn(error2);

            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper1, mapper2));

            // when
            Optional<MappedError> result = registry.map(exception, Locale.KOREAN);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().detail()).isEqualTo("첫 번째 매퍼");
        }
    }

    @Nested
    @DisplayName("defaultMapping 메서드 테스트")
    class DefaultMappingTest {

        @Test
        @DisplayName("기본 매핑은 BAD_REQUEST 상태를 반환한다")
        void defaultMapping_ShouldReturnBadRequest() {
            // given
            DomainException exception = new TestDomainException("ANY-ERROR", "에러 메시지");
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of());

            // when
            MappedError result = registry.defaultMapping(exception);

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.title()).isEqualTo("Bad Request");
            assertThat(result.detail()).isEqualTo("에러 메시지");
            assertThat(result.type()).isEqualTo(URI.create("about:blank"));
        }

        @Test
        @DisplayName("예외 메시지가 null이면 기본 메시지를 사용한다")
        void defaultMapping_WithNullMessage_ShouldUseDefaultMessage() {
            // given
            DomainException exception = new TestDomainException("ANY-ERROR", null);
            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of());

            // when
            MappedError result = registry.defaultMapping(exception);

            // then
            assertThat(result.detail()).isEqualTo("Invalid request");
        }
    }

    @Nested
    @DisplayName("Locale 테스트")
    class LocaleTest {

        @Test
        @DisplayName("영어 로케일로 매핑할 수 있다")
        void map_WithEnglishLocale_ShouldWork() {
            // given
            ErrorMapper mapper = mock(ErrorMapper.class);
            DomainException exception = new TestDomainException("ERROR-001", "error");
            MappedError englishError =
                    new MappedError(
                            HttpStatus.BAD_REQUEST,
                            "Error",
                            "English message",
                            URI.create("about:blank"));

            when(mapper.supports("ERROR-001")).thenReturn(true);
            when(mapper.map(exception, Locale.ENGLISH)).thenReturn(englishError);

            ErrorMapperRegistry registry = new ErrorMapperRegistry(List.of(mapper));

            // when
            Optional<MappedError> result = registry.map(exception, Locale.ENGLISH);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().detail()).isEqualTo("English message");
        }
    }

    private static class TestDomainException extends DomainException {
        TestDomainException(String code, String message) {
            super(code, message);
        }
    }
}
