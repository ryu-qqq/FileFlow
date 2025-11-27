package com.ryuqq.fileflow.application.session.port.out.query;

import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionSearchCriteria;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 업로드 세션 조회 Query Port.
 *
 * <p>세션 ID 또는 멱등성 키 기반으로 업로드 세션을 조회합니다.
 */
public interface FindUploadSessionQueryPort {

    /**
     * 세션 ID로 단일 업로드 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 조회된 세션 (없으면 Optional.empty())
     */
    Optional<SingleUploadSession> findSingleUploadById(UploadSessionId sessionId);

    /**
     * 멱등성 키로 단일 업로드 세션을 조회합니다.
     *
     * @param idempotencyKey 멱등성 키
     * @return 조회된 세션 (없으면 Optional.empty())
     */
    Optional<SingleUploadSession> findSingleUploadByIdempotencyKey(IdempotencyKey idempotencyKey);

    /**
     * 세션 ID로 Multipart 업로드 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 조회된 세션 (없으면 Optional.empty())
     */
    Optional<MultipartUploadSession> findMultipartUploadById(UploadSessionId sessionId);

    /**
     * 세션 ID로 업로드 세션을 조회합니다 (Single 또는 Multipart).
     *
     * <p>세션 타입에 관계없이 공통 인터페이스로 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 조회된 세션 (없으면 Optional.empty())
     */
    Optional<UploadSession> findById(UploadSessionId sessionId);

    /**
     * 만료 시간이 지난 단일 업로드 세션 목록을 조회합니다.
     *
     * <p>PREPARING 또는 ACTIVE 상태이면서 만료 시간이 지난 세션을 조회합니다.
     *
     * @param expiredBefore 이 시간 이전에 만료된 세션을 조회
     * @param limit 최대 조회 개수
     * @return 만료된 세션 목록
     */
    List<SingleUploadSession> findExpiredSingleUploads(Instant expiredBefore, int limit);

    /**
     * 만료 시간이 지난 멀티파트 업로드 세션 목록을 조회합니다.
     *
     * <p>PREPARING 또는 ACTIVE 상태이면서 만료 시간이 지난 세션을 조회합니다.
     *
     * @param expiredBefore 이 시간 이전에 만료된 세션을 조회
     * @param limit 최대 조회 개수
     * @return 만료된 세션 목록
     */
    List<MultipartUploadSession> findExpiredMultipartUploads(Instant expiredBefore, int limit);

    /**
     * 세션 ID와 테넌트 ID로 업로드 세션을 조회합니다.
     *
     * <p>테넌트 스코프를 적용하여 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @param tenantId 테넌트 ID
     * @return 조회된 세션 (없으면 Optional.empty())
     */
    Optional<UploadSession> findByIdAndTenantId(UploadSessionId sessionId, Long tenantId);

    /**
     * 검색 조건에 맞는 업로드 세션 목록을 조회합니다.
     *
     * @param criteria 검색 조건
     * @return 업로드 세션 목록
     */
    List<UploadSession> findByCriteria(UploadSessionSearchCriteria criteria);

    /**
     * 검색 조건에 맞는 업로드 세션 개수를 조회합니다.
     *
     * @param criteria 검색 조건
     * @return 전체 개수
     */
    long countByCriteria(UploadSessionSearchCriteria criteria);
}
