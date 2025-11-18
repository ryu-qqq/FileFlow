package com.ryuqq.fileflow.domain.file.fixture;

import com.ryuqq.fileflow.domain.file.vo.JobType;

/**
 * JobType TestFixture (Object Mother 패턴)
 */
public class JobTypeFixture {

    // 이미지 가공 타입

    /**
     * 썸네일 생성 타입
     */
    public static JobType thumbnailGeneration() {
        return JobType.THUMBNAIL_GENERATION;
    }

    /**
     * 이미지 리사이즈 타입
     */
    public static JobType imageResize() {
        return JobType.IMAGE_RESIZE;
    }

    /**
     * 이미지 형식 변환 타입
     */
    public static JobType imageFormatConversion() {
        return JobType.IMAGE_FORMAT_CONVERSION;
    }

    /**
     * 이미지 압축 타입
     */
    public static JobType imageCompression() {
        return JobType.IMAGE_COMPRESSION;
    }

    // HTML 가공 타입

    /**
     * HTML 파싱 타입
     */
    public static JobType htmlParsing() {
        return JobType.HTML_PARSING;
    }

    /**
     * HTML to PDF 타입
     */
    public static JobType htmlToPdf() {
        return JobType.HTML_TO_PDF;
    }

    /**
     * HTML 스크린샷 타입
     */
    public static JobType htmlScreenshot() {
        return JobType.HTML_SCREENSHOT;
    }

    // 문서 가공 타입

    /**
     * PDF 텍스트 추출 타입
     */
    public static JobType pdfTextExtraction() {
        return JobType.PDF_TEXT_EXTRACTION;
    }

    /**
     * 문서 변환 타입
     */
    public static JobType documentConversion() {
        return JobType.DOCUMENT_CONVERSION;
    }

    // 엑셀 가공 타입

    /**
     * Excel to CSV 타입
     */
    public static JobType excelToCsv() {
        return JobType.EXCEL_TO_CSV;
    }

    /**
     * Excel 데이터 추출 타입
     */
    public static JobType excelDataExtraction() {
        return JobType.EXCEL_DATA_EXTRACTION;
    }
}
