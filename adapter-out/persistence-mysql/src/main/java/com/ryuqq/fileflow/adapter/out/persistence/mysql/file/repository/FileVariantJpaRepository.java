package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileVariantJpaEntity;
import com.ryuqq.fileflow.domain.file.variant.VariantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * FileVariant JPA Repository
 *
 * <p><strong>역할</strong>: FileVariant 엔티티의 DB 접근 인터페이스</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스</li>
 *   <li>✅ Long FK 전략 (parentFileAssetId로 조회)</li>
 *   <li>✅ QueryDSL 대신 @Query 사용 (단순 조회)</li>
 *   <li>❌ Native Query 최소화</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface FileVariantJpaRepository extends JpaRepository<FileVariantJpaEntity, Long> {

    /**
     * Parent FileAsset ID로 모든 Variant 조회
     *
     * @param parentFileAssetId Parent FileAsset ID
     * @return Variant 목록
     */
    @Query("SELECT fv FROM FileVariantJpaEntity fv " +
           "WHERE fv.parentFileAssetId = :parentFileAssetId " +
           "ORDER BY fv.createdAt DESC")
    List<FileVariantJpaEntity> findByParentFileAssetId(@Param("parentFileAssetId") Long parentFileAssetId);

    /**
     * Parent FileAsset ID와 Variant Type으로 조회
     *
     * @param parentFileAssetId Parent FileAsset ID
     * @param variantType Variant Type
     * @return Variant (있으면)
     */
    @Query("SELECT fv FROM FileVariantJpaEntity fv " +
           "WHERE fv.parentFileAssetId = :parentFileAssetId " +
           "AND fv.variantType = :variantType")
    Optional<FileVariantJpaEntity> findByParentFileAssetIdAndVariantType(
        @Param("parentFileAssetId") Long parentFileAssetId,
        @Param("variantType") VariantType variantType
    );

    /**
     * Parent FileAsset ID에 특정 Variant Type이 존재하는지 확인
     *
     * @param parentFileAssetId Parent FileAsset ID
     * @param variantType Variant Type
     * @return 존재 여부
     */
    @Query("SELECT COUNT(fv) > 0 FROM FileVariantJpaEntity fv " +
           "WHERE fv.parentFileAssetId = :parentFileAssetId " +
           "AND fv.variantType = :variantType")
    boolean existsByParentFileAssetIdAndVariantType(
        @Param("parentFileAssetId") Long parentFileAssetId,
        @Param("variantType") VariantType variantType
    );
}
