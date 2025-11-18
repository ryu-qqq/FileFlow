package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;
import com.ryuqq.fileflow.domain.iam.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileProcessingJobId;
import com.ryuqq.fileflow.domain.vo.JobStatus;
import com.ryuqq.fileflow.domain.vo.JobType;
import com.ryuqq.fileflow.domain.vo.RetryCount;

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
     * FileProcessingJob.forNew() 팩토리 메서드 (영속화 전, ID null)
     * <p>
     * 기본값으로 FileProcessingJob을 생성합니다. PENDING 상태.
     * RetryCount는 Job 전략 (최대 2회)으로 자동 설정됩니다.
     * </p>
     *
     * @return 생성된 FileProcessingJob Aggregate (ID null)
     */
    public static FileProcessingJob forNew() {
        return FileProcessingJob.forNew(
                FileId.of(UuidV7GeneratorFixture.aUuidV7()),
                JobTypeFixture.thumbnailGeneration(),
                "uploads/2024/01/test-file.jpg",
                java.time.Clock.systemUTC()
        );
    }

    /**
     * FileProcessingJob.of() 팩토리 메서드 (ID 필수, 비즈니스 로직용)
     * <p>
     * 모든 필드를 커스터마이징할 수 있는 팩토리 메서드입니다.
     * </p>
     *
     * @param jobId      작업 ID (필수, null 불가)
     * @param fileId     파일 ID
     * @param jobType    작업 유형
     * @param inputS3Key 입력 S3 키
     * @param status     작업 상태
     * @return 생성된 FileProcessingJob Aggregate
     */
    public static FileProcessingJob of(
            FileProcessingJobId jobId,
            FileId fileId,
            JobType jobType,
            String inputS3Key,
            JobStatus status
    ) {
        LocalDateTime now = LocalDateTime.now();
        return FileProcessingJob.reconstitute(
                jobId,
                fileId,
                jobType,
                status,
                RetryCount.forJob(),
                inputS3Key,
                null, // outputS3Key
                null, // errorMessage
                now,  // createdAt
                null, // processedAt
                now,  // updatedAt
                java.time.Clock.systemUTC()
        );
    }

    /**
     * FileProcessingJob.reconstitute() 팩토리 메서드 (영속성 복원용)
     * <p>
     * 모든 필드를 지정하여 FileProcessingJob을 복원합니다.
     * </p>
     *
     * @param jobId       작업 ID (필수, null 불가)
     * @param fileId      파일 ID
     * @param jobType     작업 유형
     * @param status      작업 상태
     * @param retryCount  재시도 횟수
     * @param inputS3Key  입력 S3 키
     * @param outputS3Key 출력 S3 키
     * @param errorMessage 에러 메시지
     * @param createdAt   생성 시각
     * @param processedAt 처리 시각
     * @param updatedAt   수정 시각
     * @return 복원된 FileProcessingJob Aggregate
     */
    public static FileProcessingJob reconstitute(
            FileProcessingJobId jobId,
            FileId fileId,
            JobType jobType,
            JobStatus status,
            RetryCount retryCount,
            String inputS3Key,
            String outputS3Key,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt,
            LocalDateTime updatedAt
    ) {
        return FileProcessingJob.reconstitute(
                jobId,
                fileId,
                jobType,
                status,
                retryCount,
                inputS3Key,
                outputS3Key,
                errorMessage,
                createdAt,
                processedAt,
                updatedAt,
                java.time.Clock.systemUTC()
        );
    }

    /**
     * COMPLETED 상태 작업
     */
    public static FileProcessingJob aCompletedJob() {
        FileProcessingJob job = forNew();
        job.markAsCompleted("processed/2024/01/thumbnail.jpg", java.time.Clock.systemUTC());
        return job;
    }

    /**
     * FAILED 상태 작업
     */
    public static FileProcessingJob aFailedJob() {
        FileProcessingJob job = forNew();
        job.markAsFailed("Processing error: Invalid image format", java.time.Clock.systemUTC());
        return job;
    }

    /**
     * FileProcessingJob Builder (Plain Java, Lombok 금지)
     */
    public static class FileProcessingJobBuilder {
        private FileProcessingJobId jobId = FileProcessingJobId.of(UuidV7GeneratorFixture.aUuidV7());
        private FileId fileId = FileId.of(UuidV7GeneratorFixture.aUuidV7());
        private JobType jobType = JobTypeFixture.thumbnailGeneration();
        private JobStatus status = JobStatusFixture.pending();
        private RetryCount retryCount = RetryCount.forJob(); // VO 적용 (Job 전략: 최대 2회)
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

        public FileProcessingJobBuilder retryCount(RetryCount retryCount) {
            this.retryCount = retryCount;
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
                    inputS3Key,
                    outputS3Key,
                    errorMessage,
                    createdAt,
                    processedAt,
                    createdAt, // updatedAt = createdAt (기본값)
                    java.time.Clock.systemUTC()
            );
        }
    }
}
