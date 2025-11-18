package com.ryuqq.fileflow.domain.file.vo;

import com.ryuqq.fileflow.domain.fixture.FileIdFixture;
import com.ryuqq.fileflow.domain.file.fixture.JobStatusFixture;
import com.ryuqq.fileflow.domain.iam.vo.FileId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileProcessingJobSearchCriteria VO 테스트
 */
@DisplayName("FileProcessingJobSearchCriteria VO Tests")
class FileProcessingJobSearchCriteriaTest {

    @Test
    @DisplayName("모든 조건을 만족하는 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaWithAllConditions() {
        // given
        FileId fileId = FileIdFixture.aFileId();
        JobStatus jobStatus = JobStatusFixture.pending();
        String jobType = "THUMBNAIL";

        // when
        FileProcessingJobSearchCriteria criteria = FileProcessingJobSearchCriteria.of(fileId, jobStatus, jobType);

        // then
        assertThat(criteria.fileId()).isEqualTo(fileId);
        assertThat(criteria.jobStatus()).isEqualTo(jobStatus);
        assertThat(criteria.jobType()).isEqualTo(jobType);
    }

    @Test
    @DisplayName("fileId로만 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaByFileId() {
        // given
        FileId fileId = FileIdFixture.aFileId();

        // when
        FileProcessingJobSearchCriteria criteria = FileProcessingJobSearchCriteria.byFileId(fileId);

        // then
        assertThat(criteria.fileId()).isEqualTo(fileId);
        assertThat(criteria.jobStatus()).isNull();
        assertThat(criteria.jobType()).isNull();
    }

    @Test
    @DisplayName("jobStatus로만 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaByJobStatus() {
        // given
        JobStatus jobStatus = JobStatusFixture.pending();

        // when
        FileProcessingJobSearchCriteria criteria = FileProcessingJobSearchCriteria.byJobStatus(jobStatus);

        // then
        assertThat(criteria.fileId()).isNull();
        assertThat(criteria.jobStatus()).isEqualTo(jobStatus);
        assertThat(criteria.jobType()).isNull();
    }

    @Test
    @DisplayName("jobType으로만 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaByJobType() {
        // given
        String jobType = "THUMBNAIL";

        // when
        FileProcessingJobSearchCriteria criteria = FileProcessingJobSearchCriteria.byJobType(jobType);

        // then
        assertThat(criteria.fileId()).isNull();
        assertThat(criteria.jobStatus()).isNull();
        assertThat(criteria.jobType()).isEqualTo(jobType);
    }
}
