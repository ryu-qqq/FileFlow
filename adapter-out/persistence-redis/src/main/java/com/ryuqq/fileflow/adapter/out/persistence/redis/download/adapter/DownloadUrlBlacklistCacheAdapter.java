package com.ryuqq.fileflow.adapter.out.persistence.redis.download.adapter;

import com.ryuqq.fileflow.application.download.port.out.cache.DownloadUrlBlacklistPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DownloadUrlBlacklistCacheAdapter implements DownloadUrlBlacklistPort {

    private static final String KEY_PREFIX = "fileflow:download:blacklist::";

    private final StringRedisTemplate redisTemplate;

    public DownloadUrlBlacklistCacheAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void register(String sourceUrl, String reason, Duration ttl) {
        String key = generateKey(sourceUrl);
        redisTemplate.opsForValue().set(key, reason, ttl);
    }

    @Override
    public boolean isBlacklisted(String sourceUrl) {
        String key = generateKey(sourceUrl);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private String generateKey(String sourceUrl) {
        return KEY_PREFIX + sha256(sourceUrl);
    }

    private String sha256(String input) {
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
