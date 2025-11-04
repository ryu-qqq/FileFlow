package com.ryuqq.fileflow.adapter.out.persistence.mysql.download.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * External Download JPA Repository
 *
 * <p>Spring Data JPA를 활용한 External Download 영속성 인터페이스</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 메서드 네이밍 규칙 준수</li>
 *   <li>✅ 복잡한 쿼리는 @Query 사용</li>
 *   <li>✅ Long FK 전략 (엔티티 조인 없음)</li>
 *   <li>❌ N+1 문제 발생 가능한 패턴 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Repository
public interface ExternalDownloadJpaRepository
    extends JpaRepository<ExternalDownloadJpaEntity, Long> {

    /**
     * Upload Session ID로 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return ExternalDownloadJpaEntity (Optional)
     */
    Optional<ExternalDownloadJpaEntity> findByUploadSessionId(Long uploadSessionId);

    /**
     * 상태별 조회
     *
     * @param status External Download 상태
     * @return ExternalDownloadJpaEntity 목록
     */
    List<ExternalDownloadJpaEntity> findByStatus(
        ExternalDownloadStatus status
    );

    /**
     * 재시도 가능한 실패 건 조회
     * (DOWNLOADING 상태이면서 재시도 횟수가 최대값 미만)
     *
     * @param maxRetry 최대 재시도 횟수
     * @param retryAfter 재시도 가능 시간
     * @return ExternalDownloadJpaEntity 목록
     */
    @Query("SELECT e FROM ExternalDownloadJpaEntity e " +
           "WHERE e.status = 'DOWNLOADING' " +
           "AND e.retryCount < :maxRetry " +
           "AND (e.lastRetryAt IS NULL OR e.lastRetryAt < :retryAfter)")
    List<ExternalDownloadJpaEntity> findRetryableDownloads(
        @Param("maxRetry") Integer maxRetry,
        @Param("retryAfter") LocalDateTime retryAfter
    );

    /**
     * 상태별 개수 조회
     *
     * @param status External Download 상태
     * @return 개수
     */
    long countByStatus(ExternalDownloadStatus status);

    /**
     * Upload Session ID 목록으로 조회
     *
     * @param uploadSessionIds Upload Session ID 목록
     * @return ExternalDownloadJpaEntity 목록
     */
    @Query("SELECT e FROM ExternalDownloadJpaEntity e " +
           "WHERE e.uploadSessionId IN :uploadSessionIds")
    List<ExternalDownloadJpaEntity> findByUploadSessionIds(
        @Param("uploadSessionIds") List<Long> uploadSessionIds
    );

    /**
     * 특정 시간 이전에 생성된 특정 상태의 다운로드 조회
     *
     * @param status 상태
     * @param createdBefore 생성 시간 기준
     * @return ExternalDownloadJpaEntity 목록
     */
    List<ExternalDownloadJpaEntity> findByStatusAndCreatedAtBefore(
        ExternalDownloadStatus status,
        LocalDateTime createdBefore
    );
}
