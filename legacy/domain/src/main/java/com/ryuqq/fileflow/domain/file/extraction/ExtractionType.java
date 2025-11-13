package com.ryuqq.fileflow.domain.file.extraction;

/**
 * Extraction Type Enum
 * 데이터 추출 유형
 *
 * <p><strong>추출 유형:</strong></p>
 * <ul>
 *   <li>METADATA: 파일 메타데이터 (EXIF, 비디오 정보, 문서 속성)</li>
 *   <li>OCR: 이미지 텍스트 추출</li>
 *   <li>FORM: 양식 데이터 추출</li>
 *   <li>HTML: HTML 구조화 데이터 추출</li>
 *   <li>TABLE: 표 데이터 추출</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum ExtractionType {
    /**
     * 파일 메타데이터 추출
     * 예: EXIF (이미지), Video Info (비디오), Document Properties (문서)
     */
    METADATA,

    /**
     * OCR (Optical Character Recognition)
     * 이미지에서 텍스트 추출
     */
    OCR,

    /**
     * 양식 데이터 추출
     * PDF 양식, 온라인 폼 등
     */
    FORM,

    /**
     * HTML 구조화 데이터 추출
     * 웹 페이지 스크래핑
     */
    HTML,

    /**
     * 표 데이터 추출
     * Excel, CSV, PDF 테이블 등
     */
    TABLE
}
