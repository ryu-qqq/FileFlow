package com.ryuqq.fileflow.domain.upload.exception;

/**
 * 업로드 검증 실패 시 발생하는 예외
 */
public class UploadValidationException extends RuntimeException {

    private final ValidationType validationType;

    public UploadValidationException(ValidationType validationType, String message) {
        super(message);
        this.validationType = validationType;
    }

    public ValidationType getValidationType() {
        return validationType;
    }

    /**
     * 검증 실패 유형
     */
    public enum ValidationType {
        /**
         * 파일 크기 제한 초과
         */
        FILE_SIZE_EXCEEDED,

        /**
         * 파일 타입 불일치
         */
        INVALID_FILE_TYPE,

        /**
         * 정책 위반
         */
        POLICY_VIOLATION,

        /**
         * Rate Limit 초과
         */
        RATE_LIMIT_EXCEEDED,

        /**
         * 세션 만료
         */
        SESSION_EXPIRED
    }
}
