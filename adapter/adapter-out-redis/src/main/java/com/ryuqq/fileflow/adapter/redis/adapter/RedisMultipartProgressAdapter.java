package com.ryuqq.fileflow.adapter.redis.adapter;

import com.ryuqq.fileflow.application.upload.port.out.MultipartProgressPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Redis 기반 멀티파트 업로드 진행률 추적 어댑터
 *
 * Redis Hash를 활용하여 각 파트의 완료 상태를 추적합니다.
 *
 * Redis 구조:
 * - Key: "upload:multipart:progress:{sessionId}"
 * - Hash Fields:
 *   - "total_parts": 전체 파트 수
 *   - "part:1": "false" | "true"
 *   - "part:2": "false" | "true"
 *   - ...
 *
 * 설계 원칙:
 * - Best Effort: Redis 저장 실패해도 업로드는 계속 진행
 * - TTL 기반 자동 만료: 세션 만료 시 진행 상태도 자동 삭제
 * - 원자적 연산: HSET을 사용하여 동시성 보장
 *
 * @author sangwon-ryu
 */
@Component
public class RedisMultipartProgressAdapter implements MultipartProgressPort {

    private static final Logger log = LoggerFactory.getLogger(RedisMultipartProgressAdapter.class);
    private static final String KEY_PREFIX = "upload:multipart:progress:";
    private static final String TOTAL_PARTS_FIELD = "total_parts";
    private static final String PART_FIELD_PREFIX = "part:";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Constructor Injection (NO Lombok)
     */
    public RedisMultipartProgressAdapter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = Objects.requireNonNull(
                stringRedisTemplate,
                "stringRedisTemplate must not be null"
        );
    }

    @Override
    public void initializeProgress(String sessionId, int totalParts, Duration ttl) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("SessionId is null or empty, skipping progress initialization");
            return;
        }

        if (totalParts <= 0) {
            log.warn("TotalParts must be positive, got: {}", totalParts);
            return;
        }

        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            log.warn("Invalid TTL: {}, skipping progress initialization", ttl);
            return;
        }

        try {
            String key = buildKey(sessionId);

            // Hash 초기화: total_parts 설정
            stringRedisTemplate.opsForHash().put(key, TOTAL_PARTS_FIELD, String.valueOf(totalParts));

            // 모든 파트를 "false"로 초기화
            for (int i = 1; i <= totalParts; i++) {
                stringRedisTemplate.opsForHash().put(key, buildPartField(i), "false");
            }

            // TTL 설정
            stringRedisTemplate.expire(key, ttl);

            log.info("Initialized multipart progress for session {} with {} parts, TTL: {} seconds",
                    sessionId, totalParts, ttl.toSeconds());
        } catch (Exception e) {
            // Best Effort: 실패해도 업로드는 계속 진행
            log.error("Failed to initialize progress for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void markPartCompleted(String sessionId, int partNumber) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("SessionId is null or empty, skipping part completion");
            return;
        }

        if (partNumber <= 0) {
            log.warn("PartNumber must be positive, got: {}", partNumber);
            return;
        }

        try {
            String key = buildKey(sessionId);
            String partField = buildPartField(partNumber);

            // 원자적 연산: 해당 파트를 "true"로 설정
            stringRedisTemplate.opsForHash().put(key, partField, "true");

            log.info("Marked part {} as completed for session {}", partNumber, sessionId);
        } catch (Exception e) {
            log.error("Failed to mark part {} completed for session {}: {}",
                    partNumber, sessionId, e.getMessage(), e);
        }
    }

    @Override
    public MultipartProgress getProgress(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("SessionId is null or empty, returning empty progress");
            return new MultipartProgress(0, 0);
        }

        try {
            String key = buildKey(sessionId);

            // Redis Hash 전체 조회
            Map<Object, Object> hashEntries = stringRedisTemplate.opsForHash().entries(key);

            if (hashEntries.isEmpty()) {
                log.debug("No progress found for session {}", sessionId);
                return new MultipartProgress(0, 0);
            }

            // total_parts 파싱
            String totalPartsStr = (String) hashEntries.get(TOTAL_PARTS_FIELD);
            if (totalPartsStr == null) {
                log.warn("total_parts field not found for session {}", sessionId);
                return new MultipartProgress(0, 0);
            }

            int totalParts = Integer.parseInt(totalPartsStr);

            // 완료된 파트 수 계산
            int completedParts = 0;
            for (int i = 1; i <= totalParts; i++) {
                String partField = buildPartField(i);
                String value = (String) hashEntries.get(partField);
                if ("true".equals(value)) {
                    completedParts++;
                }
            }

            log.debug("Progress for session {}: {}/{} parts completed",
                    sessionId, completedParts, totalParts);

            return new MultipartProgress(completedParts, totalParts);

        } catch (Exception e) {
            log.error("Failed to get progress for session {}: {}", sessionId, e.getMessage(), e);
            return new MultipartProgress(0, 0);
        }
    }

    @Override
    public void deleteProgress(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("SessionId is null or empty, skipping progress deletion");
            return;
        }

        try {
            String key = buildKey(sessionId);
            Boolean deleted = stringRedisTemplate.delete(key);
            log.info("Deleted multipart progress for session {}: {}", sessionId, deleted);
        } catch (Exception e) {
            log.error("Failed to delete progress for session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * Redis Key 생성
     *
     * @param sessionId 세션 ID
     * @return Redis Key
     */
    private String buildKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    /**
     * 파트 필드명 생성
     *
     * @param partNumber 파트 번호 (1-based)
     * @return 필드명 (예: "part:1")
     */
    private String buildPartField(int partNumber) {
        return PART_FIELD_PREFIX + partNumber;
    }
}
