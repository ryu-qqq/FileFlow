package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.vo.FileStatus;

import java.time.LocalDateTime;

/**
 * File Aggregate TestFixture (Object Mother 패턴 + Builder 패턴)
 */
public class FileFixture {

    /**
     * 기본 File 생성 (Builder 시작)
     */
    public static FileBuilder aFile() {
        return new FileBuilder();
    }

    /**
     * 이미지 파일 (JPG)
     */
    public static File aJpgImage() {
        return aFile()
                .fileName("test-image.jpg")
                .mimeType("image/jpeg")
                .fileSize(1024000L) // 1MB
                .category("IMAGE")
                .build();
    }

    /**
     * PDF 문서 파일
     */
    public static File aPdfDocument() {
        return aFile()
                .fileName("document.pdf")
                .mimeType("application/pdf")
                .fileSize(512000L) // 512KB
                .category("DOCUMENT")
                .build();
    }

    /**
     * Excel 파일
     */
    public static File anExcelFile() {
        return aFile()
                .fileName("data.xlsx")
                .mimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .fileSize(256000L) // 256KB
                .category("EXCEL")
                .build();
    }

    /**
     * File Builder (Plain Java, Lombok 금지)
     */
    public static class FileBuilder {
        private String fileId = UuidV7GeneratorFixture.aUuidV7();
        private String fileName = "test-file.dat";
        private long fileSize = 1024L; // 1KB
        private String mimeType = "application/octet-stream";
        private FileStatus status = FileStatusFixture.pending();
        private String s3Key = "uploads/2024/01/test-file.dat";
        private String s3Bucket = "fileflow-storage";
        private String cdnUrl = "https://cdn.example.com/uploads/2024/01/test-file.dat";
        private Long uploaderId = 12345L;
        private String category = "OTHER";
        private String tags = null;
        private int version = 1;
        private LocalDateTime deletedAt = null;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public FileBuilder fileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public FileBuilder fileName(String fileName) {
            this.fileName = fileName;
            this.s3Key = "uploads/2024/01/" + fileName; // s3Key도 함께 업데이트
            this.cdnUrl = "https://cdn.example.com/uploads/2024/01/" + fileName; // cdnUrl도 업데이트
            return this;
        }

        public FileBuilder fileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public FileBuilder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public FileBuilder status(FileStatus status) {
            this.status = status;
            return this;
        }

        public FileBuilder s3Key(String s3Key) {
            this.s3Key = s3Key;
            return this;
        }

        public FileBuilder s3Bucket(String s3Bucket) {
            this.s3Bucket = s3Bucket;
            return this;
        }

        public FileBuilder cdnUrl(String cdnUrl) {
            this.cdnUrl = cdnUrl;
            return this;
        }

        public FileBuilder uploaderId(Long uploaderId) {
            this.uploaderId = uploaderId;
            return this;
        }

        public FileBuilder category(String category) {
            this.category = category;
            return this;
        }

        public FileBuilder tags(String tags) {
            this.tags = tags;
            return this;
        }

        public FileBuilder version(int version) {
            this.version = version;
            return this;
        }

        public FileBuilder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public FileBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FileBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * File 객체 생성
         */
        public File build() {
            return new File(
                    fileId,
                    fileName,
                    fileSize,
                    mimeType,
                    status,
                    s3Key,
                    s3Bucket,
                    cdnUrl,
                    uploaderId,
                    category,
                    tags,
                    version,
                    deletedAt,
                    createdAt,
                    updatedAt
            );
        }
    }
}
