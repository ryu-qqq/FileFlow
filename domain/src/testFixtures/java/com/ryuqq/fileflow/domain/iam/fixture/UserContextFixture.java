package com.ryuqq.fileflow.domain.iam.fixture;

import com.ryuqq.fileflow.domain.iam.vo.UserContext;

/**
 * UserContext Value Object Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class UserContextFixture {

    private UserContextFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 Admin UserContext Fixture */
    public static UserContext defaultAdminUserContext() {
        return UserContext.admin("admin@fileflow.com");
    }

    /** 기본 Seller UserContext Fixture */
    public static UserContext defaultSellerUserContext() {
        return UserContext.seller(1001L, "Test Company", "seller@test.com");
    }

    /** 기본 Customer UserContext Fixture */
    public static UserContext defaultCustomerUserContext() {
        return UserContext.customer(9999L);
    }

    /** Custom Admin UserContext Fixture */
    public static UserContext customAdminUserContext(String email) {
        return UserContext.admin(email);
    }

    /** Custom Seller UserContext Fixture */
    public static UserContext customSellerUserContext(
            long organizationId, String companyName, String email) {
        return UserContext.seller(organizationId, companyName, email);
    }

    /** Custom Customer UserContext Fixture */
    public static UserContext customCustomerUserContext(long userId) {
        return UserContext.customer(userId);
    }
}
