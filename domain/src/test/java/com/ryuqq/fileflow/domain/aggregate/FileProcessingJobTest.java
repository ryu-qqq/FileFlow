package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.fixture.FileIdFixture;
import com.ryuqq.fileflow.domain.fixture.FileProcessingJobFixture;
import com.ryuqq.fileflow.domain.fixture.FileProcessingJobIdFixture;
import com.ryuqq.fileflow.domain.fixture.JobStatusFixture;
import com.ryuqq.fileflow.domain.fixture.JobTypeFixture;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileProcessingJobId;
import com.ryuqq.fileflow.domain.vo.JobType;
import com.ryuqq.fileflow.domain.vo.RetryCount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileProcessingJob Aggregate Root 테스트")
class FileProcessingJobTest {

    @Test
    @DisplayName("유효한 데이터로 FileProcessingJob을 생성할 수 있어야 한다")
    void shouldCreateJobWithValidData() {
        // Given & When
        FileId fileId = FileId.of("file-uuid-v7-123");
        FileProcessingJob job = FileProcessingJobFixture.aJob()
                .jobType(JobTypeFixture.thumbnailGeneration())
                .fileId(fileId)
                .inputS3Key("uploads/2024/01/image.jpg")
                .build();

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getJobId()).isNotNull();
        assertThat(job.getFileId()).isEqualTo(fileId);
        assertThat(job.getJobType()).isEqualTo(JobTypeFixture.thumbnailGeneration());
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.pending());
        assertThat(job.getInputS3Key()).isEqualTo("uploads/2024/01/image.jpg");
        assertThat(job.getMaxRetryCount()).isEqualTo(2); // Job 전략: 최대 2회
        assertThat(job.getRetryCount()).isEqualTo(0);
        assertThat(job.getOutputS3Key()).isNull();
        assertThat(job.getErrorMessage()).isNull();
        assertThat(job.getCreatedAt()).isNotNull();
        assertThat(job.getProcessedAt()).isNull();
    }

    @Test
    @DisplayName("필수 필드가 올바르게 설정되어야 한다")
    void shouldHaveRequiredFields() {
        // Given & When - FileProcessingJobFixture 사용
        FileProcessingJob job = FileProcessingJobFixture.aJob().build();

        // Then - 필수 필드 검증
        assertThat(job.getJobId()).isNotNull();
        assertThat(job.getFileId()).isNotNull();
        assertThat(job.getJobType()).isNotNull();
        assertThat(job.getStatus()).isNotNull();
        assertThat(job.getInputS3Key()).isNotBlank();
        assertThat(job.getMaxRetryCount()).isPositive();
        assertThat(job.getCreatedAt()).isNotNull();
    }

    // ===== 3종 팩토리 메서드 테스트 =====

    @Test
    @DisplayName("forNew()는 ID가 null인 새 작업을 생성해야 한다")
    void shouldCreateNewJobWithForNew() {
        // Given
        FileId fileId = FileIdFixture.aFileId();
        JobType jobType = JobTypeFixture.thumbnailGeneration();
        String inputS3Key = "uploads/2024/01/image.jpg";

        // When
        FileProcessingJob job = FileProcessingJob.forNew(
                fileId,
                jobType,
                inputS3Key,
                java.time.Clock.systemUTC()
        );

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getJobId()).isNotNull();
        assertThat(job.getJobId().isNew()).isTrue(); // ID가 null이어야 함
        assertThat(job.getFileId()).isEqualTo(fileId);
        assertThat(job.getJobType()).isEqualTo(jobType);
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.pending());
        assertThat(job.getInputS3Key()).isEqualTo(inputS3Key);
        assertThat(job.getMaxRetryCount()).isEqualTo(2); // Job 전략: 최대 2회
        assertThat(job.getRetryCount()).isEqualTo(0);
        assertThat(job.getOutputS3Key()).isNull();
        assertThat(job.getErrorMessage()).isNull();
        assertThat(job.getCreatedAt()).isNotNull();
        assertThat(job.getProcessedAt()).isNull();
    }

    @Test
    @DisplayName("of()는 유효한 ID로 작업을 생성해야 한다")
    void shouldCreateJobWithOf() {
        // Given
        FileProcessingJobId jobId = FileProcessingJobIdFixture.aFileProcessingJobId();
        FileId fileId = FileIdFixture.aFileId();
        JobType jobType = JobTypeFixture.thumbnailGeneration();
        String inputS3Key = "uploads/2024/01/image.jpg";

        // When
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        FileProcessingJob job = FileProcessingJob.of(
                jobId,
                fileId,
                jobType,
                JobStatusFixture.pending(),
                RetryCount.forJob(),
                inputS3Key,
                null,
                null,
                now, // createdAt
                null, // processedAt
                now, // updatedAt
                java.time.Clock.systemUTC()
        );

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getJobId()).isEqualTo(jobId);
        assertThat(job.getFileId()).isEqualTo(fileId);
        assertThat(job.getJobType()).isEqualTo(jobType);
    }

    @Test
    @DisplayName("of()는 null ID로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenOfWithNullId() {
        // Given
        FileProcessingJobId nullJobId = null;
        FileId fileId = FileIdFixture.aFileId();

        // When & Then
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        assertThatThrownBy(() -> FileProcessingJob.of(
                nullJobId,
                fileId,
                JobTypeFixture.thumbnailGeneration(),
                JobStatusFixture.pending(),
                RetryCount.forJob(),
                "uploads/image.jpg",
                null,
                null,
                now, // createdAt
                null, // processedAt
                now, // updatedAt
                java.time.Clock.systemUTC()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID는 null이거나 새로운 ID일 수 없습니다");
    }

    @Test
    @DisplayName("reconstitute()는 영속성 계층에서 작업을 재구성해야 한다")
    void shouldReconstituteJob() {
        // Given
        FileProcessingJobId jobId = FileProcessingJobIdFixture.aFileProcessingJobId();
        FileId fileId = FileIdFixture.aFileId();
        JobType jobType = JobTypeFixture.thumbnailGeneration();
        String inputS3Key = "uploads/2024/01/image.jpg";

        // When
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        FileProcessingJob job = FileProcessingJob.reconstitute(
                jobId,
                fileId,
                jobType,
                JobStatusFixture.pending(),
                RetryCount.forJob(),
                inputS3Key,
                null,
                null,
                now, // createdAt
                null, // processedAt
                now, // updatedAt
                java.time.Clock.systemUTC()
        );

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getJobId()).isEqualTo(jobId);
        assertThat(job.getFileId()).isEqualTo(fileId);
        assertThat(job.getJobType()).isEqualTo(jobType);
    }

    @Test
    @DisplayName("reconstitute()는 null ID로 재구성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenReconstituteWithNullId() {
        // Given
        FileProcessingJobId nullJobId = null;
        FileId fileId = FileIdFixture.aFileId();

        // When & Then
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        assertThatThrownBy(() -> FileProcessingJob.reconstitute(
                nullJobId,
                fileId,
                JobTypeFixture.thumbnailGeneration(),
                JobStatusFixture.pending(),
                RetryCount.forJob(),
                "uploads/image.jpg",
                null,
                null,
                now, // createdAt
                null, // processedAt
                now, // updatedAt
                java.time.Clock.systemUTC()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("재구성을 위한 ID는 null이거나 새로운 ID일 수 없습니다");
    }

    // ===== Clock 의존성 테스트 =====

    @Test
    @DisplayName("forNew()는 Clock을 사용하여 createdAt을 설정해야 한다")
    void shouldUseClockForCreatedAt() {
        // Given
        java.time.Clock fixedClock = java.time.Clock.fixed(
                java.time.Instant.parse("2025-01-15T10:00:00Z"),
                java.time.ZoneId.of("UTC")
        );
        FileId fileId = FileIdFixture.aFileId();
        JobType jobType = JobTypeFixture.thumbnailGeneration();

        // When
        FileProcessingJob job = FileProcessingJob.forNew(
                fileId,
                jobType,
                "uploads/image.jpg",
                fixedClock
        );

        // Then
        assertThat(job.getCreatedAt()).isEqualTo(java.time.LocalDateTime.of(2025, 1, 15, 10, 0, 0));
    }

    @Test
    @DisplayName("markAsCompleted()는 Clock을 사용하여 processedAt을 설정해야 한다")
    void shouldUseClockForProcessedAt() {
        // Given
        java.time.Clock fixedClock = java.time.Clock.fixed(
                java.time.Instant.parse("2025-01-15T12:00:00Z"),
                java.time.ZoneId.of("UTC")
        );
        FileId fileId = FileIdFixture.aFileId();
        FileProcessingJob job = FileProcessingJob.forNew(
                fileId,
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg",
                fixedClock
        );

        // When
        job.markAsCompleted("output.jpg", fixedClock);

        // Then
        assertThat(job.getProcessedAt()).isEqualTo(java.time.LocalDateTime.of(2025, 1, 15, 12, 0, 0));
    }

    @Test
    @DisplayName("고정된 Clock으로 작업을 생성하면 예측 가능한 시간이 설정되어야 한다")
    void shouldCreateJobWithFixedClock() {
        // Given
        java.time.Clock fixedClock = java.time.Clock.fixed(
                java.time.Instant.parse("2025-01-15T14:30:00Z"),
                java.time.ZoneId.of("UTC")
        );
        FileId fileId = FileIdFixture.aFileId();

        // When
        FileProcessingJob job = FileProcessingJob.forNew(
                fileId,
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg",
                fixedClock
        );

        // Then
        assertThat(job.getCreatedAt()).isEqualTo(java.time.LocalDateTime.of(2025, 1, 15, 14, 30, 0));
        assertThat(job.getProcessedAt()).isNull();
    }

    // ===== create() 팩토리 메서드 테스트 =====

    @Test
    @DisplayName("forNew() 팩토리 메서드로 신규 작업을 PENDING 상태로 생성해야 한다")
    void shouldCreateJobWithForNewAndPendingStatus() {
        // Given
        FileId fileId = FileIdFixture.aFileId();
        JobType jobType = JobTypeFixture.thumbnailGeneration();
        String inputS3Key = "uploads/2024/01/image.jpg";
        java.time.Clock clock = java.time.Clock.systemUTC();

        // When
        FileProcessingJob job = FileProcessingJob.forNew(fileId, jobType, inputS3Key, clock);

        // Then
        assertThat(job.getJobId()).isNotNull();
        assertThat(job.getJobId().isNew()).isTrue(); // ID는 영속화 전 (null)
        assertThat(job.getFileId()).isEqualTo(fileId);
        assertThat(job.getJobType()).isEqualTo(jobType);
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.pending()); // PENDING 상태
        assertThat(job.getInputS3Key()).isEqualTo(inputS3Key);
        assertThat(job.getMaxRetryCount()).isEqualTo(2); // Job 전략: 최대 2회
        assertThat(job.getRetryCount()).isEqualTo(0); // 초기 재시도 횟수 0
        assertThat(job.getOutputS3Key()).isNull();
        assertThat(job.getErrorMessage()).isNull();
        assertThat(job.getCreatedAt()).isNotNull();
        assertThat(job.getProcessedAt()).isNull();
    }

    // ===== 상태 전환 메서드 테스트 =====

    @Test
    @DisplayName("PENDING 상태에서 PROCESSING 상태로 전환할 수 있어야 한다")
    void shouldMarkAsProcessing() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.createJob(
                "file-uuid-v7-123",
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg"
        );
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.pending());

        // When
        job.markAsProcessing();

        // Then
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.processing());
        assertThat(job.getProcessedAt()).isNull(); // 완료 전이므로 null
    }

    @Test
    @DisplayName("작업을 완료 처리하고 outputS3Key를 설정할 수 있어야 한다")
    void shouldMarkAsCompleted() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.createJob(
                "file-uuid-v7-123",
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg"
        );
        String outputS3Key = "processed/2024/01/thumbnail.jpg";

        // When
        job.markAsCompleted(outputS3Key, java.time.Clock.systemUTC());

        // Then
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.completed());
        assertThat(job.getOutputS3Key()).isEqualTo(outputS3Key);
        assertThat(job.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("작업을 실패 처리하고 errorMessage를 설정할 수 있어야 한다")
    void shouldMarkAsFailed() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.createJob(
                "file-uuid-v7-123",
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg"
        );
        String errorMessage = "Image processing failed: Invalid format";

        // When
        job.markAsFailed(errorMessage, java.time.Clock.systemUTC());

        // Then
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.failed());
        assertThat(job.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(job.getProcessedAt()).isNotNull();
    }

    // ===== 부가 메서드 테스트 =====

    @Test
    @DisplayName("재시도 횟수를 증가시킬 수 있어야 한다")
    void shouldIncrementRetryCount() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.createJob(
                "file-uuid-v7-123",
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg"
        );
        assertThat(job.getRetryCount()).isEqualTo(0);

        // When
        job.incrementRetryCount();

        // Then
        assertThat(job.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("재시도 가능 여부를 확인할 수 있어야 한다 - 가능한 경우")
    void shouldReturnTrueWhenCanRetry() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.aJob()
                .retryCount(RetryCount.forJob().increment()) // current=1, max=2 → canRetry=true
                .build();

        // When
        boolean canRetry = job.canRetry();

        // Then
        assertThat(canRetry).isTrue();
    }

    @Test
    @DisplayName("재시도 가능 여부를 확인할 수 있어야 한다 - 불가능한 경우")
    void shouldReturnFalseWhenCannotRetry() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.aJob()
                .retryCount(RetryCount.forJob().increment().increment()) // current=2, max=2 → canRetry=false
                .build();

        // When
        boolean canRetry = job.canRetry();

        // Then
        assertThat(canRetry).isFalse();
    }

    // ===== Cycle 10: 불변→가변 패턴 전환 테스트 =====

    @Test
    @DisplayName("markAsProcessing()는 동일한 객체를 변경해야 한다 (가변 패턴)")
    void shouldMutateStatusWhenMarkAsProcessing() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.aJob()
                .status(JobStatusFixture.pending())
                .build();

        // When
        job.markAsProcessing();

        // Then - 동일한 객체가 변경됨
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.processing());
    }

    @Test
    @DisplayName("markAsCompleted()는 동일한 객체를 변경해야 한다 (가변 패턴)")
    void shouldMutateStatusWhenMarkAsCompleted() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.aJob()
                .status(JobStatusFixture.processing())
                .build();
        String outputS3Key = "processed/output.jpg";

        // When
        job.markAsCompleted(outputS3Key, java.time.Clock.systemUTC());

        // Then - 동일한 객체가 변경됨
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.completed());
        assertThat(job.getOutputS3Key()).isEqualTo(outputS3Key);
        assertThat(job.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsFailed()는 동일한 객체를 변경해야 한다 (가변 패턴)")
    void shouldMutateStatusWhenMarkAsFailed() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.aJob()
                .status(JobStatusFixture.processing())
                .build();
        String errorMessage = "Processing failed";

        // When
        job.markAsFailed(errorMessage, java.time.Clock.systemUTC());

        // Then - 동일한 객체가 변경됨
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.failed());
        assertThat(job.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(job.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsCompleted()는 새 객체를 반환하지 않아야 한다 (동일 객체 변경)")
    void shouldNotReturnNewInstanceWhenMarkAsCompleted() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.aJob()
                .status(JobStatusFixture.processing())
                .build();

        // When
        job.markAsCompleted("output.jpg", java.time.Clock.systemUTC());

        // Then - 반환값이 void이므로 상태만 검증
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.completed());
    }

    // ========================================
    // Cycle 12: updatedAt + getIdValue() Tests
    // ========================================

    @Test
    @DisplayName("forNew()로 생성 시 updatedAt이 설정되어야 한다")
    void shouldHaveUpdatedAtWhenCreated() {
        // Given
        java.time.Clock fixedClock = java.time.Clock.fixed(
                java.time.Instant.parse("2025-01-15T10:00:00Z"),
                java.time.ZoneId.of("UTC")
        );

        // When
        FileProcessingJob job = FileProcessingJob.forNew(
                FileIdFixture.aFileId(),
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg",
                fixedClock
        );

        // Then
        assertThat(job.getUpdatedAt()).isNotNull();
        assertThat(job.getUpdatedAt()).isEqualTo(job.getCreatedAt());
    }

    @Test
    @DisplayName("markAsCompleted() 호출 시 updatedAt이 갱신되어야 한다")
    void shouldUpdateUpdatedAtWhenMarkAsCompleted() {
        // Given
        java.time.Clock initialClock = java.time.Clock.fixed(
                java.time.Instant.parse("2025-01-15T10:00:00Z"),
                java.time.ZoneId.of("UTC")
        );
        java.time.Clock updatedClock = java.time.Clock.fixed(
                java.time.Instant.parse("2025-01-15T11:00:00Z"),
                java.time.ZoneId.of("UTC")
        );

        FileProcessingJob job = FileProcessingJob.forNew(
                FileIdFixture.aFileId(),
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg",
                initialClock
        );

        java.time.LocalDateTime initialUpdatedAt = job.getUpdatedAt();

        // When
        job.markAsCompleted("output.jpg", updatedClock);

        // Then
        assertThat(job.getUpdatedAt()).isNotNull();
        assertThat(job.getUpdatedAt()).isNotEqualTo(initialUpdatedAt);
        assertThat(job.getUpdatedAt()).isEqualTo(java.time.LocalDateTime.now(updatedClock));
    }

    @Test
    @DisplayName("markAsFailed() 호출 시 updatedAt이 갱신되어야 한다")
    void shouldUpdateUpdatedAtWhenMarkAsFailed() {
        // Given
        java.time.Clock initialClock = java.time.Clock.fixed(
                java.time.Instant.parse("2025-01-15T10:00:00Z"),
                java.time.ZoneId.of("UTC")
        );
        java.time.Clock updatedClock = java.time.Clock.fixed(
                java.time.Instant.parse("2025-01-15T11:00:00Z"),
                java.time.ZoneId.of("UTC")
        );

        FileProcessingJob job = FileProcessingJob.forNew(
                FileIdFixture.aFileId(),
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg",
                initialClock
        );

        java.time.LocalDateTime initialUpdatedAt = job.getUpdatedAt();

        // When
        job.markAsFailed("Processing error", updatedClock);

        // Then
        assertThat(job.getUpdatedAt()).isNotNull();
        assertThat(job.getUpdatedAt()).isNotEqualTo(initialUpdatedAt);
        assertThat(job.getUpdatedAt()).isEqualTo(java.time.LocalDateTime.now(updatedClock));
    }

    @Test
    @DisplayName("getJobIdValue()는 JobId의 원시 값을 반환해야 한다 (Law of Demeter)")
    void shouldReturnJobIdValueWithoutChaining() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.aJob().build();

        // When
        String jobIdValue = job.getJobIdValue();

        // Then
        assertThat(jobIdValue).isNotNull();
        assertThat(jobIdValue).isEqualTo(job.getJobId().getValue());
    }

    @Test
    @DisplayName("getFileIdValue()는 FileId의 원시 값을 반환해야 한다 (Law of Demeter)")
    void shouldReturnFileIdValueWithoutChaining() {
        // Given
        String expectedFileId = "file-uuid-v7-123";
        FileProcessingJob job = FileProcessingJobFixture.aJob()
                .fileId(FileId.of(expectedFileId))
                .build();

        // When
        String fileIdValue = job.getFileIdValue();

        // Then
        assertThat(fileIdValue).isNotNull();
        assertThat(fileIdValue).isEqualTo(expectedFileId);
    }
}
