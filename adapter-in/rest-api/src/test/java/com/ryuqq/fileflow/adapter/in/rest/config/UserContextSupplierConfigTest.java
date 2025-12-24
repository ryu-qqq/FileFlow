package com.ryuqq.fileflow.adapter.in.rest.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.application.common.context.UserContextHolder;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * UserContextSupplierConfig 단위 테스트.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("UserContextSupplierConfig 단위 테스트")
class UserContextSupplierConfigTest {

    private UserContextSupplierConfig config;

    @BeforeEach
    void setUp() {
        config = new UserContextSupplierConfig();
        UserContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Nested
    @DisplayName("userContextSupplier 빈 테스트")
    class UserContextSupplierBeanTest {

        @Test
        @DisplayName("Supplier 빈을 생성할 수 있다")
        void userContextSupplier_ShouldCreateSupplierBean() {
            // when
            Supplier<UserContext> supplier = config.userContextSupplier();

            // then
            assertThat(supplier).isNotNull();
        }

        @Test
        @DisplayName("ThreadLocal에 설정된 UserContext를 반환한다")
        void userContextSupplier_WhenContextSet_ShouldReturnContext() {
            // given
            UserContext expectedContext = UserContext.admin("admin@test.com");
            UserContextHolder.set(expectedContext);
            Supplier<UserContext> supplier = config.userContextSupplier();

            // when
            UserContext actualContext = supplier.get();

            // then
            assertThat(actualContext).isEqualTo(expectedContext);
        }

        @Test
        @DisplayName("ThreadLocal이 비어있으면 IllegalStateException이 발생한다")
        void userContextSupplier_WhenContextNotSet_ShouldThrowException() {
            // given
            Supplier<UserContext> supplier = config.userContextSupplier();

            // when & then
            assertThatThrownBy(supplier::get)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("UserContext가 설정되지 않았습니다");
        }

        @Test
        @DisplayName("clear 후 Supplier가 IllegalStateException을 발생시킨다")
        void userContextSupplier_AfterClear_ShouldThrowException() {
            // given
            UserContext context = UserContext.admin("admin@test.com");
            UserContextHolder.set(context);
            Supplier<UserContext> supplier = config.userContextSupplier();
            assertThat(supplier.get()).isNotNull();

            // when
            UserContextHolder.clear();

            // then
            assertThatThrownBy(supplier::get)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("UserContext가 설정되지 않았습니다");
        }

        @Test
        @DisplayName("Seller 컨텍스트도 정상적으로 반환한다")
        void userContextSupplier_WithSellerContext_ShouldReturnContext() {
            // given
            OrganizationId organizationId = OrganizationId.generate();
            UserContext sellerContext =
                    UserContext.seller(organizationId, "Test Company", "seller@test.com");
            UserContextHolder.set(sellerContext);
            Supplier<UserContext> supplier = config.userContextSupplier();

            // when
            UserContext actualContext = supplier.get();

            // then
            assertThat(actualContext).isEqualTo(sellerContext);
            assertThat(actualContext.isSeller()).isTrue();
            assertThat(actualContext.organizationIdValue()).isEqualTo(organizationId);
        }

        @Test
        @DisplayName("Customer 컨텍스트도 정상적으로 반환한다")
        void userContextSupplier_WithCustomerContext_ShouldReturnContext() {
            // given
            UserId userId = UserId.generate();
            UserContext customerContext = UserContext.customer(userId);
            UserContextHolder.set(customerContext);
            Supplier<UserContext> supplier = config.userContextSupplier();

            // when
            UserContext actualContext = supplier.get();

            // then
            assertThat(actualContext).isEqualTo(customerContext);
            assertThat(actualContext.isCustomer()).isTrue();
        }
    }
}
