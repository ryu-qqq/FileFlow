package com.ryuqq.fileflow.application.session.manager.query;

import com.ryuqq.fileflow.application.session.port.out.query.FindCompletedPartQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CompletedPart 조회 전용 ReadManager.
 *
 * <p>FindCompletedPartQueryPort를 래핑하여 조회 기능을 제공합니다.
 *
 * <p><strong>설계 원칙</strong>:
 *
 * <ul>
 *   <li>단일 Port 의존성 (FindCompletedPartQueryPort)
 *   <li>모든 메서드에 @Transactional(readOnly=true)
 *   <li>Service가 직접 Port 호출하지 않고 ReadManager 통해 조회
 * </ul>
 */
@Component
public class CompletedPartReadManager {

    private final FindCompletedPartQueryPort findCompletedPartQueryPort;

    public CompletedPartReadManager(FindCompletedPartQueryPort findCompletedPartQueryPort) {
        this.findCompletedPartQueryPort = findCompletedPartQueryPort;
    }

    /**
     * 세션 ID와 Part 번호로 CompletedPart를 조회합니다.
     *
     * @param sessionId 세션 ID
     * @param partNumber Part 번호
     * @return CompletedPart (없으면 empty)
     */
    @Transactional(readOnly = true)
    public Optional<CompletedPart> findBySessionIdAndPartNumber(
            UploadSessionId sessionId, int partNumber) {
        return findCompletedPartQueryPort.findBySessionIdAndPartNumber(sessionId, partNumber);
    }

    /**
     * 세션 ID로 모든 CompletedPart를 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return CompletedPart 목록
     */
    @Transactional(readOnly = true)
    public List<CompletedPart> findAllBySessionId(UploadSessionId sessionId) {
        return findCompletedPartQueryPort.findAllBySessionId(sessionId);
    }
}
