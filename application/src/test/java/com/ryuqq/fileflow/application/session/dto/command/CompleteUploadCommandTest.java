package com.ryuqq.fileflow.application.session.dto.command;

import com.ryuqq.fileflow.domain.session.vo.SessionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CompleteUploadCommand DTO 테스트
 * <p>
 * 업로드 완료 요청 Command 검증
 * </p>
 */
class CompleteUploadCommandTest {

    @Test
    @DisplayName("CompleteUploadCommand를 생성해야 한다")
    void shouldCreateCommand() {
        // given
        SessionId sessionId = SessionId.generate();

        // when
        CompleteUploadCommand command = new CompleteUploadCommand(sessionId);

        // then
        assertThat(command.sessionId()).isNotNull();
        assertThat(command.sessionId()).isEqualTo(sessionId);
    }
}
