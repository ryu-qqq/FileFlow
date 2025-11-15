package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.fixture.FileIdFixture;
import com.ryuqq.fileflow.domain.fixture.FileProcessingJobFixture;
import com.ryuqq.fileflow.domain.fixture.FileProcessingJobIdFixture;
import com.ryuqq.fileflow.domain.fixture.JobStatusFixture;
import com.ryuqq.fileflow.domain.fixture.JobTypeFixture;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileProcessingJobId;
import com.ryuqq.fileflow.domain.vo.JobType;
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
                .maxRetryCount(3)
                .build();

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getJobId()).isNotNull();
        assertThat(job.getFileId()).isEqualTo(fileId);
        assertThat(job.getJobType()).isEqualTo(JobTypeFixture.thumbnailGeneration());
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.pending());
        assertThat(job.getInputS3Key()).isEqualTo("uploads/2024/01/image.jpg");
        assertThat(job.getMaxRetryCount()).isEqualTo(3);
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
        int maxRetryCount = 3;

        // When
        FileProcessingJob job = FileProcessingJob.forNew(
                fileId,
                jobType,
                inputS3Key,
                maxRetryCount,
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
        assertThat(job.getMaxRetryCount()).isEqualTo(maxRetryCount);
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
        FileProcessingJob job = FileProcessingJob.of(
                jobId,
                fileId,
                jobType,
                JobStatusFixture.pending(),
                0,
                3,
                inputS3Key,
                null,
                null,
                java.time.LocalDateTime.now(),
                null,
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
        assertThatThrownBy(() -> FileProcessingJob.of(
                nullJobId,
                fileId,
                JobTypeFixture.thumbnailGeneration(),
                JobStatusFixture.pending(),
                0,
                3,
                "uploads/image.jpg",
                null,
                null,
                java.time.LocalDateTime.now(),
                null,
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
        FileProcessingJob job = FileProcessingJob.reconstitute(
                jobId,
                fileId,
                jobType,
                JobStatusFixture.pending(),
                0,
                3,
                inputS3Key,
                null,
                null,
                java.time.LocalDateTime.now(),
                null,
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
        assertThatThrownBy(() -> FileProcessingJob.reconstitute(
                nullJobId,
                fileId,
                JobTypeFixture.thumbnailGeneration(),
                JobStatusFixture.pending(),
                0,
                3,
                "uploads/image.jpg",
                null,
                null,
                java.time.LocalDateTime.now(),
                null,
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
                3,
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
                3,
                fixedClock
        );

        // When
        FileProcessingJob completedJob = job.markAsCompleted("output.jpg", fixedClock);

        // Then
        assertThat(completedJob.getProcessedAt()).isEqualTo(java.time.LocalDateTime.of(2025, 1, 15, 12, 0, 0));
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
                3,
                fixedClock
        );

        // Then
        assertThat(job.getCreatedAt()).isEqualTo(java.time.LocalDateTime.of(2025, 1, 15, 14, 30, 0));
        assertThat(job.getProcessedAt()).isNull();
    }

    // ===== create() 팩토리 메서드 테스트 =====

    @Test
    @DisplayName("create() 팩토리 메서드로 UUID v7과 PENDING 상태로 작업을 생성해야 한다")
    void shouldCreateJobWithUuidV7AndPendingStatus() {
        // Given
        String fileIdValue = "file-uuid-v7-123";
        JobType jobType = JobTypeFixture.thumbnailGeneration();
        String inputS3Key = "uploads/2024/01/image.jpg";
        int maxRetryCount = 3;

        // When
        @SuppressWarnings("deprecation")
        FileProcessingJob job = FileProcessingJob.create(fileIdValue, jobType, inputS3Key, maxRetryCount);

        // Then
        assertThat(job.getJobId()).isNotNull(); // UUID v7 자동 생성
        assertThat(job.getJobId().getValue()).hasSize(36); // UUID 표준 길이
        assertThat(job.getFileId()).isEqualTo(FileId.of(fileIdValue));
        assertThat(job.getJobType()).isEqualTo(jobType);
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.pending()); // PENDING 상태
        assertThat(job.getInputS3Key()).isEqualTo(inputS3Key);
        assertThat(job.getMaxRetryCount()).isEqualTo(maxRetryCount);
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
                "uploads/image.jpg",
                3
        );
        assertThat(job.getStatus()).isEqualTo(JobStatusFixture.pending());

        // When
        FileProcessingJob processingJob = job.markAsProcessing();

        // Then
        assertThat(processingJob.getStatus()).isEqualTo(JobStatusFixture.processing());
        assertThat(processingJob.getProcessedAt()).isNull(); // 완료 전이므로 null
    }

    @Test
    @DisplayName("작업을 완료 처리하고 outputS3Key를 설정할 수 있어야 한다")
    void shouldMarkAsCompleted() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.createJob(
                "file-uuid-v7-123",
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg",
                3
        );
        String outputS3Key = "processed/2024/01/thumbnail.jpg";

        // When
        FileProcessingJob completedJob = job.markAsCompleted(outputS3Key, java.time.Clock.systemUTC());

        // Then
        assertThat(completedJob.getStatus()).isEqualTo(JobStatusFixture.completed());
        assertThat(completedJob.getOutputS3Key()).isEqualTo(outputS3Key);
        assertThat(completedJob.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("작업을 실패 처리하고 errorMessage를 설정할 수 있어야 한다")
    void shouldMarkAsFailed() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.createJob(
                "file-uuid-v7-123",
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg",
                3
        );
        String errorMessage = "Image processing failed: Invalid format";

        // When
        FileProcessingJob failedJob = job.markAsFailed(errorMessage, java.time.Clock.systemUTC());

        // Then
        assertThat(failedJob.getStatus()).isEqualTo(JobStatusFixture.failed());
        assertThat(failedJob.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(failedJob.getProcessedAt()).isNotNull();
    }

    // ===== 부가 메서드 테스트 =====

    @Test
    @DisplayName("재시도 횟수를 증가시킬 수 있어야 한다")
    void shouldIncrementRetryCount() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.createJob(
                "file-uuid-v7-123",
                JobTypeFixture.thumbnailGeneration(),
                "uploads/image.jpg",
                3
        );
        assertThat(job.getRetryCount()).isEqualTo(0);

        // When
        FileProcessingJob retriedJob = job.incrementRetryCount();

        // Then
        assertThat(retriedJob.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("재시도 가능 여부를 확인할 수 있어야 한다 - 가능한 경우")
    void shouldReturnTrueWhenCanRetry() {
        // Given
        FileProcessingJob job = FileProcessingJobFixture.aJob()
                .retryCount(2)
                .maxRetryCount(3)
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
                .retryCount(3)
                .maxRetryCount(3)
                .build();

        // When
        boolean canRetry = job.canRetry();

        // Then
        assertThat(canRetry).isFalse();
    }
}
