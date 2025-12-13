package com.ryuqq.fileflow.adapter.in.rest.asset.dto.command;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * DeleteFileAssetApiRequest 단위 테스트.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("DeleteFileAssetApiRequest 단위 테스트")
class DeleteFileAssetApiRequestTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("삭제 사유와 함께 요청을 생성할 수 있다")
        void create_WithReason_ShouldSucceed() {
            // given
            String reason = "더 이상 필요하지 않은 파일";

            // when
            DeleteFileAssetApiRequest request = new DeleteFileAssetApiRequest(reason);

            // then
            assertThat(request.reason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("삭제 사유 없이 요청을 생성할 수 있다")
        void create_WithNullReason_ShouldSucceed() {
            // when
            DeleteFileAssetApiRequest request = new DeleteFileAssetApiRequest(null);

            // then
            assertThat(request.reason()).isNull();
        }

        @Test
        @DisplayName("empty 팩토리 메서드로 사유 없이 요청을 생성할 수 있다")
        void create_WithEmpty_ShouldSucceed() {
            // when
            DeleteFileAssetApiRequest request = DeleteFileAssetApiRequest.empty();

            // then
            assertThat(request.reason()).isNull();
        }
    }

    @Nested
    @DisplayName("사유 테스트")
    class ReasonTest {

        @Test
        @DisplayName("빈 문자열 사유로 요청을 생성할 수 있다")
        void create_WithEmptyReason_ShouldSucceed() {
            // when
            DeleteFileAssetApiRequest request = new DeleteFileAssetApiRequest("");

            // then
            assertThat(request.reason()).isEmpty();
        }

        @Test
        @DisplayName("긴 사유로 요청을 생성할 수 있다")
        void create_WithLongReason_ShouldSucceed() {
            // given
            String longReason = "이 파일은 ".repeat(100) + "더 이상 필요하지 않습니다.";

            // when
            DeleteFileAssetApiRequest request = new DeleteFileAssetApiRequest(longReason);

            // then
            assertThat(request.reason()).isEqualTo(longReason);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 사유를 가진 요청은 동등하다")
        void equals_WithSameReason_ShouldBeEqual() {
            // given
            DeleteFileAssetApiRequest request1 = new DeleteFileAssetApiRequest("사유");
            DeleteFileAssetApiRequest request2 = new DeleteFileAssetApiRequest("사유");

            // when & then
            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("다른 사유를 가진 요청은 동등하지 않다")
        void equals_WithDifferentReason_ShouldNotBeEqual() {
            // given
            DeleteFileAssetApiRequest request1 = new DeleteFileAssetApiRequest("사유1");
            DeleteFileAssetApiRequest request2 = new DeleteFileAssetApiRequest("사유2");

            // when & then
            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("null 사유를 가진 요청들도 동등하다")
        void equals_WithNullReason_ShouldBeEqual() {
            // given
            DeleteFileAssetApiRequest request1 = new DeleteFileAssetApiRequest(null);
            DeleteFileAssetApiRequest request2 = DeleteFileAssetApiRequest.empty();

            // when & then
            assertThat(request1).isEqualTo(request2);
        }
    }
}
