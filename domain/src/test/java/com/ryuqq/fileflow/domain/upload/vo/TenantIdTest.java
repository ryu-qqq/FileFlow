package com.ryuqq.fileflow.domain.upload.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TenantId 테스트")
class TenantIdTest {

    @Test
    @DisplayName("유효한 문자열로 TenantId를 생성한다")
    void createTenantIdWithValidString() {
        // given
        String value = "tenant-123";

        // when
        TenantId tenantId = TenantId.of(value);

        // then
        assertThat(tenantId.value()).isEqualTo("tenant-123");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("null 또는 빈 문자열로 생성 시 예외가 발생한다")
    void throwsExceptionWhenValueIsNullOrEmpty(String invalidValue) {
        // when & then
        assertThatThrownBy(() -> TenantId.of(invalidValue))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TenantId cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "tenant@123",
        "tenant#123",
        "tenant$123",
        "tenant%123",
        "tenant&123",
        "tenant*123",
        "tenant.123",
        "tenant/123",
        "tenant\\123",
        "tenant 123",
        "tenant!123"
    })
    @DisplayName("허용되지 않은 문자가 포함된 경우 예외가 발생한다")
    void throwsExceptionWhenValueContainsInvalidCharacters(String invalidValue) {
        // when & then
        assertThatThrownBy(() -> TenantId.of(invalidValue))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must contain only alphanumeric characters, hyphens, and underscores");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "tenant123",
        "tenant_123",
        "tenant-123",
        "TENANT123",
        "TENANT_123",
        "TENANT-123",
        "Tenant123",
        "tenant-abc-123",
        "tenant_abc_123",
        "123-tenant",
        "123_tenant"
    })
    @DisplayName("유효한 형식의 TenantId를 생성한다")
    void createTenantIdWithValidFormats(String validValue) {
        // when
        TenantId tenantId = TenantId.of(validValue);

        // then
        assertThat(tenantId.value()).isEqualTo(validValue);
    }

    @Test
    @DisplayName("최소 길이(1자)의 TenantId를 생성한다")
    void createTenantIdWithMinimumLength() {
        // given
        String value = "a";

        // when
        TenantId tenantId = TenantId.of(value);

        // then
        assertThat(tenantId.value()).isEqualTo("a");
    }

    @Test
    @DisplayName("최대 길이(64자)의 TenantId를 생성한다")
    void createTenantIdWithMaximumLength() {
        // given
        String value = "a".repeat(64);

        // when
        TenantId tenantId = TenantId.of(value);

        // then
        assertThat(tenantId.value()).hasSize(64);
    }

    @Test
    @DisplayName("64자를 초과하는 경우 예외가 발생한다")
    void throwsExceptionWhenLengthExceeds64() {
        // given
        String value = "a".repeat(65);

        // when & then
        assertThatThrownBy(() -> TenantId.of(value))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TenantId length must not exceed 64 characters");
    }

    @Test
    @DisplayName("동일한 값의 TenantId는 같다")
    void equalTenantIdsAreEqual() {
        // given
        TenantId tenantId1 = TenantId.of("tenant-123");
        TenantId tenantId2 = TenantId.of("tenant-123");

        // when & then
        assertThat(tenantId1).isEqualTo(tenantId2);
        assertThat(tenantId1.hashCode()).isEqualTo(tenantId2.hashCode());
    }

    @Test
    @DisplayName("다른 값의 TenantId는 다르다")
    void differentTenantIdsAreNotEqual() {
        // given
        TenantId tenantId1 = TenantId.of("tenant-123");
        TenantId tenantId2 = TenantId.of("tenant-456");

        // when & then
        assertThat(tenantId1).isNotEqualTo(tenantId2);
    }

    @Test
    @DisplayName("대소문자를 구분한다")
    void isCaseSensitive() {
        // given
        TenantId tenantId1 = TenantId.of("tenant");
        TenantId tenantId2 = TenantId.of("TENANT");

        // when & then
        assertThat(tenantId1).isNotEqualTo(tenantId2);
    }

    @Test
    @DisplayName("숫자만으로 구성된 TenantId를 생성한다")
    void createTenantIdWithOnlyNumbers() {
        // given
        String value = "123456";

        // when
        TenantId tenantId = TenantId.of(value);

        // then
        assertThat(tenantId.value()).isEqualTo("123456");
    }

    @Test
    @DisplayName("영문자만으로 구성된 TenantId를 생성한다")
    void createTenantIdWithOnlyLetters() {
        // given
        String value = "tenant";

        // when
        TenantId tenantId = TenantId.of(value);

        // then
        assertThat(tenantId.value()).isEqualTo("tenant");
    }

    @Test
    @DisplayName("하이픈만으로 구성된 TenantId를 생성한다")
    void createTenantIdWithOnlyHyphens() {
        // given
        String value = "---";

        // when
        TenantId tenantId = TenantId.of(value);

        // then
        assertThat(tenantId.value()).isEqualTo("---");
    }

    @Test
    @DisplayName("언더스코어만으로 구성된 TenantId를 생성한다")
    void createTenantIdWithOnlyUnderscores() {
        // given
        String value = "___";

        // when
        TenantId tenantId = TenantId.of(value);

        // then
        assertThat(tenantId.value()).isEqualTo("___");
    }

    @Test
    @DisplayName("복잡한 조합의 TenantId를 생성한다")
    void createTenantIdWithComplexCombination() {
        // given
        String value = "Tenant_123-ABC-xyz_789";

        // when
        TenantId tenantId = TenantId.of(value);

        // then
        assertThat(tenantId.value()).isEqualTo("Tenant_123-ABC-xyz_789");
    }

    @Test
    @DisplayName("toString이 값을 반환한다")
    void toStringReturnsValue() {
        // given
        TenantId tenantId = TenantId.of("tenant-123");

        // when
        String result = tenantId.toString();

        // then
        assertThat(result).contains("tenant-123");
    }

    @Test
    @DisplayName("하이픈으로 시작하는 TenantId를 생성한다")
    void createTenantIdStartingWithHyphen() {
        // given
        String value = "-tenant";

        // when
        TenantId tenantId = TenantId.of(value);

        // then
        assertThat(tenantId.value()).isEqualTo("-tenant");
    }

    @Test
    @DisplayName("언더스코어로 시작하는 TenantId를 생성한다")
    void createTenantIdStartingWithUnderscore() {
        // given
        String value = "_tenant";

        // when
        TenantId tenantId = TenantId.of(value);

        // then
        assertThat(tenantId.value()).isEqualTo("_tenant");
    }

    @Test
    @DisplayName("숫자로 시작하는 TenantId를 생성한다")
    void createTenantIdStartingWithNumber() {
        // given
        String value = "123tenant";

        // when
        TenantId tenantId = TenantId.of(value);

        // then
        assertThat(tenantId.value()).isEqualTo("123tenant");
    }
}
