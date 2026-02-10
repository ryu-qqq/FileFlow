package com.ryuqq.fileflow.application.transform.port.out.query;

import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.id.TransformRequestId;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransformRequestQueryPort {

    Optional<TransformRequest> findById(TransformRequestId id);

    List<TransformRequest> findByStatusAndCreatedBefore(
            TransformStatus status, Instant createdBefore, int limit);
}
