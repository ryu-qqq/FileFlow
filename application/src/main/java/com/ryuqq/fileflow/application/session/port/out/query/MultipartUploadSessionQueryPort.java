package com.ryuqq.fileflow.application.session.port.out.query;

import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** MultipartUploadSession 조회 포트 (Query) */
public interface MultipartUploadSessionQueryPort {

    /**
     * ID로 세션 단건 조회
     *
     * @param id 세션 ID
     * @return 세션 (Optional)
     */
    Optional<MultipartUploadSession> findById(MultipartUploadSessionId id);

    /**
     * 만료 시간이 지났지만 활성 상태(INITIATED, UPLOADING)인 고아 세션 목록 조회
     *
     * @param now 기준 시각
     * @param limit 최대 조회 수
     * @return 고아 세션 목록
     */
    List<MultipartUploadSession> findExpiredSessions(Instant now, int limit);
}
