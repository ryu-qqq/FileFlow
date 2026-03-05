package com.ryuqq.fileflow.application.transform.dto.bundle;

import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import java.time.Instant;

public record TransformFailureBundle(
        TransformRequest request,
        String errorMessage,
        Instant failedAt,
        TransformCallbackOutbox callbackOutbox) {

    public boolean hasCallback() {
        return callbackOutbox != null;
    }
}
