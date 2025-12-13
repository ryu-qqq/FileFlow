package com.ryuqq.fileflow.application.session.manager.command;

import com.ryuqq.fileflow.application.session.port.out.command.CompletedPartPersistencePort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CompletedPart 영속화 TransactionManager.
 *
 * <p>CompletedPart(파트 업로드 완료 정보)의 영속화를 담당합니다.
 *
 * <p><strong>컨벤션</strong>:
 *
 * <ul>
 *   <li>단일 PersistencePort 의존성
 *   <li>persist* 메서드만 허용
 *   <li>@Component + @Transactional 필수
 * </ul>
 */
@Component
@Transactional
public class CompletedPartTransactionManager {

    private final CompletedPartPersistencePort completedPartPersistencePort;

    public CompletedPartTransactionManager(
            CompletedPartPersistencePort completedPartPersistencePort) {
        this.completedPartPersistencePort = completedPartPersistencePort;
    }

    /**
     * 완료된 파트를 저장합니다.
     *
     * <p>MarkPartUploadedService에서 개별 파트 업로드 완료 시 호출됩니다.
     *
     * @param sessionId 세션 ID
     * @param completedPart 완료된 파트 정보
     * @return 저장된 CompletedPart
     */
    public CompletedPart persist(UploadSessionId sessionId, CompletedPart completedPart) {
        return completedPartPersistencePort.persist(sessionId, completedPart);
    }

    /**
     * 여러 파트를 한 트랜잭션에서 저장합니다.
     *
     * <p>InitMultipartUploadService에서 초기화된 파트들을 일괄 저장 시 호출됩니다.
     *
     * @param sessionId 세션 ID
     * @param completedParts 저장할 파트 목록
     */
    public void persistAll(UploadSessionId sessionId, List<CompletedPart> completedParts) {
        for (CompletedPart part : completedParts) {
            completedPartPersistencePort.persist(sessionId, part);
        }
    }
}
