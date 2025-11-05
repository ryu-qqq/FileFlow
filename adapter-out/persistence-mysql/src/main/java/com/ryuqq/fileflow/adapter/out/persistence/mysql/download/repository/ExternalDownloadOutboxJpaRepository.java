package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.domain.download.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * External Download Outbox JPA Repository
 *
 * <p>Spring Data JPA를 활용한 External Download Outbox 영속성 인터페이스</p>
 * <p>Transactional Outbox Pattern을 위한 Repository입니다.</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 메서드 네이밍 규칙 준수</li>
 *   <li>✅ 복잡한 쿼리는 @Query 사용</li>
 *   <li>✅ Long FK 전략 (엔티티 조인 없음)</li>
 *   <li>✅ 멱등성 키 UNIQUE 제약 활용</li>
 *   <li>❌ N+1 문제 발생 가능한 패턴 금지</li>
 * </ul>
 *
 * <h3>Outbox 패턴 쿼리</h3>
 * <ul>
 *   <li>멱등성 키 조회: 중복 이벤트 발행 방지</li>
 *   <li>상태별 조회: PENDING, PROCESSING, COMPLETED, FAILED</li>
 *   <li>처리 대기 건 조회: PENDING 상태의 Outbox 메시지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Repository
public interface ExternalDownloadOutboxJpaRepository
    extends JpaRepository<ExternalDownloadOutboxJpaEntity, Long> {

    /**
     * 멱등성 키로 조회
     *
     * <p>중복 이벤트 발행 방지를 위해 사용합니다.</p>
     * <p>UNIQUE 제약이 있어 최대 1건만 반환됩니다.</p>
     *
     * @param idempotencyKey 멱등성 키
     * @return ExternalDownloadOutboxJpaEntity (Optional)
     */
    Optional<ExternalDownloadOutboxJpaEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * Download ID로 조회
     *
     * @param downloadId External Download ID
     * @return ExternalDownloadOutboxJpaEntity (Optional)
     */
    Optional<ExternalDownloadOutboxJpaEntity> findByDownloadId(Long downloadId);

    /**
     * Upload Session ID로 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return ExternalDownloadOutboxJpaEntity (Optional)
     */
    Optional<ExternalDownloadOutboxJpaEntity> findByUploadSessionId(Long uploadSessionId);

    /**
     * 상태별 조회
     *
     * @param status Outbox 상태
     * @return ExternalDownloadOutboxJpaEntity 목록
     */
    List<ExternalDownloadOutboxJpaEntity> findByStatus(OutboxStatus status);

    /**
     * 상태별 조회 (생성 시간 오름차순)
     *
     * <p>PENDING 상태의 Outbox 메시지를 생성 시간 순으로 조회합니다.</p>
     * <p>비동기 처리 워커에서 사용합니다.</p>
     *
     * @param status Outbox 상태
     * @return ExternalDownloadOutboxJpaEntity 목록 (생성 시간 오름차순)
     */
    List<ExternalDownloadOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status);

    /**
     * 처리 대기 중인 Outbox 메시지 조회 (제한 개수)
     *
     * <p>PENDING 상태의 Outbox 메시지를 생성 시간 순으로 제한된 개수만큼 조회합니다.</p>
     * <p>배치 처리 시 사용합니다.</p>
     *
     * @param status Outbox 상태
     * @param limit 조회 제한 개수
     * @return ExternalDownloadOutboxJpaEntity 목록
     */
    @Query("SELECT o FROM ExternalDownloadOutboxJpaEntity o " +
           "WHERE o.status = :status " +
           "ORDER BY o.createdAt ASC " +
           "LIMIT :limit")
    List<ExternalDownloadOutboxJpaEntity> findPendingOutboxMessages(
        @Param("status") OutboxStatus status,
        @Param("limit") int limit
    );

    /**
     * 재시도 가능한 실패 건 조회
     *
     * <p>FAILED 상태이면서 재시도 횟수가 최대값 미만인 Outbox 메시지를 조회합니다.</p>
     *
     * @param maxRetry 최대 재시도 횟수
     * @return ExternalDownloadOutboxJpaEntity 목록
     */
    @Query("SELECT o FROM ExternalDownloadOutboxJpaEntity o " +
           "WHERE o.status = 'FAILED' " +
           "AND o.retryCount < :maxRetry " +
           "ORDER BY o.createdAt ASC")
    List<ExternalDownloadOutboxJpaEntity> findRetryableOutboxMessages(
        @Param("maxRetry") Integer maxRetry
    );

    /**
     * 특정 시간 이전에 생성된 완료 건 조회
     *
     * <p>COMPLETED 상태이면서 특정 시간 이전에 생성된 Outbox 메시지를 조회합니다.</p>
     * <p>오래된 완료 건 정리(Cleanup) 시 사용합니다.</p>
     *
     * @param status Outbox 상태
     * @param createdBefore 생성 시간 기준
     * @return ExternalDownloadOutboxJpaEntity 목록
     */
    List<ExternalDownloadOutboxJpaEntity> findByStatusAndCreatedAtBefore(
        OutboxStatus status,
        LocalDateTime createdBefore
    );

    /**
     * 특정 시간 이전에 생성된 완료 건 삭제
     *
     * <p>COMPLETED 상태이면서 특정 시간 이전에 생성된 Outbox 메시지를 삭제합니다.</p>
     * <p>오래된 완료 건 정리(Cleanup) 시 사용합니다.</p>
     *
     * @param status Outbox 상태
     * @param createdBefore 생성 시간 기준
     * @return 삭제된 개수
     */
    int deleteByStatusAndCreatedAtBefore(
        OutboxStatus status,
        LocalDateTime createdBefore
    );

    /**
     * 상태별 개수 조회
     *
     * @param status Outbox 상태
     * @return 개수
     */
    long countByStatus(OutboxStatus status);

    /**
     * Download ID 목록으로 조회
     *
     * @param downloadIds External Download ID 목록
     * @return ExternalDownloadOutboxJpaEntity 목록
     */
    @Query("SELECT o FROM ExternalDownloadOutboxJpaEntity o " +
           "WHERE o.downloadId IN :downloadIds")
    List<ExternalDownloadOutboxJpaEntity> findByDownloadIds(
        @Param("downloadIds") List<Long> downloadIds
    );
}
