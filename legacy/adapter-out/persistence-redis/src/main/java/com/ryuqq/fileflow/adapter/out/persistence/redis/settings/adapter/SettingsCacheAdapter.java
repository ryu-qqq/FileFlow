package com.ryuqq.fileflow.adapter.out.persistence.redis.settings.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.fileflow.adapter.out.persistence.redis.config.RedisCacheConfig;
import com.ryuqq.fileflow.adapter.out.persistence.redis.settings.dto.CachedSettingsForMerge;
import com.ryuqq.fileflow.application.settings.port.out.LoadSettingsPort.SettingsForMerge;
import com.ryuqq.fileflow.application.settings.port.out.SettingsCachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Settings Cache Adapter - Settings 캐시 구현
 *
 * <p>Redis를 사용하여 3레벨 병합 Settings를 캐싱하는 Outbound Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>병합된 Settings 캐시 조회/저장</li>
 *   <li>조직/테넌트별 캐시 무효화</li>
 *   <li>사용자별 캐시 무효화</li>
 *   <li>TTL 관리 (10분 고정)</li>
 * </ul>
 *
 * <p><strong>캐시 전략:</strong></p>
 * <ul>
 *   <li>✅ Look-Aside 패턴 (Cache-Aside)</li>
 *   <li>✅ TTL 10분 (설정 변경 빈도 고려)</li>
 *   <li>✅ JSON 직렬화 (Jackson)</li>
 *   <li>✅ Cache Fallback (실패 시 로그만 남기고 서비스 정상 동작)</li>
 * </ul>
 *
 * <p><strong>캐시 키 형식:</strong></p>
 * <pre>
 * "settings:org:{orgId}:tenant:{tenantId}"
 * 예: "settings:org:123:tenant:456"
 * 예: "settings:org:null:tenant:456" (orgId가 null인 경우)
 * </pre>
 *
 * <p><strong>캐시 무효화:</strong></p>
 * <ul>
 *   <li>ORG Level Setting 변경 시 → invalidateOrg(orgId)</li>
 *   <li>TENANT Level Setting 변경 시 → invalidateTenant(tenantId)</li>
 *   <li>DEFAULT Level Setting 변경 시 → invalidateAll()</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@Component
public class SettingsCacheAdapter implements SettingsCachePort {

    private static final Logger log = LoggerFactory.getLogger(SettingsCacheAdapter.class);
    private static final String CACHE_KEY_PREFIX = "settings:org:";
    private static final String CACHE_KEY_PATTERN = "settings:*";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Constructor
     *
     * @param redisTemplate Redis 템플릿 (Auto-wired)
     * @param objectMapper JSON 매퍼 (Auto-wired)
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public SettingsCacheAdapter(
        RedisTemplate<String, Object> redisTemplate,
        ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 캐시에서 병합된 Settings 조회
     *
     * <p>주어진 orgId와 tenantId에 대해 3레벨 병합된 Settings를 캐시에서 조회합니다.</p>
     *
     * <p><strong>캐시 키 생성:</strong></p>
     * <pre>
     * "settings:org:{orgId}:tenant:{tenantId}"
     * orgId == null → "settings:org:null:tenant:{tenantId}"
     * </pre>
     *
     * <p><strong>반환값:</strong></p>
     * <ul>
     *   <li>캐시 Hit: Optional.of(SettingsForMerge)</li>
     *   <li>캐시 Miss: Optional.empty()</li>
     *   <li>예외 발생 시: Optional.empty() (Cache Fallback)</li>
     * </ul>
     *
     * @param orgId Organization ID (nullable)
     * @param tenantId Tenant ID (nullable)
     * @return Optional&lt;SettingsForMerge&gt;
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Override
    public Optional<SettingsForMerge> findMergedSettings(Long orgId, Long tenantId) {
        String cacheKey = buildCacheKey(orgId, tenantId);

        try {
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);

            if (cachedValue == null) {
                log.debug("Cache MISS: key={}", cacheKey);
                return Optional.empty();
            }

            // Cache DTO를 Domain 객체로 변환
            CachedSettingsForMerge cachedSettings = (CachedSettingsForMerge) cachedValue;
            SettingsForMerge settings = cachedSettings.toDomain();

            log.debug("Cache HIT: key={}", cacheKey);
            return Optional.of(settings);

        } catch (Exception e) {
            // Cache Fallback: 캐시 조회 실패 시에도 서비스는 정상 동작
            log.error("Cache 조회 실패 (Fallback to DB): key={}", cacheKey, e);
            return Optional.empty();
        }
    }

