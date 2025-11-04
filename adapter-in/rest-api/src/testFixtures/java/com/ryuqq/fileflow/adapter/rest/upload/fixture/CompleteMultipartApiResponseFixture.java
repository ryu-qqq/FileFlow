package com.ryuqq.fileflow.adapter.rest.upload.fixture;

import com.ryuqq.fileflow.adapter.rest.upload.dto.response.CompleteMultipartApiResponse;

/**
 * CompleteMultipartApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see CompleteMultipartApiResponse
 */
public class CompleteMultipartApiResponseFixture {

    /**
     * 기본값으로 CompleteMultipartApiResponse 생성
     *
     * @return 기본값을 가진 CompleteMultipartApiResponse
     */
    public static CompleteMultipartApiResponse create() {
        return CompleteMultipartApiResponse.of(
            12345L,
            "\"d41d8cd98f00b204e9800998ecf8427e\"",
            "https://s3.amazonaws.com/bucket/uploads/2024/01/01/large-video.mp4"
        );
    }

    /**
     * 특정 File ID로 CompleteMultipartApiResponse 생성
     *
     * @param fileId 파일 ID
     * @return 지정된 File ID를 가진 CompleteMultipartApiResponse
     */
    public static CompleteMultipartApiResponse createWithFileId(Long fileId) {
        return CompleteMultipartApiResponse.of(
            fileId,
            "\"d41d8cd98f00b204e9800998ecf8427e\"",
            "https://s3.amazonaws.com/bucket/uploads/2024/01/01/large-video.mp4"
        );
    }

    /**
     * 특정 ETag로 CompleteMultipartApiResponse 생성
     *
     * @param etag S3 ETag
     * @return 지정된 ETag를 가진 CompleteMultipartApiResponse
     */
    public static CompleteMultipartApiResponse createWithEtag(String etag) {
        return CompleteMultipartApiResponse.of(
            12345L,
            etag,
            "https://s3.amazonaws.com/bucket/uploads/2024/01/01/large-video.mp4"
        );
    }

    /**
     * 모든 필드를 지정하여 CompleteMultipartApiResponse 생성
     *
     * @param fileId 파일 ID
     * @param etag S3 ETag
     * @param location S3 Location URL
     * @return CompleteMultipartApiResponse
     */
    public static CompleteMultipartApiResponse createWith(
        Long fileId,
        String etag,
        String location
    ) {
        return CompleteMultipartApiResponse.of(fileId, etag, location);
    }

    // Private 생성자
    private CompleteMultipartApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
