package com.ryuqq.fileflow.domain.policy.fixture;

import com.ryuqq.fileflow.domain.policy.FileMetadata;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;

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
        return FileMetadata.of(
            FileName.of("test-image.jpg"),
            MimeType.of("image/jpeg"),
            FileSize.of(1048576L)
        );
    }

    /**
     * 특정 값으로 FileMetadata 생성
     *
     * @param name 파일명
     * @param mimeType MIME 타입
     * @param size 파일 크기
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata create(FileName name, MimeType mimeType, FileSize size) {
        return FileMetadata.of(name, mimeType, size);
    }

    /**
     * JPG 이미지 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createJpgImage() {
        return FileMetadata.of(
            FileName.of("image.jpg"),
            MimeType.of("image/jpeg"),
            FileSize.of(2097152L) // 2MB
        );
    }

    /**
     * PNG 이미지 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createPngImage() {
        return FileMetadata.of(
            FileName.of("image.png"),
            MimeType.of("image/png"),
            FileSize.of(3145728L) // 3MB
        );
    }

    /**
     * PDF 문서 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createPdfDocument() {
        return FileMetadata.of(
            FileName.of("document.pdf"),
            MimeType.of("application/pdf"),
            FileSize.of(5242880L) // 5MB
        );
    }

    /**
     * 텍스트 파일 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createTextFile() {
        return FileMetadata.of(
            FileName.of("document.txt"),
            MimeType.of("text/plain"),
            FileSize.of(10240L) // 10KB
        );
    }

    /**
     * 비디오 파일 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createVideoFile() {
        return FileMetadata.of(
            FileName.of("video.mp4"),
            MimeType.of("video/mp4"),
            FileSize.of(104857600L) // 100MB
        );
    }

    /**
     * 최소 크기 파일 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createMinimumSize() {
        return FileMetadata.of(
            FileName.of("tiny.txt"),
            MimeType.of("text/plain"),
            FileSize.of(1L) // 1 byte
        );
    }

    /**
     * 큰 파일 FileMetadata 생성
     *
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata createLargeFile() {
        return FileMetadata.of(
            FileName.of("large-video.mkv"),
            MimeType.of("video/x-matroska"),
            FileSize.of(1073741824L) // 1GB
        );
    }

    /**
     * Builder 패턴으로 FileMetadata 생성
     *
     * @return FileMetadataBuilder 인스턴스
     */
    public static FileMetadataBuilder builder() {
        return new FileMetadataBuilder();
    }

    /**
     * FileMetadata Builder
     */
    public static class FileMetadataBuilder {
        private FileName name = FileName.of("test-file.txt");
        private MimeType mimeType = MimeType.of("text/plain");
        private FileSize size = FileSize.of(1024L);

        public FileMetadataBuilder name(FileName name) {
            this.name = name;
            return this;
        }

        public FileMetadataBuilder mimeType(MimeType mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public FileMetadataBuilder size(FileSize size) {
            this.size = size;
            return this;
        }

        public FileMetadata build() {
            return FileMetadata.of(name, mimeType, size);
        }
    }
}
