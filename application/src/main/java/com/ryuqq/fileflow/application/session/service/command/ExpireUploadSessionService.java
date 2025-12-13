package com.ryuqq.fileflow.application.session.service.command;

import com.ryuqq.fileflow.application.session.dto.command.ExpireUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.response.ExpireUploadSessionResponse;
import com.ryuqq.fileflow.application.session.manager.command.MultipartUploadSessionTransactionManager;
import com.ryuqq.fileflow.application.session.manager.command.SingleUploadSessionTransactionManager;
import com.ryuqq.fileflow.application.session.manager.query.UploadSessionReadManager;
import com.ryuqq.fileflow.application.session.port.in.command.ExpireUploadSessionUseCase;
import com.ryuqq.fileflow.application.session.strategy.ExpireStrategy;
import com.ryuqq.fileflow.application.session.strategy.ExpireStrategyProvider;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.exception.SessionNotFoundException;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import org.springframework.stereotype.Service;

/**
 * 업로드 세션 만료 처리 Service.
 *
 * <p>만료 시간이 지난 세션을 EXPIRED 상태로 전환합니다.
 *
 * <p><strong>처리 플로우</strong>:
 *
 * <ol>
 *   <li>SessionId로 세션 조회 (ReadManager, 공통 인터페이스)
 *   <li>세션 타입에 맞는 만료 전략 선택
 *   <li>전략을 통한 만료 처리 (Domain 만료 + 타입별 추가 작업)
 *   <li>RDB에 저장 (Manager)
 *   <li>Response 변환
 * </ol>
 *
 * <p><strong>전략 패턴</strong>:
 *
 * <ul>
 *   <li>SingleUploadExpireStrategy: Domain 만료만 수행
 *   <li>MultipartUploadExpireStrategy: Domain 만료 + S3 Part 정리
 * </ul>
 */
@Service
public class ExpireUploadSessionService implements ExpireUploadSessionUseCase {

    private final UploadSessionReadManager uploadSessionReadManager;
    private final SingleUploadSessionTransactionManager singleUploadSessionTransactionManager;
    private final MultipartUploadSessionTransactionManager multipartUploadSessionTransactionManager;
    private final ExpireStrategyProvider expireStrategyProvider;

    public ExpireUploadSessionService(
            UploadSessionReadManager uploadSessionReadManager,
            SingleUploadSessionTransactionManager singleUploadSessionTransactionManager,
            MultipartUploadSessionTransactionManager multipartUploadSessionTransactionManager,
            ExpireStrategyProvider expireStrategyProvider) {
        this.uploadSessionReadManager = uploadSessionReadManager;
        this.singleUploadSessionTransactionManager = singleUploadSessionTransactionManager;
        this.multipartUploadSessionTransactionManager = multipartUploadSessionTransactionManager;
        this.expireStrategyProvider = expireStrategyProvider;
    }

    @Override
    public ExpireUploadSessionResponse execute(ExpireUploadSessionCommand command) {
        // 1. SessionId로 세션 조회 (공통 인터페이스)
        UploadSessionId sessionId = UploadSessionId.of(command.sessionId());
        UploadSession session =
                uploadSessionReadManager
                        .findById(sessionId)
                        .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));

        // 2. 세션 타입에 맞는 전략으로 만료 처리
        return expire(session);
    }

    @Override
    public ExpireUploadSessionResponse execute(UploadSession session) {
        return expire(session);
    }

    private ExpireUploadSessionResponse expire(UploadSession session) {
        // 1. 세션 타입에 맞는 전략으로 만료 처리
        ExpireStrategy<UploadSession> strategy = expireStrategyProvider.getStrategy(session);
        strategy.expire(session);

        // 2. RDB 저장 (타입에 따라 분기)
        UploadSession expiredSession;
        if (session instanceof SingleUploadSession singleSession) {
            expiredSession = singleUploadSessionTransactionManager.persist(singleSession);
        } else if (session instanceof MultipartUploadSession multipartSession) {
            expiredSession = multipartUploadSessionTransactionManager.persist(multipartSession);
        } else {
            throw new IllegalStateException("지원하지 않는 세션 타입: " + session.getClass().getName());
        }

        // 3. Response 변환
        return ExpireUploadSessionResponse.of(
                expiredSession.getId().value().toString(),
                expiredSession.getStatus().name(),
                expiredSession.getBucket().bucketName(),
                expiredSession.getS3Key().key(),
                expiredSession.getExpiresAt());
    }
}
