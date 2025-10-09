package com.ryuqq.fileflow.domain.upload.exception;

/**
 * 체크섬 불일치 예외
 *
 * 클라이언트가 제공한 ETag와 S3에 저장된 ETag가 일치하지 않을 때 발생합니다.
 */
public class ChecksumMismatchException extends RuntimeException {

    private final String sessionId;
    private final String expectedEtag;
    private final String actualEtag;

    public ChecksumMismatchException(String sessionId, String expectedEtag, String actualEtag) {
        super(String.format(
                "Checksum mismatch. SessionId: %s, Expected ETag: %s, Actual ETag: %s",
                sessionId, expectedEtag, actualEtag
        ));
        this.sessionId = sessionId;
        this.expectedEtag = expectedEtag;
        this.actualEtag = actualEtag;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getExpectedEtag() {
        return expectedEtag;
    }

    public String getActualEtag() {
        return actualEtag;
    }
}
