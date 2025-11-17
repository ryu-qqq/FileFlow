package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileStatus;
import com.ryuqq.fileflow.domain.vo.RetryCount;
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
     * File.forNew() 팩토리 메서드 (영속화 전, ID null)
     * <p>
     * 기본값으로 File을 생성합니다. PENDING 상태, UUID v7 자동 생성.
     * </p>
     *
     * @return 생성된 File Aggregate (ID null)
     */
    public static File forNew() {
        String fileName = "test-file.dat";
        long fileSize = 1024L; // 1KB
        String mimeType = "application/octet-stream";
        String s3Key = "uploads/2024/01/" + fileName;
        String s3Bucket = "fileflow-storage";
        UploaderId uploaderId = UploaderIdFixture.anUploaderId();
        String category = "OTHER";
        return File.forNew(fileName, fileSize, mimeType, s3Key, s3Bucket, uploaderId, category, null, Clock.systemUTC());
    }

    /**
     * File.of() 팩토리 메서드 (ID 필수, 비즈니스 로직용)
     * <p>
     * 모든 필드를 커스터마이징할 수 있는 팩토리 메서드입니다.
     * </p>
     *
     * @param fileId     파일 ID (필수, null 불가)
     * @param fileName   파일명
     * @param fileSize   파일 크기 (바이트)
     * @param mimeType   MIME 타입
     * @param status     파일 상태
     * @param uploaderId 업로더 ID
     * @param category   파일 카테고리
     * @return 생성된 File Aggregate
     */
    public static File of(FileId fileId, String fileName, long fileSize, String mimeType, FileStatus status, UploaderId uploaderId, String category) {
        String s3Key = "uploads/2024/01/" + fileName;
        String s3Bucket = "fileflow-storage";
        String cdnUrl = "https://cdn.example.com/" + s3Key;
        RetryCount retryCount = RetryCount.forFile();
        int version = 1;
        LocalDateTime now = LocalDateTime.now();

        return File.of(
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
                null, // tags
                retryCount,
                version,
                null, // deletedAt
                now, // createdAt
                now  // updatedAt
        );
    }

    /**
     * File.reconstitute() 팩토리 메서드 (영속성 복원용)
     * <p>
     * 모든 필드를 지정하여 File을 복원합니다.
     * </p>
     *
     * @param fileId     파일 ID (필수, null 불가)
     * @param fileName   파일명
     * @param fileSize   파일 크기 (바이트)
     * @param mimeType   MIME 타입
     * @param status     파일 상태
     * @param s3Key      S3 객체 키
     * @param s3Bucket   S3 버킷명
     * @param cdnUrl     CDN URL
     * @param uploaderId 업로더 ID
     * @param category   파일 카테고리
     * @param tags       태그
     * @param retryCount 재시도 횟수
     * @param version    낙관적 락 버전
     * @param deletedAt  소프트 삭제 시각
     * @param createdAt  생성 시각
     * @param updatedAt  수정 시각
     * @return 복원된 File Aggregate
     */
    public static File reconstitute(
            FileId fileId,
            String fileName,
            long fileSize,
            String mimeType,
            FileStatus status,
            String s3Key,
            String s3Bucket,
            String cdnUrl,
            UploaderId uploaderId,
            String category,
            String tags,
            RetryCount retryCount,
            int version,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
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
     * PENDING 상태 파일
     */
    public static File aPendingFile() {
        return aFile()
                .fileName("pending.jpg")
                .fileSize(1024000L)
                .mimeType("image/jpeg")
                .uploaderId(UploaderId.of(1L))
                .category("IMAGE")
                .status(FileStatusFixture.pending())
                .build();
    }

    /**
     * UPLOADING 상태 파일
     */
    public static File aUploadingFile() {
        return aFile()
                .fileName("uploading.jpg")
                .fileSize(1024000L)
                .mimeType("image/jpeg")
                .uploaderId(UploaderId.of(1L))
                .category("IMAGE")
                .status(FileStatusFixture.uploading())
                .build();
    }

    /**
     * COMPLETED 상태 파일
     */
    public static File aCompletedFile() {
        return aFile()
                .fileName("completed.jpg")
                .fileSize(1024000L)
                .mimeType("image/jpeg")
                .uploaderId(UploaderId.of(1L))
                .category("IMAGE")
                .status(FileStatusFixture.completed())
                .build();
    }

    /**
     * PROCESSING 상태 파일
     */
    public static File aProcessingFile() {
        return aFile()
                .fileName("processing.jpg")
                .fileSize(1024000L)
                .mimeType("image/jpeg")
                .uploaderId(UploaderId.of(1L))
                .category("IMAGE")
                .status(FileStatusFixture.processing())
                .build();
    }

    /**
     * FAILED 상태 파일
     */
    public static File aFailedFile() {
        return aFile()
                .fileName("failed.jpg")
                .fileSize(1024000L)
                .mimeType("image/jpeg")
                .uploaderId(UploaderId.of(1L))
                .category("IMAGE")
                .status(FileStatusFixture.failed())
                .build();
    }

    /**
     * 삭제된 파일 (softDelete)
     */
    public static File aDeletedFile() {
        LocalDateTime now = LocalDateTime.now();
        return aFile()
                .fileName("deleted.jpg")
                .fileSize(1024000L)
                .mimeType("image/jpeg")
                .uploaderId(UploaderId.of(1L))
                .category("IMAGE")
                .deletedAt(now)
                .build();
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
        private RetryCount retryCount = RetryCount.forFile(); // VO 적용
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

        public FileBuilder retryCount(RetryCount retryCount) {
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
