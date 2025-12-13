package com.ryuqq.fileflow.application.session.manager.query;

import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionSearchCriteria;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * UploadSession 조회 전용 ReadManager.
 *
 * <p>FindUploadSessionQueryPort를 래핑하여 조회 기능을 제공합니다.
 *
 * <p><strong>설계 원칙</strong>:
 *
 * <ul>
 *   <li>단일 Port 의존성 (FindUploadSessionQueryPort)
 *   <li>모든 메서드에 @Transactional(readOnly=true)
 *   <li>Service가 직접 Port 호출하지 않고 ReadManager 통해 조회
 * </ul>
 */
@Component
public class UploadSessionReadManager {

    private final FindUploadSessionQueryPort findUploadSessionQueryPort;

    public UploadSessionReadManager(FindUploadSessionQueryPort findUploadSessionQueryPort) {
        this.findUploadSessionQueryPort = findUploadSessionQueryPort;
    }

    /**
     * 세션 ID로 업로드 세션을 조회합니다 (Single 또는 Multipart).
     *
     * @param sessionId 세션 ID
     * @return 조회된 세션 (없으면 Optional.empty())
     */
    @Transactional(readOnly = true)
    public Optional<UploadSession> findById(UploadSessionId sessionId) {
        return findUploadSessionQueryPort.findById(sessionId);
    }

    /**
     * 세션 ID와 테넌트 ID로 업로드 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @param tenantId 테넌트 ID (UUIDv7 문자열)
     * @return 조회된 세션 (없으면 Optional.empty())
     */
    @Transactional(readOnly = true)
    public Optional<UploadSession> findByIdAndTenantId(UploadSessionId sessionId, String tenantId) {
        return findUploadSessionQueryPort.findByIdAndTenantId(sessionId, tenantId);
    }

    /**
     * 세션 ID로 단일 업로드 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 조회된 세션 (없으면 Optional.empty())
     */
    @Transactional(readOnly = true)
    public Optional<SingleUploadSession> findSingleUploadById(UploadSessionId sessionId) {
        return findUploadSessionQueryPort.findSingleUploadById(sessionId);
    }

    /**
     * 멱등성 키로 단일 업로드 세션을 조회합니다.
     *
     * @param idempotencyKey 멱등성 키
     * @return 조회된 세션 (없으면 Optional.empty())
     */
    @Transactional(readOnly = true)
    public Optional<SingleUploadSession> findSingleUploadByIdempotencyKey(
            IdempotencyKey idempotencyKey) {
        return findUploadSessionQueryPort.findSingleUploadByIdempotencyKey(idempotencyKey);
    }

    /**
     * 세션 ID로 Multipart 업로드 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 조회된 세션 (없으면 Optional.empty())
     */
    @Transactional(readOnly = true)
    public Optional<MultipartUploadSession> findMultipartUploadById(UploadSessionId sessionId) {
        return findUploadSessionQueryPort.findMultipartUploadById(sessionId);
    }

    /**
     * 만료 시간이 지난 단일 업로드 세션 목록을 조회합니다.
     *
     * @param expiredBefore 이 시간 이전에 만료된 세션을 조회
     * @param limit 최대 조회 개수
     * @return 만료된 세션 목록
     */
    @Transactional(readOnly = true)
    public List<SingleUploadSession> findExpiredSingleUploads(Instant expiredBefore, int limit) {
        return findUploadSessionQueryPort.findExpiredSingleUploads(expiredBefore, limit);
    }

    /**
     * 만료 시간이 지난 멀티파트 업로드 세션 목록을 조회합니다.
     *
     * @param expiredBefore 이 시간 이전에 만료된 세션을 조회
     * @param limit 최대 조회 개수
     * @return 만료된 세션 목록
     */
    @Transactional(readOnly = true)
    public List<MultipartUploadSession> findExpiredMultipartUploads(
            Instant expiredBefore, int limit) {
        return findUploadSessionQueryPort.findExpiredMultipartUploads(expiredBefore, limit);
    }

    /**
     * 검색 조건에 맞는 업로드 세션 목록을 조회합니다.
     *
     * @param criteria 검색 조건
     * @return 업로드 세션 목록
     */
    @Transactional(readOnly = true)
    public List<UploadSession> findByCriteria(UploadSessionSearchCriteria criteria) {
        return findUploadSessionQueryPort.findByCriteria(criteria);
    }

    /**
     * 검색 조건에 맞는 업로드 세션 개수를 조회합니다.
     *
     * @param criteria 검색 조건
     * @return 전체 개수
     */
    @Transactional(readOnly = true)
    public long countByCriteria(UploadSessionSearchCriteria criteria) {
        return findUploadSessionQueryPort.countByCriteria(criteria);
    }
}
