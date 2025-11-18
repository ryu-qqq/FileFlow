package com.ryuqq.fileflow.domain.iam.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UploaderId Value Object 테스트
 * <p>
 * Long 타입 FK VO 패턴 (MessageOutboxId 기반)
 * </p>
 */
class UploaderIdTest {

    @Test
    @DisplayName("유효한 ID 값으로 UploaderId를 생성해야 한다")
    void shouldCreateValidUploaderId() {
        // given
        Long validId = 12345L;

        // when
        UploaderId uploaderId = UploaderId.of(validId);

        // then
        assertThat(uploaderId).isNotNull();
        assertThat(uploaderId.getValue()).isEqualTo(validId);
    }

    @Test
    @DisplayName("null 값으로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNull() {
        // given
        Long nullId = null;

        // when & then
        assertThatThrownBy(() -> UploaderId.of(nullId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Uploader ID는 null일 수 없습니다");
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환해야 한다")
    void shouldReturnSameValueFromGetValue() {
        // given
        Long expectedValue = 99999L;
        UploaderId uploaderId = UploaderId.of(expectedValue);

        // when
        Long actualValue = uploaderId.getValue();

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("같은 값을 가진 UploaderId는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        Long id = 12345L;
        UploaderId uploaderId1 = UploaderId.of(id);
        UploaderId uploaderId2 = UploaderId.of(id);

        // when & then
        assertThat(uploaderId1).isEqualTo(uploaderId2);
    }

    @Test
    @DisplayName("같은 값을 가진 UploaderId는 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenValueIsSame() {
        // given
        Long id = 12345L;
        UploaderId uploaderId1 = UploaderId.of(id);
        UploaderId uploaderId2 = UploaderId.of(id);

        // when & then
        assertThat(uploaderId1.hashCode()).isEqualTo(uploaderId2.hashCode());
    }

    @Test
    @DisplayName("forNew()는 null 값을 가진 UploaderId를 생성해야 한다")
    void shouldCreateNullUploaderIdWithForNew() {
        // when
        UploaderId uploaderId = UploaderId.forNew();

        // then
        assertThat(uploaderId).isNotNull();
        assertThat(uploaderId.getValue()).isNull();
        assertThat(uploaderId.isNew()).isTrue();
    }

    @Test
    @DisplayName("isNew()는 값이 null인 경우 true를 반환해야 한다")
    void shouldReturnTrueWhenIsNewForNullValue() {
        // given
        UploaderId uploaderId = UploaderId.forNew();

        // when & then
        assertThat(uploaderId.isNew()).isTrue();
    }

    @Test
    @DisplayName("isNew()는 값이 있는 경우 false를 반환해야 한다")
    void shouldReturnFalseWhenIsNewForNonNullValue() {
        // given
        UploaderId uploaderId = UploaderId.of(12345L);

        // when & then
        assertThat(uploaderId.isNew()).isFalse();
    }
}
