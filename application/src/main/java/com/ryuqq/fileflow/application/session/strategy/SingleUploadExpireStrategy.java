package com.ryuqq.fileflow.application.session.strategy;

import com.ryuqq.fileflow.application.session.manager.UploadSessionManager;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import org.springframework.stereotype.Component;

/**
 * Single 업로드 세션 만료 전략.
 *
 * <p>Single 업로드는 S3에 파일이 완전히 저장되거나 없으므로 Domain 만료 처리만 수행합니다.
 *
 * <p><strong>처리 내용</strong>:
 *
 * <ul>
 *   <li>Domain에서 상태 전환 (PREPARING/ACTIVE → EXPIRED)
 * </ul>
 */
@Component
public class SingleUploadExpireStrategy implements ExpireStrategy<SingleUploadSession> {

    private final UploadSessionManager uploadSessionManager;

    public SingleUploadExpireStrategy(UploadSessionManager uploadSessionManager) {
        this.uploadSessionManager = uploadSessionManager;
    }

    @Override
    public void expire(SingleUploadSession session) {
        session.expire();
        uploadSessionManager.save(session);
    }
}
