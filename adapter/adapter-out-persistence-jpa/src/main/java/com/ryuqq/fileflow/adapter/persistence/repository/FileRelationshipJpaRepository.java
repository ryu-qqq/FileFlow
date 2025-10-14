package com.ryuqq.fileflow.adapter.persistence.repository;

import com.ryuqq.fileflow.adapter.persistence.entity.FileRelationshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FileRelationshipEntity JPA Repository
 *
 * 역할:
 * - FileRelationshipEntity의 CRUD 작업 담당
 * - 파일 관계 조회 및 저장
 *
 * 주요 쿼리:
 * - 원본 파일 ID로 모든 관련 파일 조회
 * - 대상 파일 ID로 원본 파일 조회
 * - 관계 유형별 조회
 *
 * @author sangwon-ryu
 */
@Repository
public interface FileRelationshipJpaRepository extends JpaRepository<FileRelationshipEntity, Long> {

    /**
     * 원본 파일 ID로 모든 파일 관계를 조회합니다.
     * 예: 원본 이미지의 모든 썸네일과 변환본 조회
     *
     * @param sourceFileId 원본 파일 ID
     * @return 파일 관계 목록
     */
    List<FileRelationshipEntity> findBySourceFileId(String sourceFileId);

    /**
     * 대상 파일 ID로 모든 파일 관계를 조회합니다.
     * 예: 썸네일의 원본 이미지 조회
     *
     * @param targetFileId 대상 파일 ID
     * @return 파일 관계 목록
     */
    List<FileRelationshipEntity> findByTargetFileId(String targetFileId);

    /**
     * 원본 파일 ID와 관계 유형으로 파일 관계를 조회합니다.
     * 예: 원본 이미지의 모든 THUMBNAIL 관계만 조회
     *
     * @param sourceFileId 원본 파일 ID
     * @param relationshipType 관계 유형
     * @return 파일 관계 목록
     */
    List<FileRelationshipEntity> findBySourceFileIdAndRelationshipType(
            String sourceFileId,
            FileRelationshipEntity.FileRelationshipTypeEntity relationshipType
    );

    /**
     * 특정 파일이 포함된 모든 관계를 조회합니다 (원본 또는 대상으로).
     *
     * @param fileId 파일 ID
     * @return 파일 관계 목록
     */
    @Query("SELECT fr FROM FileRelationshipEntity fr " +
           "WHERE fr.sourceFileId = :fileId OR fr.targetFileId = :fileId")
    List<FileRelationshipEntity> findAllByFileId(@Param("fileId") String fileId);

    /**
     * 특정 원본-대상 파일 쌍의 관계가 존재하는지 확인합니다.
     *
     * @param sourceFileId 원본 파일 ID
     * @param targetFileId 대상 파일 ID
     * @param relationshipType 관계 유형
     * @return 존재 여부
     */
    boolean existsBySourceFileIdAndTargetFileIdAndRelationshipType(
            String sourceFileId,
            String targetFileId,
            FileRelationshipEntity.FileRelationshipTypeEntity relationshipType
    );
}
