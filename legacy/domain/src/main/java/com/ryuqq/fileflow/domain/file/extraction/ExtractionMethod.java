package com.ryuqq.fileflow.domain.file.extraction;

/**
 * Extraction Method Enum
 * 데이터 추출 방법/엔진
 *
 * <p><strong>추출 엔진:</strong></p>
 * <ul>
 *   <li>TIKA: Apache Tika (메타데이터, 텍스트)</li>
 *   <li>TEXTRACT: AWS Textract (OCR, Form, Table)</li>
 *   <li>TESSERACT: Tesseract OCR Engine</li>
 *   <li>CUSTOM: 커스텀 추출 로직</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum ExtractionMethod {
    /**
     * Apache Tika
     * 메타데이터 및 텍스트 추출
     */
    TIKA,

    /**
     * AWS Textract
     * OCR, 양식, 표 추출 (고급 AI 기반)
     */
    TEXTRACT,

    /**
     * Tesseract OCR
     * 오픈소스 OCR 엔진
     */
    TESSERACT,

    /**
     * 커스텀 추출 로직
     * 프로젝트 특화 추출 방법
     */
    CUSTOM
}
