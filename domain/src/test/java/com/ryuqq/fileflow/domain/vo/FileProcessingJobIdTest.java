package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileProcessingJobId Value Object 테스트
 */
class FileProcessingJobIdTest {

    @Test
    @DisplayName("유효한 ID 값으로 FileProcessingJobId를 생성해야 한다")
    void shouldCreateValidFileProcessingJobId() {
        // given
        String validId = "01JCQM5K3P9XYZ123456ABCD";

        // when
        FileProcessingJobId jobId = FileProcessingJobId.of(validId);

        // then
        assertThat(jobId).isNotNull();
        assertThat(jobId.getValue()).isEqualTo(validId);
    }

    @Test
    @DisplayName("null 값으로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNull() {
        // given
        String nullId = null;

        // when & then
        assertThatThrownBy(() -> FileProcessingJobId.of(nullId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileProcessingJob ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("빈 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsBlank() {
        // given
        String blankId = "   ";

        // when & then
        assertThatThrownBy(() -> FileProcessingJobId.of(blankId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileProcessingJob ID는 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환해야 한다")
    void shouldReturnSameValueFromGetValue() {
        // given
        String expectedValue = "01JCQM5K3P9XYZ123456ABCD";
        FileProcessingJobId jobId = FileProcessingJobId.of(expectedValue);

        // when
        String actualValue = jobId.getValue();

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("같은 값을 가진 FileProcessingJobId는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String id = "01JCQM5K3P9XYZ123456ABCD";
        FileProcessingJobId jobId1 = FileProcessingJobId.of(id);
        FileProcessingJobId jobId2 = FileProcessingJobId.of(id);

        // when & then
        assertThat(jobId1).isEqualTo(jobId2);
    }

    @Test
    @DisplayName("같은 값을 가진 FileProcessingJobId는 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenValueIsSame() {
        // given
        String id = "01JCQM5K3P9XYZ123456ABCD";
        FileProcessingJobId jobId1 = FileProcessingJobId.of(id);
        FileProcessingJobId jobId2 = FileProcessingJobId.of(id);

        // when & then
        assertThat(jobId1.hashCode()).isEqualTo(jobId2.hashCode());
    }

    @Test
    @DisplayName("forNew()는 null 값을 가진 FileProcessingJobId를 생성해야 한다")
    void shouldCreateNullFileProcessingJobIdWithForNew() {
        // when
        FileProcessingJobId jobId = FileProcessingJobId.forNew();

        // then
        assertThat(jobId).isNotNull();
        assertThat(jobId.getValue()).isNull();
        assertThat(jobId.isNew()).isTrue();
    }

    @Test
    @DisplayName("isNew()는 값이 null인 경우 true를 반환해야 한다")
    void shouldReturnTrueWhenIsNewForNullValue() {
        // given
        FileProcessingJobId jobId = FileProcessingJobId.forNew();

        // when & then
        assertThat(jobId.isNew()).isTrue();
    }

    @Test
    @DisplayName("isNew()는 값이 있는 경우 false를 반환해야 한다")
    void shouldReturnFalseWhenIsNewForNonNullValue() {
        // given
        FileProcessingJobId jobId = FileProcessingJobId.of("01JCQM5K3P9XYZ123456ABCD");

        // when & then
        assertThat(jobId.isNew()).isFalse();
    }
}
