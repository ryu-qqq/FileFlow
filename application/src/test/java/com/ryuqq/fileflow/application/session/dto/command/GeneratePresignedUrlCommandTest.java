package com.ryuqq.fileflow.application.session.dto.command;

import com.ryuqq.fileflow.domain.file.vo.FileCategory;
import com.ryuqq.fileflow.domain.file.vo.FileName;
import com.ryuqq.fileflow.domain.file.vo.FileSize;
import com.ryuqq.fileflow.domain.file.vo.MimeType;
import com.ryuqq.fileflow.domain.iam.vo.UploaderType;
import com.ryuqq.fileflow.domain.session.vo.SessionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GeneratePresignedUrlCommand DTO 테스트
 * <p>
 * Presigned URL 발급 요청 Command 검증
 * </p>
 */
class GeneratePresignedUrlCommandTest {

    @Test
    @DisplayName("GeneratePresignedUrlCommand를 생성해야 한다")
    void shouldCreateCommand() {
        // when
        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
            SessionId.generate(),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            FileCategory.of("banner", UploaderType.ADMIN)
        );

        // then
        assertThat(command.sessionId()).isNotNull();
        assertThat(command.fileName().value()).isEqualTo("example.jpg");
        assertThat(command.fileSize().value()).isEqualTo(1048576L);
        assertThat(command.mimeType().value()).isEqualTo("image/jpeg");
        assertThat(command.category()).isNotNull();
    }

    @Test
    @DisplayName("category가 null이어도 생성되어야 한다")
    void shouldCreateCommandWithNullCategory() {
        // when
        GeneratePresignedUrlCommand command = new GeneratePresignedUrlCommand(
            SessionId.generate(),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            null
        );

        // then
        assertThat(command.sessionId()).isNotNull();
        assertThat(command.category()).isNull();
    }
}
