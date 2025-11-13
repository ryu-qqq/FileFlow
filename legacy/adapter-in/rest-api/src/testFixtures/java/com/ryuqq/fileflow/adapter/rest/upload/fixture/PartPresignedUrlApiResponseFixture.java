package com.ryuqq.fileflow.adapter.rest.upload.fixture;

import com.ryuqq.fileflow.adapter.rest.upload.dto.response.PartPresignedUrlApiResponse;

/**
 * PartPresignedUrlApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see PartPresignedUrlApiResponse
 */
public class PartPresignedUrlApiResponseFixture {

    /**
     * 기본값으로 PartPresignedUrlApiResponse 생성 (Part 1, 1시간 만료)
     *
     * @return 기본값을 가진 PartPresignedUrlApiResponse
     */
    public static PartPresignedUrlApiResponse create() {
        return PartPresignedUrlApiResponse.of(
            1,
            "https://s3.amazonaws.com/bucket/key?partNumber=1&X-Amz-Signature=...",
            3600L
        );
    }

    /**
     * 특정 파트 번호로 PartPresignedUrlApiResponse 생성
     *
     * @param partNumber 파트 번호
     * @return 지정된 파트 번호를 가진 PartPresignedUrlApiResponse
     */
    public static PartPresignedUrlApiResponse createWithPartNumber(Integer partNumber) {
        return PartPresignedUrlApiResponse.of(
            partNumber,
            "https://s3.amazonaws.com/bucket/key?partNumber=" + partNumber + "&X-Amz-Signature=...",
            3600L
        );
    }

    /**
     * 특정 만료 시간으로 PartPresignedUrlApiResponse 생성
     *
     * @param expiresInSeconds 만료 시간 (초)
     * @return 지정된 만료 시간을 가진 PartPresignedUrlApiResponse
     */
    public static PartPresignedUrlApiResponse createWithExpiration(Long expiresInSeconds) {
        return PartPresignedUrlApiResponse.of(
            1,
            "https://s3.amazonaws.com/bucket/key?partNumber=1&X-Amz-Signature=...",
            expiresInSeconds
        );
    }

    /**
     * 모든 필드를 지정하여 PartPresignedUrlApiResponse 생성
     *
     * @param partNumber 파트 번호
     * @param presignedUrl Presigned URL
     * @param expiresInSeconds 만료 시간 (초)
     * @return PartPresignedUrlApiResponse
     */
    public static PartPresignedUrlApiResponse createWith(
        Integer partNumber,
        String presignedUrl,
        Long expiresInSeconds
    ) {
        return PartPresignedUrlApiResponse.of(partNumber, presignedUrl, expiresInSeconds);
    }

    // Private 생성자
    private PartPresignedUrlApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
