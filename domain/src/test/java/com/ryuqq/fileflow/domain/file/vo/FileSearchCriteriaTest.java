package com.ryuqq.fileflow.domain.file.vo;

import com.ryuqq.fileflow.domain.file.fixture.FileStatusFixture;
import com.ryuqq.fileflow.domain.fixture.UploaderIdFixture;
import com.ryuqq.fileflow.domain.iam.vo.UploaderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileSearchCriteria VO 테스트
 */
@DisplayName("FileSearchCriteria VO Tests")
class FileSearchCriteriaTest {

    @Test
    @DisplayName("모든 조건을 만족하는 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaWithAllConditions() {
        // given
        UploaderId uploaderId = UploaderIdFixture.anUploaderId();
        FileStatus status = FileStatusFixture.completed();
        String category = "DOCUMENT";

        // when
        FileSearchCriteria criteria = FileSearchCriteria.of(uploaderId, status, category);

        // then
        assertThat(criteria.uploaderId()).isEqualTo(uploaderId);
        assertThat(criteria.status()).isEqualTo(status);
        assertThat(criteria.category()).isEqualTo(category);
    }

    @Test
    @DisplayName("uploaderId로만 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaByUploaderId() {
        // given
        UploaderId uploaderId = UploaderIdFixture.anUploaderId();

        // when
        FileSearchCriteria criteria = FileSearchCriteria.byUploaderId(uploaderId);

        // then
        assertThat(criteria.uploaderId()).isEqualTo(uploaderId);
        assertThat(criteria.status()).isNull();
        assertThat(criteria.category()).isNull();
    }

    @Test
    @DisplayName("status로만 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaByStatus() {
        // given
        FileStatus status = FileStatusFixture.completed();

        // when
        FileSearchCriteria criteria = FileSearchCriteria.byStatus(status);

        // then
        assertThat(criteria.uploaderId()).isNull();
        assertThat(criteria.status()).isEqualTo(status);
        assertThat(criteria.category()).isNull();
    }

    @Test
    @DisplayName("category로만 검색 조건을 생성할 수 있어야 한다")
    void shouldCreateSearchCriteriaByCategory() {
        // given
        String category = "IMAGE";

        // when
        FileSearchCriteria criteria = FileSearchCriteria.byCategory(category);

        // then
        assertThat(criteria.uploaderId()).isNull();
        assertThat(criteria.status()).isNull();
        assertThat(criteria.category()).isEqualTo(category);
    }
}
