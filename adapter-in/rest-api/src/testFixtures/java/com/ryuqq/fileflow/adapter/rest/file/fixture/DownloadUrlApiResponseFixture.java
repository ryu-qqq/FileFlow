package com.ryuqq.fileflow.adapter.rest.file.fixture;

import com.ryuqq.fileflow.adapter.rest.file.dto.response.DownloadUrlApiResponse;

import java.time.LocalDateTime;

/**
 * DownloadUrlApiResponse 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see DownloadUrlApiResponse
 */
public class DownloadUrlApiResponseFixture {

    /**
     * 기본값으로 DownloadUrlApiResponse 생성
     *
     * @return 기본값을 가진 DownloadUrlApiResponse
     */
    public static DownloadUrlApiResponse create() {
        return DownloadUrlApiResponse.of(
            1L,
            "test-file.pdf",
            "https://s3.amazonaws.com/bucket/key?X-Amz-Signature=...",
            LocalDateTime.now().plusHours(1)
        );
    }

    /**
     * 특정 File ID로 DownloadUrlApiResponse 생성
     *
     * @param fileId File ID
     * @return 지정된 File ID를 가진 DownloadUrlApiResponse
     */
    public static DownloadUrlApiResponse createWithFileId(Long fileId) {
        return DownloadUrlApiResponse.of(
            fileId,
            "test-file.pdf",
            "https://s3.amazonaws.com/bucket/key?X-Amz-Signature=...",
            LocalDateTime.now().plusHours(1)
        );
    }

    /**
     * 특정 만료 시간으로 DownloadUrlApiResponse 생성
     *
     * @param expiresAt 만료 시간
     * @return 지정된 만료 시간을 가진 DownloadUrlApiResponse
     */
    public static DownloadUrlApiResponse createWithExpiration(LocalDateTime expiresAt) {
        return DownloadUrlApiResponse.of(
            1L,
            "test-file.pdf",
            "https://s3.amazonaws.com/bucket/key?X-Amz-Signature=...",
            expiresAt
        );
    }

    /**
     * 모든 필드를 지정하여 DownloadUrlApiResponse 생성
     *
     * @param fileId File ID
     * @param fileName 파일명
     * @param downloadUrl Presigned Download URL
     * @param expiresAt URL 만료 시간
     * @return DownloadUrlApiResponse
     */
    public static DownloadUrlApiResponse createWith(
        Long fileId,
        String fileName,
        String downloadUrl,
        LocalDateTime expiresAt
    ) {
        return DownloadUrlApiResponse.of(fileId, fileName, downloadUrl, expiresAt);
    }

    // Private 생성자
    private DownloadUrlApiResponseFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
