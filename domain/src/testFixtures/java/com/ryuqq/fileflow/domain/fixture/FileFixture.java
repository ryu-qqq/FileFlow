package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileStatus;
import com.ryuqq.fileflow.domain.vo.UploaderId;

import java.time.Clock;
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
     * File.create() 팩토리 메서드 사용 (PENDING 상태, UUID v7 자동 생성)
     *
     * @param fileName   파일명
     * @param fileSize   파일 크기 (바이트)
     * @param mimeType   MIME 타입
     * @param uploaderId 업로더 ID
     * @param category   파일 카테고리
     * @return 생성된 File Aggregate
     */
    public static File createFile(String fileName, long fileSize, String mimeType, Long uploaderId, String category) {
        String s3Key = "uploads/2024/01/" + fileName;
        String s3Bucket = "fileflow-storage";
        return File.create(fileName, fileSize, mimeType, s3Key, s3Bucket, uploaderId, category, null);
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
     * UPLOADING 상태 파일
     */
    public static File aUploadingFile() {
        File file = createFile("uploading.jpg", 1024000L, "image/jpeg", 1L, "IMAGE");
        file.markAsUploading();
        return file;
    }

    /**
     * COMPLETED 상태 파일
     */
    public static File aCompletedFile() {
        File file = createFile("completed.jpg", 1024000L, "image/jpeg", 1L, "IMAGE");
        file.markAsCompleted();
        return file;
    }

    /**
     * PROCESSING 상태 파일
     */
    public static File aProcessingFile() {
        File file = createFile("processing.jpg", 1024000L, "image/jpeg", 1L, "IMAGE");
        file.markAsCompleted();
        file.markAsProcessing();
        return file;
    }

    /**
     * FAILED 상태 파일
     */
    public static File aFailedFile() {
        File file = createFile("failed.jpg", 1024000L, "image/jpeg", 1L, "IMAGE");
        file.markAsFailed("Upload error");
        return file;
    }

    /**
     * 삭제된 파일 (softDelete)
     */
    public static File aDeletedFile() {
        File file = createFile("deleted.jpg", 1024000L, "image/jpeg", 1L, "IMAGE");
        file.softDelete();
        return file;
    }

    /**
     * File Builder (Plain Java, Lombok 금지)
     */
    public static class FileBuilder {
        private FileId fileId = FileIdFixture.aFileId();
        private String fileName = "test-file.dat";
        private long fileSize = 1024L; // 1KB
        private String mimeType = "application/octet-stream";
        private FileStatus status = FileStatusFixture.pending();
        private String s3Key = "uploads/2024/01/test-file.dat";
        private String s3Bucket = "fileflow-storage";
        private String cdnUrl = "https://cdn.example.com/uploads/2024/01/test-file.dat";
        private UploaderId uploaderId = UploaderIdFixture.anUploaderId();
        private String category = "OTHER";
        private String tags = null;
        private int retryCount = 0;
        private int version = 1;
        private LocalDateTime deletedAt = null;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public FileBuilder fileId(FileId fileId) {
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

        public FileBuilder uploaderId(UploaderId uploaderId) {
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

        public FileBuilder retryCount(int retryCount) {
            this.retryCount = retryCount;
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
            return File.reconstitute(
                    Clock.systemUTC(),
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
                    retryCount,
                    version,
                    deletedAt,
                    createdAt,
                    updatedAt
            );
        }
    }
}
