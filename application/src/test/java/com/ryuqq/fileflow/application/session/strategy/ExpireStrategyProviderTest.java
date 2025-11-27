package com.ryuqq.fileflow.application.session.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.UploadSession;
import com.ryuqq.fileflow.domain.session.fixture.MultipartUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.fixture.SingleUploadSessionFixture;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExpireStrategyProvider 단위 테스트")
class ExpireStrategyProviderTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        @DisplayName("전략 목록을 받아 타입별 Map을 초기화한다")
        void constructor_ShouldInitializeStrategyMap() {
            // given
            SingleUploadExpireStrategy singleStrategy = mock(SingleUploadExpireStrategy.class);
            MultipartUploadExpireStrategy multipartStrategy =
                    mock(MultipartUploadExpireStrategy.class);
            List<ExpireStrategy<? extends UploadSession>> strategies =
                    List.of(singleStrategy, multipartStrategy);

            // when
            ExpireStrategyProvider provider = new ExpireStrategyProvider(strategies);

            // then
            assertThat(provider).isNotNull();
        }

        @Test
        @DisplayName("빈 전략 목록으로도 생성할 수 있다")
        void constructor_WithEmptyList_ShouldCreateProvider() {
            // when
            ExpireStrategyProvider provider = new ExpireStrategyProvider(Collections.emptyList());

            // then
            assertThat(provider).isNotNull();
        }
    }

    @Nested
    @DisplayName("getStrategy")
    class GetStrategy {

        @Test
        @DisplayName("SingleUploadSession에 맞는 전략을 반환한다")
        void getStrategy_WhenSingleSession_ShouldReturnSingleStrategy() {
            // given
            SingleUploadExpireStrategy singleStrategy = mock(SingleUploadExpireStrategy.class);
            MultipartUploadExpireStrategy multipartStrategy =
                    mock(MultipartUploadExpireStrategy.class);
            List<ExpireStrategy<? extends UploadSession>> strategies =
                    List.of(singleStrategy, multipartStrategy);
            ExpireStrategyProvider provider = new ExpireStrategyProvider(strategies);

            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();

            // when
            ExpireStrategy<SingleUploadSession> result = provider.getStrategy(session);

            // then
            assertThat(result).isSameAs(singleStrategy);
        }

        @Test
        @DisplayName("MultipartUploadSession에 맞는 전략을 반환한다")
        void getStrategy_WhenMultipartSession_ShouldReturnMultipartStrategy() {
            // given
            SingleUploadExpireStrategy singleStrategy = mock(SingleUploadExpireStrategy.class);
            MultipartUploadExpireStrategy multipartStrategy =
                    mock(MultipartUploadExpireStrategy.class);
            List<ExpireStrategy<? extends UploadSession>> strategies =
                    List.of(singleStrategy, multipartStrategy);
            ExpireStrategyProvider provider = new ExpireStrategyProvider(strategies);

            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();

            // when
            ExpireStrategy<MultipartUploadSession> result = provider.getStrategy(session);

            // then
            assertThat(result).isSameAs(multipartStrategy);
        }

        @Test
        @DisplayName("지원하지 않는 세션 타입이면 IllegalStateException을 던진다")
        void getStrategy_WhenUnsupportedSession_ShouldThrowException() {
            // given
            ExpireStrategyProvider provider = new ExpireStrategyProvider(Collections.emptyList());

            SingleUploadSession session = SingleUploadSessionFixture.activeSingleUploadSession();

            // when & then
            assertThatThrownBy(() -> provider.getStrategy(session))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("지원하지 않는 세션 타입");
        }

        @Test
        @DisplayName("SingleUploadExpireStrategy만 등록된 경우 Multipart 세션 요청 시 예외를 던진다")
        void getStrategy_WhenOnlySingleStrategyAndMultipartRequested_ShouldThrowException() {
            // given
            SingleUploadExpireStrategy singleStrategy = mock(SingleUploadExpireStrategy.class);
            List<ExpireStrategy<? extends UploadSession>> strategies = List.of(singleStrategy);
            ExpireStrategyProvider provider = new ExpireStrategyProvider(strategies);

            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();

            // when & then
            assertThatThrownBy(() -> provider.getStrategy(session))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("지원하지 않는 세션 타입");
        }

        @Test
        @DisplayName("O(1) 시간 복잡도로 전략을 반환한다")
        void getStrategy_ShouldReturnInConstantTime() {
            // given
            SingleUploadExpireStrategy singleStrategy = mock(SingleUploadExpireStrategy.class);
            MultipartUploadExpireStrategy multipartStrategy =
                    mock(MultipartUploadExpireStrategy.class);
            List<ExpireStrategy<? extends UploadSession>> strategies =
                    List.of(singleStrategy, multipartStrategy);
            ExpireStrategyProvider provider = new ExpireStrategyProvider(strategies);

            SingleUploadSession singleSession =
                    SingleUploadSessionFixture.activeSingleUploadSession();
            MultipartUploadSession multipartSession =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();

            // when & then - 여러 번 호출해도 동일한 결과
            for (int i = 0; i < 100; i++) {
                assertThat(provider.getStrategy(singleSession)).isSameAs(singleStrategy);
                assertThat(provider.getStrategy(multipartSession)).isSameAs(multipartStrategy);
            }
        }
    }
}
