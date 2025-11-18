package com.ryuqq.fileflow.domain.iam.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UploaderType Enum 테스트
 * <p>
 * 업로더 타입 (ADMIN, SELLER, CUSTOMER) 검증
 * </p>
 */
@DisplayName("UploaderType Enum 테스트")
class UploaderTypeTest {

    @Test
    @DisplayName("모든 필수 업로더 타입을 포함해야 한다")
    void shouldContainAllRequiredUploaderTypes() {
        // Given & When
        UploaderType[] uploaderTypes = UploaderType.values();

        // Then
        assertThat(uploaderTypes).hasSize(3);
        assertThat(uploaderTypes).contains(
                UploaderType.ADMIN,
                UploaderType.SELLER,
                UploaderType.CUSTOMER
        );
    }

    @Test
    @DisplayName("ADMIN 타입이 존재해야 한다")
    void shouldHaveAdminType() {
        // When
        UploaderType admin = UploaderType.ADMIN;

        // Then
        assertThat(admin).isNotNull();
        assertThat(admin.name()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("SELLER 타입이 존재해야 한다")
    void shouldHaveSellerType() {
        // When
        UploaderType seller = UploaderType.SELLER;

        // Then
        assertThat(seller).isNotNull();
        assertThat(seller.name()).isEqualTo("SELLER");
    }

    @Test
    @DisplayName("CUSTOMER 타입이 존재해야 한다")
    void shouldHaveCustomerType() {
        // When
        UploaderType customer = UploaderType.CUSTOMER;

        // Then
        assertThat(customer).isNotNull();
        assertThat(customer.name()).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("세 타입은 서로 달라야 한다")
    void shouldBeDifferentFromEachOther() {
        // Given
        UploaderType admin = UploaderType.ADMIN;
        UploaderType seller = UploaderType.SELLER;
        UploaderType customer = UploaderType.CUSTOMER;

        // Then
        assertThat(admin).isNotEqualTo(seller);
        assertThat(seller).isNotEqualTo(customer);
        assertThat(customer).isNotEqualTo(admin);
    }
}
