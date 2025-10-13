package com.ryuqq.fileflow.application.upload.dto;

/**
 * 업로드 완료 확인 Command
 *
 * 클라이언트가 S3에 파일 업로드를 완료한 후 서버에 알리기 위한 Command 객체입니다.
 * S3 Event보다 먼저 도착할 수 있으므로, 이 요청으로 업로드 상태를 먼저 업데이트합니다.
 *
 * @param sessionId 업로드 세션 ID
 * @param uploadPath S3 업로드 경로 (Presigned URL 응답에 포함된 uploadPath)
 * @param etag S3 ETag (선택적 - 제공시 검증 수행)
 * @author sangwon-ryu
 */
public record ConfirmUploadCommand(
        String sessionId,
        String uploadPath,
        String etag
) {
    /**
     * Compact constructor로 검증 로직 수행
     */
    public ConfirmUploadCommand {
        validateSessionId(sessionId);
        validateUploadPath(uploadPath);
        // etag는 선택적이므로 null 허용
    }

    /**
     * ETag 없이 Command를 생성합니다.
     *
     * @param sessionId 세션 ID
     * @param uploadPath S3 업로드 경로
     * @return ConfirmUploadCommand 인스턴스
     */
    public static ConfirmUploadCommand withoutEtag(String sessionId, String uploadPath) {
        return new ConfirmUploadCommand(sessionId, uploadPath, null);
    }

    /**
     * ETag와 함께 Command를 생성합니다.
     *
     * @param sessionId 세션 ID
     * @param uploadPath S3 업로드 경로
     * @param etag S3 ETag
     * @return ConfirmUploadCommand 인스턴스
     */
    public static ConfirmUploadCommand withEtag(String sessionId, String uploadPath, String etag) {
        return new ConfirmUploadCommand(sessionId, uploadPath, etag);
    }

    /**
     * ETag가 제공되었는지 확인합니다.
     *
     * @return ETag 존재 여부
     */
    public boolean hasEtag() {
        return etag != null && !etag.trim().isEmpty();
    }

    // ========== Validation Methods ==========

    private static void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null or empty");
        }
    }

    private static void validateUploadPath(String uploadPath) {
        if (uploadPath == null || uploadPath.trim().isEmpty()) {
            throw new IllegalArgumentException("UploadPath cannot be null or empty");
        }
    }
}
