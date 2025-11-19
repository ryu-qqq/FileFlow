package com.ryuqq.fileflow.domain.session.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRole Enum Tests")
class UserRoleTest {

    @ParameterizedTest(name = "{0} should map to namespace {1}")
    @CsvSource({
        "ADMIN, connectly",
        "SELLER, setof",
        "DEFAULT, setof"
    })
    @DisplayName("모든 사용자 Role은 정의된 네임스페이스를 반환해야 한다")
    void shouldReturnCorrectNamespace(UserRole role, String namespace) {
        assertThat(role.getNamespace()).isEqualTo(namespace);
        assertThat(role.namespace()).isEqualTo(namespace);
    }
}

