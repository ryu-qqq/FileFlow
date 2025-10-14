package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.domain.file.FileRelationship;

import java.util.List;

/**
 * FileRelationship 저장 Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * FileRelationship을 영구 저장소에 저장하는 역할을 정의합니다.
 *
 * 사용 사례:
 * - 썸네일 생성 후 원본-썸네일 관계 저장
 * - 이미지 최적화 후 원본-최적화본 관계 저장
 * - 파일 변환 후 원본-변환본 관계 저장
 *
 * @author sangwon-ryu
 */
public interface SaveFileRelationshipPort {

    /**
     * FileRelationship을 저장합니다.
     *
     * @param fileRelationship 저장할 파일 관계
     * @return 저장된 FileRelationship (ID 포함)
     */
    FileRelationship save(FileRelationship fileRelationship);

    /**
     * 여러 FileRelationship을 일괄 저장합니다.
     * 배치 썸네일 생성 시 여러 관계를 한 번에 저장할 때 사용합니다.
     *
     * @param fileRelationships 저장할 파일 관계 목록
     * @return 저장된 FileRelationship 목록 (ID 포함)
     */
    List<FileRelationship> saveAll(List<FileRelationship> fileRelationships);
}
