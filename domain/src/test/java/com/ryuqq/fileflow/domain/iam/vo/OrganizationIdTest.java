package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.common.util.UuidV7Generator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("OrganizationId 테스트")
class OrganizationIdTest {

    @Nested
    @DisplayName("생성자")
    class ConstructorTest {

        @Test
        @DisplayName("유효한 UUIDv7로 생성할 수 있다")
        void shouldCreateWithValidUuidV7() {
            // given
            String validUuid = UuidV7Generator.generate();

            // when
            OrganizationId organizationId = new OrganizationId(validUuid);

            // then
            assertThat(organizationId.value()).isEqualTo(validUuid);
        }

        @Test
        @DisplayName("null 값은 예외를 던진다")
        void shouldThrowForNullValue() {
            assertThatThrownBy(() -> new OrganizationId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("OrganizationId")
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("빈 문자열은 예외를 던진다")
        void shouldThrowForEmptyString() {
            assertThatThrownBy(() -> new OrganizationId(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("OrganizationId");
        }

        @Test
        @DisplayName("잘못된 UUID 형식은 예외를 던진다")
        void shouldThrowForInvalidUuidFormat() {
            assertThatThrownBy(() -> new OrganizationId("invalid-uuid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("OrganizationId")
                    .hasMessageContaining("UUIDv7");
        }

        @Test
        @DisplayName("UUIDv4는 예외를 던진다")
        void shouldThrowForUuidV4() {
            // given - UUIDv4
            String uuidV4 = "550e8400-e29b-41d4-a716-446655440000";

            // when & then
            assertThatThrownBy(() -> new OrganizationId(uuidV4))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UUIDv7");
        }
    }

    @Nested
    @DisplayName("of() 정적 팩토리")
    class OfTest {

        @Test
        @DisplayName("유효한 값으로 생성할 수 있다")
        void shouldCreateWithValidValue() {
            // given
            String validUuid = UuidV7Generator.generate();

            // when
            OrganizationId organizationId = OrganizationId.of(validUuid);

            // then
            assertThat(organizationId.value()).isEqualTo(validUuid);
        }
    }

    @Nested
    @DisplayName("generate() 정적 팩토리")
    class GenerateTest {

        @Test
        @DisplayName("새로운 UUIDv7 기반 OrganizationId를 생성한다")
        void shouldGenerateNewOrganizationId() {
            // when
            OrganizationId organizationId = OrganizationId.generate();

            // then
            assertThat(organizationId).isNotNull();
            assertThat(organizationId.value()).hasSize(36);
            assertThat(UuidV7Generator.isValid(organizationId.value())).isTrue();
        }

        @Test
        @DisplayName("생성된 OrganizationId는 고유하다")
        void shouldGenerateUniqueOrganizationIds() {
            // when
            OrganizationId organizationId1 = OrganizationId.generate();
            OrganizationId organizationId2 = OrganizationId.generate();

            // then
            assertThat(organizationId1).isNotEqualTo(organizationId2);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 값을 가진 OrganizationId는 동등하다")
        void shouldBeEqualForSameValue() {
            // given
            String uuid = UuidV7Generator.generate();

            // when
            OrganizationId organizationId1 = OrganizationId.of(uuid);
            OrganizationId organizationId2 = OrganizationId.of(uuid);

            // then
            assertThat(organizationId1).isEqualTo(organizationId2);
            assertThat(organizationId1.hashCode()).isEqualTo(organizationId2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 OrganizationId는 동등하지 않다")
        void shouldNotBeEqualForDifferentValue() {
            // given
            OrganizationId organizationId1 = OrganizationId.generate();
            OrganizationId organizationId2 = OrganizationId.generate();

            // then
            assertThat(organizationId1).isNotEqualTo(organizationId2);
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringTest {

        @Test
        @DisplayName("toString()은 value를 반환한다")
        void shouldReturnValue() {
            // given
            String uuid = UuidV7Generator.generate();
            OrganizationId organizationId = OrganizationId.of(uuid);

            // when & then
            assertThat(organizationId.toString()).isEqualTo(uuid);
        }
    }
}
