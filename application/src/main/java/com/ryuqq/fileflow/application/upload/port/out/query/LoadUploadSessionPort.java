package com.ryuqq.fileflow.application.upload.port.out.query;

import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Upload Session 조회 Port (Query)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Query Port입니다.
 * CQRS 패턴의 Query 책임만 담당하며, Command 작업은 {@code SaveUploadSessionPort},
 * {@code DeleteUploadSessionPort}에서 처리합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Upload Session Aggregate 조회 (단건, 목록)</li>
 *   <li>CQRS Query 패턴 구현</li>
 *   <li>조회 성능 최적화 (Pagination, Index 활용)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Query Port (Read 전담, Write 금지)</li>
 *   <li>✅ Domain 객체만 반환 (Infrastructure Entity, DTO 금지)</li>
 *   <li>✅ Infrastructure 독립적 (JPA, QueryDSL 구현 세부사항 은닉)</li>
 *   <li>✅ Immutable Query (조회만 수행, 상태 변경 없음)</li>
 * </ul>
 *
 * <p><strong>Transaction 요구사항:</strong></p>
 * <ul>
 *   <li>모든 메서드는 {@code @Transactional(readOnly = true)} 권장</li>
 *   <li>조회 작업이므로 DB Lock 불필요</li>
 * </ul>
 *
 * <p><strong>성능 고려사항:</strong></p>
 * <ul>
 *   <li>{@link #findByStatusAndCreatedBefore}는 대량 데이터 조회 가능 → Pagination 권장</li>
 *   <li>SessionKey, Status 컬럼에 Index 필수</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see com.ryuqq.fileflow.application.upload.port.out.command.SaveUploadSessionPort
 * @see com.ryuqq.fileflow.application.upload.port.out.command.DeleteUploadSessionPort
 */
public interface LoadUploadSessionPort {

    /**
     * ID로 Upload Session 조회
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>외부 다운로드 완료 시 UploadSession 조회</li>
     *   <li>FileAsset 생성 시 연관 UploadSession 확인</li>
     * </ul>
     *
     * <p><strong>성능:</strong> Primary Key 조회 (O(1))</p>
     *
     * @param id Upload Session ID (양수, Not Null)
     * @return Upload Session (존재하지 않으면 {@code Optional.empty()})
     * @throws IllegalArgumentException id가 null 또는 음수인 경우
     */
    Optional<UploadSession> findById(Long id);

    /**
     * Session Key로 Upload Session 조회
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>클라이언트가 업로드 완료 요청 시 (세션 키 기반)</li>
     *   <li>Presigned URL 생성 시 세션 유효성 검증</li>
     *   <li>멀티파트 업로드 Part 완료 시</li>
     * </ul>
     *
     * <p><strong>성능:</strong> Unique Index 조회 (O(log n))</p>
     *
     * <p><strong>주의:</strong></p>
     * <ul>
     *   <li>SessionKey는 Unique 제약이 있어야 함 (DB Level)</li>
     *   <li>SessionKey 컬럼에 Index 필수</li>
     * </ul>
     *
     * @param sessionKey Session Key (Not Null)
     * @return Upload Session (존재하지 않으면 {@code Optional.empty()})
     * @throws IllegalArgumentException sessionKey가 null인 경우
     */
    Optional<UploadSession> findBySessionKey(SessionKey sessionKey);

    /**
     * Tenant ID와 상태로 Upload Session 개수 조회
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>Tenant별 활성 업로드 세션 모니터링</li>
     *   <li>Rate Limiting 구현 (Tenant당 동시 업로드 제한)</li>
     * </ul>
     *
     * <p><strong>성능:</strong> Index 활용 시 O(log n)</p>
     *
     * <p><strong>주의:</strong></p>
     * <ul>
     *   <li>복합 Index 필수: {@code (tenant_id, status)}</li>
     * </ul>
     *
     * @param tenantId Tenant ID (Not Null)
     * @param status 세션 상태 (Not Null)
     * @return 세션 개수 (0 이상)
     * @throws IllegalArgumentException tenantId 또는 status가 null인 경우
     */
    long countByTenantIdAndStatus(Long tenantId, SessionStatus status);

    /**
     * 상태와 생성시간 기준으로 만료된 세션 조회 (Pessimistic Lock + Limit)
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>CleanupExpiredSessionsScheduler: 만료된 세션 정리</li>
     *   <li>동시 실행되는 스케줄러 인스턴스 간 경합 방지</li>
     * </ul>
     *
     * <p><strong>Pessimistic Lock (FOR UPDATE SKIP LOCKED):</strong></p>
     * <ul>
     *   <li>잠금 획득된 Row만 반환 (잠금 실패 시 Skip)</li>
     *   <li>동시 실행 스케줄러가 같은 세션을 처리하지 않도록 보장</li>
     *   <li>Deadlock 방지: SKIP LOCKED 사용</li>
     * </ul>
     *
     * <p><strong>성능:</strong></p>
     * <ul>
     *   <li>Index 활용: {@code idx_status_created_at (status, created_at)}</li>
     *   <li>Limit으로 대량 조회 방지 (권장: 100~1000)</li>
     *   <li>트랜잭션 종료 시 Lock 자동 해제</li>
     * </ul>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>반드시 트랜잭션 내에서 호출되어야 함</li>
     *   <li>Lock Timeout 설정 권장 (기본: SKIP LOCKED)</li>
     *   <li>트랜잭션은 짧게 유지 (외부 API 호출 금지)</li>
     * </ul>
     *
     * @param status 세션 상태 (PENDING, IN_PROGRESS 등, Not Null)
     * @param threshold 이 시간 이전에 생성된 세션만 조회 (Not Null)
     * @param limit 최대 조회 건수 (1 이상, 권장: 100~1000)
     * @return 만료된 세션 목록 (잠금 획득된 세션만, 최대 limit 건)
     * @throws IllegalArgumentException status, threshold가 null이거나 limit < 1인 경우
     */
    List<UploadSession> findByStatusAndCreatedBeforeWithLock(
        SessionStatus status,
        LocalDateTime threshold,
        int limit
    );

    /**
     * 여러 상태와 생성시간 기준으로 만료된 세션 조회 (Pessimistic Lock + Limit)
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>CleanupExpiredSessionsScheduler: 복수 상태 일괄 정리</li>
     *   <li>PENDING과 IN_PROGRESS를 한 번에 조회하여 처리</li>
     * </ul>
     *
     * <p><strong>Pessimistic Lock (FOR UPDATE SKIP LOCKED):</strong></p>
     * <ul>
     *   <li>{@link #findByStatusAndCreatedBeforeWithLock}와 동일한 Lock 전략</li>
     *   <li>여러 상태에 대해 동일한 Lock 메커니즘 적용</li>
     * </ul>
     *
     * <p><strong>성능:</strong></p>
     * <ul>
     *   <li>⚠️ IN 절 사용으로 Index 효율 저하 가능</li>
     *   <li>가급적 단일 상태 조회 메서드 사용 권장</li>
     *   <li>Index: {@code idx_status_created_at (status, created_at)}</li>
     * </ul>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>statuses가 비어있으면 빈 리스트 반환</li>
     *   <li>반드시 트랜잭션 내에서 호출</li>
     *   <li>트랜잭션은 짧게 유지</li>
     * </ul>
     *
     * @param statuses 세션 상태 목록 (Not Null, Not Empty)
     * @param threshold 이 시간 이전에 생성된 세션만 조회 (Not Null)
     * @param limit 최대 조회 건수 (1 이상, 권장: 100~1000)
     * @return 만료된 세션 목록 (잠금 획득된 세션만, 최대 limit 건)
     * @throws IllegalArgumentException statuses, threshold가 null이거나 limit < 1인 경우
     */
    List<UploadSession> findByStatusInAndCreatedBeforeWithLock(
        List<SessionStatus> statuses,
        LocalDateTime threshold,
        int limit
    );
}
