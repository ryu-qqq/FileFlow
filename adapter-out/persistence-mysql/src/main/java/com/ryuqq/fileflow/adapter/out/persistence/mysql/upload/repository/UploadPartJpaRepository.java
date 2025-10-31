package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadPartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Upload Part JPA Repository
 *
 * <p>Spring Data JPA를 활용한 Upload Part 영속성 인터페이스</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 메서드 네이밍 규칙 준수</li>
 *   <li>✅ 복잡한 쿼리는 @Query 사용</li>
 *   <li>✅ Long FK 전략 (엔티티 조인 없음)</li>
 *   <li>✅ Bulk 삭제는 @Modifying 사용</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Repository
public interface UploadPartJpaRepository
    extends JpaRepository<UploadPartJpaEntity, Long> {

    /**
     * Multipart Upload ID로 조회
     *
     * @param multipartUploadId Multipart Upload ID
     * @return UploadPartJpaEntity 목록
     */
    List<UploadPartJpaEntity> findByMultipartUploadId(Long multipartUploadId);

    /**
     * Multipart Upload ID와 파트 번호로 조회
     *
     * @param multipartUploadId Multipart Upload ID
     * @param partNumber 파트 번호
     * @return UploadPartJpaEntity (Optional)
     */
    Optional<UploadPartJpaEntity> findByMultipartUploadIdAndPartNumber(
        Long multipartUploadId,
        Integer partNumber
    );

    /**
     * Multipart Upload ID로 삭제
     *
     * @param multipartUploadId Multipart Upload ID
     */
    @Modifying
    @Query("DELETE FROM UploadPartJpaEntity p " +
           "WHERE p.multipartUploadId = :multipartUploadId")
    void deleteByMultipartUploadId(@Param("multipartUploadId") Long multipartUploadId);

    /**
     * Multipart Upload ID로 파트 개수 조회
     *
     * @param multipartUploadId Multipart Upload ID
     * @return 파트 개수
     */
    long countByMultipartUploadId(Long multipartUploadId);

    /**
     * Multipart Upload ID 목록으로 조회
     *
     * @param multipartUploadIds Multipart Upload ID 목록
     * @return UploadPartJpaEntity 목록
     */
    @Query("SELECT p FROM UploadPartJpaEntity p " +
           "WHERE p.multipartUploadId IN :multipartUploadIds " +
           "ORDER BY p.multipartUploadId, p.partNumber")
    List<UploadPartJpaEntity> findByMultipartUploadIds(
        @Param("multipartUploadIds") List<Long> multipartUploadIds
    );
}
