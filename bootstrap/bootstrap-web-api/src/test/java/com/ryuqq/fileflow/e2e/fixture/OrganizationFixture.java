package com.ryuqq.fileflow.e2e.fixture;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.entity.UserOrgMembershipJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.iam.usercontext.repository.UserOrgMembershipJpaRepository;
import com.ryuqq.fileflow.adapter.rest.iam.organization.dto.request.CreateOrganizationApiRequest;
import org.springframework.stereotype.Component;

/**
 * OrganizationFixture - Organization 테스트 데이터 생성 유틸리티
 *
 * <p>E2E 테스트에서 사용할 Organization 관련 테스트 데이터를 생성하는 Fixture 클래스입니다.</p>
 *
 * <p><strong>Mother Object 패턴:</strong></p>
 * <ul>
 *   <li>✅ 재사용 가능한 테스트 데이터 생성</li>
 *   <li>✅ 테스트 가독성 향상</li>
 *   <li>✅ 테스트 유지보수성 향상</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-26
 */
@Component
public class OrganizationFixture {

    private final UserOrgMembershipJpaRepository userOrgMembershipRepository;

    /**
     * Constructor - Spring이 Repository 자동 주입
     *
     * @param userOrgMembershipRepository UserOrgMembership Repository
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public OrganizationFixture(UserOrgMembershipJpaRepository userOrgMembershipRepository) {
        this.userOrgMembershipRepository = userOrgMembershipRepository;
    }

    /**
     * Organization 생성 요청 생성
     *
     * @param tenantId Tenant ID
     * @param orgCode Organization 코드
     * @param name Organization 이름
     * @return CreateOrganizationRequest
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static CreateOrganizationApiRequest createRequest(Long tenantId, String orgCode, String name) {
        return new CreateOrganizationApiRequest(tenantId, orgCode, name);
    }

    /**
     * Organization 생성 요청 생성 (이름 자동 생성)
     *
     * @param tenantId Tenant ID
     * @param orgCode Organization 코드
     * @return CreateOrganizationRequest
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static CreateOrganizationApiRequest createRequest(Long tenantId, String orgCode) {
        return createRequest(tenantId, orgCode, "Org-" + orgCode);
    }

    /**
     * Organization 생성 요청 생성 (orgCode 자동 생성)
     *
     * @param tenantId Tenant ID
     * @return CreateOrganizationRequest
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static CreateOrganizationApiRequest createRequest(Long tenantId) {
        String orgCode = "ORG" + System.currentTimeMillis();
        return createRequest(tenantId, orgCode);
    }

    /**
     * 여러 Organization 생성 요청 배열 생성
     *
     * @param tenantId Tenant ID
     * @param count 생성할 Organization 개수
     * @return CreateOrganizationRequest 배열
     * @author ryu-qqq
     * @since 2025-10-26
     */
    public static CreateOrganizationApiRequest[] createRequests(Long tenantId, int count) {
        CreateOrganizationApiRequest[] requests = new CreateOrganizationApiRequest[count];
        long timestamp = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            String orgCode = "ORG" + timestamp + "-" + i;
            requests[i] = createRequest(tenantId, orgCode);
        }
        return requests;
    }

    /**
     * UserOrgMembership 생성 (UserContext를 Organization에 연결)
     *
     * <p>UserContext가 Organization에 소속되도록 Membership을 생성합니다.</p>
     * <p>기본 MembershipType은 "MEMBER"입니다.</p>
     *
     * @param userContextId UserContext ID
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @return 저장된 UserOrgMembershipJpaEntity
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public UserOrgMembershipJpaEntity createUserOrgMembership(
        Long userContextId,
        Long organizationId,
        Long tenantId
    ) {
        UserOrgMembershipJpaEntity membership = UserOrgMembershipJpaEntity.create(
            userContextId,
            tenantId,
            organizationId,
            "MEMBER"
        );
        return userOrgMembershipRepository.save(membership);
    }

    /**
     * UserOrgMembership 생성 (MembershipType 지정 가능)
     *
     * @param userContextId UserContext ID
     * @param organizationId Organization ID
     * @param tenantId Tenant ID
     * @param membershipType Membership 타입 (OWNER, ADMIN, MEMBER)
     * @return 저장된 UserOrgMembershipJpaEntity
     * @author ryu-qqq
     * @since 2025-10-27
     */
    public UserOrgMembershipJpaEntity createUserOrgMembership(
        Long userContextId,
        Long organizationId,
        Long tenantId,
        String membershipType
    ) {
        UserOrgMembershipJpaEntity membership = UserOrgMembershipJpaEntity.create(
            userContextId,
            tenantId,
            organizationId,
            membershipType
        );
        return userOrgMembershipRepository.save(membership);
    }
}
