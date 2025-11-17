package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.CompleteUploadCommand;
import com.ryuqq.fileflow.application.fixture.CompleteUploadCommandFixture;
import com.ryuqq.fileflow.application.port.out.query.LoadFilePort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.fixture.FileFixture;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * CompleteUploadService 테스트
 * <p>
 * Application Layer Service 테스트 규칙:
 * - Mock Port 사용 (Outbound Port)
 * - TestFixture 필수 사용 (Command, Domain)
 * - Transaction 경계 검증 (@Transactional 내 외부 API 호출 금지)
 * - CQRS 준수 검증 (Command UseCase)
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CompleteUploadServiceTest {

    @Mock
    private LoadFilePort loadFilePort;

    private CompleteUploadService completeUploadService;

    @BeforeEach
    void setUp() {
        java.time.Clock clock = java.time.Clock.fixed(
                java.time.Instant.parse("2024-11-16T10:00:00Z"),
                java.time.ZoneId.systemDefault()
        );

        completeUploadService = new CompleteUploadService(
                loadFilePort,
                clock
        );
    }

    /**
     * 🔴 RED Phase: 잘못된 상태 검증 테스트
     * <p>
     * PENDING, UPLOADING 상태가 아닌 파일은 업로드 완료 처리할 수 없습니다.
     * COMPLETED, FAILED 상태에서 InvalidFileStatusException 발생해야 합니다.
     * </p>
     */
    @Test
    @DisplayName("COMPLETED 상태 파일은 업로드 완료 처리할 수 없다")
    void shouldThrowExceptionWhenAlreadyCompleted() {
        // Given: COMPLETED 상태 파일
        CompleteUploadCommand command = CompleteUploadCommandFixture.create();
        File completedFile = FileFixture.aCompletedFile();

        given(loadFilePort.loadById(any(FileId.class)))
                .willReturn(Optional.of(completedFile));

        // When & Then: IllegalStateException 발생 (Domain에서 검증)
        assertThatThrownBy(() -> completeUploadService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("UPLOADING");
    }

    /**
     * 🔴 RED Phase: FAILED 상태 검증 테스트
     * <p>
     * FAILED 상태 파일도 업로드 완료 처리할 수 없습니다.
     * </p>
     */
    @Test
    @DisplayName("FAILED 상태 파일은 업로드 완료 처리할 수 없다")
    void shouldThrowExceptionWhenFailed() {
        // Given: FAILED 상태 파일
        CompleteUploadCommand command = CompleteUploadCommandFixture.create();
        File failedFile = FileFixture.aFailedFile();

        given(loadFilePort.loadById(any(FileId.class)))
                .willReturn(Optional.of(failedFile));

        // When & Then: IllegalStateException 발생 (Domain에서 검증)
        assertThatThrownBy(() -> completeUploadService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("UPLOADING");
    }
}
