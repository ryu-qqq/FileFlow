package com.ryuqq.fileflow.adapter.out.persistence.redis.download.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadUrlBlacklistCacheAdapter 단위 테스트")
class DownloadUrlBlacklistCacheAdapterTest {

    private static final String KEY_PREFIX = "fileflow:download:blacklist::";

    @InjectMocks private DownloadUrlBlacklistCacheAdapter sut;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @Nested
    @DisplayName("register 메서드")
    class RegisterTest {

        @Test
        @DisplayName("URL과 reason을 SHA-256 해시 키로 Redis에 TTL과 함께 저장한다")
        void register_ValidUrlAndReason_StoresWithTtl() {
            // given
            String sourceUrl = "https://example.com/malicious.jpg";
            String reason = "403 Forbidden";
            Duration ttl = Duration.ofHours(24);
            String expectedKey = KEY_PREFIX + sha256(sourceUrl);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            sut.register(sourceUrl, reason, ttl);

            // then
            then(valueOperations).should().set(expectedKey, reason, ttl);
        }
    }

    @Nested
    @DisplayName("isBlacklisted 메서드")
    class IsBlacklistedTest {

        @Test
        @DisplayName("키가 존재하면 true를 반환한다")
        void isBlacklisted_KeyExists_ReturnsTrue() {
            // given
            String sourceUrl = "https://example.com/blocked.png";
            String expectedKey = KEY_PREFIX + sha256(sourceUrl);

            given(redisTemplate.hasKey(expectedKey)).willReturn(Boolean.TRUE);

            // when
            boolean result = sut.isBlacklisted(sourceUrl);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("키가 존재하지 않으면 false를 반환한다")
        void isBlacklisted_KeyNotExists_ReturnsFalse() {
            // given
            String sourceUrl = "https://example.com/safe.png";
            String expectedKey = KEY_PREFIX + sha256(sourceUrl);

            given(redisTemplate.hasKey(expectedKey)).willReturn(Boolean.FALSE);

            // when
            boolean result = sut.isBlacklisted(sourceUrl);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("hasKey가 null을 반환하면 false를 반환한다")
        void isBlacklisted_HasKeyReturnsNull_ReturnsFalse() {
            // given
            String sourceUrl = "https://example.com/unknown.png";
            String expectedKey = KEY_PREFIX + sha256(sourceUrl);

            given(redisTemplate.hasKey(expectedKey)).willReturn(null);

            // when
            boolean result = sut.isBlacklisted(sourceUrl);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("generateKey SHA-256 일관성")
    class KeyGenerationTest {

        @Test
        @DisplayName("동일 URL은 항상 동일한 키를 생성한다")
        void generateKey_SameUrl_ProducesSameKey() {
            // given
            String sourceUrl = "https://example.com/image.jpg";
            String expectedKey = KEY_PREFIX + sha256(sourceUrl);

            given(redisTemplate.hasKey(expectedKey)).willReturn(Boolean.TRUE);

            // when
            boolean firstCall = sut.isBlacklisted(sourceUrl);

            // then
            assertThat(firstCall).isTrue();
            then(redisTemplate).should().hasKey(expectedKey);
        }

        @Test
        @DisplayName("다른 URL은 다른 키를 생성한다")
        void generateKey_DifferentUrls_ProduceDifferentKeys() {
            // given
            String url1 = "https://example.com/image1.jpg";
            String url2 = "https://example.com/image2.jpg";

            String hash1 = sha256(url1);
            String hash2 = sha256(url2);

            // then
            assertThat(hash1).isNotEqualTo(hash2);
            assertThat(KEY_PREFIX + hash1).isNotEqualTo(KEY_PREFIX + hash2);
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
