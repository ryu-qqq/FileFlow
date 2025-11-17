package com.ryuqq.fileflow.application.dto.command;

import com.ryuqq.fileflow.application.fixture.ProcessFileCommandFixture;
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
        // Given: Fixture로 Command 생성
        ProcessFileCommand command = ProcessFileCommandFixture.aCommand();

        // When & Then: Record 타입 검증
        assertThat(command).isNotNull();
        assertThat(command.getClass().isRecord()).isTrue();
    }

    @Test
    @DisplayName("ProcessFileCommand는 fileId 필드를 가져야 한다")
    void shouldHaveFileIdField() {
        // Given: Fixture로 커스텀 fileId Command 생성
        Long expectedFileId = 100L;
        ProcessFileCommand command = ProcessFileCommandFixture.withFileId(expectedFileId);

        // When & Then: fileId 필드 검증
        assertThat(command.fileId()).isEqualTo(expectedFileId);
    }

    @Test
    @DisplayName("ProcessFileCommand는 jobTypes 필드를 가져야 한다")
    void shouldHaveJobTypesField() {
        // Given: Fixture로 커스텀 jobTypes Command 생성
        List<String> expectedJobTypes = List.of("THUMBNAIL");
        ProcessFileCommand command = ProcessFileCommandFixture.withJobTypes(expectedJobTypes);

        // When & Then: jobTypes 필드 검증
        assertThat(command.jobTypes()).isEqualTo(expectedJobTypes);
    }

    @Test
    @DisplayName("ProcessFileCommand는 fileId와 jobTypes를 받는 생성자를 가져야 한다")
    void shouldHaveConstructorWithFileIdAndJobTypes() {
        // Given: Fixture로 썸네일 전용 Command 생성
        ProcessFileCommand command = ProcessFileCommandFixture.thumbnailOnly();

        // When & Then: 필드가 올바르게 설정됨
        assertThat(command.fileId()).isNotNull();
        assertThat(command.jobTypes()).containsExactly("THUMBNAIL");
    }
}
