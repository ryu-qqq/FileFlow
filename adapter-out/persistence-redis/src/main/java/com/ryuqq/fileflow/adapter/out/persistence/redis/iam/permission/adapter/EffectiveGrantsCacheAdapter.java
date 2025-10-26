package com.ryuqq.fileflow.adapter.out.persistence.redis.iam.permission.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.redis.config.RedisCacheConfig;
import com.ryuqq.fileflow.adapter.out.persistence.redis.iam.permission.dto.CachedGrant;
import com.ryuqq.fileflow.application.iam.permission.port.out.GrantsCachePort;
import com.ryuqq.fileflow.domain.iam.permission.Grant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Effective Grants Cache Adapter - Grants 캐시 구현
 *
 * <p>Redis를 사용하여 사용자의 유효 권한(Grants)을 캐싱하는 Outbound Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Grants 캐시 조회/저장</li>
 *   <li>사용자별 캐시 무효화</li>
 *   <li>전체 캐시 무효화</li>
 *   <li>TTL 관리 (5분 고정)</li>
 * </ul>
 *
 * <p><strong>캐시 전략:</strong></p>
 * <ul>
 *   <li>✅ Look-Aside 패턴 (Cache-Aside)</li>
 *   <li>✅ TTL 5분 (권한 변경 빈도 고려)</li>
 *   <li>✅ JSON 직렬화 (Jackson)</li>
 *   <li>✅ Cache Fallback (실패 시 로그만 남기고 서비스 정상 동작)</li>
 * </ul>
 *
 * <p><strong>캐시 키 형식:</strong></p>
 * <pre>
 * "grants:user:{userId}:tenant:{tenantId}:org:{orgId}"
 * 예: "grants:user:123:tenant:456:org:789"
 * </pre>
 *
 * <p><strong>캐시 무효화:</strong></p>
 * <ul>
 *   <li>사용자 Role 변경 시 → invalidateUser(userId)</li>
 *   <li>Role-Permission 매핑 변경 시 → invalidateAll()</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@Component
public class EffectiveGrantsCacheAdapter implements GrantsCachePort {

    private static final Logger log = LoggerFactory.getLogger(EffectiveGrantsCacheAdapter.class);
    private static final String CACHE_KEY_PREFIX = "grants:user:";
    private static final String CACHE_KEY_PATTERN = CACHE_KEY_PREFIX + "*";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Constructor
     *
     * @param redisTemplate Redis 템플릿 (Auto-wired)
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public EffectiveGrantsCacheAdapter(
        RedisTemplate<String, Object> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 캐시에서 Grants 조회
     *
     * <p>주어진 사용자 컨텍스트에 대해 캐시된 Grants를 조회합니다.</p>
     *
     * <p><strong>캐시 키 생성:</strong></p>
     * <pre>
     * "grants:user:{userId}:tenant:{tenantId}:org:{orgId}"
     * </pre>
     *
     * <p><strong>반환값:</strong></p>
     * <ul>
     *   <li>캐시 Hit: Optional.of(List&lt;Grant&gt;) - 빈 List도 캐시될 수 있음</li>
     *   <li>캐시 Miss: Optional.empty()</li>
     *   <li>예외 발생 시: Optional.empty() (Cache Fallback)</li>
     * </ul>
     *
     * @param userId 사용자 ID (Not null)
     * @param tenantId 테넌트 ID (Not null)
     * @param organizationId 조직 ID (Not null)
     * @return Optional&lt;List&lt;Grant&gt;&gt;
     * @throws IllegalArgumentException userId, tenantId, organizationId가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Override
    public Optional<List<Grant>> findEffectiveGrants(Long userId, Long tenantId, Long organizationId) {
        validateIds(userId, tenantId, organizationId);

        String cacheKey = buildCacheKey(userId, tenantId, organizationId);

        try {
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);

            if (cachedValue == null) {
                log.debug("Cache MISS: key={}", cacheKey);
                return Optional.empty();
            }

            // GenericJackson2JsonRedisSerializer가 이미 역직렬화를 완료함
            // @class 타입 힌트가 포함된 JSON → List<CachedGrant> 자동 역직렬화
            @SuppressWarnings("unchecked")
            List<CachedGrant> cachedGrants = (List<CachedGrant>) cachedValue;

            // CachedGrant DTO → Domain Grant 변환
            List<Grant> grants = cachedGrants.stream()
                .map(CachedGrant::toDomain)
                .collect(Collectors.toList());

            log.debug("Cache HIT: key={}, size={}", cacheKey, grants.size());
            return Optional.of(grants);

        } catch (Exception e) {
            // Cache Fallback: 캐시 조회 실패 시에도 서비스는 정상 동작
            log.error("Cache 조회 실패 (Fallback to DB): key={}", cacheKey, e);
            return Optional.empty();
        }
    }

