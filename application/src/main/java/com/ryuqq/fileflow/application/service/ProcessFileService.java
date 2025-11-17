package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.ProcessFileCommand;
import com.ryuqq.fileflow.application.port.out.command.FileProcessingJobPersistencePort;
import com.ryuqq.fileflow.application.port.out.command.MessageOutboxPersistencePort;
import com.ryuqq.fileflow.application.port.out.query.FileQueryPort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;
import com.ryuqq.fileflow.domain.aggregate.MessageOutbox;
import com.ryuqq.fileflow.domain.vo.AggregateId;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileStatus;
import com.ryuqq.fileflow.domain.vo.JobType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;

/**
 * 파일 가공 UseCase 구현
 * <p>
 * Zero-Tolerance 규칙:
 * - 파일 가공은 COMPLETED 상태에서만 가능
 * - PENDING, UPLOADING, FAILED 상태에서는 가공 불가
 * - @Transactional 내 외부 API 호출 절대 금지
 * - Transactional Outbox Pattern 적용 (MessageOutbox)
 * </p>
 */
@Service
public class ProcessFileService {

    private final FileQueryPort fileQueryPort;
    private final FileProcessingJobPersistencePort fileProcessingJobPersistencePort;
    private final MessageOutboxPersistencePort messageOutboxPersistencePort;
    private final Clock clock;

    public ProcessFileService(
            FileQueryPort fileQueryPort,
            FileProcessingJobPersistencePort fileProcessingJobPersistencePort,
            MessageOutboxPersistencePort messageOutboxPersistencePort,
            Clock clock
    ) {
        this.fileQueryPort = Objects.requireNonNull(fileQueryPort, "fileQueryPort must not be null");
        this.fileProcessingJobPersistencePort = Objects.requireNonNull(fileProcessingJobPersistencePort, "fileProcessingJobPersistencePort must not be null");
        this.messageOutboxPersistencePort = Objects.requireNonNull(messageOutboxPersistencePort, "messageOutboxPersistencePort must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * 파일 가공 UseCase 실행
     * <p>
     * Cycle 26: 상태 검증 구현 완료
     * Cycle 27: FileProcessingJob 생성 구현 (최소 구현)
     * </p>
     * <p>
     * Zero-Tolerance Transaction 경계:
     * - FileProcessingJob 생성 + MessageOutbox 생성 (트랜잭션 안 - DB 작업)
     * </p>
     *
     * @param command 파일 가공 명령
     * @throws IllegalArgumentException 파일이 존재하지 않을 때
     * @throws IllegalStateException    파일 상태가 COMPLETED가 아닐 때
     */
    @Transactional
    public void execute(ProcessFileCommand command) {
        // 1. File 조회
        FileId fileId = FileId.of(command.fileId().toString());
        File file = fileQueryPort.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + command.fileId()));

        // 2. 상태 검증 (COMPLETED만 허용)
        validateFileStatus(file);

        // 3. FileProcessingJob 생성 (각 jobType마다)
        for (String jobTypeString : command.jobTypes()) {
            JobType jobType = JobType.valueOf(jobTypeString);
            FileProcessingJob job = FileProcessingJob.forNew(
                    fileId,
                    jobType,
                    file.getS3Key(),
                    clock
            );

            // FileProcessingJob 영속화
            fileProcessingJobPersistencePort.persist(job);
        }

        // 4. MessageOutbox 생성 (FILE_PROCESSING_REQUESTED 이벤트)
        MessageOutbox outbox = createFileProcessingRequestedOutbox(file);
        messageOutboxPersistencePort.persist(outbox);
    }

    /**
     * 파일 상태 검증
     * <p>
     * COMPLETED 상태가 아니면 IllegalStateException 발생
     * </p>
     */
    private void validateFileStatus(File file) {
        if (!FileStatus.COMPLETED.equals(file.getStatus())) {
            throw new IllegalStateException(
                    "File must be in COMPLETED status for processing. Current status: " + file.getStatus() +
                            ". Only COMPLETED files can be processed."
            );
        }
    }

    /**
     * FILE_PROCESSING_REQUESTED 이벤트 Outbox 생성
     * <p>
     * Transactional Outbox Pattern 적용
     * </p>
     */
    private MessageOutbox createFileProcessingRequestedOutbox(File file) {
        String payload = buildProcessingPayload(file);

        return MessageOutbox.forNew(
                "FILE_PROCESSING_REQUESTED",
                AggregateId.of(file.getFileIdValue()),
                payload,
                clock
        );
    }

    /**
     * FILE_PROCESSING_REQUESTED 이벤트 Payload 생성
     */
    private String buildProcessingPayload(File file) {
        return String.format(
                "{\"fileId\":\"%s\",\"fileName\":\"%s\",\"s3Key\":\"%s\",\"status\":\"%s\"}",
                file.getFileIdValue(),
                file.getFileName(),
                file.getS3Key(),
                file.getStatus()
        );
    }
}
