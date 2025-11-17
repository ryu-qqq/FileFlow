package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.CompleteUploadCommand;
import com.ryuqq.fileflow.application.fixture.CompleteUploadCommandFixture;
import com.ryuqq.fileflow.application.port.out.command.MessageOutboxPersistencePort;
import com.ryuqq.fileflow.application.port.out.external.S3ClientPort;
import com.ryuqq.fileflow.application.port.out.query.FileQueryPort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.aggregate.MessageOutbox;
import com.ryuqq.fileflow.domain.fixture.FileFixture;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileStatus;
import com.ryuqq.fileflow.domain.vo.MessageOutboxId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
    private FileQueryPort fileQueryPort;

    @Mock
    private S3ClientPort s3ClientPort;

    @Mock
    private MessageOutboxPersistencePort messageOutboxPersistencePort;

    private CompleteUploadService completeUploadService;

    @BeforeEach
    void setUp() {
        java.time.Clock clock = java.time.Clock.fixed(
                java.time.Instant.parse("2024-11-16T10:00:00Z"),
                java.time.ZoneId.systemDefault()
        );

        completeUploadService = new CompleteUploadService(
                fileQueryPort,
                s3ClientPort,
                messageOutboxPersistencePort,
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

        given(fileQueryPort.findById(any(FileId.class)))
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

        given(fileQueryPort.findById(any(FileId.class)))
                .willReturn(Optional.of(failedFile));

        // When & Then: IllegalStateException 발생 (Domain에서 검증)
        assertThatThrownBy(() -> completeUploadService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("UPLOADING");
    }

    /**
     * 🔴 RED Phase: S3 Object 존재 확인 테스트
     * <p>
     * S3 Object가 존재하지 않으면 업로드 완료 처리할 수 없습니다.
     * 트랜잭션 밖에서 S3 Object 존재 확인을 수행해야 합니다.
     * </p>
     */
    @Test
    @DisplayName("S3 Object가 존재하지 않으면 업로드 완료 처리할 수 없다")
    void shouldThrowExceptionWhenS3ObjectNotExists() {
        // Given: UPLOADING 상태 파일 (정상, 업로드 진행 중)
        CompleteUploadCommand command = CompleteUploadCommandFixture.create();
        File uploadingFile = FileFixture.aUploadingFile();

        given(fileQueryPort.findById(any(FileId.class)))
                .willReturn(Optional.of(uploadingFile));

        // Given: S3 Object가 존재하지 않음 (Mock S3ClientPort)
        given(s3ClientPort.headObject(anyString()))
                .willThrow(new RuntimeException("S3 Object not found"));

        // When & Then: RuntimeException 발생 (S3ObjectNotFoundException)
        assertThatThrownBy(() -> completeUploadService.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("S3");
    }

    /**
     * 🔴 RED Phase: MessageOutbox 생성 테스트
     * <p>
     * 업로드 완료 시 FILE_UPLOADED 이벤트를 MessageOutbox에 저장해야 합니다.
     * Transaction 경계: S3 확인 (밖) → File 상태 업데이트 + Outbox 생성 (안)
     * </p>
     */
    @Test
    @DisplayName("업로드 완료 시 FILE_UPLOADED 이벤트를 MessageOutbox에 저장해야 한다")
    void shouldCreateMessageOutboxWhenUploadCompleted() {
        // Given: UPLOADING 상태 파일 (정상)
        CompleteUploadCommand command = CompleteUploadCommandFixture.create();
        File uploadingFile = FileFixture.aUploadingFile();

        given(fileQueryPort.findById(any(FileId.class)))
                .willReturn(Optional.of(uploadingFile));

        // Given: S3 Object 존재 (정상)
        given(s3ClientPort.headObject(anyString()))
                .willReturn(new S3ClientPort.HeadObjectResponse(1024L, "image/jpeg", "2024-11-16", "etag123"));

        // Given: MessageOutbox 저장 성공
        given(messageOutboxPersistencePort.persist(any(MessageOutbox.class)))
                .willReturn(MessageOutboxId.of("outbox-123"));

        // When: 업로드 완료 실행
        completeUploadService.execute(command);

        // Then: MessageOutbox가 persist 되었는지 검증
        verify(messageOutboxPersistencePort).persist(any(MessageOutbox.class));
    }
}
