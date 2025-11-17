package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.ProcessFileCommand;
import com.ryuqq.fileflow.application.port.out.query.LoadFilePort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 파일 가공 UseCase 구현
 * <p>
 * Zero-Tolerance 규칙:
 * - 파일 가공은 COMPLETED 상태에서만 가능
 * - PENDING, UPLOADING, FAILED 상태에서는 가공 불가
 * - @Transactional 내 외부 API 호출 절대 금지
 * </p>
 */
@Service
public class ProcessFileService {

    private final LoadFilePort loadFilePort;

    public ProcessFileService(LoadFilePort loadFilePort) {
        this.loadFilePort = Objects.requireNonNull(loadFilePort, "loadFilePort must not be null");
    }

    /**
     * 파일 가공 UseCase 실행
     * <p>
     * Cycle 26: 상태 검증 구현 (최소 구현)
     * </p>
     *
     * @param command 파일 가공 명령
     * @throws IllegalArgumentException 파일이 존재하지 않을 때
     * @throws IllegalStateException    파일 상태가 COMPLETED가 아닐 때
     */
    public void execute(ProcessFileCommand command) {
        // 1. File 조회
        FileId fileId = FileId.of(command.fileId().toString());
        File file = loadFilePort.loadById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + command.fileId()));

        // 2. 상태 검증 (COMPLETED만 허용)
        validateFileStatus(file);

        // TODO: Cycle 27에서 FileProcessingJob 생성 구현
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
}
