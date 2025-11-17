package com.ryuqq.fileflow.application.dto.command;

import com.ryuqq.fileflow.application.fixture.UploadFromExternalUrlCommandFixture;
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
        // Given: Fixture로 Command 생성
        UploadFromExternalUrlCommand command = UploadFromExternalUrlCommandFixture.aCommand();

        // When & Then: Record 타입 검증
        assertThat(command).isNotNull();
        assertThat(command.getClass().isRecord()).isTrue();
    }

    @Test
    @DisplayName("UploadFromExternalUrlCommand는 externalUrl 필드를 가져야 한다")
    void shouldHaveExternalUrlField() {
        // Given: Fixture로 커스텀 externalUrl Command 생성
        String expectedExternalUrl = "https://custom.com/file.jpg";
        UploadFromExternalUrlCommand command = UploadFromExternalUrlCommandFixture.withExternalUrl(expectedExternalUrl);

        // When & Then: externalUrl 필드 검증
        assertThat(command.externalUrl()).isEqualTo(expectedExternalUrl);
    }

    @Test
    @DisplayName("UploadFromExternalUrlCommand는 uploaderId 필드를 가져야 한다")
    void shouldHaveUploaderIdField() {
        // Given: Fixture로 커스텀 uploaderId Command 생성
        Long expectedUploaderId = 100L;
        UploadFromExternalUrlCommand command = UploadFromExternalUrlCommandFixture.withUploaderId(expectedUploaderId);

        // When & Then: uploaderId 필드 검증
        assertThat(command.uploaderId()).isEqualTo(expectedUploaderId);
    }

    @Test
    @DisplayName("UploadFromExternalUrlCommand는 category 필드를 가져야 한다")
    void shouldHaveCategoryField() {
        // Given: Fixture로 커스텀 category Command 생성
        String expectedCategory = "IMAGE";
        UploadFromExternalUrlCommand command = UploadFromExternalUrlCommandFixture.withCategory(expectedCategory);

        // When & Then: category 필드 검증
        assertThat(command.category()).isEqualTo(expectedCategory);
    }

    @Test
    @DisplayName("UploadFromExternalUrlCommand는 tags 필드를 가져야 한다")
    void shouldHaveTagsField() {
        // Given: Fixture로 커스텀 tags Command 생성
        List<String> expectedTags = List.of("custom", "tags");
        UploadFromExternalUrlCommand command = UploadFromExternalUrlCommandFixture.withTags(expectedTags);

        // When & Then: tags 필드 검증
        assertThat(command.tags()).isEqualTo(expectedTags);
    }

    @Test
    @DisplayName("UploadFromExternalUrlCommand는 webhookUrl 필드를 가져야 한다")
    void shouldHaveWebhookUrlField() {
        // Given: Fixture로 커스텀 webhookUrl Command 생성
        String expectedWebhookUrl = "https://custom.webhook.com/callback";
        UploadFromExternalUrlCommand command = UploadFromExternalUrlCommandFixture.withWebhookUrl(expectedWebhookUrl);

        // When & Then: webhookUrl 필드 검증
        assertThat(command.webhookUrl()).isEqualTo(expectedWebhookUrl);
    }
}
