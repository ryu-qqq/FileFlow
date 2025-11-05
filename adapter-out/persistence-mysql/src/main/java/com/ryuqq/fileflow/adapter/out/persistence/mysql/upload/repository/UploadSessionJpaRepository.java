package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadSessionJpaEntity;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Upload Session JPA Repository
 *
 * <p>Spring Data JPA Repository for {@link UploadSessionJpaEntity}</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Upload Session의 CRUD 작업</li>
 *   <li>Session Key, 상태, 생성 시간 기준 조회</li>
 *   <li>Spring Data JPA가 구현체 자동 생성</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Repository Interface만 정의 (구현체는 Spring Data JPA가 생성)</li>
 *   <li>✅ 도메인 용어 사용 (findBySessionKey, findByStatus)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 없음)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Repository
public interface UploadSessionJpaRepository extends JpaRepository<UploadSessionJpaEntity, Long> {

    /**
     * Session Key로 Upload Session 조회
     *
     * @param sessionKey Session Key
     * @return Upload Session (Optional)
     */
    Optional<UploadSessionJpaEntity> findBySessionKey(String sessionKey);

    /**
     * 상태와 생성 시간 기준으로 Upload Session 목록 조회
     *
     * <p>만료된 세션 정리 등에 사용됩니다.</p>
     *
     * @param status 세션 상태
     * @param createdBefore 이 시간 이전에 생성된 세션
     * @return Upload Session 목록
     */
    List<UploadSessionJpaEntity> findByStatusAndCreatedAtBefore(
        SessionStatus status,
        LocalDateTime createdBefore
    );

    /**
     * 상태별 Upload Session 목록 조회
     *
     * @param status 세션 상태
     * @return Upload Session 목록
     */
    List<UploadSessionJpaEntity> findByStatus(SessionStatus status);
}
