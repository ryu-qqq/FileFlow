package com.ryuqq.fileflow.domain.file.aggregate;

import com.ryuqq.fileflow.domain.util.UuidV7Generator;
import com.ryuqq.fileflow.domain.iam.vo.FileId;
import com.ryuqq.fileflow.domain.file.vo.FileProcessingJobId;
import com.ryuqq.fileflow.domain.file.vo.JobStatus;
import com.ryuqq.fileflow.domain.file.vo.JobType;
import com.ryuqq.fileflow.domain.vo.RetryCount;

import java.time.LocalDateTime;

/**
 * 파일 처리 작업 Aggregate Root
 * <p>
 * 파일 처리 작업(썸네일 생성, HTML 파싱 등)을 관리하는 도메인 엔티티입니다.
 * </p>
 */
public class FileProcessingJob {

    private final FileProcessingJobId jobId;
    private final FileId fileId;
    private final JobType jobType;
    private JobStatus status;
    private RetryCount retryCount; // VO 적용
    private final String inputS3Key;
    private String outputS3Key;
    private String errorMessage;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private LocalDateTime updatedAt;  // 가변: 모든 비즈니스 메서드에서 자동 갱신
    private final java.time.Clock clock;

    /**
     * FileProcessingJob Aggregate 생성자
     * <p>
     * Private 생성자: 외부에서 직접 생성 불가, 팩토리 메서드 사용 필수
     * </p>
     *
     * @param jobId        작업 고유 ID (FileProcessingJobId VO)
     * @param fileId       파일 고유 ID (FileId VO)
     * @param jobType      작업 유형 (THUMBNAIL_GENERATION, HTML_PARSING 등)
     * @param status       작업 상태
     * @param retryCount   재시도 횟수 (RetryCount VO)
     * @param inputS3Key   입력 파일 S3 키
     * @param outputS3Key  출력 파일 S3 키 (완료 후)
     * @param errorMessage 에러 메시지 (실패 시)
     * @param createdAt    생성 시각
     * @param processedAt  처리 완료 시각
     * @param updatedAt    최종 수정 시각
     * @param clock        시간 처리 Clock (테스트 가능한 시간 처리)
     */
    private FileProcessingJob(
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
            LocalDateTime updatedAt,
            java.time.Clock clock
    ) {
        this.jobId = jobId;
        this.fileId = fileId;
        this.jobType = jobType;
        this.status = status;
        this.retryCount = retryCount;
        this.inputS3Key = inputS3Key;
        this.outputS3Key = outputS3Key;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.updatedAt = updatedAt;
        this.clock = clock;
    }

    /**
     * 작업 고유 ID 조회
     */
    public FileProcessingJobId getJobId() {
        return jobId;
    }

    /**
     * 파일 고유 ID 조회
     */
    public FileId getFileId() {
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
        return retryCount.current();
    }

