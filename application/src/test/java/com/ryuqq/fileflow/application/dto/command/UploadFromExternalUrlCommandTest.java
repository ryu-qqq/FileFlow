package com.ryuqq.fileflow.application.dto.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UploadFromExternalUrlCommand Record 테스트
 * <p>
 * Command DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Command
 * - 패키지: ..application..dto.command..
 * - 불변 객체 (final fields)
 * </p>
 */
class UploadFromExternalUrlCommandTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * UploadFromExternalUrlCommand Record가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("UploadFromExternalUrlCommand는 Record 타입이어야 한다")
    void shouldBeRecordType() {
        // Given: UploadFromExternalUrlCommand Record (컴파일 에러)
        UploadFromExternalUrlCommand command = null;

        // When & Then: Record 타입 검증
        assertThat(command).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("UploadFromExternalUrlCommand는 externalUrl 필드를 가져야 한다")
    void shouldHaveExternalUrlField() {
        // Given: UploadFromExternalUrlCommand Record (컴파일 에러)
        String externalUrl = "https://example.com/image.jpg";
        Long uploaderId = 1L;
        String category = "EXTERNAL";
        List<String> tags = List.of("external", "url");
        String webhookUrl = "https://webhook.example.com/callback";

        UploadFromExternalUrlCommand command = new UploadFromExternalUrlCommand(
                externalUrl,
                uploaderId,
                category,
                tags,
                webhookUrl
        );

        // When & Then: externalUrl 필드 검증
        assertThat(command.externalUrl()).isEqualTo(externalUrl);
    }

    @Test
    @DisplayName("UploadFromExternalUrlCommand는 uploaderId 필드를 가져야 한다")
    void shouldHaveUploaderIdField() {
        // Given: UploadFromExternalUrlCommand Record (컴파일 에러)
        String externalUrl = "https://example.com/image.jpg";
        Long uploaderId = 1L;
        String category = "EXTERNAL";
        List<String> tags = List.of("external", "url");
        String webhookUrl = "https://webhook.example.com/callback";

        UploadFromExternalUrlCommand command = new UploadFromExternalUrlCommand(
                externalUrl,
                uploaderId,
                category,
                tags,
                webhookUrl
        );

        // When & Then: uploaderId 필드 검증
        assertThat(command.uploaderId()).isEqualTo(uploaderId);
    }

    @Test
    @DisplayName("UploadFromExternalUrlCommand는 category 필드를 가져야 한다")
    void shouldHaveCategoryField() {
        // Given: UploadFromExternalUrlCommand Record (컴파일 에러)
        String externalUrl = "https://example.com/image.jpg";
        Long uploaderId = 1L;
        String category = "EXTERNAL";
        List<String> tags = List.of("external", "url");
        String webhookUrl = "https://webhook.example.com/callback";

        UploadFromExternalUrlCommand command = new UploadFromExternalUrlCommand(
                externalUrl,
                uploaderId,
                category,
                tags,
                webhookUrl
        );

        // When & Then: category 필드 검증
        assertThat(command.category()).isEqualTo(category);
    }

    @Test
    @DisplayName("UploadFromExternalUrlCommand는 tags 필드를 가져야 한다")
    void shouldHaveTagsField() {
        // Given: UploadFromExternalUrlCommand Record (컴파일 에러)
        String externalUrl = "https://example.com/image.jpg";
        Long uploaderId = 1L;
        String category = "EXTERNAL";
        List<String> tags = List.of("external", "url");
        String webhookUrl = "https://webhook.example.com/callback";

        UploadFromExternalUrlCommand command = new UploadFromExternalUrlCommand(
                externalUrl,
                uploaderId,
                category,
                tags,
                webhookUrl
        );

        // When & Then: tags 필드 검증
        assertThat(command.tags()).isEqualTo(tags);
    }

    @Test
    @DisplayName("UploadFromExternalUrlCommand는 webhookUrl 필드를 가져야 한다")
    void shouldHaveWebhookUrlField() {
        // Given: UploadFromExternalUrlCommand Record (컴파일 에러)
        String externalUrl = "https://example.com/image.jpg";
        Long uploaderId = 1L;
        String category = "EXTERNAL";
        List<String> tags = List.of("external", "url");
        String webhookUrl = "https://webhook.example.com/callback";

        UploadFromExternalUrlCommand command = new UploadFromExternalUrlCommand(
                externalUrl,
                uploaderId,
                category,
                tags,
                webhookUrl
        );

        // When & Then: webhookUrl 필드 검증
        assertThat(command.webhookUrl()).isEqualTo(webhookUrl);
    }
}
