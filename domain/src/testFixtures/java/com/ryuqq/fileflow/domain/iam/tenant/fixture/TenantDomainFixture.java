package com.ryuqq.fileflow.domain.iam.tenant.fixture;

import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantName;
import com.ryuqq.fileflow.domain.iam.tenant.TenantStatus;

import java.time.LocalDateTime;

/**
 * Tenant Domain 테스트 Fixture
 *
 * <p>테스트에서 Tenant 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <h3>네이밍 규칙:</h3>
 * <ul>
 *   <li>클래스명: {@code *Fixture} 접미사 필수</li>
 *   <li>기본 생성 메서드: {@code create*()} - 기본값으로 객체 생성</li>
 *   <li>커스터마이징 메서드: {@code create*With*()} - 특정 값 지정하여 생성</li>
 * </ul>
 *
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * // 기본값으로 생성 (신규, ID 없음)
 * Tenant tenant = TenantDomainFixture.create();
 *
 * // 특정 이름으로 생성
 * Tenant tenant = TenantDomainFixture.createWithName("My Tenant");
 *
 * // ID 포함하여 생성 (조회 시나리오)
 * Tenant tenant = TenantDomainFixture.createWithId(123L, "My Tenant");
 * }</pre>
 *
 * @author Claude Code
 * @since 1.0.0
 * @see Tenant
 */
public class TenantDomainFixture {

    /**
     * 기본값으로 Tenant 생성 (신규, ID 없음)
     *
     * <p>name: "Test Tenant"</p>
     *
     * @return 기본값을 가진 신규 Tenant (ID = null)
     */
    public static Tenant create() {
        return createWithName("Test Tenant");
    }

    /**
     * 특정 이름으로 Tenant 생성 (신규, ID 없음)
     *
     * @param name 테넌트 이름
     * @return 지정된 이름을 가진 신규 Tenant (ID = null)
     */
    public static Tenant createWithName(String name) {
        return Tenant.forNew(TenantName.of(name));
    }

    /**
     * ID 포함하여 Tenant 생성 (조회 시나리오용)
     *
     * <p>영속화된 상태의 Domain 객체를 테스트할 때 사용합니다.</p>
     *
     * @param id Tenant ID
     * @param name 테넌트 이름
     * @return ID를 가진 Tenant
     */
    public static Tenant createWithId(Long id, String name) {
        return Tenant.reconstitute(
            TenantId.of(id),
            TenantName.of(name),
            TenantStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
        );
    }

    /**
     * 여러 개의 Tenant 생성 (목록 테스트용, 신규)
     *
     * @param count 생성할 개수
     * @return Tenant 배열 (ID = null)
     */
    public static Tenant[] createMultiple(int count) {
        Tenant[] tenants = new Tenant[count];
        for (int i = 0; i < count; i++) {
            tenants[i] = createWithName("Test Tenant " + (i + 1));
        }
        return tenants;
    }

    /**
     * ID를 포함한 여러 개의 Tenant 생성 (조회 시나리오)
     *
     * @param startId 시작 ID
     * @param count 생성할 개수
     * @return Tenant 배열 (ID 포함)
     */
    public static Tenant[] createMultipleWithId(long startId, int count) {
        Tenant[] tenants = new Tenant[count];
        for (int i = 0; i < count; i++) {
            tenants[i] = createWithId(
                startId + i,
                "Test Tenant " + (i + 1)
            );
        }
        return tenants;
    }

    /**
     * 일시 정지된 Tenant 생성 (테스트용)
     *
     * @param id Tenant ID
     * @return 일시 정지된 Tenant
     */
    public static Tenant createSuspended(Long id) {
        return Tenant.reconstitute(
            TenantId.of(id),
            TenantName.of("Suspended Tenant"),
            TenantStatus.SUSPENDED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
        );
    }

    /**
     * 삭제된 Tenant 생성 (테스트용)
     *
     * @param id Tenant ID
     * @return 삭제된 Tenant
     */
    public static Tenant createDeleted(Long id) {
        return Tenant.reconstitute(
            TenantId.of(id),
            TenantName.of("Deleted Tenant"),
            TenantStatus.SUSPENDED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );
    }

    // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
    private TenantDomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
