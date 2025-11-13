package com.ryuqq.fileflow.domain.upload;

/**
 * 업로드 타입 Enum
 */
public enum UploadType {
    SINGLE,     // 단일 파일 업로드
    MULTIPART   // 대용량 파일 분할 업로드
}
