package com.ryuqq.fileflow.application.upload.dto.command;

/**
 * 파트 업로드 완료 Command
 *
 * @param sessionKey 세션 키
 * @param partNumber 파트 번호
 * @param etag ETag
 * @param partSize 파트 크기 (bytes)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record MarkPartUploadedCommand(
    String sessionKey,
    Integer partNumber,
    String etag,
    Long partSize
) {
    /**
     * Static Factory Method
     *
     * @param sessionKey 세션 키
     * @param partNumber 파트 번호
     * @param etag ETag
     * @param partSize 파트 크기
     * @return MarkPartUploadedCommand 인스턴스
     */
    public static MarkPartUploadedCommand of(
        String sessionKey,
        Integer partNumber,
        String etag,
        Long partSize
    ) {
        return new MarkPartUploadedCommand(sessionKey, partNumber, etag, partSize);
    }
}
