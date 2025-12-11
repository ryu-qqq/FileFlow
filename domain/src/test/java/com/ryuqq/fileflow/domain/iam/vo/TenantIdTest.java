package com.ryuqq.fileflow.domain.iam.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.common.util.UuidV7Generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TenantId 테스트")
class TenantIdTest {

    @Nested
    @DisplayName("생성자")
    class ConstructorTest {

        @Test
        @DisplayName("유효한 UUIDv7로 생성할 수 있다")
        void shouldCreateWithValidUuidV7() {
            // given
            String validUuid = UuidV7Generator.generate();

            // when
            TenantId tenantId = new TenantId(validUuid);

            // then
            assertThat(tenantId.value()).isEqualTo(validUuid);
        }

        @Test
        @DisplayName("null 값은 예외를 던진다")
        void shouldThrowForNullValue() {
            assertThatThrownBy(() -> new TenantId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("TenantId")
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("빈 문자열은 예외를 던진다")
        void shouldThrowForEmptyString() {
            assertThatThrownBy(() -> new TenantId(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("TenantId");
        }

        @Test
        @DisplayName("잘못된 UUID 형식은 예외를 던진다")
        void shouldThrowForInvalidUuidFormat() {
            assertThatThrownBy(() -> new TenantId("invalid-uuid"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("TenantId")
                    .hasMessageContaining("UUIDv7");
        }

        @Test
        @DisplayName("UUIDv4는 예외를 던진다")
        void shouldThrowForUuidV4() {
            // given - UUIDv4
            String uuidV4 = "550e8400-e29b-41d4-a716-446655440000";

            // when & then
            assertThatThrownBy(() -> new TenantId(uuidV4))
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
            TenantId tenantId = TenantId.of(validUuid);

            // then
            assertThat(tenantId.value()).isEqualTo(validUuid);
        }
    }

    @Nested
    @DisplayName("generate() 정적 팩토리")
    class GenerateTest {

        @Test
        @DisplayName("새로운 UUIDv7 기반 TenantId를 생성한다")
        void shouldGenerateNewTenantId() {
            // when
            TenantId tenantId = TenantId.generate();

            // then
            assertThat(tenantId).isNotNull();
            assertThat(tenantId.value()).hasSize(36);
            assertThat(UuidV7Generator.isValid(tenantId.value())).isTrue();
        }

        @Test
        @DisplayName("생성된 TenantId는 고유하다")
        void shouldGenerateUniqueTenantIds() {
            // when
            TenantId tenantId1 = TenantId.generate();
            TenantId tenantId2 = TenantId.generate();

            // then
            assertThat(tenantId1).isNotEqualTo(tenantId2);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 값을 가진 TenantId는 동등하다")
        void shouldBeEqualForSameValue() {
            // given
            String uuid = UuidV7Generator.generate();

            // when
            TenantId tenantId1 = TenantId.of(uuid);
            TenantId tenantId2 = TenantId.of(uuid);

            // then
            assertThat(tenantId1).isEqualTo(tenantId2);
            assertThat(tenantId1.hashCode()).isEqualTo(tenantId2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 TenantId는 동등하지 않다")
        void shouldNotBeEqualForDifferentValue() {
            // given
            TenantId tenantId1 = TenantId.generate();
            TenantId tenantId2 = TenantId.generate();

            // then
            assertThat(tenantId1).isNotEqualTo(tenantId2);
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
            TenantId tenantId = TenantId.of(uuid);

            // when & then
            assertThat(tenantId.toString()).isEqualTo(uuid);
        }
    }
}
