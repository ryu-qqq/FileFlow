package com.ryuqq.fileflow.domain.iam.organization.fixture;

import com.ryuqq.fileflow.domain.iam.organization.OrgCode;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationId;
import com.ryuqq.fileflow.domain.iam.organization.OrganizationStatus;

import java.time.LocalDateTime;

/**
 * Organization Domain 테스트 Fixture
 *
 * <p>테스트에서 Organization 객체를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
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
 * Organization org = OrganizationDomainFixture.create();
 *
 * // 특정 이름으로 생성
 * Organization org = OrganizationDomainFixture.createWithName("My Organization");
 *
 * // ID 포함하여 생성 (조회 시나리오)
 * Organization org = OrganizationDomainFixture.createWithId(123L, 1L, "My Organization");
 * }</pre>
 *
 * @author Claude Code
 * @since 1.0.0
 * @see Organization
 */
public class OrganizationDomainFixture {

    /**
     * 기본값으로 Organization 생성 (신규, ID 없음)
     *
     * <p>tenantId: 1L, orgCode: "ORG001", name: "Test Organization"</p>
     *
     * @return 기본값을 가진 신규 Organization (ID = null)
     */
    public static Organization create() {
        return createWithName("Test Organization");
    }

    /**
     * 특정 이름으로 Organization 생성 (신규, ID 없음)
     *
     * @param name 조직 이름
     * @return 지정된 이름을 가진 신규 Organization (ID = null)
     */
    public static Organization createWithName(String name) {
        return Organization.forNew(
            1L,  // tenantId
            OrgCode.of("ORG001"),
            name
        );
    }

    /**
     * tenantId와 이름으로 Organization 생성 (신규, ID 없음)
     *
     * @param tenantId Tenant ID
     * @param name 조직 이름
     * @return 신규 Organization (ID = null)
     */
    public static Organization createWith(Long tenantId, String name) {
        return Organization.forNew(
            tenantId,
            OrgCode.of("ORG001"),
            name
        );
    }

    /**
     * ID 포함하여 Organization 생성 (조회 시나리오용)
     *
     * <p>영속화된 상태의 Domain 객체를 테스트할 때 사용합니다.</p>
     *
     * @param id Organization ID
     * @param tenantId Tenant ID
     * @param name 조직 이름
     * @return ID를 가진 Organization
     */
    public static Organization createWithId(Long id, Long tenantId, String name) {
        return Organization.reconstitute(
            OrganizationId.of(id),
            tenantId,
            OrgCode.of("ORG001"),
            name,
            OrganizationStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
        );
    }

    /**
     * 여러 개의 Organization 생성 (목록 테스트용, 신규)
     *
     * @param count 생성할 개수
     * @return Organization 배열 (ID = null)
     */
    public static Organization[] createMultiple(int count) {
        Organization[] organizations = new Organization[count];
        for (int i = 0; i < count; i++) {
            organizations[i] = createWithName("Test Organization " + (i + 1));
        }
        return organizations;
    }

    /**
     * ID를 포함한 여러 개의 Organization 생성 (조회 시나리오)
     *
     * @param startId 시작 ID
     * @param count 생성할 개수
     * @return Organization 배열 (ID 포함)
     */
    public static Organization[] createMultipleWithId(long startId, int count) {
        Organization[] organizations = new Organization[count];
        for (int i = 0; i < count; i++) {
            organizations[i] = createWithId(
                startId + i,
                1L,
                "Test Organization " + (i + 1)
            );
        }
        return organizations;
    }

    /**
     * 비활성화된 Organization 생성 (테스트용)
     *
     * @param id Organization ID
     * @param tenantId Tenant ID
     * @return 비활성화된 Organization
     */
    public static Organization createInactive(Long id, Long tenantId) {
        return Organization.reconstitute(
            OrganizationId.of(id),
            tenantId,
            OrgCode.of("ORG001"),
            "Inactive Organization",
            OrganizationStatus.INACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            false
        );
    }

    /**
     * 삭제된 Organization 생성 (테스트용)
     *
     * @param id Organization ID
     * @param tenantId Tenant ID
     * @return 삭제된 Organization
     */
    public static Organization createDeleted(Long id, Long tenantId) {
        return Organization.reconstitute(
            OrganizationId.of(id),
            tenantId,
            OrgCode.of("ORG001"),
            "Deleted Organization",
            OrganizationStatus.INACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );
    }

    // Private 생성자 - Utility 클래스이므로 인스턴스화 방지
    private OrganizationDomainFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
