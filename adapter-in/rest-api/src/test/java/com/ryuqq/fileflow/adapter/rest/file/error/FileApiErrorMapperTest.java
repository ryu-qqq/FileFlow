package com.ryuqq.fileflow.adapter.rest.file.error;

import com.ryuqq.fileflow.adapter.rest.config.properties.ApiErrorProperties;
import com.ryuqq.fileflow.domain.common.DomainException;
import com.ryuqq.fileflow.domain.file.asset.exception.FileAssetAccessDeniedException;
import com.ryuqq.fileflow.domain.file.asset.exception.FileAssetAlreadyDeletedException;
import com.ryuqq.fileflow.domain.file.asset.exception.FileAssetNotFoundException;
import com.ryuqq.fileflow.domain.file.asset.exception.FileAssetProcessingException;
import com.ryuqq.fileflow.domain.file.asset.exception.InvalidFileAssetStateException;
import com.ryuqq.fileflow.domain.file.fixture.FileIdFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileApiErrorMapper 테스트
 *
 * <p>File 도메인 예외를 HTTP 응답으로 매핑하는 로직을 테스트합니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DisplayName("FileApiErrorMapper 테스트")
class FileApiErrorMapperTest {

    private FileApiErrorMapper errorMapper;
    private MessageSource messageSource;
    private ApiErrorProperties errorProperties;

    @BeforeEach
    void setUp() {
        messageSource = createMessageSource();
        errorProperties = createErrorProperties();
        errorMapper = new FileApiErrorMapper(messageSource, errorProperties);
    }

    private MessageSource createMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    private ApiErrorProperties createErrorProperties() {
        ApiErrorProperties properties = new ApiErrorProperties();
        properties.setBaseUrl("https://api.example.com/problems");
        properties.setUseAboutBlank(false);
        return properties;
    }

    @Nested
    @DisplayName("supports 메서드 테스트")
    class SupportsTests {

        @Test
        @DisplayName("supports_WithFileErrorCode_ShouldReturnTrue - FILE-로 시작하는 코드는 지원")
        void supports_WithFileErrorCode_ShouldReturnTrue() {
            // When & Then
            assertThat(errorMapper.supports("FILE-001")).isTrue();
            assertThat(errorMapper.supports("FILE-002")).isTrue();
            assertThat(errorMapper.supports("FILE-101")).isTrue();
            assertThat(errorMapper.supports("FILE-301")).isTrue();
        }

