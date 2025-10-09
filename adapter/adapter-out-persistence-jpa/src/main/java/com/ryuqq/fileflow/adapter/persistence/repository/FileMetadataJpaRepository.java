package com.ryuqq.fileflow.adapter.persistence.repository;

import com.ryuqq.fileflow.adapter.persistence.entity.FileMetadataEntity;
import com.ryuqq.fileflow.domain.file.MetadataType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 파일 메타데이터 JPA Repository
 *
 * 주요 기능:
 * - 파일별 메타데이터 조회
 * - 특정 메타데이터 키 검색
 * - 메타데이터 타입별 필터링
 * - 키-값 쌍 기반 CRUD 연산
 *
 * @author sangwon-ryu
 */
@Repository
public interface FileMetadataJpaRepository extends JpaRepository<FileMetadataEntity, Long> {

    /**
     * 파일 ID와 메타데이터 키로 메타데이터를 조회합니다.
     *
     * @param fileId 파일 ID
     * @param metadataKey 메타데이터 키
     * @return 메타데이터 (Optional)
     */
    Optional<FileMetadataEntity> findByFileIdAndMetadataKey(String fileId, String metadataKey);

    /**
     * 특정 파일의 모든 메타데이터를 조회합니다.
     *
     * @param fileId 파일 ID
     * @return 메타데이터 리스트
     */
    List<FileMetadataEntity> findByFileId(String fileId);

    /**
     * 특정 파일의 특정 타입 메타데이터만 조회합니다.
     *
     * @param fileId 파일 ID
     * @param valueType 메타데이터 타입
     * @return 메타데이터 리스트
     */
    List<FileMetadataEntity> findByFileIdAndValueType(String fileId, MetadataType valueType);

    /**
     * 특정 메타데이터 키를 가진 모든 파일의 메타데이터를 조회합니다.
     * 예: 모든 파일의 "width" 메타데이터 조회
     *
     * @param metadataKey 메타데이터 키
     * @return 메타데이터 리스트
     */
    List<FileMetadataEntity> findByMetadataKey(String metadataKey);

    /**
     * 특정 파일의 메타데이터가 존재하는지 확인합니다.
     *
     * @param fileId 파일 ID
     * @param metadataKey 메타데이터 키
     * @return 존재하면 true
     */
    boolean existsByFileIdAndMetadataKey(String fileId, String metadataKey);

    /**
     * 특정 파일의 모든 메타데이터를 삭제합니다.
     *
     * @param fileId 파일 ID
     * @return 삭제된 레코드 수
     */
    long deleteByFileId(String fileId);

    /**
     * 특정 파일의 특정 키 메타데이터를 삭제합니다.
     *
     * @param fileId 파일 ID
     * @param metadataKey 메타데이터 키
     * @return 삭제된 레코드 수 (0 또는 1)
     */
    long deleteByFileIdAndMetadataKey(String fileId, String metadataKey);

    /**
     * 특정 파일의 메타데이터 개수를 조회합니다.
     *
     * @param fileId 파일 ID
     * @return 메타데이터 개수
     */
    long countByFileId(String fileId);

    /**
     * 특정 메타데이터 키-값 쌍을 가진 파일 ID 목록을 조회합니다.
     * 예: width=1920인 모든 파일 조회
     *
     * @param metadataKey 메타데이터 키
     * @param metadataValue 메타데이터 값
     * @return 파일 ID 리스트
     */
    @Query("SELECT DISTINCT fm.fileId FROM FileMetadataEntity fm " +
            "WHERE fm.metadataKey = :metadataKey AND fm.metadataValue = :metadataValue")
    List<String> findFileIdsByMetadataKeyAndValue(
            @Param("metadataKey") String metadataKey,
            @Param("metadataValue") String metadataValue
    );

    /**
     * 여러 메타데이터 키를 가진 파일의 메타데이터를 조회합니다.
     * Spring Data JPA의 쿼리 메서드 파생 기능을 사용합니다.
     *
     * @param fileId 파일 ID
     * @param metadataKeys 메타데이터 키 리스트
     * @return 메타데이터 리스트
     */
    List<FileMetadataEntity> findByFileIdAndMetadataKeyIn(
            String fileId,
            List<String> metadataKeys
    );
}
