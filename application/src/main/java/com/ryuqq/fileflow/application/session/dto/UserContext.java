package com.ryuqq.fileflow.application.session.dto;

import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UploaderId;
import com.ryuqq.fileflow.domain.iam.vo.UploaderType;

/**
 * 사용자 컨텍스트 (JWT에서 추출)
 * <p>
 * JWT 토큰에서 추출된 사용자 인증 정보를 담는 DTO입니다.
 * UseCase 실행 시 필요한 사용자 컨텍스트를 제공합니다.
 * </p>
 *
 * @param tenantId 테넌트 ID
 * @param uploaderId 업로더 ID
 * @param uploaderType 업로더 타입 (ADMIN, CUSTOMER, THIRD_PARTY)
 * @param uploaderSlug 업로더 식별자 (예: "connectly")
 */
public record UserContext(
    TenantId tenantId,
    UploaderId uploaderId,
    UploaderType uploaderType,
    String uploaderSlug
) {}
