package com.ryuqq.fileflow.domain.iam.tenant.fixture;

import com.ryuqq.fileflow.domain.iam.tenant.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Tenant Test Fixture
 *
 * <p>테스트에서 Tenant 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 신규 Tenant (ID 없음)
 * Tenant tenant = TenantFixture.createNew();
 *
 * // ID가 있는 Tenant
 * Tenant tenant = TenantFixture.createWithId(1L);
 *
 * // 커스텀 Tenant
 * Tenant tenant = TenantFixture.builder()
 *     .id(1L)
 *     .name("Acme Corporation")
 *     .status(TenantStatus.SUSPENDED)
 *     .build();
 *
 * // 여러 Tenant 생성
 * List<Tenant> tenants = TenantFixture.createMultiple(5);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class TenantFixture {

    private static final String DEFAULT_NAME = "Test Tenant";
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private TenantFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }


    /**
     * 신규 Tenant를 생성합니다 (ID 없음).
     *
     * <p>기본값:</p>
     * <ul>
     *   <li>name: "Test Tenant"</li>
     *   <li>status: ACTIVE</li>
     *   <li>deleted: false</li>
     * </ul>
     *
     * @return Tenant 인스턴스 (ID = null)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Tenant createNew() {
        return Tenant.forNew(TenantName.of(DEFAULT_NAME));
    }

    /**
     * 신규 Tenant를 생성합니다 (커스텀 이름).
     *
     * @param name Tenant 이름
     * @return Tenant 인스턴스 (ID = null)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Tenant createNew(String name) {
        return Tenant.forNew(TenantName.of(name));
    }

    /**
     * ID가 있는 Tenant를 생성합니다.
     *
     * <p>기본값:</p>
     * <ul>
     *   <li>name: "Test Tenant"</li>
     *   <li>status: ACTIVE</li>
     *   <li>deleted: false</li>
     * </ul>
     *
     * @param id Tenant ID
     * @return Tenant 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Tenant createWithId(Long id) {
        return Tenant.of(TenantId.of(id), TenantName.of(DEFAULT_NAME));
    }

    /**
     * ID가 있는 Tenant를 생성합니다 (커스텀 이름).
     *
     * @param id Tenant ID
     * @param name Tenant 이름
     * @return Tenant 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Tenant createWithId(Long id, String name) {
        return Tenant.of(TenantId.of(id), TenantName.of(name));
    }

    /**
     * 여러 개의 Tenant를 생성합니다.
     *
     * <p>ID는 1부터 시작하는 연속된 값을 사용합니다.</p>
     * <p>Tenant 이름은 "Test Tenant 1", "Test Tenant 2", ... 형식입니다.</p>
     *
     * @param count 생성할 Tenant 개수
     * @return Tenant 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static java.util.List<Tenant> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> Tenant.of(
                TenantId.of((long) i),
                TenantName.of(DEFAULT_NAME + " " + i)
            ))
            .toList();
    }

    /**
     * DB에서 조회된 Tenant를 재구성합니다 (reconstitute).
     *
     * <p>모든 상태를 커스터마이징할 수 있습니다.</p>
     *
     * @param id Tenant ID
     * @param name Tenant 이름
     * @param status Tenant 상태
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @param deleted 삭제 여부
     * @return 재구성된 Tenant
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Tenant reconstitute(
        Long id,
        String name,
        TenantStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean deleted
    ) {
        return Tenant.reconstitute(
            TenantId.of(id),
            TenantName.of(name),
            status,
            createdAt,
            updatedAt,
            deleted
        );
    }

    /**
     * 일시 정지된 Tenant를 생성합니다.
     *
     * @param id Tenant ID
     * @return 일시 정지된 Tenant
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Tenant createSuspended(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return Tenant.reconstitute(
            TenantId.of(id),
            TenantName.of(DEFAULT_NAME),
            TenantStatus.SUSPENDED,
            now,
            now,
            false
        );
    }

    /**
     * 삭제된 Tenant를 생성합니다.
     *
     * @param id Tenant ID
     * @return 삭제된 Tenant
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static Tenant createDeleted(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return Tenant.reconstitute(
            TenantId.of(id),
            TenantName.of(DEFAULT_NAME),
            TenantStatus.SUSPENDED,
            now,
            now,
            true
        );
    }

    /**
     * Tenant Builder를 생성합니다.
     *
     * <p>복잡한 테스트 시나리오를 위한 Builder 패턴을 제공합니다.</p>
     *
     * @return TenantBuilder 인스턴스
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static TenantBuilder builder() {
        return new TenantBuilder();
    }

    /**
     * Tenant Builder 클래스
     *
     * <p>테스트에서 Tenant를 유연하게 생성하기 위한 Builder입니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static class TenantBuilder {
        private Long id;
        private String name = DEFAULT_NAME;
        private TenantStatus status = TenantStatus.ACTIVE;
        private Clock clock = Clock.systemDefaultZone();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean deleted = false;

        /**
         * Tenant ID를 설정합니다.
         *
         * @param id Tenant ID
         * @return TenantBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public TenantBuilder id(Long id) {
            this.id = id;
            return this;
        }

        /**
         * Tenant 이름을 설정합니다.
         *
         * @param name Tenant 이름
         * @return TenantBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public TenantBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Tenant 상태를 설정합니다.
         *
         * @param status Tenant 상태
         * @return TenantBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public TenantBuilder status(TenantStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Clock을 설정합니다 (시간 제어용).
         *
         * @param clock Clock 인스턴스
         * @return TenantBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public TenantBuilder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * 고정된 시간을 설정합니다 (테스트용).
         *
         * @param instant 고정할 시간
         * @return TenantBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public TenantBuilder fixedClock(Instant instant) {
            this.clock = Clock.fixed(instant, ZoneId.systemDefault());
            return this;
        }

        /**
         * 생성 일시를 설정합니다.
         *
         * @param createdAt 생성 일시
         * @return TenantBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public TenantBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * 수정 일시를 설정합니다.
         *
         * @param updatedAt 수정 일시
         * @return TenantBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public TenantBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * 삭제 여부를 설정합니다.
         *
         * @param deleted 삭제 여부
         * @return TenantBuilder
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public TenantBuilder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        /**
         * Tenant를 생성합니다.
         *
         * @return Tenant 인스턴스
         * @author ryu-qqq
         * @since 2025-10-30
         */
        public Tenant build() {
            if (id == null) {
                // ID 없이 신규 생성
                return Tenant.forNew(TenantName.of(name));
            }

            // reconstitute 사용 (모든 상태 복원)
            LocalDateTime now = LocalDateTime.now(clock);
            return Tenant.reconstitute(
                TenantId.of(id),
                TenantName.of(name),
                status,
                createdAt != null ? createdAt : now,
                updatedAt != null ? updatedAt : now,
                deleted
            );
        }
    }
}
