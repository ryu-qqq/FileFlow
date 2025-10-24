package com.ryuqq.fileflow.fixtures;

import com.ryuqq.fileflow.application.iam.organization.dto.response.OrganizationResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * OrganizationResponseFixtures - Organization Response Object Mother Pattern
 *
 * <p>Organization Response DTO의 테스트 픽스쳐를 생성하는 팩토리 클래스입니다.</p>
 * <p>Object Mother 패턴을 사용하여 테스트에서 필요한 다양한 Response를 제공합니다.</p>
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 기본 OrganizationResponse
 * OrganizationResponse response = OrganizationResponseFixtures.organizationResponse("tenant-id");
 *
 * // Sales 조직 Response
 * OrganizationResponse response = OrganizationResponseFixtures.salesOrganizationResponse("tenant-id");
 *
 * // INACTIVE 상태의 OrganizationResponse
 * OrganizationResponse response = OrganizationResponseFixtures.inactiveOrganizationResponse("tenant-id");
 *
 * // 다수의 OrganizationResponse 리스트
 * List<OrganizationResponse> responses = OrganizationResponseFixtures.organizationResponseList("tenant-id", 10);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-23
 */
public final class OrganizationResponseFixtures {

    private OrganizationResponseFixtures() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * 기본 OrganizationResponse를 생성합니다 (ACTIVE 상태).
     *
     * <p>ID 1L, "ORG-DEFAULT" 조직 코드, "Default Organization" 이름으로 생성합니다.</p>
     *
     * @param tenantId Tenant ID (String - Tenant PK 타입과 일치)
     * @return OrganizationResponse (ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationResponse organizationResponse(String tenantId) {
        return new OrganizationResponse(
            1L,
            tenantId,
            "ORG-DEFAULT",
            "Default Organization",
            "ACTIVE",
            false,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now()
        );
    }

    /**
     * 특정 ID와 이름을 가진 OrganizationResponse를 생성합니다 (ACTIVE 상태).
     *
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @return OrganizationResponse (ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationResponse organizationResponse(Long organizationId, String tenantId, String orgCode, String name) {
        return new OrganizationResponse(
            organizationId,
            tenantId,
            orgCode,
            name,
            "ACTIVE",
            false,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now()
        );
    }

    /**
     * 완전히 커스터마이징된 OrganizationResponse를 생성합니다.
     *
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @param orgCode 조직 코드
     * @param name 조직 이름
     * @param status Organization 상태 (ACTIVE, INACTIVE)
     * @param deleted 삭제 여부
     * @return OrganizationResponse
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationResponse organizationResponse(
        Long organizationId,
        String tenantId,
        String orgCode,
        String name,
        String status,
        boolean deleted
    ) {
        return new OrganizationResponse(
            organizationId,
            tenantId,
            orgCode,
            name,
            status,
            deleted,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now()
        );
    }

    /**
     * Sales 조직 OrganizationResponse를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @return OrganizationResponse (SALES, "Sales Department", ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationResponse salesOrganizationResponse(String tenantId) {
        return new OrganizationResponse(
            1L,
            tenantId,
            "SALES",
            "Sales Department",
            "ACTIVE",
            false,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now()
        );
    }

    /**
     * HR 조직 OrganizationResponse를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @return OrganizationResponse (HR, "Human Resources", ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationResponse hrOrganizationResponse(String tenantId) {
        return new OrganizationResponse(
            2L,
            tenantId,
            "HR",
            "Human Resources",
            "ACTIVE",
            false,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now()
        );
    }

    /**
     * IT 조직 OrganizationResponse를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @return OrganizationResponse (IT, "IT Department", ACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationResponse itOrganizationResponse(String tenantId) {
        return new OrganizationResponse(
            3L,
            tenantId,
            "IT",
            "IT Department",
            "ACTIVE",
            false,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now()
        );
    }

    /**
     * INACTIVE 상태의 OrganizationResponse를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @return OrganizationResponse (INACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationResponse inactiveOrganizationResponse(String tenantId) {
        return new OrganizationResponse(
            999L,
            tenantId,
            "INACTIVE-ORG",
            "Inactive Organization",
            "INACTIVE",
            false,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1)
        );
    }

    /**
     * 삭제된 OrganizationResponse를 생성합니다 (Soft Delete).
     *
     * @param tenantId Tenant ID
     * @return OrganizationResponse (deleted = true, status = INACTIVE)
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static OrganizationResponse deletedOrganizationResponse(String tenantId) {
        return new OrganizationResponse(
            999L,
            tenantId,
            "DELETED-ORG",
            "Deleted Organization",
            "INACTIVE",
            true,  // deleted
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(30)
        );
    }

    /**
     * 특정 개수의 OrganizationResponse 리스트를 생성합니다.
     *
     * <p>테스트에서 다수의 Organization이 필요할 때 사용합니다 (Pagination 테스트 등).</p>
     *
     * @param tenantId Tenant ID
     * @param count 생성할 OrganizationResponse 개수
     * @return OrganizationResponse 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<OrganizationResponse> organizationResponseList(String tenantId, int count) {
        List<OrganizationResponse> responses = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            responses.add(new OrganizationResponse(
                (long) (i + 1),
                tenantId,
                "ORG-" + String.format("%03d", i + 1),
                "Organization " + (i + 1),
                "ACTIVE",
                false,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
            ));
        }
        return responses;
    }

    /**
     * 특정 개수의 INACTIVE 상태 OrganizationResponse 리스트를 생성합니다.
     *
     * @param tenantId Tenant ID
     * @param count 생성할 OrganizationResponse 개수
     * @return INACTIVE OrganizationResponse 리스트
     * @author ryu-qqq
     * @since 2025-10-23
     */
    public static List<OrganizationResponse> inactiveOrganizationResponseList(String tenantId, int count) {
        List<OrganizationResponse> responses = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            responses.add(new OrganizationResponse(
                (long) (i + 1),
                tenantId,
                "INACTIVE-" + String.format("%03d", i + 1),
                "Inactive Organization " + (i + 1),
                "INACTIVE",
                false,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now().minusDays(1)
            ));
        }
        return responses;
    }
}