    /**
     * 병합된 Settings 캐싱
     *
     * <p>주어진 orgId와 tenantId에 대해 3레벨 병합된 Settings를 캐시에 저장합니다.
     * TTL 10분으로 자동 만료됩니다.</p>
     *
     * <p><strong>저장 전략:</strong></p>
     * <ul>
     *   <li>✅ 빈 SettingsForMerge도 캐시 가능 (설정 없는 경우도 캐시)</li>
     *   <li>✅ TTL 10분 고정 (설정 변경 빈도 고려)</li>
     *   <li>✅ 저장 실패 시 로그만 남기고 예외 발생 안 함 (Cache Fallback)</li>
     * </ul>
     *
     * @param orgId Organization ID (nullable)
     * @param tenantId Tenant ID (nullable)
     * @param settings 3레벨 병합 Settings (Not null)
     * @throws IllegalArgumentException settings가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Override
    public void save(Long orgId, Long tenantId, SettingsForMerge settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Settings는 null일 수 없습니다");
        }

        String cacheKey = buildCacheKey(orgId, tenantId);

        try {
            // Domain 객체를 Cache DTO로 변환 (Jackson 직렬화 가능)
            CachedSettingsForMerge cachedSettings = CachedSettingsForMerge.from(settings);

            redisTemplate.opsForValue().set(
                cacheKey,
                cachedSettings,
                RedisCacheConfig.SETTINGS_TTL
            );

            log.debug("Cache SAVE: key={}, ttl={}",
                cacheKey, RedisCacheConfig.SETTINGS_TTL);

        } catch (Exception e) {
            // Cache Fallback: 캐시 저장 실패 시에도 서비스는 정상 동작
            log.error("Cache 저장 실패 (서비스는 정상 동작): key={}", cacheKey, e);
        }
    }

    /**
     * 특정 Organization의 모든 Settings 캐시 무효화
     *
     * <p>주어진 orgId에 해당하는 모든 캐시를 무효화합니다.
     * 패턴 매칭을 사용하여 모든 tenant 조합의 캐시를 삭제합니다.</p>
     *
     * <p><strong>무효화 로직:</strong></p>
     * <ol>
     *   <li>패턴 생성: "settings:org:{orgId}:*"</li>
     *   <li>Redis SCAN으로 매칭되는 모든 키 조회</li>
     *   <li>DEL 명령으로 일괄 삭제</li>
     * </ol>
     *
     * @param orgId Organization ID (Not null)
     * @throws IllegalArgumentException orgId가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Override
    public void invalidateOrg(Long orgId) {
        if (orgId == null || orgId < 0) {
            throw new IllegalArgumentException("orgId는 null이거나 음수일 수 없습니다");
        }

        String pattern = CACHE_KEY_PREFIX + orgId + ":*";

        try {
            Set<String> keys = scanKeys(pattern);

            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.info("Cache INVALIDATE (Org): orgId={}, deletedKeys={}", orgId, deletedCount);
            } else {
                log.debug("Cache INVALIDATE (Org): orgId={}, noKeysFound", orgId);
            }

        } catch (Exception e) {
            // Cache Fallback: 무효화 실패 시에도 TTL로 자동 만료됨
            log.error("Cache 무효화 실패 (TTL로 자동 만료 예정): orgId={}", orgId, e);
        }
    }

    /**
     * 특정 Tenant의 모든 Settings 캐시 무효화
     *
     * <p>주어진 tenantId에 해당하는 모든 캐시를 무효화합니다.
     * 패턴 매칭을 사용하여 모든 org 조합의 캐시를 삭제합니다.</p>
     *
     * <p><strong>무효화 로직:</strong></p>
     * <ol>
     *   <li>패턴 생성: "settings:org:*:tenant:{tenantId}"</li>
     *   <li>Redis SCAN으로 매칭되는 모든 키 조회</li>
     *   <li>DEL 명령으로 일괄 삭제</li>
     * </ol>
     *
     * @param tenantId Tenant ID (Not null)
     * @throws IllegalArgumentException tenantId가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Override
    public void invalidateTenant(Long tenantId) {
        if (tenantId == null || tenantId < 0) {
            throw new IllegalArgumentException("tenantId는 null이거나 음수일 수 없습니다");
        }

        String pattern = CACHE_KEY_PREFIX + "*:tenant:" + tenantId;

        try {
            Set<String> keys = scanKeys(pattern);

            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.info("Cache INVALIDATE (Tenant): tenantId={}, deletedKeys={}", tenantId, deletedCount);
            } else {
                log.debug("Cache INVALIDATE (Tenant): tenantId={}, noKeysFound", tenantId);
            }

        } catch (Exception e) {
            // Cache Fallback: 무효화 실패 시에도 TTL로 자동 만료됨
            log.error("Cache 무효화 실패 (TTL로 자동 만료 예정): tenantId={}", tenantId, e);
        }
    }

    /**
     * 모든 Settings 캐시 무효화
     *
     * <p>모든 Settings 캐시를 무효화합니다.
     * DEFAULT Level Setting 변경 시 사용됩니다.</p>
     *
     * <p><strong>무효화 로직:</strong></p>
     * <ol>
     *   <li>패턴 생성: "settings:*"</li>
     *   <li>Redis SCAN으로 모든 settings 키 조회</li>
     *   <li>DEL 명령으로 일괄 삭제</li>
     * </ol>
     *
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Override
    public void invalidateAll() {
        try {
            Set<String> keys = scanKeys(CACHE_KEY_PATTERN);

            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.warn("Cache INVALIDATE (ALL): deletedKeys={}", deletedCount);
            } else {
                log.debug("Cache INVALIDATE (ALL): noKeysFound");
            }

        } catch (Exception e) {
            // Cache Fallback: 무효화 실패 시에도 TTL로 자동 만료됨
            log.error("Cache 전체 무효화 실패 (TTL로 자동 만료 예정)", e);
        }
    }

    /**
     * 특정 사용자와 관련된 모든 Settings 캐시 무효화
     *
     * <p>현재 구현에서는 사용자별 캐시 무효화를 지원하지 않습니다.
     * 사용자의 조직/테넌트 정보가 필요하므로, 호출자가 직접
     * invalidateOrg() 또는 invalidateTenant()를 호출해야 합니다.</p>
     *
     * <p><strong>사용 방법:</strong></p>
     * <pre>{@code
     * // 사용자의 조직/테넌트 정보를 조회한 후
     * UserContext userContext = userContextRepository.findByUserId(userId);
     * settingsCachePort.invalidateOrg(userContext.getOrganizationId());
     * settingsCachePort.invalidateTenant(userContext.getTenantId());
     * }</pre>
     *
     * @param userId 사용자 ID (Not null)
     * @throws IllegalArgumentException userId가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Override
    public void invalidateUser(Long userId) {
        if (userId == null || userId < 0) {
            throw new IllegalArgumentException("userId는 null이거나 음수일 수 없습니다");
        }

        // 현재 구현에서는 사용자별 캐시 무효화를 지원하지 않음
        // 호출자가 직접 invalidateOrg() 또는 invalidateTenant()를 호출해야 함
        log.warn("Settings 캐시는 사용자별 무효화를 직접 지원하지 않습니다. " +
            "호출자가 invalidateOrg() 또는 invalidateTenant()를 호출하세요. userId={}", userId);
    }

    /**
     * 캐시 키 생성
     *
     * <p>orgId와 tenantId로부터 캐시 키를 생성합니다.</p>
     *
     * <p><strong>키 형식:</strong></p>
     * <pre>
     * "settings:org:{orgId}:tenant:{tenantId}"
     * orgId == null → "settings:org:null:tenant:{tenantId}"
     * </pre>
     *
     * @param orgId Organization ID (nullable)
     * @param tenantId Tenant ID (nullable)
     * @return 캐시 키
     * @author ryu-qqq
     * @since 2025-10-26
     */
    private String buildCacheKey(Long orgId, Long tenantId) {
        String orgPart = orgId != null ? String.valueOf(orgId) : "null";
        String tenantPart = tenantId != null ? String.valueOf(tenantId) : "null";
        return String.format("%s%s:tenant:%s", CACHE_KEY_PREFIX, orgPart, tenantPart);
    }

