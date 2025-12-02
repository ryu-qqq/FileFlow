package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ProcessedFileAssetId 단위 테스트")
class ProcessedFileAssetIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 ID를 생성할 수 있다")
        void forNew_ShouldCreateNewId() {
            // given & when
            ProcessedFileAssetId id1 = ProcessedFileAssetId.forNew();
            ProcessedFileAssetId id2 = ProcessedFileAssetId.forNew();

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
            ProcessedFileAssetId id = ProcessedFileAssetId.of(uuid);

            // then
            assertThat(id.value()).isEqualTo(uuid);
        }

        @Test
        @DisplayName("of(String)로 문자열 UUID를 가진 ID를 생성할 수 있다")
        void of_WithStringUUID_ShouldCreateIdWithSpecificValue() {
            // given
            String uuidString = "550e8400-e29b-41d4-a716-446655440002";
            UUID expectedUuid = UUID.fromString(uuidString);

            // when
            ProcessedFileAssetId id = ProcessedFileAssetId.of(uuidString);

            // then
            assertThat(id.value()).isEqualTo(expectedUuid);
        }

        @Test
        @DisplayName("null UUID로 생성 시 예외가 발생한다")
        void of_WithNullUUID_ShouldThrowException() {
            // given
            UUID nullUuid = null;

            // when & then
            assertThatThrownBy(() -> ProcessedFileAssetId.of(nullUuid))
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
            ProcessedFileAssetId id1 = ProcessedFileAssetId.of(uuid);
            ProcessedFileAssetId id2 = ProcessedFileAssetId.of(uuid);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 UUID를 가진 ID는 동등하지 않다")
        void equals_WithDifferentUUID_ShouldNotBeEqual() {
            // given
            ProcessedFileAssetId id1 = ProcessedFileAssetId.forNew();
            ProcessedFileAssetId id2 = ProcessedFileAssetId.forNew();

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("기타 메서드 테스트")
    class UtilityMethodTest {

        @Test
        @DisplayName("getValue()는 UUID 문자열을 반환한다")
        void getValue_ShouldReturnUUIDString() {
            // given
            ProcessedFileAssetId id = ProcessedFileAssetId.forNew();

            // when
            String value = id.getValue();

            // then
            assertThat(value).isEqualTo(id.value().toString());
            assertThat(UUID.fromString(value)).isEqualTo(id.value());
        }
    }
}
