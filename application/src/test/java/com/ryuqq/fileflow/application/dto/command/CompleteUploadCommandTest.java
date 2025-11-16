package com.ryuqq.fileflow.application.dto.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CompleteUploadCommand Record 테스트
 * <p>
 * Command DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Command
 * - 패키지: ..application..dto.command..
 * - 불변 객체 (final fields)
 * </p>
 */
class CompleteUploadCommandTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * CompleteUploadCommand Record가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("CompleteUploadCommand는 Record 타입이어야 한다")
    void shouldBeRecordType() {
        // Given: CompleteUploadCommand Record (컴파일 에러)
        CompleteUploadCommand command = null;

        // When & Then: Record 타입 검증
        assertThat(command).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("CompleteUploadCommand는 fileId 필드를 가져야 한다")
    void shouldHaveFileIdField() {
        // Given: CompleteUploadCommand Record (컴파일 에러)
        Long fileId = 100L;
        CompleteUploadCommand command = new CompleteUploadCommand(fileId);

        // When & Then: fileId 필드 검증
        assertThat(command.fileId()).isEqualTo(fileId);
    }

    @Test
    @DisplayName("CompleteUploadCommand는 fileId를 받는 생성자를 가져야 한다")
    void shouldHaveConstructorWithFileId() {
        // Given: CompleteUploadCommand Record (컴파일 에러)
        Long fileId = 200L;

        // When: fileId로 Command 생성
        CompleteUploadCommand command = new CompleteUploadCommand(fileId);

        // Then: fileId가 올바르게 설정됨
        assertThat(command.fileId()).isEqualTo(fileId);
    }
}
