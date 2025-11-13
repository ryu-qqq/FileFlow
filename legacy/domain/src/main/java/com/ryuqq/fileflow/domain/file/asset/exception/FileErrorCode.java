package com.ryuqq.fileflow.domain.file.asset.exception;

/**
 * File Domain 에러 코드
 *
 * <p><strong>코드 체계:</strong></p>
 * <ul>
 *   <li>FILE-001 ~ FILE-099: FileAsset 관련 에러</li>
 *   <li>FILE-101 ~ FILE-199: FileVariant 관련 에러</li>
 *   <li>FILE-201 ~ FILE-299: ExtractedData 관련 에러</li>
 *   <li>FILE-301 ~ FILE-399: Pipeline 관련 에러</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum FileErrorCode {

    // FileAsset 관련 (001 ~ 099)
    FILE_ASSET_NOT_FOUND("FILE-001", "FileAsset not found"),
    FILE_ASSET_ALREADY_DELETED("FILE-002", "FileAsset already deleted"),
    FILE_ASSET_ACCESS_DENIED("FILE-003", "Access denied to FileAsset"),
    INVALID_FILE_ASSET_STATE("FILE-004", "Invalid FileAsset state"),
    FILE_ASSET_PROCESSING("FILE-005", "FileAsset is still processing"),

    // FileVariant 관련 (101 ~ 199)
    FILE_VARIANT_NOT_FOUND("FILE-101", "FileVariant not found"),
    FILE_VARIANT_GENERATION_FAILED("FILE-102", "FileVariant generation failed"),

    // ExtractedData 관련 (201 ~ 299)
    EXTRACTED_DATA_NOT_FOUND("FILE-201", "ExtractedData not found"),
    METADATA_EXTRACTION_FAILED("FILE-202", "Metadata extraction failed"),

    // Pipeline 관련 (301 ~ 399)
    PIPELINE_EXECUTION_FAILED("FILE-301", "Pipeline execution failed"),
    PIPELINE_TIMEOUT("FILE-302", "Pipeline timeout");

    private final String code;
    private final String defaultMessage;

    FileErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}

