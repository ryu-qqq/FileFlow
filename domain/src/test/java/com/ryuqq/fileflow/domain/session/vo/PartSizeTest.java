package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.session.fixture.PartSizeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PartSize 단위 테스트")
class PartSizeTest {

    private static final long MIN_BYTES = 5L * 1024 * 1024;
    private static final long MAX_BYTES = 5L * 1024 * 1024 * 1024;

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 바이트 값으로 PartSize를 생성할 수 있다")
        void of_WithValidBytes_ShouldCreatePartSize() {
            PartSize partSize = PartSize.of(10 * 1024 * 1024L);

            assertThat(partSize.bytes()).isEqualTo(10 * 1024 * 1024L);
        }

        @Test
        @DisplayName("최소값 미만이면 예외가 발생한다")
        void of_WithLessThanMin_ShouldThrowException() {
            long invalidBytes = MIN_BYTES - 1;

            assertThatThrownBy(() -> PartSize.of(invalidBytes))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part 크기는 5MB ~ 5GB 사이여야 합니다");
        }

        @Test
        @DisplayName("최대값 초과 시 예외가 발생한다")
        void of_WithGreaterThanMax_ShouldThrowException() {
            long invalidBytes = MAX_BYTES + 1;

            assertThatThrownBy(() -> PartSize.of(invalidBytes))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Part 크기는 5MB ~ 5GB 사이여야 합니다");
        }
    }

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("MB 단위 생성 테스트")
    class OfMegaBytesTest {

        @Test
        @DisplayName("MB 단위로 PartSize를 생성할 수 있다")
        void ofMegaBytes_ShouldCreatePartSize() {
            PartSize partSize = PartSize.ofMegaBytes(64);

            assertThat(partSize.bytes()).isEqualTo(64L * 1024 * 1024);
        }
    }

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("MB 변환 테스트")
    class ToMegaBytesTest {

        @Test
        @DisplayName("바이트 값을 MB 단위로 변환한다")
        void toMegaBytes_ShouldReturnMegaBytes() {
            PartSize partSize = PartSize.of(128L * 1024 * 1024);

            assertThat(partSize.toMegaBytes()).isEqualTo(128L);
        }
    }

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가지면 동등하다")
        void equals_WithSameValue_ShouldBeEqual() {
            PartSize partSize1 = PartSize.of(16L * 1024 * 1024);
            PartSize partSize2 = PartSize.of(16L * 1024 * 1024);

            assertThat(partSize1).isEqualTo(partSize2);
            assertThat(partSize1.hashCode()).isEqualTo(partSize2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가지면 동등하지 않다")
        void equals_WithDifferentValue_ShouldNotBeEqual() {
            PartSize partSize1 = PartSize.of(16L * 1024 * 1024);
            PartSize partSize2 = PartSize.of(32L * 1024 * 1024);

            assertThat(partSize1).isNotEqualTo(partSize2);
        }
    }

    @Nested
    @SuppressWarnings("unused")
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 PartSize가 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            PartSize defaultPartSize = PartSizeFixture.defaultPartSize();
            PartSize minimumPartSize = PartSizeFixture.minimumPartSize();
            PartSize largePartSize = PartSizeFixture.largePartSize();
            PartSize customPartSize = PartSizeFixture.customPartSize(20 * 1024 * 1024L);

            assertThat(defaultPartSize.bytes()).isEqualTo(10 * 1024 * 1024L);
            assertThat(minimumPartSize.bytes()).isEqualTo(MIN_BYTES);
            assertThat(largePartSize.bytes()).isEqualTo(100 * 1024 * 1024L);
            assertThat(customPartSize.bytes()).isEqualTo(20 * 1024 * 1024L);
        }
    }
}
