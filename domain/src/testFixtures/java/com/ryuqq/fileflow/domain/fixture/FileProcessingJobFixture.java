package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileProcessingJobId;
import com.ryuqq.fileflow.domain.vo.JobStatus;
import com.ryuqq.fileflow.domain.vo.JobType;

import java.time.LocalDateTime;

/**
 * FileProcessingJob Aggregate TestFixture (Object Mother 패턴 + Builder 패턴)
 */
public class FileProcessingJobFixture {

    /**
     * 기본 FileProcessingJob 생성 (Builder 시작)
     */
    public static FileProcessingJobBuilder aJob() {
        return new FileProcessingJobBuilder();
    }

    /**
     * FileProcessingJob.forNew() 팩토리 메서드 사용 (PENDING 상태, ID null)
     * <p>
     * 새 작업 생성 시 사용하는 헬퍼 메서드입니다.
     * </p>
     *
     * @param fileId        파일 ID (String)
     * @param jobType       작업 유형
     * @param inputS3Key    입력 S3 키
     * @param maxRetryCount 최대 재시도 횟수
     * @return 생성된 FileProcessingJob Aggregate
     */
    public static FileProcessingJob createJob(String fileId, JobType jobType, String inputS3Key, int maxRetryCount) {
        return FileProcessingJob.forNew(
                FileId.of(fileId),
                jobType,
                inputS3Key,
                maxRetryCount,
                java.time.Clock.systemUTC()
        );
    }

    /**
     * COMPLETED 상태 작업
     */
    public static FileProcessingJob aCompletedJob() {
        FileProcessingJob job = createJob(
                "file-uuid-v7-123",
                JobTypeFixture.thumbnailGeneration(),
                "uploads/2024/01/image.jpg",
                3
        );
        return job.markAsCompleted("processed/2024/01/thumbnail.jpg", java.time.Clock.systemUTC());
    }

    /**
     * FAILED 상태 작업
     */
    public static FileProcessingJob aFailedJob() {
        FileProcessingJob job = createJob(
                "file-uuid-v7-123",
                JobTypeFixture.thumbnailGeneration(),
                "uploads/2024/01/image.jpg",
                3
        );
        return job.markAsFailed("Processing error: Invalid image format", java.time.Clock.systemUTC());
    }

    /**
     * FileProcessingJob Builder (Plain Java, Lombok 금지)
     */
    public static class FileProcessingJobBuilder {
        private FileProcessingJobId jobId = FileProcessingJobId.of(UuidV7GeneratorFixture.aUuidV7());
        private FileId fileId = FileId.of(UuidV7GeneratorFixture.aUuidV7());
        private JobType jobType = JobTypeFixture.thumbnailGeneration();
        private JobStatus status = JobStatusFixture.pending();
        private int retryCount = 0;
        private int maxRetryCount = 3;
        private String inputS3Key = "uploads/2024/01/test-file.jpg";
        private String outputS3Key = null;
        private String errorMessage = null;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime processedAt = null;

        public FileProcessingJobBuilder jobId(FileProcessingJobId jobId) {
            this.jobId = jobId;
            return this;
        }

        public FileProcessingJobBuilder fileId(FileId fileId) {
            this.fileId = fileId;
            return this;
        }

        public FileProcessingJobBuilder jobType(JobType jobType) {
            this.jobType = jobType;
            return this;
        }

        public FileProcessingJobBuilder status(JobStatus status) {
            this.status = status;
            return this;
        }

        public FileProcessingJobBuilder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public FileProcessingJobBuilder maxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
            return this;
        }

        public FileProcessingJobBuilder inputS3Key(String inputS3Key) {
            this.inputS3Key = inputS3Key;
            return this;
        }

        public FileProcessingJobBuilder outputS3Key(String outputS3Key) {
            this.outputS3Key = outputS3Key;
            return this;
        }

        public FileProcessingJobBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public FileProcessingJobBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FileProcessingJobBuilder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        /**
         * FileProcessingJob 객체 생성
         * <p>
         * reconstitute() 팩토리 메서드를 사용하여 테스트용 객체 생성
         * </p>
         */
        public FileProcessingJob build() {
            return FileProcessingJob.reconstitute(
                    jobId,
                    fileId,
                    jobType,
                    status,
                    retryCount,
                    maxRetryCount,
                    inputS3Key,
                    outputS3Key,
                    errorMessage,
                    createdAt,
                    processedAt,
                    java.time.Clock.systemUTC()
            );
        }
    }
}
