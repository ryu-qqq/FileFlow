package com.ryuqq.fileflow.adapter.rest.upload.fixture;

import com.ryuqq.fileflow.adapter.rest.upload.dto.response.CompleteSingleUploadApiResponse;

/**
 * CompleteSingleUploadApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see CompleteSingleUploadApiResponse
 */
public class CompleteSingleUploadApiResponseFixture {

    /**
     * 기본값으로 CompleteSingleUploadApiResponse 생성
     *
     * @return 기본값을 가진 CompleteSingleUploadApiResponse
     */
    public static CompleteSingleUploadApiResponse create() {
        return new CompleteSingleUploadApiResponse(
            12345L,
            "\"d41d8cd98f00b204e9800998ecf8427e\"",
            10485760L
        );
    }

    /**
     * 특정 File ID로 CompleteSingleUploadApiResponse 생성
     *
     * @param fileId 파일 ID
     * @return 지정된 File ID를 가진 CompleteSingleUploadApiResponse
     */
    public static CompleteSingleUploadApiResponse createWithFileId(Long fileId) {
        return new CompleteSingleUploadApiResponse(
            fileId,
            "\"d41d8cd98f00b204e9800998ecf8427e\"",
            10485760L
        );
    }

    /**
     * 특정 파일 크기로 CompleteSingleUploadApiResponse 생성
     *
     * @param fileSize 파일 크기 (bytes)
     * @return 지정된 파일 크기를 가진 CompleteSingleUploadApiResponse
     */
    public static CompleteSingleUploadApiResponse createWithFileSize(Long fileSize) {
        return new CompleteSingleUploadApiResponse(
            12345L,
            "\"d41d8cd98f00b204e9800998ecf8427e\"",
            fileSize
        );
    }

    /**
     * 모든 필드를 지정하여 CompleteSingleUploadApiResponse 생성
     *
     * @param fileId 파일 ID
     * @param etag S3 ETag
     * @param fileSize 파일 크기 (bytes)
     * @return CompleteSingleUploadApiResponse
     */
    public static CompleteSingleUploadApiResponse createWith(
        Long fileId,
        String etag,
        Long fileSize
    ) {
        return new CompleteSingleUploadApiResponse(fileId, etag, fileSize);
    }

    // Private 생성자
    private CompleteSingleUploadApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
