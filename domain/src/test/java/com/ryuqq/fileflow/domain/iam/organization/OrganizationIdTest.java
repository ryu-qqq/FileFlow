package com.ryuqq.fileflow.domain.iam.organization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OrganizationId 유효성 검증 테스트
 *
 * @author ryu-qqq
 * @since 2025-10-22
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("OrganizationId 테스트")
class OrganizationIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 Long 값으로 OrganizationId를 생성할 수 있다")
        void createWithValidValue() {
            // given
            Long validValue = 1L;

            // when
            OrganizationId organizationId = new OrganizationId(validValue);

            // then
            assertThat(organizationId.value()).isEqualTo(validValue);
        }

        @Test
        @DisplayName("양수 값으로 OrganizationId를 생성할 수 있다")
        void createWithPositiveValue() {
            // given
            Long positiveValue = 100L;

            // when
            OrganizationId organizationId = new OrganizationId(positiveValue);

            // then
            assertThat(organizationId.value()).isEqualTo(positiveValue);
        }

        @Test
        @DisplayName("null 값으로 생성하면 예외가 발생한다")
        void createWithNullValue() {
            // given
            Long nullValue = null;

            // when & then
            assertThatThrownBy(() -> new OrganizationId(nullValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization ID는 필수입니다");
        }

        @Test
        @DisplayName("0 값으로 생성하면 예외가 발생한다")
        void createWithZeroValue() {
            // given
            Long zeroValue = 0L;

            // when & then
            assertThatThrownBy(() -> new OrganizationId(zeroValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization ID는 양수여야 합니다");
        }

        @Test
        @DisplayName("음수 값으로 생성하면 예외가 발생한다")
        void createWithNegativeValue() {
            // given
            Long negativeValue = -1L;

            // when & then
            assertThatThrownBy(() -> new OrganizationId(negativeValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organization ID는 양수여야 합니다");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 OrganizationId는 동등하다")
        void equalityWithSameValue() {
            // given
            OrganizationId id1 = new OrganizationId(1L);
            OrganizationId id2 = new OrganizationId(1L);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 OrganizationId는 동등하지 않다")
        void inequalityWithDifferentValue() {
            // given
            OrganizationId id1 = new OrganizationId(1L);
            OrganizationId id2 = new OrganizationId(2L);

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }
    }
}
