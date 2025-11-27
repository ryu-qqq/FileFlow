package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.fixture.UploadSessionIdFixture;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UploadSessionId 단위 테스트")
class UploadSessionIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 ID를 생성할 수 있다")
        void forNew_ShouldCreateNewId() {
            // given & when
            UploadSessionId id1 = UploadSessionId.forNew();
            UploadSessionId id2 = UploadSessionId.forNew();

            // then
            assertThat(id1).isNotNull();
            assertThat(id2).isNotNull();
            assertThat(id1).isNotEqualTo(id2); // 매번 다른 UUID 생성
            assertThat(id1.value()).isNotNull();
            assertThat(id2.value()).isNotNull();
        }

        @Test
        @DisplayName("of(UUID)로 특정 UUID를 가진 ID를 생성할 수 있다")
        void of_WithUUID_ShouldCreateIdWithSpecificValue() {
            // given
            UUID uuid = UUID.randomUUID();

            // when
            UploadSessionId id = UploadSessionId.of(uuid);

            // then
            assertThat(id.value()).isEqualTo(uuid);
        }

        @Test
        @DisplayName("of(String)로 문자열 UUID를 가진 ID를 생성할 수 있다")
        void of_WithStringUUID_ShouldCreateIdWithSpecificValue() {
            // given
            String uuidString = "550e8400-e29b-41d4-a716-446655440000";
            UUID expectedUuid = UUID.fromString(uuidString);

            // when
            UploadSessionId id = UploadSessionId.of(uuidString);

            // then
            assertThat(id.value()).isEqualTo(expectedUuid);
        }

        @Test
        @DisplayName("잘못된 형식의 문자열로 생성 시 예외가 발생한다")
        void of_WithInvalidStringFormat_ShouldThrowException() {
            // given
            String invalidUuid = "invalid-uuid-format";

            // when & then
            assertThatThrownBy(() -> UploadSessionId.of(invalidUuid))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null UUID로 생성 시 예외가 발생한다")
        void of_WithNullUUID_ShouldThrowException() {
            // given
            UUID nullUuid = null;

            // when & then
            assertThatThrownBy(() -> UploadSessionId.of(nullUuid))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 UUID를 가진 ID는 동등하다")
        void equals_WithSameUUID_ShouldBeEqual() {
            // given
            UUID uuid = UUID.randomUUID();
            UploadSessionId id1 = UploadSessionId.of(uuid);
            UploadSessionId id2 = UploadSessionId.of(uuid);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 UUID를 가진 ID는 동등하지 않다")
        void equals_WithDifferentUUID_ShouldNotBeEqual() {
            // given
            UploadSessionId id1 = UploadSessionId.forNew();
            UploadSessionId id2 = UploadSessionId.forNew();

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("null과 비교하면 동등하지 않다")
        void equals_WithNull_ShouldNotBeEqual() {
            // given
            UploadSessionId id = UploadSessionId.forNew();

            // when & then
            assertThat(id).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과 비교하면 동등하지 않다")
        void equals_WithDifferentType_ShouldNotBeEqual() {
            // given
            UploadSessionId id = UploadSessionId.forNew();
            String other = "different-type";

            // when & then
            assertThat(id).isNotEqualTo(other);
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 ID가 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            UploadSessionId defaultId = UploadSessionIdFixture.defaultUploadSessionId();
            UploadSessionId fixedId = UploadSessionIdFixture.fixedUploadSessionId();
            UploadSessionId customId =
                    UploadSessionIdFixture.customUploadSessionId(UUID.randomUUID());

            // then
            assertThat(defaultId).isNotNull();
            assertThat(fixedId).isNotNull();
            assertThat(customId).isNotNull();
            assertThat(fixedId.value().toString())
                    .isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString()이 적절한 형태로 출력된다")
        void toString_ShouldReturnAppropriateFormat() {
            // given
            UploadSessionId id = UploadSessionId.forNew();

            // when
            String result = id.toString();

            // then
            assertThat(result).contains("UploadSessionId");
            assertThat(result).contains(id.value().toString());
        }
    }
}
