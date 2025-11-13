package com.ryuqq.fileflow.application.upload.dto.command;

/**
 * 완료된 파트 정보 Command
 *
 * <p>S3 Multipart Upload에서 완료된 파트 정보를 나타냅니다.</p>
 *
 * @param partNumber 파트 번호
 * @param etag ETag
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record CompletedPartCommand(
    Integer partNumber,
    String etag
) {
    /**
     * Static Factory Method
     *
     * @param partNumber 파트 번호
     * @param etag ETag
     * @return CompletedPartCommand
     */
    public static CompletedPartCommand of(Integer partNumber, String etag) {
        return new CompletedPartCommand(partNumber, etag);
    }
}
