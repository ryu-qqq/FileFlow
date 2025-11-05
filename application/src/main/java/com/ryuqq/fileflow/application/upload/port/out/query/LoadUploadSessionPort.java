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
     * 상태와 생성 시간 기준으로 Upload Session 목록 조회
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>만료된 PENDING 세션 정리 (Batch Job)</li>
     *   <li>특정 상태의 오래된 세션 모니터링</li>
     *   <li>세션 생명주기 관리 (Expired, Abandoned)</li>
     * </ul>
     *
     * <p><strong>성능 고려사항:</strong></p>
     * <ul>
     *   <li>⚠️ 대량 데이터 반환 가능 → <strong>Pagination 권장</strong></li>
     *   <li>복합 Index 필수: {@code (status, created_at)}</li>
     *   <li>Batch 처리 시 {@code LIMIT} 절 사용 권장 (예: 1000건씩)</li>
     * </ul>
     *
     * <p><strong>주의:</strong></p>
     * <ul>
     *   <li>결과가 많을 경우 메모리 부족 위험</li>
     *   <li>Adapter 구현체에서 적절한 Limit 설정 필요</li>
     *   <li>향후 {@code Pageable} 파라미터 추가 고려</li>
     * </ul>
     *
     * <p><strong>예시:</strong></p>
     * <pre>{@code
     * // 30분 이상 PENDING 상태인 세션 조회
     * LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
     * List<UploadSession> expiredSessions =
     *     loadPort.findByStatusAndCreatedBefore(SessionStatus.PENDING, threshold);
     * }</pre>
     *
     * @param status 세션 상태 (Not Null)
     * @param createdBefore 이 시간 이전에 생성된 세션 (Not Null)
     * @return Upload Session 목록 (결과 없으면 빈 List, null 아님)
     * @throws IllegalArgumentException status 또는 createdBefore가 null인 경우
     */
    List<UploadSession> findByStatusAndCreatedBefore(
        SessionStatus status,
        LocalDateTime createdBefore
    );

    /**
     * 여러 상태 중 하나에 해당하고 생성 시간 기준으로 Upload Session 목록 조회
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>PENDING 또는 IN_PROGRESS 세션 중 오래된 것 조회</li>
     *   <li>복수 상태 기반 Batch 정리 작업</li>
     * </ul>
     *
     * <p><strong>성능 고려사항:</strong></p>
     * <ul>
     *   <li>⚠️ {@code IN} 절 사용 → Index 활용 제한적</li>
     *   <li>가능하면 단일 상태 조회 메서드 사용 권장</li>
     * </ul>
     *
     * @param statuses 세션 상태 목록 (Not Null, Not Empty)
     * @param createdBefore 이 시간 이전에 생성된 세션 (Not Null)
     * @return Upload Session 목록 (결과 없으면 빈 List, null 아님)
     * @throws IllegalArgumentException statuses가 null 또는 빈 리스트인 경우
     * @throws IllegalArgumentException createdBefore가 null인 경우
     */
    List<UploadSession> findByStatusInAndCreatedBefore(
        List<SessionStatus> statuses,
        LocalDateTime createdBefore
    );

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
}
