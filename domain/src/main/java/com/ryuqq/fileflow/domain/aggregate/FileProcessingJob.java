package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.util.UuidV7Generator;
import com.ryuqq.fileflow.domain.vo.JobStatus;
import com.ryuqq.fileflow.domain.vo.JobType;

import java.time.LocalDateTime;

/**
 * 파일 처리 작업 Aggregate Root
 * <p>
 * 파일 처리 작업(썸네일 생성, HTML 파싱 등)을 관리하는 도메인 엔티티입니다.
 * </p>
 */
public class FileProcessingJob {

    private final String jobId;
    private final String fileId;
    private final JobType jobType;
    private final JobStatus status;
    private final int retryCount;
    private final int maxRetryCount;
    private final String inputS3Key;
    private final String outputS3Key;
    private final String errorMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime processedAt;

    /**
     * FileProcessingJob Aggregate 생성자
     *
     * @param jobId         작업 고유 ID (UUID v7)
     * @param fileId        파일 고유 ID (UUID v7)
     * @param jobType       작업 유형 (THUMBNAIL_GENERATION, HTML_PARSING 등)
     * @param status        작업 상태
     * @param retryCount    재시도 횟수
     * @param maxRetryCount 최대 재시도 횟수
     * @param inputS3Key    입력 파일 S3 키
     * @param outputS3Key   출력 파일 S3 키 (완료 후)
     * @param errorMessage  에러 메시지 (실패 시)
     * @param createdAt     생성 시각
     * @param processedAt   처리 완료 시각
     */
    public FileProcessingJob(
            String jobId,
            String fileId,
            JobType jobType,
            JobStatus status,
            int retryCount,
            int maxRetryCount,
            String inputS3Key,
            String outputS3Key,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime processedAt
    ) {
        this.jobId = jobId;
        this.fileId = fileId;
        this.jobType = jobType;
        this.status = status;
        this.retryCount = retryCount;
        this.maxRetryCount = maxRetryCount;
        this.inputS3Key = inputS3Key;
        this.outputS3Key = outputS3Key;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * 작업 고유 ID 조회
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * 파일 고유 ID 조회
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * 작업 유형 조회
     */
    public JobType getJobType() {
        return jobType;
    }

    /**
     * 작업 상태 조회
     */
    public JobStatus getStatus() {
        return status;
    }

    /**
     * 재시도 횟수 조회
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 최대 재시도 횟수 조회
     */
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * 입력 파일 S3 키 조회
     */
    public String getInputS3Key() {
        return inputS3Key;
    }

    /**
     * 출력 파일 S3 키 조회
     */
    public String getOutputS3Key() {
        return outputS3Key;
    }

    /**
     * 에러 메시지 조회
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 생성 시각 조회
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 처리 완료 시각 조회
     */
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    /**
     * 파일 처리 작업 생성 팩토리 메서드
     * <p>
     * UUID v7을 자동 생성하고 초기 상태를 PENDING으로 설정합니다.
     * </p>
     *
     * @param fileId        파일 고유 ID
     * @param jobType       작업 유형
     * @param inputS3Key    입력 파일 S3 키
     * @param maxRetryCount 최대 재시도 횟수
     * @return 생성된 FileProcessingJob Aggregate
     */
    public static FileProcessingJob create(
            String fileId,
            JobType jobType,
            String inputS3Key,
            int maxRetryCount
    ) {
        // UUID v7 자동 생성
        String jobId = UuidV7Generator.generate();

        // 현재 시각
        LocalDateTime now = LocalDateTime.now();

        return new FileProcessingJob(
                jobId,
                fileId,
                jobType,
                JobStatus.PENDING, // 초기 상태는 PENDING
                0, // 초기 재시도 횟수 0
                maxRetryCount,
                inputS3Key,
                null, // outputS3Key는 null
                null, // errorMessage는 null
                now, // createdAt
                null  // processedAt는 null
        );
    }

    /**
     * 상태 전환 헬퍼 메서드
     * <p>
     * 새로운 상태로 FileProcessingJob 객체를 생성합니다.
     * </p>
     *
     * @param newStatus    새로운 작업 상태
     * @param outputS3Key  출력 S3 키 (nullable)
     * @param errorMessage 에러 메시지 (nullable)
     * @param processedAt  처리 완료 시각 (nullable)
     * @return 새로운 FileProcessingJob 객체
     */
    private FileProcessingJob withStatus(
            JobStatus newStatus,
            String outputS3Key,
            String errorMessage,
            LocalDateTime processedAt
    ) {
        return new FileProcessingJob(
                this.jobId,
                this.fileId,
                this.jobType,
                newStatus,
                this.retryCount,
                this.maxRetryCount,
                this.inputS3Key,
                outputS3Key,
                errorMessage,
                this.createdAt,
                processedAt
        );
    }

    /**
     * 작업 상태를 PROCESSING으로 변경
     *
     * @return 새로운 FileProcessingJob 객체 (PROCESSING 상태)
     */
    public FileProcessingJob markAsProcessing() {
        return withStatus(JobStatus.PROCESSING, this.outputS3Key, this.errorMessage, null);
    }

    /**
     * 작업 상태를 COMPLETED로 변경
     *
     * @param outputS3Key 출력 파일 S3 키
     * @return 새로운 FileProcessingJob 객체 (COMPLETED 상태)
     */
    public FileProcessingJob markAsCompleted(String outputS3Key) {
        return withStatus(JobStatus.COMPLETED, outputS3Key, this.errorMessage, LocalDateTime.now());
    }

    /**
     * 작업 상태를 FAILED로 변경
     *
     * @param errorMessage 에러 메시지
     * @return 새로운 FileProcessingJob 객체 (FAILED 상태)
     */
    public FileProcessingJob markAsFailed(String errorMessage) {
        return withStatus(JobStatus.FAILED, this.outputS3Key, errorMessage, LocalDateTime.now());
    }

    /**
     * 재시도 횟수 증가
     *
     * @return 새로운 FileProcessingJob 객체 (retryCount + 1)
     */
    public FileProcessingJob incrementRetryCount() {
        return new FileProcessingJob(
                this.jobId,
                this.fileId,
                this.jobType,
                this.status,
                this.retryCount + 1,
                this.maxRetryCount,
                this.inputS3Key,
                this.outputS3Key,
                this.errorMessage,
                this.createdAt,
                this.processedAt
        );
    }

    /**
     * 재시도 가능 여부 확인
     *
     * @return 재시도 가능하면 true, 불가능하면 false
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetryCount;
    }
}
