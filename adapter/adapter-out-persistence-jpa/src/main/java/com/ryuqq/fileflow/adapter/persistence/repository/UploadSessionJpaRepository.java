package com.ryuqq.fileflow.adapter.persistence.repository;

import com.ryuqq.fileflow.adapter.persistence.entity.UploadSessionEntity;
import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UploadSession JPA Repository
 *
 * 비즈니스 규칙:
 * - sessionId는 UUID 형식의 고유 식별자
 * - idempotencyKey로 중복 요청 방지
 * - 만료된 세션 조회 시 PENDING, UPLOADING 상태만 대상
 *
 * @author sangwon-ryu
 */
@Repository
public interface UploadSessionJpaRepository extends JpaRepository<UploadSessionEntity, Long> {

    /**
     * sessionId로 업로드 세션 조회
     *
     * @param sessionId 세션 ID
     * @return 조회된 세션 (존재하지 않으면 Optional.empty())
     */
    Optional<UploadSessionEntity> findBySessionId(String sessionId);

    /**
     * idempotencyKey로 업로드 세션 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return 조회된 세션 (존재하지 않으면 Optional.empty())
     */
    Optional<UploadSessionEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * sessionId로 세션 존재 여부 확인
     *
     * @param sessionId 세션 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean existsBySessionId(String sessionId);

    /**
     * sessionId로 세션 삭제
     *
     * @param sessionId 세션 ID
     */
    void deleteBySessionId(String sessionId);

    /**
     * 만료된 업로드 세션 목록 조회
     *
     * PENDING 또는 UPLOADING 상태이면서 expiresAt이 현재 시간보다 이전인 세션들을 반환합니다.
     *
     * @param now 현재 시간
     * @return 만료된 세션 목록
     */
    @Query("SELECT u FROM UploadSessionEntity u " +
           "WHERE u.status IN ('PENDING', 'UPLOADING') " +
           "AND u.expiresAt < :now")
    List<UploadSessionEntity> findExpiredSessions(@Param("now") LocalDateTime now);
}
