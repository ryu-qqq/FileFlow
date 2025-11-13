package com.ryuqq.fileflow.domain.pipeline;

import com.ryuqq.fileflow.domain.pipeline.fixture.PipelineResultFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PipelineResult 테스트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Tag("unit")
@Tag("domain")
class PipelineResultTest {

    @Test
    @DisplayName("성공 결과를 생성할 수 있다")
    void createSuccess() {
        // When
        PipelineResult result = PipelineResult.success();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    @DisplayName("실패 결과를 생성할 수 있다 (에러 메시지)")
    void createFailureWithMessage() {
        // Given
        String errorMessage = "Test error message";

        // When
        PipelineResult result = PipelineResult.failure(errorMessage);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.errorMessage()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("실패 결과를 생성할 수 있다 (예외)")
    void createFailureWithException() {
        // Given
        Exception exception = new RuntimeException("Test exception");

        // When
        PipelineResult result = PipelineResult.failure(exception);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.errorMessage()).isEqualTo("Test exception");
    }

    @Test
    @DisplayName("Fixture를 사용하여 성공 결과를 생성할 수 있다")
    void createSuccessWithFixture() {
        // When
        PipelineResult result = PipelineResultFixture.success();

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();
    }

    @Test
    @DisplayName("Fixture를 사용하여 실패 결과를 생성할 수 있다")
    void createFailureWithFixture() {
        // When
        PipelineResult result = PipelineResultFixture.failure();

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.errorMessage()).isNotNull();
    }
}

