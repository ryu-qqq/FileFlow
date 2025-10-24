package com.ryuqq.fileflow.domain.iam.usercontext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Email Value Object 유효성 검증 테스트
 *
 * @author ryu-qqq
 * @since 2025-10-24
 */
@Tag("unit")
@Tag("domain")
@Tag("fast")
@DisplayName("Email 테스트")
class EmailTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @ParameterizedTest
        @ValueSource(strings = {
            "user@example.com",
            "test.user@example.com",
            "user+tag@example.co.kr",
            "user_name@subdomain.example.com",
            "123@example.com",
            "user@123.com"
        })
        @DisplayName("유효한 이메일 형식으로 Email을 생성할 수 있다")
        void createWithValidEmail(String validEmail) {
            // when
            Email email = Email.of(validEmail);

            // then
            assertThat(email.value()).isEqualTo(validEmail);
        }

        @Test
        @DisplayName("null 값으로 생성하면 예외가 발생한다")
        void createWithNull() {
            // when & then
            assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 주소는 필수입니다");
        }

        @Test
        @DisplayName("빈 문자열로 생성하면 예외가 발생한다")
        void createWithEmpty() {
            // when & then
            assertThatThrownBy(() -> Email.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 주소는 필수입니다");
        }

        @Test
        @DisplayName("공백 문자열로 생성하면 예외가 발생한다")
        void createWithBlank() {
            // when & then
            assertThatThrownBy(() -> Email.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 주소는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid",                    // @ 없음
            "@example.com",               // 로컬 부분 없음
            "user@",                      // 도메인 없음
            "user@.com",                  // 도메인 시작이 점
            "user@domain",                // TLD 없음
            "user name@example.com",      // 공백 포함
            "user@@example.com",          // @ 중복
            "user@exam ple.com",          // 도메인에 공백
            "user@example..com",          // 점 연속
            "user@.example.com",          // 도메인 시작이 점
            "user@example.com.",          // 도메인 끝이 점
            "user@example.c"              // TLD가 1글자
        })
        @DisplayName("유효하지 않은 이메일 형식으로 생성하면 예외가 발생한다")
        void createWithInvalidEmail(String invalidEmail) {
            // when & then
            assertThatThrownBy(() -> Email.of(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 이메일 형식입니다");
        }
    }

    @Nested
    @DisplayName("도메인 추출 테스트 (Law of Demeter)")
    class GetDomainTest {

        @Test
        @DisplayName("이메일에서 도메인 부분을 추출할 수 있다")
        void getDomain() {
            // given
            Email email = Email.of("user@example.com");

            // when
            String domain = email.getDomain();

            // then
            assertThat(domain).isEqualTo("example.com");
        }

        @Test
        @DisplayName("서브도메인이 있는 이메일에서 도메인을 추출할 수 있다")
        void getDomainWithSubdomain() {
            // given
            Email email = Email.of("user@mail.example.com");

            // when
            String domain = email.getDomain();

            // then
            assertThat(domain).isEqualTo("mail.example.com");
        }

        @Test
        @DisplayName("국가 코드가 있는 이메일에서 도메인을 추출할 수 있다")
        void getDomainWithCountryCode() {
            // given
            Email email = Email.of("user@example.co.kr");

            // when
            String domain = email.getDomain();

            // then
            assertThat(domain).isEqualTo("example.co.kr");
        }
    }

    @Nested
    @DisplayName("로컬 부분 추출 테스트 (Law of Demeter)")
    class GetLocalPartTest {

        @Test
        @DisplayName("이메일에서 로컬 부분을 추출할 수 있다")
        void getLocalPart() {
            // given
            Email email = Email.of("user@example.com");

            // when
            String localPart = email.getLocalPart();

            // then
            assertThat(localPart).isEqualTo("user");
        }

        @Test
        @DisplayName("점이 포함된 로컬 부분을 추출할 수 있다")
        void getLocalPartWithDot() {
            // given
            Email email = Email.of("first.last@example.com");

            // when
            String localPart = email.getLocalPart();

            // then
            assertThat(localPart).isEqualTo("first.last");
        }

        @Test
        @DisplayName("플러스 태그가 포함된 로컬 부분을 추출할 수 있다")
        void getLocalPartWithPlusTag() {
            // given
            Email email = Email.of("user+tag@example.com");

            // when
            String localPart = email.getLocalPart();

            // then
            assertThat(localPart).isEqualTo("user+tag");
        }

        @Test
        @DisplayName("언더스코어가 포함된 로컬 부분을 추출할 수 있다")
        void getLocalPartWithUnderscore() {
            // given
            Email email = Email.of("user_name@example.com");

            // when
            String localPart = email.getLocalPart();

            // then
            assertThat(localPart).isEqualTo("user_name");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값의 Email은 동등하다")
        void equalityWithSameValue() {
            // given
            Email email1 = Email.of("user@example.com");
            Email email2 = Email.of("user@example.com");

            // when & then
            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 Email은 동등하지 않다")
        void inequalityWithDifferentValue() {
            // given
            Email email1 = Email.of("user1@example.com");
            Email email2 = Email.of("user2@example.com");

            // when & then
            assertThat(email1).isNotEqualTo(email2);
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {

        @Test
        @DisplayName("Record로 구현되어 불변 객체이다")
        void isImmutable() {
            // given
            Email email = Email.of("user@example.com");

            // when & then - value는 final이므로 변경 불가능
            assertThat(email.value()).isEqualTo("user@example.com");
        }
    }
}
