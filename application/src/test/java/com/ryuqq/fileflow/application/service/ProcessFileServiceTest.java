package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.ProcessFileCommand;
import com.ryuqq.fileflow.application.fixture.ProcessFileCommandFixture;
import com.ryuqq.fileflow.application.port.out.command.FileProcessingJobPersistencePort;
import com.ryuqq.fileflow.application.port.out.command.MessageOutboxPersistencePort;
import com.ryuqq.fileflow.application.port.out.query.FileQueryPort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;
import com.ryuqq.fileflow.domain.aggregate.MessageOutbox;
import com.ryuqq.fileflow.domain.fixture.FileFixture;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileProcessingJobId;
import com.ryuqq.fileflow.domain.vo.MessageOutboxId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * ProcessFileService 테스트
 * <p>
 * Application Layer Service 테스트 규칙:
 * - Mock Port 사용 (Outbound Port)
 * - TestFixture 필수 사용 (Command, Domain)
 * - Transaction 경계 검증 (@Transactional 내 외부 API 호출 금지)
 * - CQRS 준수 검증 (Command UseCase)
 * </p>
 * <p>
 * ProcessFileService Zero-Tolerance 규칙:
 * - 파일 가공은 COMPLETED 상태에서만 가능
 * - PENDING, UPLOADING, FAILED 상태에서는 가공 불가
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ProcessFileServiceTest {

    @Mock
    private FileQueryPort fileQueryPort;

    @Mock
    private FileProcessingJobPersistencePort fileProcessingJobPersistencePort;

    @Mock
    private MessageOutboxPersistencePort messageOutboxPersistencePort;

    private ProcessFileService processFileService;

    @BeforeEach
    void setUp() {
        java.time.Clock clock = java.time.Clock.fixed(
                java.time.Instant.parse("2024-11-16T10:00:00Z"),
                java.time.ZoneId.systemDefault()
        );

        processFileService = new ProcessFileService(
                fileQueryPort,
                fileProcessingJobPersistencePort,
                messageOutboxPersistencePort,
                clock
        );
    }

    /**
     * 🔴 RED Phase: PENDING 상태 검증 테스트
     * <p>
     * PENDING 상태 파일은 아직 업로드되지 않았으므로 가공할 수 없습니다.
     * IllegalStateException 발생해야 합니다.
     * </p>
     */
    @Test
    @DisplayName("PENDING 상태 파일은 가공할 수 없다")
    void shouldThrowExceptionWhenFileNotCompleted_Pending() {
        // Given: PENDING 상태 파일
        ProcessFileCommand command = ProcessFileCommandFixture.create();
        File pendingFile = FileFixture.aPendingFile();

        given(fileQueryPort.findById(any(FileId.class)))
                .willReturn(Optional.of(pendingFile));

        // When & Then: IllegalStateException 발생
        assertThatThrownBy(() -> processFileService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMPLETED");
    }

    /**
     * 🔴 RED Phase: UPLOADING 상태 검증 테스트
     * <p>
     * UPLOADING 상태 파일은 업로드 진행 중이므로 가공할 수 없습니다.
     * </p>
     */
    @Test
    @DisplayName("UPLOADING 상태 파일은 가공할 수 없다")
    void shouldThrowExceptionWhenFileNotCompleted_Uploading() {
        // Given: UPLOADING 상태 파일
        ProcessFileCommand command = ProcessFileCommandFixture.create();
        File uploadingFile = FileFixture.aUploadingFile();

        given(fileQueryPort.findById(any(FileId.class)))
                .willReturn(Optional.of(uploadingFile));

        // When & Then: IllegalStateException 발생
        assertThatThrownBy(() -> processFileService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMPLETED");
    }

    /**
     * 🔴 RED Phase: FAILED 상태 검증 테스트
     * <p>
     * FAILED 상태 파일은 업로드 실패했으므로 가공할 수 없습니다.
     * </p>
     */
    @Test
    @DisplayName("FAILED 상태 파일은 가공할 수 없다")
    void shouldThrowExceptionWhenFileNotCompleted_Failed() {
        // Given: FAILED 상태 파일
        ProcessFileCommand command = ProcessFileCommandFixture.create();
        File failedFile = FileFixture.aFailedFile();

        given(fileQueryPort.findById(any(FileId.class)))
                .willReturn(Optional.of(failedFile));

        // When & Then: IllegalStateException 발생
        assertThatThrownBy(() -> processFileService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMPLETED");
    }

    /**
     * 🔴 RED Phase: 파일 존재 확인 테스트
     * <p>
     * 파일이 존재하지 않으면 가공할 수 없습니다.
     * RuntimeException 또는 FileNotFoundException 발생해야 합니다.
     * </p>
     */
    @Test
    @DisplayName("파일이 존재하지 않으면 가공할 수 없다")
    void shouldThrowExceptionWhenFileNotFound() {
        // Given: 파일이 존재하지 않음
        ProcessFileCommand command = ProcessFileCommandFixture.create();

        given(fileQueryPort.findById(any(FileId.class)))
                .willReturn(Optional.empty());

        // When & Then: RuntimeException 발생
        assertThatThrownBy(() -> processFileService.execute(command))
                .isInstanceOf(RuntimeException.class);
    }

    /**
     * 🔴 RED Phase (Cycle 27): FileProcessingJob 생성 테스트
     * <p>
     * COMPLETED 상태 파일에 대해 FileProcessingJob을 생성해야 합니다.
     * 각 jobType마다 FileProcessingJob을 생성하고, MessageOutbox도 생성해야 합니다.
     * </p>
     */
    @Test
    @DisplayName("COMPLETED 상태 파일에 대해 FileProcessingJob을 생성해야 한다")
    void shouldCreateFileProcessingJobs() {
        // Given: COMPLETED 상태 파일
        ProcessFileCommand command = ProcessFileCommandFixture.withJobTypes(
                List.of("THUMBNAIL_GENERATION", "IMAGE_COMPRESSION")
        );
        File completedFile = FileFixture.aCompletedFile();

        given(fileQueryPort.findById(any(FileId.class)))
                .willReturn(Optional.of(completedFile));

        // Given: FileProcessingJob 저장 성공
        given(fileProcessingJobPersistencePort.persist(any(FileProcessingJob.class)))
                .willReturn(FileProcessingJobId.forNew());

        // Given: MessageOutbox 저장 성공
        given(messageOutboxPersistencePort.persist(any(MessageOutbox.class)))
                .willReturn(MessageOutboxId.of("outbox-123"));

        // When: 파일 가공 실행
        processFileService.execute(command);

        // Then: FileProcessingJob이 2개 생성되었는지 검증 (THUMBNAIL_GENERATION, IMAGE_COMPRESSION)
        verify(fileProcessingJobPersistencePort, times(2)).persist(any(FileProcessingJob.class));

        // Then: MessageOutbox가 생성되었는지 검증 (FILE_PROCESSING_REQUESTED 이벤트)
        verify(messageOutboxPersistencePort).persist(any(MessageOutbox.class));
    }
}