    /**
     * Redis SCAN을 사용한 Non-blocking 키 조회
     *
     * <p>Redis KEYS 명령어 대신 SCAN을 사용하여 패턴 매칭되는 키를 조회합니다.
     * SCAN은 non-blocking 방식으로 동작하여 운영 환경에서 Redis 성능에 영향을 주지 않습니다.</p>
     *
     * <p><strong>KEYS vs SCAN:</strong></p>
     * <ul>
     *   <li>❌ KEYS: 블로킹 연산, 운영 환경에서 성능 문제 발생</li>
     *   <li>✅ SCAN: Non-blocking, 커서 기반 반복 조회</li>
     * </ul>
     *
     * <p><strong>SCAN 옵션:</strong></p>
     * <ul>
     *   <li>match(pattern): 패턴 매칭 조건</li>
     *   <li>count(1000): 한 번에 조회할 키 개수 힌트 (정확한 개수는 아님)</li>
     * </ul>
     *
     * @param pattern Redis 키 패턴 (예: "settings:org:123:*")
     * @return 패턴에 매칭되는 키 Set (빈 Set 가능)
     * @author ryu-qqq
     * @since 2025-10-26
     */
    private Set<String> scanKeys(String pattern) {
        return redisTemplate.execute((org.springframework.data.redis.connection.RedisConnection connection) -> {
            Set<String> keySet = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.scan(
                ScanOptions.scanOptions()
                    .match(pattern)
                    .count(1000)
                    .build()
            )) {
                cursor.forEachRemaining(key -> keySet.add(new String(key)));
            } catch (Exception e) {
                log.error("Error scanning keys for pattern: {}", pattern, e);
            }
            return keySet;
        });
    }
}
