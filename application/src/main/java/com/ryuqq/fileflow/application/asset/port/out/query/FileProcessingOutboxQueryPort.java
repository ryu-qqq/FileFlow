package com.ryuqq.fileflow.application.asset.port.out.query;

import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.vo.FileProcessingOutboxId;
import java.util.List;
import java.util.Optional;

/**
 * FileProcessingOutbox Query Port.
 *
 * <p>파일 처리 Outbox 조회를 위한 출력 포트이다.
 *
 * <p>스케줄러에서 미전송/실패 이벤트를 조회하여 재처리한다.
 */
public interface FileProcessingOutboxQueryPort {

    /**
     * ID로 Outbox를 조회한다.
     *
     * @param outboxId Outbox ID
     * @return FileProcessingOutbox (Optional)
     */
    Optional<FileProcessingOutbox> findById(FileProcessingOutboxId outboxId);

    /**
     * 대기 중인(PENDING) 이벤트 목록을 조회한다.
     *
     * <p>아직 전송되지 않은 Outbox 이벤트를 조회하여 전송 처리한다.
     *
     * @param limit 최대 조회 개수
     * @return 대기 중인 FileProcessingOutbox 목록
     */
    List<FileProcessingOutbox> findPendingEvents(int limit);

    /**
     * 재시도 가능한 실패 이벤트 목록을 조회한다.
     *
     * <p>전송 실패했지만 재시도 횟수가 남은 이벤트를 조회한다.
     *
     * @param limit 최대 조회 개수
     * @return 재시도 가능한 FileProcessingOutbox 목록
     */
    List<FileProcessingOutbox> findRetryableFailedEvents(int limit);
}
