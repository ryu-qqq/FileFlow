package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.CompleteUploadCommand;
import com.ryuqq.fileflow.application.port.in.command.CompleteUploadUseCase;
import com.ryuqq.fileflow.application.port.out.command.MessageOutboxPersistencePort;
import com.ryuqq.fileflow.application.port.out.external.S3ClientPort;
import com.ryuqq.fileflow.application.port.out.query.FileQueryPort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.aggregate.MessageOutbox;
import com.ryuqq.fileflow.domain.vo.AggregateId;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Objects;
import java.util.Set;

/**
 * 업로드 완료 UseCase 구현
 * <p>
 * Zero-Tolerance 규칙:
 * - @Transactional 내 외부 API 호출 절대 금지
 * - Transaction 경계: File 조회/상태 업데이트만 트랜잭션 내부
 * - S3 API 호출: 트랜잭션 외부에서 실행 (Cycle 22에서 구현)
 * </p>
 */
@Service
public class CompleteUploadService implements CompleteUploadUseCase {

    private static final Set<FileStatus> ALLOWED_STATUSES = Set.of(
            FileStatus.PENDING,
            FileStatus.UPLOADING
    );

    private final FileQueryPort fileQueryPort;
    private final S3ClientPort s3ClientPort;
    private final MessageOutboxPersistencePort messageOutboxPersistencePort;
    private final Clock clock;

    public CompleteUploadService(
            FileQueryPort fileQueryPort,
            S3ClientPort s3ClientPort,
            MessageOutboxPersistencePort messageOutboxPersistencePort,
            Clock clock
    ) {
        this.fileQueryPort = Objects.requireNonNull(fileQueryPort, "fileQueryPort must not be null");
        this.s3ClientPort = Objects.requireNonNull(s3ClientPort, "s3ClientPort must not be null");
        this.messageOutboxPersistencePort = Objects.requireNonNull(messageOutboxPersistencePort, "messageOutboxPersistencePort must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * 업로드 완료 UseCase 실행
     * <p>
     * Cycle 21: 상태 검증 구현 완료
     * Cycle 22: S3 Object 존재 확인 구현 완료
     * Cycle 23: MessageOutbox 생성 구현 중
     * </p>
     * <p>
     * Zero-Tolerance Transaction 경계:
     * - S3 Object 존재 확인 (트랜잭션 밖 - 외부 API)
     * - File 상태 업데이트 + MessageOutbox 생성 (트랜잭션 안 - DB 작업)
     * </p>
     */
    @Override
    public void execute(CompleteUploadCommand command) {
        // 1. File 조회
        FileId fileId = FileId.of(command.fileId().toString());
        File file = fileQueryPort.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + command.fileId()));

        // 2. 상태 검증
        validateFileStatus(file);

        // 3. S3 Object 존재 확인 (트랜잭션 밖 - 외부 API 호출)
        verifyS3ObjectExists(file);

        // 4. File 상태 업데이트 (COMPLETED)
        file.markAsCompleted();

        // 5. MessageOutbox 생성 (FILE_UPLOADED 이벤트)
        createFileUploadedOutbox(file);
    }

    /**
     * 파일 상태 검증
     * <p>
     * PENDING, UPLOADING 상태만 업로드 완료 가능합니다.
     * COMPLETED, FAILED 상태는 불가능합니다.
     * </p>
     */
    private void validateFileStatus(File file) {
        FileStatus currentStatus = file.getStatus();

        if (!ALLOWED_STATUSES.contains(currentStatus)) {
            throw new IllegalStateException(
                    "업로드 완료 처리할 수 없는 상태입니다. " +
                            "허용 상태: PENDING, UPLOADING / " +
                            "현재 상태: " + currentStatus
            );
        }
    }

    /**
     * S3 Object 존재 확인
     * <p>
     * Zero-Tolerance 규칙:
     * - 외부 API 호출이므로 @Transactional 메서드 밖에서 호출
     * - Timeout: 10초 (S3ClientPort 정의)
     * - Retry: 3회 (S3ClientPort 정의)
     * </p>
     */
    private void verifyS3ObjectExists(File file) {
        String s3Key = file.getS3Key();

        try {
            s3ClientPort.headObject(s3Key);
        } catch (Exception e) {
            throw new RuntimeException("S3 Object not found: " + s3Key, e);
        }
    }

    /**
     * FILE_UPLOADED 이벤트를 MessageOutbox에 저장
     * <p>
     * Transactional Outbox Pattern:
     * - File 상태 업데이트와 동일한 트랜잭션에서 실행
     * - 이벤트 발행 보장 (DB 트랜잭션 성공 시 이벤트 발행)
     * </p>
     */
    private void createFileUploadedOutbox(File file) {
        // FILE_UPLOADED 이벤트 페이로드 생성 (JSON)
        String payload = String.format(
                "{\"fileId\":\"%s\",\"fileName\":\"%s\",\"s3Key\":\"%s\",\"fileSize\":%d,\"mimeType\":\"%s\"}",
                file.getFileIdValue(),
                file.getFileName(),
                file.getS3Key(),
                file.getFileSize(),
                file.getMimeType()
        );

        // MessageOutbox 생성 (Factory Method)
        MessageOutbox outbox = MessageOutbox.forNew(
                "FILE_UPLOADED",
                AggregateId.of(file.getFileIdValue()),
                payload,
                clock
        );

        // MessageOutbox 영속화
        messageOutboxPersistencePort.persist(outbox);
    }
}
