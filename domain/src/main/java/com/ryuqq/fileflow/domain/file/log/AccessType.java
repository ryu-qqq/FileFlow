package com.ryuqq.fileflow.domain.file.log;

/**
 * Access Type Enum
 *
 * <p>파일 접근 유형을 정의합니다.</p>
 *
 * <p><strong>접근 유형:</strong></p>
 * <ul>
 *   <li><strong>READ</strong>: 메타데이터 조회 (GET /api/v1/files/{id})</li>
 *   <li><strong>DOWNLOAD</strong>: 다운로드 URL 생성 (POST /api/v1/files/{id}/download)</li>
 *   <li><strong>DELETE</strong>: Soft Delete (DELETE /api/v1/files/{id})</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum AccessType {

    /**
     * 메타데이터 조회
     */
    READ,

    /**
     * 다운로드 URL 생성
     */
    DOWNLOAD,

    /**
     * 파일 삭제
     */
    DELETE
}
