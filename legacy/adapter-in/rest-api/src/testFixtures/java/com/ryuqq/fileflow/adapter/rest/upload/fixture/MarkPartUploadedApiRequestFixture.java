package com.ryuqq.fileflow.adapter.rest.upload.fixture;

import com.ryuqq.fileflow.adapter.rest.upload.dto.request.MarkPartUploadedApiRequest;

/**
 * MarkPartUploadedApiRequest 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see MarkPartUploadedApiRequest
 */
public class MarkPartUploadedApiRequestFixture {

    /**
     * 기본값으로 MarkPartUploadedApiRequest 생성 (5MB 파트)
     *
     * @return 기본값을 가진 MarkPartUploadedApiRequest
     */
    public static MarkPartUploadedApiRequest create() {
        return new MarkPartUploadedApiRequest(
            "\"d41d8cd98f00b204e9800998ecf8427e\"",
            5242880L
        );
    }

    /**
     * 특정 ETag로 MarkPartUploadedApiRequest 생성
     *
     * @param etag S3 ETag
     * @return 지정된 ETag를 가진 MarkPartUploadedApiRequest
     */
    public static MarkPartUploadedApiRequest createWithEtag(String etag) {
        return new MarkPartUploadedApiRequest(
            etag,
            5242880L
        );
    }

    /**
     * 특정 파트 크기로 MarkPartUploadedApiRequest 생성
     *
     * @param partSize 파트 크기 (bytes)
     * @return 지정된 파트 크기를 가진 MarkPartUploadedApiRequest
     */
    public static MarkPartUploadedApiRequest createWithPartSize(Long partSize) {
        return new MarkPartUploadedApiRequest(
            "\"d41d8cd98f00b204e9800998ecf8427e\"",
            partSize
        );
    }

    /**
     * 모든 필드를 지정하여 MarkPartUploadedApiRequest 생성
     *
     * @param etag S3 ETag
     * @param partSize 파트 크기 (bytes)
     * @return MarkPartUploadedApiRequest
     */
    public static MarkPartUploadedApiRequest createWith(String etag, Long partSize) {
        return new MarkPartUploadedApiRequest(etag, partSize);
    }

    // Private 생성자
    private MarkPartUploadedApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
