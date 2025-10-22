package com.ryuqq.fileflow.domain.iam.tenant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TenantName 비즈니스 규칙 검증 테스트
 *
 * @author Claude
 * @since 2025-10-22
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("TenantName 테스트")
class TenantNameTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 이름으로 TenantName을 생성할 수 있다")
        void createWithValidName() {
            // given
            String validName = "Acme Corporation";

            // when
            TenantName tenantName = new TenantName(validName);

            // then
            assertThat(tenantName.getValue()).isEqualTo(validName);
        }

        @Test
        @DisplayName("앞뒤 공백은 자동으로 제거된다")
        void trimWhitespace() {
            // given
            String nameWithWhitespace = "  Acme Corp  ";

            // when
            TenantName tenantName = new TenantName(nameWithWhitespace);

            // then
            assertThat(tenantName.getValue()).isEqualTo("Acme Corp");
        }

        @Test
        @DisplayName("최소 길이(2자)의 이름을 생성할 수 있다")
        void createWithMinimumLength() {
            // given
            String minLengthName = "AB";

            // when
            TenantName tenantName = new TenantName(minLengthName);

            // then
            assertThat(tenantName.getValue()).isEqualTo(minLengthName);
        }

        @Test
        @DisplayName("최대 길이(50자)의 이름을 생성할 수 있다")
        void createWithMaximumLength() {
            // given
            String maxLengthName = "A".repeat(50);

            // when
            TenantName tenantName = new TenantName(maxLengthName);

            // then
            assertThat(tenantName.getValue()).isEqualTo(maxLengthName);
        }
    }

    @Nested
    @DisplayName("유효성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("null 값으로 생성하면 예외가 발생한다")
        void createWithNull() {
            // given
            String nullValue = null;

            // when & then
            assertThatThrownBy(() -> new TenantName(nullValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant 이름은 필수입니다");
        }

        @Test
        @DisplayName("빈 문자열로 생성하면 예외가 발생한다")
        void createWithEmpty() {
            // given
            String emptyValue = "";

            // when & then
            assertThatThrownBy(() -> new TenantName(emptyValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant 이름은 필수입니다");
        }

        @Test
        @DisplayName("공백만 있는 문자열로 생성하면 예외가 발생한다")
        void createWithBlank() {
            // given
            String blankValue = "   ";

            // when & then
            assertThatThrownBy(() -> new TenantName(blankValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant 이름은 필수입니다");
        }

        @Test
        @DisplayName("최소 길이(2자)보다 짧으면 예외가 발생한다")
        void createWithTooShort() {
            // given
            String tooShortName = "A";

            // when & then
            assertThatThrownBy(() -> new TenantName(tooShortName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최소 2자");
        }

        @Test
        @DisplayName("최대 길이(50자)를 초과하면 예외가 발생한다")
        void createWithTooLong() {
            // given
            String tooLongName = "A".repeat(51);

            // when & then
            assertThatThrownBy(() -> new TenantName(tooLongName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최대 50자");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 TenantName은 동등하다")
        void equalityWithSameValue() {
            // given
            TenantName name1 = new TenantName("Acme Corp");
            TenantName name2 = new TenantName("Acme Corp");

            // when & then
            assertThat(name1).isEqualTo(name2);
            assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 TenantName은 동등하지 않다")
        void inequalityWithDifferentValue() {
            // given
            TenantName name1 = new TenantName("Acme Corp");
            TenantName name2 = new TenantName("TechCorp");

            // when & then
            assertThat(name1).isNotEqualTo(name2);
        }
    }
}
