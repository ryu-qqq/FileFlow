package com.ryuqq.fileflow.domain.iam.usercontext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ExternalUserId Value Object 유효성 검증 테스트
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("ExternalUserId 테스트")
class ExternalUserIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 String 값으로 ExternalUserId를 생성할 수 있다")
        void createWithValidValue() {
            // given
            String value = "auth0|507f1f77bcf86cd799439011";

            // when
            ExternalUserId externalUserId = ExternalUserId.of(value);

            // then
            assertThat(externalUserId.value()).isEqualTo(value);
        }

        @Test
        @DisplayName("다양한 IDP sub claim 형식을 지원한다")
        void createWithVariousIdpFormats() {
            // Auth0
            assertThat(ExternalUserId.of("auth0|507f1f77bcf86cd799439011").value())
                .isEqualTo("auth0|507f1f77bcf86cd799439011");

            // Google
            assertThat(ExternalUserId.of("google-oauth2|108203290283982930829").value())
                .isEqualTo("google-oauth2|108203290283982930829");

            // GitHub
            assertThat(ExternalUserId.of("github|12345678").value())
                .isEqualTo("github|12345678");

            // Generic UUID
            assertThat(ExternalUserId.of("550e8400-e29b-41d4-a716-446655440000").value())
                .isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        }

        @Test
        @DisplayName("null 값으로 생성하면 예외가 발생한다")
        void createWithNull() {
            // when & then
            assertThatThrownBy(() -> ExternalUserId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("External User ID는 필수입니다");
        }

        @Test
        @DisplayName("빈 문자열로 생성하면 예외가 발생한다")
        void createWithEmpty() {
            // when & then
            assertThatThrownBy(() -> ExternalUserId.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("External User ID는 필수입니다");
        }

        @Test
        @DisplayName("공백 문자열로 생성하면 예외가 발생한다")
        void createWithBlank() {
            // when & then
            assertThatThrownBy(() -> ExternalUserId.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("External User ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값의 ExternalUserId는 동등하다")
        void equalityWithSameValue() {
            // given
            ExternalUserId id1 = ExternalUserId.of("auth0|507f1f77bcf86cd799439011");
            ExternalUserId id2 = ExternalUserId.of("auth0|507f1f77bcf86cd799439011");

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 ExternalUserId는 동등하지 않다")
        void inequalityWithDifferentValue() {
            // given
            ExternalUserId id1 = ExternalUserId.of("auth0|507f1f77bcf86cd799439011");
            ExternalUserId id2 = ExternalUserId.of("github|12345678");

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("Record로 구현되어 불변 객체이다")
        void isImmutable() {
            // given
            ExternalUserId externalUserId = ExternalUserId.of("auth0|507f1f77bcf86cd799439011");

            // when & then - value는 final이므로 변경 불가능
            assertThat(externalUserId.value()).isEqualTo("auth0|507f1f77bcf86cd799439011");
        }
    }
}
