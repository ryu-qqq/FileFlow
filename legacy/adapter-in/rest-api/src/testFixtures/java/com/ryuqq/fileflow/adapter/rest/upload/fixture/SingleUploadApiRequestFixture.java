package com.ryuqq.fileflow.adapter.rest.upload.fixture;

import com.ryuqq.fileflow.adapter.rest.upload.dto.request.SingleUploadApiRequest;

/**
 * SingleUploadApiRequest 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see SingleUploadApiRequest
 */
public class SingleUploadApiRequestFixture {

    /**
     * 기본값으로 SingleUploadApiRequest 생성 (10MB 파일)
     *
     * @return 기본값을 가진 SingleUploadApiRequest
     */
    public static SingleUploadApiRequest create() {
        return new SingleUploadApiRequest(
            "document.pdf",
            10485760L,
            "application/pdf",
            "d41d8cd98f00b204e9800998ecf8427e"
        );
    }

    /**
     * 특정 파일명으로 SingleUploadApiRequest 생성
     *
     * @param fileName 파일명
     * @return 지정된 파일명을 가진 SingleUploadApiRequest
     */
    public static SingleUploadApiRequest createWithFileName(String fileName) {
        return new SingleUploadApiRequest(
            fileName,
            10485760L,
            "application/pdf",
            "d41d8cd98f00b204e9800998ecf8427e"
        );
    }

    /**
     * 이미지 파일 SingleUploadApiRequest 생성
     *
     * @return 이미지 파일 SingleUploadApiRequest
     */
    public static SingleUploadApiRequest createImageUpload() {
        return new SingleUploadApiRequest(
            "profile.jpg",
            2097152L,
            "image/jpeg",
            "abc123def456"
        );
    }

    /**
     * 모든 필드를 지정하여 SingleUploadApiRequest 생성
     *
     * @param fileName 파일명
     * @param fileSize 파일 크기 (bytes)
     * @param contentType 콘텐츠 타입
     * @param checksum 체크섬
     * @return SingleUploadApiRequest
     */
    public static SingleUploadApiRequest createWith(
        String fileName,
        Long fileSize,
        String contentType,
        String checksum
    ) {
        return new SingleUploadApiRequest(fileName, fileSize, contentType, checksum);
    }

    // Private 생성자
    private SingleUploadApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
