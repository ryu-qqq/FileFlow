package com.ryuqq.fileflow.application.download.dto.response;

/**
 * 콜백 알림 요청 바디.
 *
 * <p>받는 쪽에서 원본 이미지를 교체할 수 있도록 다운로드 결과 정보를 포함합니다.
 *
 * @param downloadTaskId 다운로드 태스크 ID
 * @param assetId 생성된 Asset ID (COMPLETED일 때만 유의미)
 * @param status 다운로드 결과 상태 (COMPLETED / FAILED)
 * @param sourceUrl 원본 다운로드 요청 URL
 * @param s3Key 저장된 S3 객체 키 (COMPLETED일 때만 유의미)
 * @param bucket S3 버킷명
 * @param fileName 저장된 파일명
 * @param contentType 파일 MIME 타입
 * @param fileSize 파일 크기 (bytes)
 * @param errorMessage 실패 사유 (FAILED일 때만 유의미)
 */
public record CallbackPayload(
        String downloadTaskId,
        String assetId,
        String status,
        String sourceUrl,
        String s3Key,
        String bucket,
        String fileName,
        String contentType,
        long fileSize,
        String errorMessage) {

    public static CallbackPayload ofCompleted(
            String downloadTaskId,
            String assetId,
            String sourceUrl,
            String s3Key,
            String bucket,
            String fileName,
            String contentType,
            long fileSize) {
        return new CallbackPayload(
                downloadTaskId,
                assetId,
                "COMPLETED",
                sourceUrl,
                s3Key,
                bucket,
                fileName,
                contentType,
                fileSize,
                null);
    }

    public static CallbackPayload ofFailed(
            String downloadTaskId, String sourceUrl, String errorMessage) {
        return new CallbackPayload(
                downloadTaskId, null, "FAILED", sourceUrl, null, null, null, null, 0, errorMessage);
    }
}
