package com.ryuqq.fileflow.application.transform.port.out.query;

import com.ryuqq.fileflow.domain.transform.aggregate.TransformCallbackOutbox;
import java.util.List;

public interface TransformCallbackOutboxQueryPort {

    List<TransformCallbackOutbox> findPendingMessages(int limit);
}
