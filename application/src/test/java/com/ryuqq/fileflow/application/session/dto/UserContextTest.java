package com.ryuqq.fileflow.application.session.dto;

import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UploaderId;
import com.ryuqq.fileflow.domain.iam.vo.UploaderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserContext DTO 테스트
 * <p>
 * JWT에서 추출된 사용자 컨텍스트 정보를 담는 DTO
 * </p>
 */
class UserContextTest {

    @Test
    @DisplayName("UserContext를 생성해야 한다")
    void shouldCreateUserContext() {
        // When
        UserContext userContext = new UserContext(
            TenantId.of(1L),
            UploaderId.of(100L),
            UploaderType.ADMIN,
            "connectly"
        );

        // Then
        assertThat(userContext.tenantId().value()).isEqualTo(1L);
        assertThat(userContext.uploaderId().value()).isEqualTo(100L);
        assertThat(userContext.uploaderType()).isEqualTo(UploaderType.ADMIN);
        assertThat(userContext.uploaderSlug()).isEqualTo("connectly");
    }

    @Test
    @DisplayName("모든 필드가 not null이어야 한다")
    void shouldHaveAllFieldsNotNull() {
        // When
        UserContext userContext = new UserContext(
            TenantId.of(1L),
            UploaderId.of(100L),
            UploaderType.ADMIN,
            "connectly"
        );

        // Then
        assertThat(userContext.tenantId()).isNotNull();
        assertThat(userContext.uploaderId()).isNotNull();
        assertThat(userContext.uploaderType()).isNotNull();
        assertThat(userContext.uploaderSlug()).isNotNull();
    }
}
