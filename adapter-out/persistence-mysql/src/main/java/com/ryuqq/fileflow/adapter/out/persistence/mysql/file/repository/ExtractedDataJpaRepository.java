package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.ExtractedDataJpaEntity;
import com.ryuqq.fileflow.domain.file.extraction.ExtractionMethod;
import com.ryuqq.fileflow.domain.file.extraction.ExtractionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ExtractedData JPA Repository
 *
 * <p><strong>역할</strong>: ExtractedData 엔티티의 DB 접근 인터페이스</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/repository/</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ Spring Data JPA 인터페이스</li>
 *   <li>✅ Long FK 전략 (fileId로 조회)</li>
 *   <li>✅ QueryDSL 대신 @Query 사용 (단순 조회)</li>
 *   <li>❌ Native Query 최소화</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface ExtractedDataJpaRepository extends JpaRepository<ExtractedDataJpaEntity, Long> {

    /**
     * File ID로 모든 추출 데이터 조회
     *
     * @param fileId File ID
     * @return 추출 데이터 목록
     */
    @Query("SELECT ed FROM ExtractedDataJpaEntity ed " +
           "WHERE ed.fileId = :fileId " +
           "AND ed.deletedAt IS NULL " +
           "ORDER BY ed.extractedAt DESC")
    List<ExtractedDataJpaEntity> findByFileId(@Param("fileId") Long fileId);

    /**
     * File ID와 Extraction Type으로 조회
     *
     * @param fileId File ID
     * @param extractionType 추출 유형
     * @return 추출 데이터 목록
     */
    @Query("SELECT ed FROM ExtractedDataJpaEntity ed " +
           "WHERE ed.fileId = :fileId " +
           "AND ed.extractionType = :extractionType " +
           "AND ed.deletedAt IS NULL " +
           "ORDER BY ed.version DESC")
    List<ExtractedDataJpaEntity> findByFileIdAndExtractionType(
        @Param("fileId") Long fileId,
        @Param("extractionType") ExtractionType extractionType
    );

    /**
     * File ID, Extraction Type, Extraction Method, Version으로 조회 (Business Key)
     *
     * @param fileId File ID
     * @param extractionType 추출 유형
     * @param extractionMethod 추출 방법
     * @param version 버전
     * @return 추출 데이터 (있으면)
     */
    @Query("SELECT ed FROM ExtractedDataJpaEntity ed " +
           "WHERE ed.fileId = :fileId " +
           "AND ed.extractionType = :extractionType " +
           "AND ed.extractionMethod = :extractionMethod " +
           "AND ed.version = :version " +
           "AND ed.deletedAt IS NULL")
    Optional<ExtractedDataJpaEntity> findByBusinessKey(
        @Param("fileId") Long fileId,
        @Param("extractionType") ExtractionType extractionType,
        @Param("extractionMethod") ExtractionMethod extractionMethod,
        @Param("version") Integer version
    );

    /**
     * Extracted UUID로 조회
     *
     * @param extractedUuid Extracted UUID
     * @return 추출 데이터 (있으면)
     */
    @Query("SELECT ed FROM ExtractedDataJpaEntity ed " +
           "WHERE ed.extractedUuid = :extractedUuid " +
           "AND ed.deletedAt IS NULL")
    Optional<ExtractedDataJpaEntity> findByExtractedUuid(@Param("extractedUuid") String extractedUuid);

    /**
     * Trace ID로 조회
     *
     * @param traceId Trace ID
     * @return 추출 데이터 목록
     */
    @Query("SELECT ed FROM ExtractedDataJpaEntity ed " +
           "WHERE ed.traceId = :traceId " +
           "AND ed.deletedAt IS NULL " +
           "ORDER BY ed.extractedAt DESC")
    List<ExtractedDataJpaEntity> findByTraceId(@Param("traceId") String traceId);
}