        @Test
        @DisplayName("supports_WithNonFileErrorCode_ShouldReturnFalse - FILE-로 시작하지 않으면 미지원")
        void supports_WithNonFileErrorCode_ShouldReturnFalse() {
            // When & Then
            assertThat(errorMapper.supports("UPLOAD-001")).isFalse();
            assertThat(errorMapper.supports("DOWNLOAD-001")).isFalse();
            assertThat(errorMapper.supports("INVALID")).isFalse();
            assertThat(errorMapper.supports(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("map 메서드 테스트 - FileAsset 관련 에러")
    class MapFileAssetErrorsTests {

        @Test
        @DisplayName("map_FileAssetNotFoundException_ShouldReturn404 - FILE-001 → 404")
        void map_FileAssetNotFoundException_ShouldReturn404() {
            // Given
            FileAssetNotFoundException exception = new FileAssetNotFoundException(
                FileIdFixture.create(123L)
            );

            // When
            ErrorMapper.MappedError mapped = errorMapper.map(exception, Locale.ENGLISH);

            // Then
            assertThat(mapped.status()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(mapped.title()).isEqualTo("Not Found");
            assertThat(mapped.detail()).contains("File not found");
            assertThat(mapped.type().toString()).contains("file-not-found");
        }

        @Test
        @DisplayName("map_FileAssetAlreadyDeletedException_ShouldReturn410 - FILE-002 → 410")
        void map_FileAssetAlreadyDeletedException_ShouldReturn410() {
            // Given
            FileAssetAlreadyDeletedException exception = new FileAssetAlreadyDeletedException(
                FileIdFixture.create(456L)
            );

            // When
            ErrorMapper.MappedError mapped = errorMapper.map(exception, Locale.ENGLISH);

            // Then
            assertThat(mapped.status()).isEqualTo(HttpStatus.GONE);
            assertThat(mapped.title()).isEqualTo("Gone");
            assertThat(mapped.detail()).contains("File already deleted");
            assertThat(mapped.type().toString()).contains("file-already-deleted");
        }

        @Test
        @DisplayName("map_FileAssetAccessDeniedException_ShouldReturn403 - FILE-003 → 403")
        void map_FileAssetAccessDeniedException_ShouldReturn403() {
            // Given
            FileAssetAccessDeniedException exception = new FileAssetAccessDeniedException(
                FileIdFixture.create(789L),
                999L
            );

            // When
            ErrorMapper.MappedError mapped = errorMapper.map(exception, Locale.ENGLISH);

            // Then
            assertThat(mapped.status()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(mapped.title()).isEqualTo("Forbidden");
            assertThat(mapped.detail()).contains("Access denied");
            assertThat(mapped.type().toString()).contains("file-access-denied");
        }

        @Test
        @DisplayName("map_InvalidFileAssetStateException_ShouldReturn409 - FILE-004 → 409")
        void map_InvalidFileAssetStateException_ShouldReturn409() {
            // Given
            InvalidFileAssetStateException exception = new InvalidFileAssetStateException(
                FileIdFixture.create(100L),
                "PROCESSING",
                "AVAILABLE"
            );

            // When
            ErrorMapper.MappedError mapped = errorMapper.map(exception, Locale.ENGLISH);

            // Then
            assertThat(mapped.status()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(mapped.title()).isEqualTo("Conflict");
            assertThat(mapped.detail()).contains("Invalid file state");
            assertThat(mapped.type().toString()).contains("file-invalid-state");
        }

        @Test
        @DisplayName("map_FileAssetProcessingException_ShouldReturn425 - FILE-005 → 425")
        void map_FileAssetProcessingException_ShouldReturn425() {
            // Given
            FileAssetProcessingException exception = new FileAssetProcessingException(
                FileIdFixture.create(200L)
            );

            // When
            ErrorMapper.MappedError mapped = errorMapper.map(exception, Locale.ENGLISH);

            // Then
            assertThat(mapped.status()).isEqualTo(HttpStatus.TOO_EARLY);
            assertThat(mapped.title()).isEqualTo("Too Early");
            assertThat(mapped.detail()).contains("File is still processing");
            assertThat(mapped.type().toString()).contains("file-processing");
        }
    }

    @Nested
    @DisplayName("국제화 메시지 테스트")
    class InternationalizationTests {

        @Test
        @DisplayName("map_WithKoreanLocale_ShouldReturnKoreanMessage - 한국어 로케일")
        void map_WithKoreanLocale_ShouldReturnKoreanMessage() {
            // Given
            FileAssetNotFoundException exception = new FileAssetNotFoundException(
                FileIdFixture.create(123L)
            );

            // When
            ErrorMapper.MappedError mapped = errorMapper.map(exception, Locale.KOREAN);

            // Then
            assertThat(mapped.detail()).contains("파일을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("map_WithEnglishLocale_ShouldReturnEnglishMessage - 영어 로케일")
        void map_WithEnglishLocale_ShouldReturnEnglishMessage() {
            // Given
            FileAssetNotFoundException exception = new FileAssetNotFoundException(
                FileIdFixture.create(123L)
            );

            // When
            ErrorMapper.MappedError mapped = errorMapper.map(exception, Locale.ENGLISH);

            // Then
            assertThat(mapped.detail()).contains("File not found");
        }
    }

    @Nested
    @DisplayName("기본 매핑 테스트")
    class DefaultMappingTests {

        @Test
        @DisplayName("map_WithUnknownErrorCode_ShouldReturnDefaultMapping - 알 수 없는 에러 코드는 기본 매핑")
        void map_WithUnknownErrorCode_ShouldReturnDefaultMapping() {
            // Given
            DomainException exception = new DomainException("FILE-999", "Unknown error");

            // When
            ErrorMapper.MappedError mapped = errorMapper.map(exception, Locale.ENGLISH);

            // Then
            assertThat(mapped.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(mapped.title()).isEqualTo("Bad Request");
            assertThat(mapped.detail()).contains("Unknown error");
        }
    }
}

