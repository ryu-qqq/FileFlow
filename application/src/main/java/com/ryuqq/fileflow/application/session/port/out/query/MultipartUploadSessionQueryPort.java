package com.ryuqq.fileflow.application.session.port.out.query;

import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
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
}
