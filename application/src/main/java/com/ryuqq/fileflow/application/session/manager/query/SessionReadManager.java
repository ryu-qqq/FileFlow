package com.ryuqq.fileflow.application.session.manager.query;

import com.ryuqq.fileflow.application.session.port.out.query.MultipartUploadSessionQueryPort;
import com.ryuqq.fileflow.application.session.port.out.query.SingleUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.id.MultipartUploadSessionId;
import com.ryuqq.fileflow.domain.session.id.SingleUploadSessionId;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SessionReadManager {

    private final SingleUploadSessionQueryPort singleQueryPort;
    private final MultipartUploadSessionQueryPort multipartQueryPort;

    public SessionReadManager(
            SingleUploadSessionQueryPort singleQueryPort,
            MultipartUploadSessionQueryPort multipartQueryPort) {
        this.singleQueryPort = singleQueryPort;
        this.multipartQueryPort = multipartQueryPort;
    }

    @Transactional(readOnly = true)
    public SingleUploadSession getSingle(String sessionId) {
        return singleQueryPort
                .findById(SingleUploadSessionId.of(sessionId))
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    @Transactional(readOnly = true)
    public MultipartUploadSession getMultipart(String sessionId) {
        return multipartQueryPort
                .findById(MultipartUploadSessionId.of(sessionId))
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    @Transactional(readOnly = true)
    public List<SingleUploadSession> findExpiredSingleSessions(Instant now, int limit) {
        return singleQueryPort.findExpiredSessions(now, limit);
    }

    @Transactional(readOnly = true)
    public List<MultipartUploadSession> findExpiredMultipartSessions(Instant now, int limit) {
        return multipartQueryPort.findExpiredSessions(now, limit);
    }
}
