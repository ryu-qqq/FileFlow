package com.ryuqq.fileflow.domain.aggregate;

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
}
