package com.ryuqq.fileflow.domain.aggregate;

import com.ryuqq.fileflow.domain.fixture.FileProcessingJobFixture;
import com.ryuqq.fileflow.domain.fixture.JobStatusFixture;
import com.ryuqq.fileflow.domain.fixture.JobTypeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FileProcessingJob Aggregate Root 테스트")
class FileProcessingJobTest {

    @Test
    @DisplayName("유효한 데이터로 FileProcessingJob을 생성할 수 있어야 한다")
    void shouldCreateJobWithValidData() {
        // Given & When
        FileProcessingJob job = FileProcessingJobFixture.aJob()
                .jobType(JobTypeFixture.thumbnailGeneration())
                .fileId("file-uuid-v7-123")
                .inputS3Key("uploads/2024/01/image.jpg")
                .maxRetryCount(3)
                .build();

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getJobId()).isNotBlank();
        assertThat(job.getFileId()).isEqualTo("file-uuid-v7-123");
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
        assertThat(job.getJobId()).isNotBlank();
        assertThat(job.getFileId()).isNotBlank();
        assertThat(job.getJobType()).isNotNull();
        assertThat(job.getStatus()).isNotNull();
        assertThat(job.getInputS3Key()).isNotBlank();
        assertThat(job.getMaxRetryCount()).isPositive();
        assertThat(job.getCreatedAt()).isNotNull();
    }
}