    /**
     * Grants 캐싱
     *
     * <p>주어진 사용자 컨텍스트에 대해 Grants를 캐시에 저장합니다.
     * TTL 5분으로 자동 만료됩니다.</p>
     *
     * <p><strong>저장 전략:</strong></p>
     * <ul>
     *   <li>✅ 빈 List도 캐시 가능 (권한 없는 사용자도 캐시)</li>
     *   <li>✅ TTL 5분 고정 (권한 변경 빈도 고려)</li>
     *   <li>✅ 저장 실패 시 로그만 남기고 예외 발생 안 함 (Cache Fallback)</li>
     * </ul>
     *
     * @param userId 사용자 ID (Not null)
     * @param tenantId 테넌트 ID (Not null)
     * @param organizationId 조직 ID (Not null)
     * @param grants Grant 리스트 (Not null, 빈 List 가능)
     * @throws IllegalArgumentException userId, tenantId, organizationId, grants가 null인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    @Override
    public void save(Long userId, Long tenantId, Long organizationId, List<Grant> grants) {
        validateIds(userId, tenantId, organizationId);

        if (grants == null) {
            throw new IllegalArgumentException("Grants는 null일 수 없습니다");
        }

        String cacheKey = buildCacheKey(userId, tenantId, organizationId);

        try {
            // Domain Grant → CachedGrant DTO 변환
            List<CachedGrant> cachedGrants = grants.stream()
                .map(CachedGrant::from)
                .collect(Collectors.toList());

            redisTemplate.opsForValue().set(
                cacheKey,
                cachedGrants,
                RedisCacheConfig.EFFECTIVE_GRANTS_TTL
            );

            log.debug("Cache SAVE: key={}, size={}, ttl={}",
                cacheKey, grants.size(), RedisCacheConfig.EFFECTIVE_GRANTS_TTL);

        } catch (Exception e) {
            // Cache Fallback: 캐시 저장 실패 시에도 서비스는 정상 동작
            log.error("Cache 저장 실패 (서비스는 정상 동작): key={}", cacheKey, e);
        }
    }

    /**
     * 특정 사용자의 모든 Grants 캐시 무효화
     *
     * <p>주어진 userId에 해당하는 모든 캐시를 무효화합니다.
     * 패턴 매칭을 사용하여 모든 tenant/org 조합의 캐시를 삭제합니다.</p>
     *
     * <p><strong>무효화 로직:</strong></p>
     * <ol>
     *   <li>패턴 생성: "grants:user:{userId}:*"</li>
     *   <li>Redis SCAN으로 매칭되는 모든 키 조회</li>
     *   <li>DEL 명령으로 일괄 삭제</li>
     * </ol>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>사용자 Role 변경 시</li>
     *   <li>사용자 조직/테넌트 변경 시</li>
     * </ul>
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

        String pattern = CACHE_KEY_PREFIX + userId + ":*";

        try {
            Set<String> keys = scanKeys(pattern);

            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keys);
                log.info("Cache INVALIDATE (User): userId={}, deletedKeys={}", userId, deletedCount);
            } else {
                log.debug("Cache INVALIDATE (User): userId={}, noKeysFound", userId);
            }

        } catch (Exception e) {
            // Cache Fallback: 무효화 실패 시에도 TTL로 자동 만료됨
            log.error("Cache 무효화 실패 (TTL로 자동 만료 예정): userId={}", userId, e);
        }
    }

    /**
     * 모든 Grants 캐시 무효화
     *
     * <p>모든 Grants 캐시를 무효화합니다.
     * Role-Permission 매핑 변경 시 사용됩니다.</p>
     *
     * <p><strong>무효화 로직:</strong></p>
     * <ol>
     *   <li>패턴 생성: "grants:*"</li>
     *   <li>Redis SCAN으로 모든 grants 키 조회</li>
     *   <li>DEL 명령으로 일괄 삭제</li>
     * </ol>
     *
     * <p><strong>주의사항:</strong></p>
     * <ul>
     *   <li>⚠️ 전체 무효화는 비용이 높음 (모든 사용자 캐시 삭제)</li>
     *   <li>⚠️ 캐시 미스로 인한 일시적 DB 부하 증가 가능</li>
     *   <li>✅ 권한 변경은 드물므로 실제로는 거의 호출되지 않음</li>
     * </ul>
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
     * 캐시 키 생성
     *
     * <p>사용자 컨텍스트 (userId, tenantId, orgId)로부터 캐시 키를 생성합니다.</p>
     *
     * <p><strong>키 형식:</strong></p>
     * <pre>
     * "grants:user:{userId}:tenant:{tenantId}:org:{orgId}"
     * </pre>
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @return 캐시 키
     * @author ryu-qqq
     * @since 2025-10-26
     */
    private String buildCacheKey(Long userId, Long tenantId, Long organizationId) {
        return String.format("%s%d:tenant:%d:org:%d", CACHE_KEY_PREFIX, userId, tenantId, organizationId);
    }

    /**
     * ID 유효성 검증
     *
     * <p>userId, tenantId, organizationId가 유효한지 검증합니다.</p>
     *
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @throws IllegalArgumentException ID가 null이거나 음수인 경우
     * @author ryu-qqq
     * @since 2025-10-26
     */
    private void validateIds(Long userId, Long tenantId, Long organizationId) {
        if (userId == null || userId < 0) {
            throw new IllegalArgumentException("userId는 null이거나 음수일 수 없습니다");
        }
        if (tenantId == null || tenantId < 0) {
            throw new IllegalArgumentException("tenantId는 null이거나 음수일 수 없습니다");
        }
        if (organizationId == null || organizationId < 0) {
            throw new IllegalArgumentException("organizationId는 null이거나 음수일 수 없습니다");
        }
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
     * @param pattern Redis 키 패턴 (예: "grants:user:123:*")
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
            } catch (IOException e) {
                log.error("Error scanning keys for pattern: {}", pattern, e);
            }
            return keySet;
        });
    }
}
