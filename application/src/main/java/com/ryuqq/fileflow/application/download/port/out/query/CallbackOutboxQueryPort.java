package com.ryuqq.fileflow.application.download.port.out.query;

import com.ryuqq.fileflow.domain.download.aggregate.CallbackOutbox;
import java.util.List;

public interface CallbackOutboxQueryPort {

    List<CallbackOutbox> findPendingMessages(int limit);

    List<CallbackOutbox> claimPendingMessages(int limit);
}
