package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadId 단위 테스트")
class ExternalDownloadIdTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("forNew()로 신규 ID를 생성할 수 있다")
        void forNew_ShouldCreateNewId() {
            // given & when
            ExternalDownloadId id = ExternalDownloadId.forNew();

            // then
            assertThat(id).isNotNull();
            assertThat(id.value()).isNotNull();
            assertThat(id.isNew()).isFalse(); // UUID는 항상 값이 있으므로 false
        }

        @Test
        @DisplayName("of(String)로 특정 값을 가진 ID를 생성할 수 있다")
        void of_WithString_ShouldCreateIdWithSpecificValue() {
            // given
            String uuidString = "00000000-0000-0000-0000-000000000100";
            UUID expectedUuid = UUID.fromString(uuidString);

            // when
            ExternalDownloadId id = ExternalDownloadId.of(uuidString);

            // then
            assertThat(id.value()).isEqualTo(expectedUuid);
            assertThat(id.getValue()).isEqualTo(uuidString);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("of(UUID)로 null 값을 전달하면 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given
            UUID nullValue = null;

            // when & then
            assertThatThrownBy(() -> ExternalDownloadId.of(nullValue))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("ExternalDownloadId");
        }

        @Test
        @DisplayName("of(String)로 잘못된 UUID 형식을 전달하면 예외가 발생한다")
        void of_WithInvalidFormat_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> ExternalDownloadId.of("invalid-uuid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 ID는 동등하다")
        void equals_WithSameValue_ShouldBeEqual() {
            // given
            ExternalDownloadId id1 = ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");
            ExternalDownloadId id2 = ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ID는 동등하지 않다")
        void equals_WithDifferentValue_ShouldNotBeEqual() {
            // given
            ExternalDownloadId id1 = ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");
            ExternalDownloadId id2 = ExternalDownloadId.of("00000000-0000-0000-0000-000000000002");

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("forNew()로 생성된 ID들은 다른 UUID를 가진다")
        void equals_ForNewIds_ShouldBeDifferent() {
            // given
            ExternalDownloadId id1 = ExternalDownloadId.forNew();
            ExternalDownloadId id2 = ExternalDownloadId.forNew();

            // when & then
            assertThat(id1).isNotEqualTo(id2); // 각각 다른 UUID 생성
        }
    }

    @Nested
    @DisplayName("isNew 테스트")
    class IsNewTest {

        @Test
        @DisplayName("forNew()로 생성된 ID도 isNew()가 false를 반환한다 (UUID는 항상 값 존재)")
        void isNew_ForNewId_ShouldReturnFalse() {
            // given
            ExternalDownloadId id = ExternalDownloadId.forNew();

            // when & then
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("of(Long)로 생성된 ID는 isNew()가 false를 반환한다")
        void isNew_ForExistingId_ShouldReturnFalse() {
            // given
            ExternalDownloadId id = ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");

            // when & then
            assertThat(id.isNew()).isFalse();
        }
    }
}
