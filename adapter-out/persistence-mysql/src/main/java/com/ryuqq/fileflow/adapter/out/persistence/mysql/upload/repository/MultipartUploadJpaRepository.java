package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.MultipartUploadJpaEntity;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Multipart Upload JPA Repository
 *
 * <p>Spring Data JPA를 활용한 Multipart Upload 영속성 인터페이스</p>
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
public interface MultipartUploadJpaRepository
    extends JpaRepository<MultipartUploadJpaEntity, Long> {

    /**
     * 업로드 세션 ID로 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return MultipartUploadJpaEntity (Optional)
     */
    Optional<MultipartUploadJpaEntity> findByUploadSessionId(Long uploadSessionId);

    /**
     * 상태별 조회
     *
     * @param status Multipart 상태
     * @return MultipartUploadJpaEntity 목록
     */
    List<MultipartUploadJpaEntity> findByStatus(MultipartUpload.MultipartStatus status);

    /**
     * 세션 ID 목록으로 조회
     *
     * @param sessionIds Upload Session ID 목록
     * @return MultipartUploadJpaEntity 목록
     */
    @Query("SELECT m FROM MultipartUploadJpaEntity m " +
           "WHERE m.uploadSessionId IN :sessionIds")
    List<MultipartUploadJpaEntity> findByUploadSessionIds(
        @Param("sessionIds") List<Long> sessionIds
    );

    /**
     * Provider Upload ID로 조회
     *
     * @param providerUploadId Provider Upload ID
     * @return MultipartUploadJpaEntity (Optional)
     */
    Optional<MultipartUploadJpaEntity> findByProviderUploadId(String providerUploadId);

    /**
     * 상태별 개수 조회
     *
     * @param status Multipart 상태
     * @return 개수
     */
    long countByStatus(MultipartUpload.MultipartStatus status);
}
