package com.ryuqq.fileflow.application.dto.command;

import com.ryuqq.fileflow.application.fixture.CompleteUploadCommandFixture;
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
        // Given: Fixture로 Command 생성
        CompleteUploadCommand command = CompleteUploadCommandFixture.aCommand();

        // When & Then: Record 타입 검증
        assertThat(command).isNotNull();
        assertThat(command.getClass().isRecord()).isTrue();
    }

    @Test
    @DisplayName("CompleteUploadCommand는 fileId 필드를 가져야 한다")
    void shouldHaveFileIdField() {
        // Given: Fixture로 커스텀 fileId Command 생성
        Long expectedFileId = 100L;
        CompleteUploadCommand command = CompleteUploadCommandFixture.withFileId(expectedFileId);

        // When & Then: fileId 필드 검증
        assertThat(command.fileId()).isEqualTo(expectedFileId);
    }

    @Test
    @DisplayName("CompleteUploadCommand는 fileId를 받는 생성자를 가져야 한다")
    void shouldHaveConstructorWithFileId() {
        // Given: Fixture로 커스텀 fileId Command 생성
        Long expectedFileId = 200L;

        // When: fileId로 Command 생성
        CompleteUploadCommand command = CompleteUploadCommandFixture.withFileId(expectedFileId);

        // Then: fileId가 올바르게 설정됨
        assertThat(command.fileId()).isEqualTo(expectedFileId);
    }
}
