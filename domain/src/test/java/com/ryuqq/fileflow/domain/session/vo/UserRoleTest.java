package com.ryuqq.fileflow.domain.session.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRole Enum Tests")
class UserRoleTest {

    @Test
    @DisplayName("모든 사용자 Role은 정의된 네임스페이스를 반환해야 한다")
    void shouldReturnCorrectNamespace() {
        assertThat(UserRole.ADMIN.getNamespace()).isEqualTo("connectly");
        assertThat(UserRole.SELLER.getNamespace()).isEqualTo("setof");
        assertThat(UserRole.DEFAULT.getNamespace()).isEqualTo("setof");
    }
}

