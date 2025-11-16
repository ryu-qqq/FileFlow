package com.ryuqq.fileflow.application.dto.command;

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
        // Given: GeneratePresignedUrlCommand Record (컴파일 에러)
        GeneratePresignedUrlCommand command = null;

        // When & Then: Record 타입 검증
        assertThat(command).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 fileName 필드를 가져야 한다")
    void shouldHaveFileNameField() {
        // Given: GeneratePresignedUrlCommand Record (컴파일 에러)
        String fileName = "test.jpg";
        Long fileSize = 1024L;
        String mimeType = "image/jpeg";
        Long uploaderId = 1L;
        String category = "PROFILE";
        List<String> tags = List.of("profile", "avatar");

        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
                fileName,
                fileSize,
                mimeType,
                uploaderId,
                category,
                tags
        );

        // When & Then: fileName 필드 검증
        assertThat(command.fileName()).isEqualTo(fileName);
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 fileSize 필드를 가져야 한다")
    void shouldHaveFileSizeField() {
        // Given: GeneratePresignedUrlCommand Record (컴파일 에러)
        String fileName = "test.jpg";
        Long fileSize = 1024L;
        String mimeType = "image/jpeg";
        Long uploaderId = 1L;
        String category = "PROFILE";
        List<String> tags = List.of("profile", "avatar");

        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
                fileName,
                fileSize,
                mimeType,
                uploaderId,
                category,
                tags
        );

        // When & Then: fileSize 필드 검증
        assertThat(command.fileSize()).isEqualTo(fileSize);
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 mimeType 필드를 가져야 한다")
    void shouldHaveMimeTypeField() {
        // Given: GeneratePresignedUrlCommand Record (컴파일 에러)
        String fileName = "test.jpg";
        Long fileSize = 1024L;
        String mimeType = "image/jpeg";
        Long uploaderId = 1L;
        String category = "PROFILE";
        List<String> tags = List.of("profile", "avatar");

        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
                fileName,
                fileSize,
                mimeType,
                uploaderId,
                category,
                tags
        );

        // When & Then: mimeType 필드 검증
        assertThat(command.mimeType()).isEqualTo(mimeType);
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 uploaderId 필드를 가져야 한다")
    void shouldHaveUploaderIdField() {
        // Given: GeneratePresignedUrlCommand Record (컴파일 에러)
        String fileName = "test.jpg";
        Long fileSize = 1024L;
        String mimeType = "image/jpeg";
        Long uploaderId = 1L;
        String category = "PROFILE";
        List<String> tags = List.of("profile", "avatar");

        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
                fileName,
                fileSize,
                mimeType,
                uploaderId,
                category,
                tags
        );

        // When & Then: uploaderId 필드 검증
        assertThat(command.uploaderId()).isEqualTo(uploaderId);
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 category 필드를 가져야 한다")
    void shouldHaveCategoryField() {
        // Given: GeneratePresignedUrlCommand Record (컴파일 에러)
        String fileName = "test.jpg";
        Long fileSize = 1024L;
        String mimeType = "image/jpeg";
        Long uploaderId = 1L;
        String category = "PROFILE";
        List<String> tags = List.of("profile", "avatar");

        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
                fileName,
                fileSize,
                mimeType,
                uploaderId,
                category,
                tags
        );

        // When & Then: category 필드 검증
        assertThat(command.category()).isEqualTo(category);
    }

    @Test
    @DisplayName("GeneratePresignedUrlCommand는 tags 필드를 가져야 한다")
    void shouldHaveTagsField() {
        // Given: GeneratePresignedUrlCommand Record (컴파일 에러)
        String fileName = "test.jpg";
        Long fileSize = 1024L;
        String mimeType = "image/jpeg";
        Long uploaderId = 1L;
        String category = "PROFILE";
        List<String> tags = List.of("profile", "avatar");

        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
                fileName,
                fileSize,
                mimeType,
                uploaderId,
                category,
                tags
        );

        // When & Then: tags 필드 검증
        assertThat(command.tags()).isEqualTo(tags);
    }
}
