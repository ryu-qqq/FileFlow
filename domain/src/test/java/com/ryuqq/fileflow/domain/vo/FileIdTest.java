package com.ryuqq.fileflow.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileId Value Object 테스트
 */
class FileIdTest {

    @Test
    @DisplayName("유효한 ID 값으로 FileId를 생성해야 한다")
    void shouldCreateValidFileId() {
        // given
        String validId = "01JCQM5K3P9XYZ123456ABCD";

        // when
        FileId fileId = FileId.of(validId);

        // then
        assertThat(fileId).isNotNull();
        assertThat(fileId.getValue()).isEqualTo(validId);
    }

    @Test
    @DisplayName("null 값으로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNull() {
        // given
        String nullId = null;

        // when & then
        assertThatThrownBy(() -> FileId.of(nullId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File ID는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("빈 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsBlank() {
        // given
        String blankId = "   ";

        // when & then
        assertThatThrownBy(() -> FileId.of(blankId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File ID는 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환해야 한다")
    void shouldReturnSameValueFromGetValue() {
        // given
        String expectedValue = "01JCQM5K3P9XYZ123456ABCD";
        FileId fileId = FileId.of(expectedValue);

        // when
        String actualValue = fileId.getValue();

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("같은 값을 가진 FileId는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String id = "01JCQM5K3P9XYZ123456ABCD";
        FileId fileId1 = FileId.of(id);
        FileId fileId2 = FileId.of(id);

        // when & then
        assertThat(fileId1).isEqualTo(fileId2);
    }

    @Test
    @DisplayName("같은 값을 가진 FileId는 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenValueIsSame() {
        // given
        String id = "01JCQM5K3P9XYZ123456ABCD";
        FileId fileId1 = FileId.of(id);
        FileId fileId2 = FileId.of(id);

        // when & then
        assertThat(fileId1.hashCode()).isEqualTo(fileId2.hashCode());
    }

    @Test
    @DisplayName("forNew()는 null 값을 가진 FileId를 생성해야 한다")
    void shouldCreateNullFileIdWithForNew() {
        // when
        FileId fileId = FileId.forNew();

        // then
        assertThat(fileId).isNotNull();
        assertThat(fileId.getValue()).isNull();
        assertThat(fileId.isNew()).isTrue();
    }

    @Test
    @DisplayName("isNew()는 값이 null인 경우 true를 반환해야 한다")
    void shouldReturnTrueWhenIsNewForNullValue() {
        // given
        FileId fileId = FileId.forNew();

        // when & then
        assertThat(fileId.isNew()).isTrue();
    }

    @Test
    @DisplayName("isNew()는 값이 있는 경우 false를 반환해야 한다")
    void shouldReturnFalseWhenIsNewForNonNullValue() {
        // given
        FileId fileId = FileId.of("01JCQM5K3P9XYZ123456ABCD");

        // when & then
        assertThat(fileId.isNew()).isFalse();
    }
}