    /**
     * 최대 재시도 횟수 조회
     */
    public int getMaxRetryCount() {
        return retryCount.max();
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
     * 최종 수정 시각 조회
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 작업 ID의 원시 값 조회
     * <p>
     * Law of Demeter 준수: getJobId().getValue() 체이닝 방지
     * </p>
     *
     * @return 작업 ID 원시 값 (String)
     */
    public String getJobIdValue() {
        return jobId.getValue();
    }

    /**
     * 파일 ID의 원시 값 조회
     * <p>
     * Law of Demeter 준수: getFileId().getValue() 체이닝 방지
     * </p>
     *
     * @return 파일 ID 원시 값 (String)
     */
    public String getFileIdValue() {
        return fileId.getValue();
    }

    /**
     * 새 파일 처리 작업 생성 팩토리 메서드
     * <p>
     * ID가 null인 새로운 작업을 생성합니다. 영속화 전 상태입니다.
     * </p>
     *
     * @param fileId     파일 고유 ID
     * @param jobType    작업 유형
     * @param inputS3Key 입력 파일 S3 키
     * @param clock      시간 처리 Clock
     * @return 생성된 FileProcessingJob Aggregate (ID null)
     */
    public static FileProcessingJob forNew(
            FileId fileId,
            JobType jobType,
            String inputS3Key,
            java.time.Clock clock
    ) {
        LocalDateTime now = LocalDateTime.now(clock);

        return new FileProcessingJob(
                FileProcessingJobId.forNew(), // ID는 영속화 시점에 생성
                fileId,
                jobType,
                JobStatus.PENDING, // 초기 상태는 PENDING
                RetryCount.forJob(), // Job 재시도 전략 (최대 2회)
                inputS3Key,
                null, // outputS3Key는 null
                null, // errorMessage는 null
                now, // createdAt
                null, // processedAt는 null
                now, // updatedAt = createdAt (초기 생성 시)
                clock
        );
    }

    /**
     * 파일 처리 작업 생성 팩토리 메서드 (비즈니스 로직용)
     * <p>
     * ID가 필수인 작업을 생성합니다. 비즈니스 로직에서 사용합니다.
     * </p>
     *
     * @param jobId        작업 고유 ID (필수, null 불가)
     * @param fileId       파일 고유 ID
     * @param jobType      작업 유형
     * @param status       작업 상태
     * @param retryCount   재시도 횟수 (RetryCount VO)
     * @param inputS3Key   입력 파일 S3 키
     * @param outputS3Key  출력 파일 S3 키
     * @param errorMessage 에러 메시지
     * @param createdAt    생성 시각
     * @param processedAt  처리 완료 시각
     * @param updatedAt    최종 수정 시각
     * @param clock        시간 처리 Clock
     * @return 생성된 FileProcessingJob Aggregate
     * @throws IllegalArgumentException ID가 null이거나 새로운 ID인 경우
     */
    public static FileProcessingJob of(
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
            LocalDateTime updatedAt,
            java.time.Clock clock
    ) {
        validateIdNotNullOrNew(jobId, "ID는 null이거나 새로운 ID일 수 없습니다");

        return new FileProcessingJob(
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
                clock
        );
    }

    /**
     * 파일 처리 작업 재구성 팩토리 메서드 (영속성 계층용)
     * <p>
     * 영속성 계층에서 조회한 데이터로 Aggregate를 재구성합니다.
     * </p>
     *
     * @param jobId        작업 고유 ID (필수, null 불가)
     * @param fileId       파일 고유 ID
     * @param jobType      작업 유형
     * @param status       작업 상태
     * @param retryCount   재시도 횟수 (RetryCount VO)
     * @param inputS3Key   입력 파일 S3 키
     * @param outputS3Key  출력 파일 S3 키
     * @param errorMessage 에러 메시지
     * @param createdAt    생성 시각
     * @param processedAt  처리 완료 시각
     * @param updatedAt    최종 수정 시각
     * @param clock        시간 처리 Clock
     * @return 재구성된 FileProcessingJob Aggregate
     * @throws IllegalArgumentException ID가 null이거나 새로운 ID인 경우
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
            LocalDateTime updatedAt,
            java.time.Clock clock
    ) {
        validateIdNotNullOrNew(jobId, "재구성을 위한 ID는 null이거나 새로운 ID일 수 없습니다");

        return new FileProcessingJob(
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
                clock
        );
    }

    /**
     * ID 검증 헬퍼 메서드
     * <p>
     * ID가 null이거나 새로운 ID(값이 null)인 경우 예외를 발생시킵니다.
     * </p>
     *
     * @param jobId        검증할 ID
     * @param errorMessage 예외 메시지
     * @throws IllegalArgumentException ID가 null이거나 새로운 ID인 경우
     */
    private static void validateIdNotNullOrNew(FileProcessingJobId jobId, String errorMessage) {
        if (jobId == null || jobId.isNew()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }


    /**
     * 작업 상태를 PROCESSING으로 변경
     */
    public void markAsProcessing() {
        this.status = JobStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 작업 상태를 COMPLETED로 변경
     *
     * @param outputS3Key 출력 파일 S3 키
     * @param clock       시간 처리 Clock
     */
    public void markAsCompleted(String outputS3Key, java.time.Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        this.status = JobStatus.COMPLETED;
        this.outputS3Key = outputS3Key;
        this.processedAt = now;
        this.updatedAt = now;
    }

    /**
     * 작업 상태를 FAILED로 변경
     *
     * @param errorMessage 에러 메시지
     * @param clock        시간 처리 Clock
     */
    public void markAsFailed(String errorMessage, java.time.Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processedAt = now;
        this.updatedAt = now;
    }

    /**
     * 재시도 횟수 증가
     */
    public void incrementRetryCount() {
        this.retryCount = this.retryCount.increment();
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 재시도 가능 여부 확인
     *
     * @return 재시도 가능하면 true, 불가능하면 false
     */
    public boolean canRetry() {
        return this.retryCount.canRetry();
    }
}
