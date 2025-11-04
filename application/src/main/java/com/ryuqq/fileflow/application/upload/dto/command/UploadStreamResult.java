package com.ryuqq.fileflow.application.upload.dto.command;

/**
 * 스트림 업로드 결과
 *
 * <p>S3 스트림 업로드 후 반환되는 결과입니다. (External Download용)</p>
 *
 * @param etag ETag
 * @param size 업로드된 파일 크기 (bytes)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record UploadStreamResult(
    String etag,
    Long size
) {
    /**
     * Static Factory Method
     *
     * @param etag ETag
     * @param size 업로드된 파일 크기 (bytes)
     * @return UploadStreamResult
     */
    public static UploadStreamResult of(String etag, Long size) {
        return new UploadStreamResult(etag, size);
    }
}
