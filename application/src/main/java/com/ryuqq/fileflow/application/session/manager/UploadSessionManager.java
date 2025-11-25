package com.ryuqq.fileflow.application.session.manager;

import com.ryuqq.fileflow.application.session.port.out.command.PersistCompletedPartPort;
import com.ryuqq.fileflow.application.session.port.out.command.PersistMultipartUploadSessionPort;
import com.ryuqq.fileflow.application.session.port.out.command.PersistSingleUploadSessionPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업로드 세션 관리 Manager.
 *
 * <p>업로드 세션의 생성, 조회, 영속화를 담당합니다.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>멱등성 키 기반 세션 중복 확인 (Single Upload)
 *   <li>RDB + Cache 이중 저장 (TTL 자동 만료)
 *   <li>업로드 세션 타입별 영속화 (Single/Multipart)
 * </ul>
 *
 * <p><strong>RDB 저장 전략</strong>:
 *
 * <ul>
 *   <li>Table Per Class: 각 세션 타입은 별도 테이블로 관리
 *   <li>SingleUploadSession → upload_session_single (PersistSingleUploadSessionPort)
 *   <li>MultipartUploadSession → upload_session_multipart (PersistMultipartUploadSessionPort)
 * </ul>
 *
 * <p><strong>Port 분리 이유</strong>:
 *
 * <ul>
 *   <li>각 Port는 별도의 JPA Entity를 사용 (Entity 섞임 방지)
 *   <li>각 Port는 별도의 Adapter로 구현 (단일 책임 원칙)
 *   <li>각 테이블에 최적화된 독립적인 영속화 로직 가능
 * </ul>
 */
@Component
public class UploadSessionManager {

    private final PersistSingleUploadSessionPort persistSingleUploadSessionPort;
    private final PersistMultipartUploadSessionPort persistMultipartUploadSessionPort;
    private final PersistCompletedPartPort persistCompletedPartPort;

    public UploadSessionManager(
            PersistSingleUploadSessionPort persistSingleUploadSessionPort,
            PersistMultipartUploadSessionPort persistMultipartUploadSessionPort,
            PersistCompletedPartPort persistCompletedPartPort) {
        this.persistSingleUploadSessionPort = persistSingleUploadSessionPort;
        this.persistMultipartUploadSessionPort = persistMultipartUploadSessionPort;
        this.persistCompletedPartPort = persistCompletedPartPort;
    }

    /**
     * 단일 업로드 세션을 저장합니다.
     *
     * <p>RDB(upload_session_single)와 Redis Cache에 모두 저장되며, Cache에는 TTL(15분)이 설정됩니다.
     *
     * @param session 저장할 세션
     * @return 저장된 세션 (ID 포함)
     */
    @Transactional
    public SingleUploadSession save(SingleUploadSession session) {
        return persistSingleUploadSessionPort.persist(session);
    }

    /**
     * Multipart 업로드 세션을 저장합니다.
     *
     * <p>RDB(upload_session_multipart)와 Redis Cache에 모두 저장되며, Cache에는 TTL(24시간)이 설정됩니다.
     *
     * @param session 저장할 세션
     * @return 저장된 세션 (ID 포함)
     */
    @Transactional
    public MultipartUploadSession save(MultipartUploadSession session) {
        return persistMultipartUploadSessionPort.persist(session);
    }

    /**
     * 완료된 파트를 저장합니다.
     *
     * <p>MarkPartUploadedService에서 개별 파트 업로드 완료 시 호출됩니다.
     *
     * @param sessionId 세션 ID
     * @param completedPart 완료된 파트 정보
     */
    @Transactional
    public CompletedPart saveCompletedPart(UploadSessionId sessionId, CompletedPart completedPart) {
        return persistCompletedPartPort.persist(sessionId, completedPart);
    }

    /**
     * 여러 파트를 한 트랜잭션에서 저장합니다.
     *
     * <p>InitMultipartUploadService에서 초기화된 파트들을 일괄 저장 시 호출됩니다.
     *
     * @param sessionId 세션 ID
     * @param completedParts 저장할 파트 목록
     */
    @Transactional
    public void saveAllCompletedParts(
            UploadSessionId sessionId, List<CompletedPart> completedParts) {
        for (CompletedPart part : completedParts) {
            persistCompletedPartPort.persist(sessionId, part);
        }
    }
}
