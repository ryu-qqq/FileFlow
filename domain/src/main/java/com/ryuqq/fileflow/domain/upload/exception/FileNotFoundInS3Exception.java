package com.ryuqq.fileflow.domain.upload.exception;

/**
 * S3에서 파일을 찾을 수 없을 때 발생하는 예외
 *
 * 클라이언트가 업로드 완료를 알렸으나 S3에 파일이 실제로 존재하지 않는 경우 발생합니다.
 */
public class FileNotFoundInS3Exception extends RuntimeException {

    private final String sessionId;
    private final String s3Bucket;
    private final String s3Key;

    public FileNotFoundInS3Exception(String sessionId, String s3Bucket, String s3Key) {
        super(String.format(
                "File not found in S3. SessionId: %s, Bucket: %s, Key: %s",
                sessionId, s3Bucket, s3Key
        ));
        this.sessionId = sessionId;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }
}
