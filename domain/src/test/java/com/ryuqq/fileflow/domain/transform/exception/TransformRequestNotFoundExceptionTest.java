package com.ryuqq.fileflow.domain.transform.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("TransformRequestNotFoundException")
class TransformRequestNotFoundExceptionTest {

    @Test
    @DisplayName("transformRequestId를 포함한 메시지와 args를 갖는다")
    void shouldContainTransformRequestIdInMessageAndArgs() {
        var ex = new TransformRequestNotFoundException("tr-123");

        assertThat(ex.code()).isEqualTo("TRANSFORM-001");
        assertThat(ex.httpStatus()).isEqualTo(404);
        assertThat(ex.getMessage()).contains("tr-123");
        assertThat(ex.args()).containsEntry("transformRequestId", "tr-123");
    }

    @Test
    @DisplayName("null transformRequestId를 전달해도 안전하게 생성된다")
    void shouldHandleNullTransformRequestId() {
        var ex = new TransformRequestNotFoundException(null);

        assertThat(ex.args()).containsEntry("transformRequestId", "null");
    }
}
