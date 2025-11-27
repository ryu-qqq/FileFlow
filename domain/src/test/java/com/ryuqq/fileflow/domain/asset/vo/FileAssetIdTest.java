package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.asset.fixture.FileAssetIdFixture;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetId 단위 테스트")
class FileAssetIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 ID를 생성할 수 있다")
        void forNew_ShouldCreateNewId() {
            // given & when
            FileAssetId id1 = FileAssetId.forNew();
            FileAssetId id2 = FileAssetId.forNew();

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
            FileAssetId id = FileAssetId.of(uuid);

            // then
            assertThat(id.value()).isEqualTo(uuid);
        }

        @Test
        @DisplayName("of(String)로 문자열 UUID를 가진 ID를 생성할 수 있다")
        void of_WithStringUUID_ShouldCreateIdWithSpecificValue() {
            // given
            String uuidString = "550e8400-e29b-41d4-a716-446655440001";
            UUID expectedUuid = UUID.fromString(uuidString);

            // when
            FileAssetId id = FileAssetId.of(uuidString);

            // then
            assertThat(id.value()).isEqualTo(expectedUuid);
        }

        @Test
        @DisplayName("null UUID로 생성 시 예외가 발생한다")
        void of_WithNullUUID_ShouldThrowException() {
            // given
            UUID nullUuid = null;

            // when & then
            assertThatThrownBy(() -> FileAssetId.of(nullUuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileAssetId는 null일 수 없습니다");
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
            FileAssetId id1 = FileAssetId.of(uuid);
            FileAssetId id2 = FileAssetId.of(uuid);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 UUID를 가진 ID는 동등하지 않다")
        void equals_WithDifferentUUID_ShouldNotBeEqual() {
            // given
            FileAssetId id1 = FileAssetId.forNew();
            FileAssetId id2 = FileAssetId.forNew();

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 ID가 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            FileAssetId defaultId = FileAssetIdFixture.defaultFileAssetId();
            FileAssetId fixedId = FileAssetIdFixture.fixedFileAssetId();
            FileAssetId customId = FileAssetIdFixture.customFileAssetId(UUID.randomUUID());

            // then
            assertThat(defaultId).isNotNull();
            assertThat(fixedId).isNotNull();
            assertThat(customId).isNotNull();
            assertThat(fixedId.value().toString())
                    .isEqualTo("550e8400-e29b-41d4-a716-446655440001");
        }
    }

    @Nested
    @DisplayName("기타 메서드 테스트")
    class UtilityMethodTest {

        @Test
        @DisplayName("isNew()는 항상 false를 반환한다")
        void isNew_ShouldAlwaysReturnFalse() {
            // given
            FileAssetId id = FileAssetId.forNew();

            // when & then
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("getValue()는 UUID 문자열을 반환한다")
        void getValue_ShouldReturnUUIDString() {
            // given
            FileAssetId id = FileAssetId.forNew();

            // when
            String value = id.getValue();

            // then
            assertThat(value).isEqualTo(id.value().toString());
            assertThat(UUID.fromString(value)).isEqualTo(id.value());
        }
    }
}
