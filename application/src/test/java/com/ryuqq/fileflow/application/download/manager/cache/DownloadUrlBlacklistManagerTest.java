package com.ryuqq.fileflow.application.download.manager.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.application.download.port.out.cache.DownloadUrlBlacklistPort;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadUrlBlacklistManager 단위 테스트")
class DownloadUrlBlacklistManagerTest {

    private static final Duration DEFAULT_BLACKLIST_TTL = Duration.ofHours(1);

    @InjectMocks private DownloadUrlBlacklistManager sut;
    @Mock private DownloadUrlBlacklistPort downloadUrlBlacklistPort;

    @Nested
    @DisplayName("registerBlacklist 메서드")
    class RegisterBlacklistTest {

        @Test
        @DisplayName("유효한 URL과 사유로 블랙리스트를 정상 등록한다")
        void registerBlacklist_ValidUrlAndReason_RegistersSuccessfully() {
            // given
            String sourceUrl = "https://example.com/image.jpg";
            String reason = "403 Forbidden";

            // when
            sut.registerBlacklist(sourceUrl, reason);

            // then
            then(downloadUrlBlacklistPort)
                    .should()
                    .register(sourceUrl, reason, DEFAULT_BLACKLIST_TTL);
        }

        @Test
        @DisplayName("Redis 예외 발생 시 예외를 무시하고 정상 종료한다")
        void registerBlacklist_RedisException_IgnoresAndContinues() {
            // given
            String sourceUrl = "https://example.com/image.jpg";
            String reason = "404 Not Found";

            willThrow(new RuntimeException("Redis connection refused"))
                    .given(downloadUrlBlacklistPort)
                    .register(sourceUrl, reason, DEFAULT_BLACKLIST_TTL);

            // when
            sut.registerBlacklist(sourceUrl, reason);

            // then
            then(downloadUrlBlacklistPort)
                    .should()
                    .register(sourceUrl, reason, DEFAULT_BLACKLIST_TTL);
        }
    }

    @Nested
    @DisplayName("isBlacklisted 메서드")
    class IsBlacklistedTest {

        @Test
        @DisplayName("블랙리스트에 등록된 URL이면 true를 반환한다")
        void isBlacklisted_BlacklistedUrl_ReturnsTrue() {
            // given
            String sourceUrl = "https://example.com/blocked.jpg";

            given(downloadUrlBlacklistPort.isBlacklisted(sourceUrl)).willReturn(true);

            // when
            boolean result = sut.isBlacklisted(sourceUrl);

            // then
            assertThat(result).isTrue();
            then(downloadUrlBlacklistPort).should().isBlacklisted(sourceUrl);
        }

        @Test
        @DisplayName("블랙리스트에 없는 URL이면 false를 반환한다")
        void isBlacklisted_NonBlacklistedUrl_ReturnsFalse() {
            // given
            String sourceUrl = "https://example.com/allowed.jpg";

            given(downloadUrlBlacklistPort.isBlacklisted(sourceUrl)).willReturn(false);

            // when
            boolean result = sut.isBlacklisted(sourceUrl);

            // then
            assertThat(result).isFalse();
            then(downloadUrlBlacklistPort).should().isBlacklisted(sourceUrl);
        }

        @Test
        @DisplayName("Redis 예외 발생 시 false를 반환하여 통과 처리한다")
        void isBlacklisted_RedisException_ReturnsFalseAsGracefulDegradation() {
            // given
            String sourceUrl = "https://example.com/image.jpg";

            given(downloadUrlBlacklistPort.isBlacklisted(sourceUrl))
                    .willThrow(new RuntimeException("Redis connection refused"));

            // when
            boolean result = sut.isBlacklisted(sourceUrl);

            // then
            assertThat(result).isFalse();
            then(downloadUrlBlacklistPort).should().isBlacklisted(sourceUrl);
        }
    }
}
