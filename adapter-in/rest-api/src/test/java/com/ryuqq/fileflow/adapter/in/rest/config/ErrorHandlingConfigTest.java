package com.ryuqq.fileflow.adapter.in.rest.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.common.error.ErrorMapperRegistry;
import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * ErrorHandlingConfig 단위 테스트.
 *
 * <p>ErrorMapper 빈들이 ErrorMapperRegistry에 올바르게 등록되는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ErrorHandlingConfig 단위 테스트")
class ErrorHandlingConfigTest {

    private final ErrorHandlingConfig config = new ErrorHandlingConfig();

    @Nested
    @DisplayName("errorMapperRegistry 빈 생성 테스트")
    class ErrorMapperRegistryBeanTest {

        @Test
        @DisplayName("ErrorMapper 목록으로 ErrorMapperRegistry를 생성할 수 있다")
        void createRegistry_WithMapperList_ShouldSucceed() {
            // given
            ErrorMapper testMapper = new TestErrorMapper();
            List<ErrorMapper> mappers = List.of(testMapper);

            // when
            ErrorMapperRegistry registry = config.errorMapperRegistry(mappers);

            // then
            assertThat(registry).isNotNull();
        }

        @Test
        @DisplayName("빈 ErrorMapper 목록으로도 ErrorMapperRegistry를 생성할 수 있다")
        void createRegistry_WithEmptyList_ShouldSucceed() {
            // given
            List<ErrorMapper> emptyMappers = List.of();

            // when
            ErrorMapperRegistry registry = config.errorMapperRegistry(emptyMappers);

            // then
            assertThat(registry).isNotNull();
        }

        @Test
        @DisplayName("여러 ErrorMapper로 ErrorMapperRegistry를 생성할 수 있다")
        void createRegistry_WithMultipleMappers_ShouldSucceed() {
            // given
            ErrorMapper mapper1 = new TestErrorMapper();
            ErrorMapper mapper2 = new AnotherTestErrorMapper();
            List<ErrorMapper> mappers = List.of(mapper1, mapper2);

            // when
            ErrorMapperRegistry registry = config.errorMapperRegistry(mappers);

            // then
            assertThat(registry).isNotNull();
        }
    }

    /** 테스트용 ErrorMapper 구현체. */
    private static class TestErrorMapper implements ErrorMapper {
        @Override
        public boolean supports(String code) {
            return code.startsWith("TEST_");
        }

        @Override
        public MappedError map(
                com.ryuqq.fileflow.domain.common.exception.DomainException ex, Locale locale) {
            return new MappedError(
                    HttpStatus.BAD_REQUEST,
                    "Test Error",
                    ex.getMessage(),
                    URI.create("about:blank"));
        }
    }

    /** 테스트용 또 다른 ErrorMapper 구현체. */
    private static class AnotherTestErrorMapper implements ErrorMapper {
        @Override
        public boolean supports(String code) {
            return code.startsWith("ANOTHER_");
        }

        @Override
        public MappedError map(
                com.ryuqq.fileflow.domain.common.exception.DomainException ex, Locale locale) {
            return new MappedError(
                    HttpStatus.NOT_FOUND,
                    "Another Error",
                    ex.getMessage(),
                    URI.create("about:blank"));
        }
    }
}
