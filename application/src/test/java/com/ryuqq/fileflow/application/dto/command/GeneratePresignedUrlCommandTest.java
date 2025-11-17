package com.ryuqq.fileflow.application.dto.command;

import com.ryuqq.fileflow.application.fixture.GeneratePresignedUrlCommandFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GeneratePresignedUrlCommand Record 테스트
 * <p>
 * Command DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Command
 * - 패키지: ..application..dto.command..
 * - 불변 객체 (final fields)
 * </p>
 */
class GeneratePresignedUrlCommandTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * GeneratePresignedUrlCommand Record가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("GeneratePresignedUrlCommand는 Record 타입이어야 한다")
    void shouldBeRecordType() {
        // Given: Fixture로 Command 생성
        GeneratePresignedUrlCommand command = GeneratePresignedUrlCommandFixture.aCommand();

        // When & Then: Record 타입 검증
        assertThat(command).isNotNull();
        assertThat(command.getClass().isRecord()).isTrue();
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 fileName 필드를 가져야 한다")
    void shouldHaveFileNameField() {
        // Given: Fixture로 Command 생성
        String expectedFileName = "custom.jpg";
        GeneratePresignedUrlCommand command = GeneratePresignedUrlCommandFixture.withFileName(expectedFileName);

        // When & Then: fileName 필드 검증
        assertThat(command.fileName()).isEqualTo(expectedFileName);
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 fileSize 필드를 가져야 한다")
    void shouldHaveFileSizeField() {
        // Given: Fixture로 Command 생성
        Long expectedFileSize = 2048L;
        GeneratePresignedUrlCommand command = GeneratePresignedUrlCommandFixture.withFileSize(expectedFileSize);

        // When & Then: fileSize 필드 검증
        assertThat(command.fileSize()).isEqualTo(expectedFileSize);
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 mimeType 필드를 가져야 한다")
    void shouldHaveMimeTypeField() {
        // Given: Fixture로 Command 생성
        String expectedMimeType = "application/pdf";
        GeneratePresignedUrlCommand command = GeneratePresignedUrlCommandFixture.withMimeType(expectedMimeType);

        // When & Then: mimeType 필드 검증
        assertThat(command.mimeType()).isEqualTo(expectedMimeType);
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 uploaderId 필드를 가져야 한다")
    void shouldHaveUploaderIdField() {
        // Given: Fixture로 Command 생성
        Long expectedUploaderId = 99L;
        GeneratePresignedUrlCommand command = GeneratePresignedUrlCommandFixture.withUploaderId(expectedUploaderId);

        // When & Then: uploaderId 필드 검증
        assertThat(command.uploaderId()).isEqualTo(expectedUploaderId);
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 category 필드를 가져야 한다")
    void shouldHaveCategoryField() {
        // Given: Fixture로 Command 생성
        String expectedCategory = "DOCUMENT";
        GeneratePresignedUrlCommand command = GeneratePresignedUrlCommandFixture.withCategory(expectedCategory);

        // When & Then: category 필드 검증
        assertThat(command.category()).isEqualTo(expectedCategory);
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 tags 필드를 가져야 한다")
    void shouldHaveTagsField() {
        // Given: Fixture로 Command 생성
        List<String> expectedTags = List.of("custom", "tag");
        GeneratePresignedUrlCommand command = GeneratePresignedUrlCommandFixture.withTags(expectedTags);

        // When & Then: tags 필드 검증
        assertThat(command.tags()).isEqualTo(expectedTags);
    }
}
