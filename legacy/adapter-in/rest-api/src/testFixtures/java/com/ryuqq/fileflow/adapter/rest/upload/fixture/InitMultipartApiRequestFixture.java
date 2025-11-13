package com.ryuqq.fileflow.adapter.rest.upload.fixture;

import com.ryuqq.fileflow.adapter.rest.upload.dto.request.InitMultipartApiRequest;

/**
 * InitMultipartApiRequest 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see InitMultipartApiRequest
 */
public class InitMultipartApiRequestFixture {

    /**
     * 기본값으로 InitMultipartApiRequest 생성
     *
     * @return 기본값을 가진 InitMultipartApiRequest
     */
    public static InitMultipartApiRequest create() {
        return new InitMultipartApiRequest(
            "large-video.mp4",
            524288000L,
            "video/mp4",
            "d41d8cd98f00b204e9800998ecf8427e"
        );
    }

    /**
     * 특정 파일명으로 InitMultipartApiRequest 생성
     *
     * @param fileName 파일명
     * @return 지정된 파일명을 가진 InitMultipartApiRequest
     */
    public static InitMultipartApiRequest createWithFileName(String fileName) {
        return new InitMultipartApiRequest(
            fileName,
            524288000L,
            "video/mp4",
            "d41d8cd98f00b204e9800998ecf8427e"
        );
    }

    /**
     * 특정 파일 크기로 InitMultipartApiRequest 생성
     *
     * @param fileSize 파일 크기 (bytes)
     * @return 지정된 파일 크기를 가진 InitMultipartApiRequest
     */
    public static InitMultipartApiRequest createWithFileSize(Long fileSize) {
        return new InitMultipartApiRequest(
            "large-video.mp4",
            fileSize,
            "video/mp4",
            "d41d8cd98f00b204e9800998ecf8427e"
        );
    }

    /**
     * 모든 필드를 지정하여 InitMultipartApiRequest 생성
     *
     * @param fileName 파일명
     * @param fileSize 파일 크기 (bytes)
     * @param contentType MIME 타입
     * @param checksum 체크섬
     * @return InitMultipartApiRequest
     */
    public static InitMultipartApiRequest createWith(
        String fileName,
        Long fileSize,
        String contentType,
        String checksum
    ) {
        return new InitMultipartApiRequest(fileName, fileSize, contentType, checksum);
    }

    // Private 생성자
    private InitMultipartApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
