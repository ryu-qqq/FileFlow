package com.ryuqq.fileflow.application.session.strategy;

import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 만료 전략 Provider.
 *
 * <p>세션 타입에 맞는 만료 전략을 O(1)로 제공합니다.
 *
 * <p><strong>내부 구조</strong>:
 *
 * <ul>
 *   <li>생성 시 모든 전략을 타입별 Map으로 초기화
 *   <li>조회 시 Class 타입으로 즉시 반환
 * </ul>
 */
@Component
public class ExpireStrategyProvider {

    private final Map<Class<? extends UploadSession>, ExpireStrategy<? extends UploadSession>>
            strategyMap;

    public ExpireStrategyProvider(List<ExpireStrategy<? extends UploadSession>> strategies) {
        this.strategyMap = new HashMap<>();

        for (ExpireStrategy<? extends UploadSession> strategy : strategies) {
            if (strategy instanceof SingleUploadExpireStrategy) {
                strategyMap.put(SingleUploadSession.class, strategy);
            } else if (strategy instanceof MultipartUploadExpireStrategy) {
                strategyMap.put(MultipartUploadSession.class, strategy);
            }
        }
    }

    /**
     * 세션 타입에 맞는 만료 전략을 반환한다.
     *
     * @param session 업로드 세션
     * @return 만료 전략
     * @throws IllegalStateException 지원하지 않는 세션 타입인 경우
     */
    @SuppressWarnings("unchecked")
    public <T extends UploadSession> ExpireStrategy<T> getStrategy(T session) {
        ExpireStrategy<? extends UploadSession> strategy = strategyMap.get(session.getClass());

        if (strategy == null) {
            throw new IllegalStateException("지원하지 않는 세션 타입: " + session.getClass().getName());
        }

        return (ExpireStrategy<T>) strategy;
    }
}
