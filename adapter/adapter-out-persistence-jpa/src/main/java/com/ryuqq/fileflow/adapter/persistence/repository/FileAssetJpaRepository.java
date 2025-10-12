package com.ryuqq.fileflow.adapter.persistence.repository;

import com.ryuqq.fileflow.adapter.persistence.entity.FileAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * FileAsset JPA Repository
 *
 * 비즈니스 규칙:
 * - fileId는 UUID 형식의 고유 식별자
 * - sessionId로 업로드 세션과 연결
 * - deletedAt이 null이 아니면 삭제된 파일 (Soft Delete)
 *
 * @author sangwon-ryu
 */
@Repository
public interface FileAssetJpaRepository extends JpaRepository<FileAssetEntity, Long> {

    /**
     * fileId로 파일 자산 조회
     *
     * @param fileId 파일 ID
     * @return 조회된 파일 자산 (존재하지 않으면 Optional.empty())
     */
    Optional<FileAssetEntity> findByFileId(String fileId);

    /**
     * sessionId로 파일 자산 조회
     *
     * @param sessionId 업로드 세션 ID
     * @return 조회된 파일 자산 (존재하지 않으면 Optional.empty())
     */
    Optional<FileAssetEntity> findBySessionId(String sessionId);

    /**
     * S3 Key로 파일 자산 조회
     *
     * @param s3Key S3 객체 키
     * @return 조회된 파일 자산 (존재하지 않으면 Optional.empty())
     */
    Optional<FileAssetEntity> findByS3Key(String s3Key);

    /**
     * tenantId로 파일 자산 목록 조회 (삭제되지 않은 파일만)
     *
     * @param tenantId 테넌트 ID
     * @return 파일 자산 목록
     */
    @Query("SELECT f FROM FileAssetEntity f WHERE f.tenantId = :tenantId AND f.deletedAt IS NULL")
    List<FileAssetEntity> findByTenantId(@Param("tenantId") String tenantId);

    /**
     * 특정 날짜 이전에 업로드된 파일 목록 조회 (삭제되지 않은 파일만)
     *
     * @param dateTime 기준 날짜
     * @return 파일 자산 목록
     */
    @Query("SELECT f FROM FileAssetEntity f WHERE f.createdAt < :dateTime AND f.deletedAt IS NULL")
    List<FileAssetEntity> findByCreatedAtBefore(@Param("dateTime") LocalDateTime dateTime);
}
