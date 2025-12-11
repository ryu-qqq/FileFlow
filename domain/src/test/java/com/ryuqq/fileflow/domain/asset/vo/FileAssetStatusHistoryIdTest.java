package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** FileAssetStatusHistoryId 단위 테스트. */
@DisplayName("FileAssetStatusHistoryId 단위 테스트")
class FileAssetStatusHistoryIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 유효한 UUID를 생성할 수 있다")
        void shouldGenerateValidUuid() {
            // given & when
            FileAssetStatusHistoryId id1 = FileAssetStatusHistoryId.forNew();
            FileAssetStatusHistoryId id2 = FileAssetStatusHistoryId.forNew();

            // then
            assertThat(id1).isNotNull();
            assertThat(id2).isNotNull();
            assertThat(id1.value()).isNotNull();
            assertThat(id2.value()).isNotNull();
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("of(String)로 유효한 문자열에서 ID를 생성할 수 있다")
        void shouldCreateFromValidString() {
            // given
            String uuidString = "550e8400-e29b-41d4-a716-446655440001";
            UUID expectedUuid = UUID.fromString(uuidString);

            // when
            FileAssetStatusHistoryId id = FileAssetStatusHistoryId.of(uuidString);

            // then
            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(expectedUuid);
        }

        @Test
        @DisplayName("null 값으로 생성 시 예외가 발생한다")
        void shouldThrowWhenValueIsNull() {
            // given
            UUID nullUuid = null;

            // when & then
            assertThatThrownBy(() -> FileAssetStatusHistoryId.of(nullUuid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FileAssetStatusHistoryId는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 UUID를 가진 ID는 동등하다")
        void shouldBeEqualWithSameUuid() {
            // given
            UUID uuid = UUID.randomUUID();
            FileAssetStatusHistoryId id1 = FileAssetStatusHistoryId.of(uuid);
            FileAssetStatusHistoryId id2 = FileAssetStatusHistoryId.of(uuid);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 UUID를 가진 ID는 동등하지 않다")
        void shouldNotBeEqualWithDifferentUuid() {
            // given
            FileAssetStatusHistoryId id1 = FileAssetStatusHistoryId.forNew();
            FileAssetStatusHistoryId id2 = FileAssetStatusHistoryId.forNew();

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("기타 메서드 테스트")
    class UtilityMethodTest {

        @Test
        @DisplayName("getValue()는 UUID 문자열을 반환한다")
        void shouldReturnUuidStringFromGetValue() {
            // given
            FileAssetStatusHistoryId id = FileAssetStatusHistoryId.forNew();

            // when
            String value = id.getValue();

            // then
            assertThat(value).isEqualTo(id.value().toString());
            assertThat(UUID.fromString(value)).isEqualTo(id.value());
        }
    }
}
