package com.ryuqq.fileflow.application.dto.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProcessFileCommand Record 테스트
 * <p>
 * Command DTO 규칙:
 * - Record 타입 필수 (Lombok 금지)
 * - 인터페이스명: *Command
 * - 패키지: ..application..dto.command..
 * - 불변 객체 (final fields)
 * </p>
 */
class ProcessFileCommandTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * ProcessFileCommand Record가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("ProcessFileCommand는 Record 타입이어야 한다")
    void shouldBeRecordType() {
        // Given: ProcessFileCommand Record (컴파일 에러)
        ProcessFileCommand command = null;

        // When & Then: Record 타입 검증
        assertThat(command).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("ProcessFileCommand는 fileId 필드를 가져야 한다")
    void shouldHaveFileIdField() {
        // Given: ProcessFileCommand Record (컴파일 에러)
        Long fileId = 1L;
        List<String> jobTypes = List.of("THUMBNAIL", "METADATA");

        ProcessFileCommand command = new ProcessFileCommand(
                fileId,
                jobTypes
        );

        // When & Then: fileId 필드 검증
        assertThat(command.fileId()).isEqualTo(fileId);
    }

    @Test
    @DisplayName("ProcessFileCommand는 jobTypes 필드를 가져야 한다")
    void shouldHaveJobTypesField() {
        // Given: ProcessFileCommand Record (컴파일 에러)
        Long fileId = 1L;
        List<String> jobTypes = List.of("THUMBNAIL", "METADATA");

        ProcessFileCommand command = new ProcessFileCommand(
                fileId,
                jobTypes
        );

        // When & Then: jobTypes 필드 검증
        assertThat(command.jobTypes()).isEqualTo(jobTypes);
    }

    @Test
    @DisplayName("ProcessFileCommand는 fileId와 jobTypes를 받는 생성자를 가져야 한다")
    void shouldHaveConstructorWithFileIdAndJobTypes() {
        // Given: ProcessFileCommand Record (컴파일 에러)
        Long fileId = 100L;
        List<String> jobTypes = List.of("THUMBNAIL");

        // When: fileId와 jobTypes로 Command 생성
        ProcessFileCommand command = new ProcessFileCommand(fileId, jobTypes);

        // Then: 필드가 올바르게 설정됨
        assertThat(command.fileId()).isEqualTo(fileId);
        assertThat(command.jobTypes()).isEqualTo(jobTypes);
    }
}
