package com.ryuqq.fileflow.application.session.port.out.query;

import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
import java.util.Optional;

/** SingleUploadSession 조회 포트 (Query) */
public interface SingleUploadSessionQueryPort {

    /**
     * ID로 세션 단건 조회
     *
     * @param id 세션 ID
     * @return 세션 (Optional)
     */
    Optional<SingleUploadSession> findById(SingleUploadSessionId id);
}
