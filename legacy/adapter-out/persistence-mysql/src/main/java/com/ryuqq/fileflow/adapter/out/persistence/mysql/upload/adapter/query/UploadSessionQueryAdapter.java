package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.query;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.UploadSessionEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadSessionQueryDslRepository;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Upload Session Query Adapter (CQRS - Query Side)
 *
 * <p>Application Layer의 {@link LoadUploadSessionPort}를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession Domain Aggregate의 조회 (읽기 전용)</li>
 *   <li>QueryDSL을 통한 DB 접근 및 성능 최적화</li>
 *   <li>Mapper를 통한 Entity → Domain 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수</li>
 *   <li>✅ @Transactional(readOnly = true) 적용 (조회 최적화)</li>
 *   <li>✅ QueryDSL 사용 (복잡한 쿼리 타입 안전)</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>✅ Long FK Strategy (JPA 관계 없음)</li>
 *   <li>✅ CQRS - Query 전용 (읽기만)</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * <p><strong>성능 최적화:</strong></p>
 * <ul>
 *   <li>QueryDSL로 동적 쿼리 타입 안전하게 구성</li>
 *   <li>복합 Index 활용: (status, created_at), (tenant_id, status)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionQueryAdapter implements LoadUploadSessionPort {

    private final UploadSessionQueryDslRepository repository;

    /**
     * 생성자
     *
     * @param repository Upload Session QueryDSL Repository
     */
    public UploadSessionQueryAdapter(UploadSessionQueryDslRepository repository) {
        this.repository = repository;
    }

    /**
     * ID로 Upload Session 조회
     *
     * <p><strong>성능:</strong> Primary Key 조회 (O(1))</p>
     *
     * @param id Upload Session ID
     * @return Upload Session (Optional)
     */
    @Override
    public Optional<UploadSession> findById(Long id) {
        return repository.findById(id)
            .map(UploadSessionEntityMapper::toDomain);
    }

    /**
     * Session Key로 Upload Session 조회
     *
     * <p><strong>성능:</strong> Unique Index 조회 (O(log n))</p>
     * <p><strong>Index:</strong> idx_session_key (session_key)</p>
     *
     * @param sessionKey Session Key
     * @return Upload Session (Optional)
     */
    @Override
    public Optional<UploadSession> findBySessionKey(SessionKey sessionKey) {
        return repository.findBySessionKey(sessionKey.value())
            .map(UploadSessionEntityMapper::toDomain);
    }

    /**
     * Tenant ID와 상태로 Upload Session 개수 조회
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>Tenant별 활성 업로드 세션 모니터링</li>
     *   <li>Rate Limiting 구현 (Tenant당 동시 업로드 제한)</li>
     * </ul>
     *
     * <p><strong>성능:</strong></p>
     * <ul>
     *   <li>복합 Index 활용: idx_tenant_status (tenant_id, status)</li>
     *   <li>COUNT(*) 쿼리 → Index만 사용 (테이블 스캔 없음)</li>
     * </ul>
     *
     * <p><strong>예시:</strong></p>
     * <pre>{@code
     * // Tenant의 진행 중인 업로드 개수 확인
     * long activeCount = queryAdapter.countByTenantIdAndStatus(
     *     tenantId,
     *     SessionStatus.IN_PROGRESS
     * );
     *
     * if (activeCount >= MAX_CONCURRENT_UPLOADS) {
     *     throw new RateLimitExceededException("Too many concurrent uploads");
     * }
     * }</pre>
     *
     * @param tenantId Tenant ID (Not Null)
     * @param status 세션 상태 (Not Null)
     * @return 세션 개수 (0 이상)
     * @throws IllegalArgumentException tenantId 또는 status가 null인 경우
     */
    @Override
    public long countByTenantIdAndStatus(Long tenantId, SessionStatus status) {
        return repository.countByTenantIdAndStatus(tenantId, status);
    }

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
     * <p><strong>트랜잭션 요구사항:</strong></p>
     * <ul>
     *   <li>반드시 {@code @Transactional} 내에서 호출되어야 함</li>
     *   <li>트랜잭션 종료 시 Lock 자동 해제</li>
     *   <li>트랜잭션은 짧게 유지 (외부 API 호출 금지)</li>
     * </ul>
     *
     * <p><strong>예시:</strong></p>
     * <pre>{@code
     * @Transactional
     * public void cleanupExpiredSessions() {
     *     LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
     *     List<UploadSession> sessions = queryAdapter.findByStatusAndCreatedBeforeWithLock(
     *         SessionStatus.PENDING,
     *         threshold,
     *         1000
     *     );
     *     // 각 세션 처리...
     * }
     * }</pre>
     *
     * @param status 세션 상태 (PENDING, IN_PROGRESS 등, Not Null)
     * @param threshold 이 시간 이전에 생성된 세션만 조회 (Not Null)
     * @param limit 최대 조회 건수 (1 이상, 권장: 100~1000)
     * @return 만료된 세션 목록 (잠금 획득된 세션만, 최대 limit 건)
     * @throws IllegalArgumentException status, threshold가 null이거나 limit < 1인 경우
     */
    @Override
    public List<UploadSession> findByStatusAndCreatedBeforeWithLock(
        SessionStatus status,
        LocalDateTime threshold,
        int limit
    ) {
        return repository.findByStatusAndCreatedBeforeWithLock(status, threshold, limit)
            .stream()
            .map(UploadSessionEntityMapper::toDomain)
            .collect(Collectors.toList());
    }

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
     * </ul>
     *
     * <p><strong>트랜잭션 요구사항:</strong></p>
     * <ul>
     *   <li>반드시 {@code @Transactional} 내에서 호출되어야 함</li>
     *   <li>트랜잭션은 짧게 유지</li>
     * </ul>
     *
     * <p><strong>예시:</strong></p>
     * <pre>{@code
     * @Transactional
     * public void cleanupExpiredSessions() {
     *     LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
     *     List<SessionStatus> statuses = Arrays.asList(
     *         SessionStatus.PENDING,
     *         SessionStatus.IN_PROGRESS
     *     );
     *     List<UploadSession> sessions = queryAdapter.findByStatusInAndCreatedBeforeWithLock(
     *         statuses,
     *         threshold,
     *         1000
     *     );
     *     // 각 세션 처리...
     * }
     * }</pre>
     *
     * @param statuses 세션 상태 목록 (Not Null, Not Empty)
     * @param threshold 이 시간 이전에 생성된 세션만 조회 (Not Null)
     * @param limit 최대 조회 건수 (1 이상, 권장: 100~1000)
     * @return 만료된 세션 목록 (잠금 획득된 세션만, 최대 limit 건)
     * @throws IllegalArgumentException statuses, threshold가 null이거나 limit < 1인 경우
     */
    @Override
    public List<UploadSession> findByStatusInAndCreatedBeforeWithLock(
        List<SessionStatus> statuses,
        LocalDateTime threshold,
        int limit
    ) {
        return repository.findByStatusInAndCreatedBeforeWithLock(statuses, threshold, limit)
            .stream()
            .map(UploadSessionEntityMapper::toDomain)
            .collect(Collectors.toList());
    }
}
