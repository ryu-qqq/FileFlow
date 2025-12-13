package com.ryuqq.fileflow.application.session.strategy;

import com.ryuqq.fileflow.application.session.factory.command.UploadSessionCommandFactory;
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
 *
 * <p><strong>영속화 책임</strong>: Service에서 처리 (Strategy는 비즈니스 로직만 담당)
 */
@Component
public class SingleUploadExpireStrategy implements ExpireStrategy<SingleUploadSession> {

    private final UploadSessionCommandFactory commandFactory;

    public SingleUploadExpireStrategy(UploadSessionCommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @Override
    public void expire(SingleUploadSession session) {
        session.expire(commandFactory.getClock());
    }
}
