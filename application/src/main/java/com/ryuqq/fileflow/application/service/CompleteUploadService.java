package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.CompleteUploadCommand;
import com.ryuqq.fileflow.application.port.in.command.CompleteUploadPort;
import com.ryuqq.fileflow.application.port.out.query.LoadFilePort;
import com.ryuqq.fileflow.domain.aggregate.File;
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
public class CompleteUploadService implements CompleteUploadPort {

    private static final Set<FileStatus> ALLOWED_STATUSES = Set.of(
            FileStatus.PENDING,
            FileStatus.UPLOADING
    );

    private final LoadFilePort loadFilePort;
    private final Clock clock;

    public CompleteUploadService(
            LoadFilePort loadFilePort,
            Clock clock
    ) {
        this.loadFilePort = Objects.requireNonNull(loadFilePort, "loadFilePort must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * 업로드 완료 UseCase 실행
     * <p>
     * Cycle 21: 상태 검증만 구현
     * Cycle 22: S3 Object 존재 확인 추가 예정
     * Cycle 23: MessageOutbox 생성 추가 예정
     * </p>
     */
    @Override
    public void execute(CompleteUploadCommand command) {
        // 1. File 조회
        FileId fileId = FileId.of(command.fileId().toString());
        File file = loadFilePort.loadById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + command.fileId()));

        // 2. 상태 검증
        validateFileStatus(file);
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
}
