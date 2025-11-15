package com.ryuqq.fileflow.domain.vo;

/**
 * 파일 가공 작업 타입을 나타내는 Value Object
 */
public enum JobType {
    /**
     * 썸네일 생성 - 이미지 썸네일 자동 생성
     */
    THUMBNAIL_GENERATION,

    /**
     * 이미지 리사이즈 - 이미지 크기 조정
     */
    IMAGE_RESIZE,

    /**
     * 이미지 형식 변환 - PNG/JPEG/WEBP 등 형식 변환
     */
    IMAGE_FORMAT_CONVERSION,

    /**
     * 이미지 압축 - 품질 유지하며 파일 크기 최적화
     */
    IMAGE_COMPRESSION,

    /**
     * HTML 파싱 - HTML 구조 분석 및 데이터 추출
     */
    HTML_PARSING,

    /**
     * HTML to PDF - HTML 페이지를 PDF로 변환
     */
    HTML_TO_PDF,

    /**
     * HTML 스크린샷 - HTML 페이지 캡처
     */
    HTML_SCREENSHOT,

    /**
     * PDF 텍스트 추출 - PDF에서 텍스트 내용 추출
     */
    PDF_TEXT_EXTRACTION,

    /**
     * 문서 변환 - DOCX/HWP 등 문서 형식 변환
     */
    DOCUMENT_CONVERSION,

    /**
     * Excel to CSV - 엑셀 파일을 CSV로 변환
     */
    EXCEL_TO_CSV,

    /**
     * Excel 데이터 추출 - 엑셀 시트 데이터 파싱
     */
    EXCEL_DATA_EXTRACTION
}
