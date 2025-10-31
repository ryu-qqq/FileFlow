package com.ryuqq.fileflow.domain.policy.fixture;

import com.ryuqq.fileflow.domain.policy.FileMetadata;

/**
 * FileMetadata Test Fixture
 *
 * <p>테스트에서 FileMetadata 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 2025-10-31
 */
public class FileMetadataFixture {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private FileMetadataFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 FileMetadata 생성 (JPG 이미지, 1MB)
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createDefault() {
        return FileMetadata.of("test-image.jpg", "image/jpeg", 1048576L);
    }

    /**
     * 특정 값으로 FileMetadata 생성
     *
     * @param name 파일명
     * @param mimeType MIME 타입
     * @param size 파일 크기
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata create(String name, String mimeType, Long size) {
        return FileMetadata.of(name, mimeType, size);
    }

    /**
     * JPG 이미지 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createJpgImage() {
        return FileMetadata.of("image.jpg", "image/jpeg", 2097152L); // 2MB
    }

    /**
     * PNG 이미지 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createPngImage() {
        return FileMetadata.of("image.png", "image/png", 3145728L); // 3MB
    }

    /**
     * PDF 문서 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createPdfDocument() {
        return FileMetadata.of("document.pdf", "application/pdf", 5242880L); // 5MB
    }

    /**
     * 텍스트 파일 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createTextFile() {
        return FileMetadata.of("document.txt", "text/plain", 10240L); // 10KB
    }

    /**
     * 비디오 파일 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createVideoFile() {
        return FileMetadata.of("video.mp4", "video/mp4", 104857600L); // 100MB
    }

    /**
     * 최소 크기 파일 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createMinimumSize() {
        return FileMetadata.of("tiny.txt", "text/plain", 1L); // 1 byte
    }

    /**
     * 큰 파일 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createLargeFile() {
        return FileMetadata.of("large-video.mkv", "video/x-matroska", 1073741824L); // 1GB
    }
}
