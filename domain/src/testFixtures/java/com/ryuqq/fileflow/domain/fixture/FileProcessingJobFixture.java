package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;
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
     * FileProcessingJob Builder (Plain Java, Lombok 금지)
     */
    public static class FileProcessingJobBuilder {
        private String jobId = UuidV7GeneratorFixture.aUuidV7();
        private String fileId = UuidV7GeneratorFixture.aUuidV7();
        private JobType jobType = JobTypeFixture.thumbnailGeneration();
        private JobStatus status = JobStatusFixture.pending();
        private int retryCount = 0;
        private int maxRetryCount = 3;
        private String inputS3Key = "uploads/2024/01/test-file.jpg";
        private String outputS3Key = null;
        private String errorMessage = null;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime processedAt = null;

        public FileProcessingJobBuilder jobId(String jobId) {
            this.jobId = jobId;
            return this;
        }

        public FileProcessingJobBuilder fileId(String fileId) {
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
         */
        public FileProcessingJob build() {
            return new FileProcessingJob(
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
                    processedAt
            );
        }
    }
}
