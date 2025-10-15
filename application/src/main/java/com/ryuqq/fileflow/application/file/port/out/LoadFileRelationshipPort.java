package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.domain.file.FileRelationship;
import com.ryuqq.fileflow.domain.upload.vo.FileId;

import java.util.List;

/**
 * FileRelationship 조회 Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * FileRelationship을 영구 저장소에서 조회하는 역할을 정의합니다.
 *
 * 사용 사례:
 * - 원본 파일의 모든 썸네일 조회
 * - 원본 파일의 변환/최적화 버전 조회
 * - 파일 관계 추적 및 분석
 *
 * @author sangwon-ryu
 */
public interface LoadFileRelationshipPort {

    /**
     * 원본 파일 ID로 모든 파일 관계를 조회합니다.
     *
     * 반환되는 관계:
     * - 썸네일 관계 (THUMBNAIL_SMALL, THUMBNAIL_MEDIUM)
     * - 최적화 관계 (OPTIMIZED)
     * - 변환 관계 (CONVERTED)
     * - 기타 파생 관계
     *
     * @param sourceFileId 원본 파일 ID
     * @return 파일 관계 목록 (없으면 빈 리스트)
     */
    List<FileRelationship> findBySourceFileId(FileId sourceFileId);

    /**
     * 대상 파일 ID로 모든 파일 관계를 조회합니다.
     * 주로 역방향 추적이 필요한 경우 사용합니다.
     *
     * @param targetFileId 대상 파일 ID
     * @return 파일 관계 목록 (없으면 빈 리스트)
     */
    List<FileRelationship> findByTargetFileId(FileId targetFileId);

    /**
     * 파일 ID(원본 또는 대상)로 모든 관련 관계를 조회합니다.
     * 파일 삭제 시 Cascade 삭제를 위해 사용됩니다.
     *
     * @param fileId 파일 ID
     * @return 파일이 포함된 모든 관계 목록 (없으면 빈 리스트)
     */
    List<FileRelationship> findByFileId(FileId fileId);
}
