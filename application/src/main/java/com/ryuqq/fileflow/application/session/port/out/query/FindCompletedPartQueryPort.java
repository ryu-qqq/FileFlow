package com.ryuqq.fileflow.application.session.port.out.query;

import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.List;
import java.util.Optional;

/**
 * CompletedPart 조회 Query Port.
 *
 * <p>Multipart 업로드의 개별 Part를 조회합니다.
 */
public interface FindCompletedPartQueryPort {

    /**
     * 세션 ID와 Part 번호로 CompletedPart를 조회한다.
     *
     * @param sessionId 세션 ID
     * @param partNumber Part 번호
     * @return CompletedPart (없으면 empty)
     */
    Optional<CompletedPart> findBySessionIdAndPartNumber(UploadSessionId sessionId, int partNumber);

    /**
     * 세션 ID로 모든 CompletedPart를 조회한다.
     *
     * @param sessionId 세션 ID
     * @return CompletedPart 목록
     */
    List<CompletedPart> findAllBySessionId(UploadSessionId sessionId);
}
