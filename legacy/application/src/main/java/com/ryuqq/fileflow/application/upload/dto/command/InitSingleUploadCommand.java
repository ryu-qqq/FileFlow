package com.ryuqq.fileflow.application.upload.dto.command;

import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

/**
 * Init Single Upload Command
 *
 * <p>단일 업로드 초기화 명령 DTO입니다.</p>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>100MB 미만 파일의 단일 업로드</li>
 *   <li>UploadSession 생성 및 Presigned URL 발급</li>
 *   <li>IAM 컨텍스트 기반 스토리지 정책 적용</li>
 * </ul>
 *
 * <p><strong>불변성 (Immutability):</strong></p>
 * <ul>
 *   <li>Java Record 사용으로 모든 필드 final</li>
 *   <li>생성 후 상태 변경 불가</li>
 * </ul>
 *
 * @param tenantId 테넌트 ID (필수)
 * @param organizationId 조직 ID (Optional, null 가능)
 * @param userContextId 유저 컨텍스트 ID (Optional, null 가능)
 * @param fileName 파일명
 * @param fileSize 파일 크기 (bytes)
 * @param contentType 콘텐츠 타입
 * @param checksum 체크섬 (선택)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record InitSingleUploadCommand(
    TenantId tenantId,
    Long organizationId,
    Long userContextId,
    String fileName,
    Long fileSize,
    String contentType,
    String checksum
) {

    /**
     * Static Factory Method (기본 필드, IAM 컨텍스트 없이)
     *
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기 (bytes)
     * @param contentType 콘텐츠 타입
     * @return InitSingleUploadCommand 인스턴스
     */
    public static InitSingleUploadCommand of(
        TenantId tenantId,
        String fileName,
        Long fileSize,
        String contentType
    ) {
        return new InitSingleUploadCommand(
            tenantId,
            null,  // organizationId
            null,  // userContextId
            fileName,
            fileSize,
            contentType,
            null   // checksum
        );
    }

    /**
     * Static Factory Method (IAM 컨텍스트 포함, 체크섬 없이)
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID (Optional)
     * @param userContextId 유저 컨텍스트 ID (Optional)
     * @param fileName 파일명
     * @param fileSize 파일 크기 (bytes)
     * @param contentType 콘텐츠 타입
     * @return InitSingleUploadCommand 인스턴스
     */
    public static InitSingleUploadCommand of(
        TenantId tenantId,
        Long organizationId,
        Long userContextId,
        String fileName,
        Long fileSize,
        String contentType
    ) {
        return new InitSingleUploadCommand(
            tenantId,
            organizationId,
            userContextId,
            fileName,
            fileSize,
            contentType,
            null  // checksum
        );
    }

    /**
     * Static Factory Method (전체 필드)
     *
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID (Optional)
     * @param userContextId 유저 컨텍스트 ID (Optional)
     * @param fileName 파일명
     * @param fileSize 파일 크기 (bytes)
     * @param contentType 콘텐츠 타입
     * @param checksum 체크섬
     * @return InitSingleUploadCommand 인스턴스
     */
    public static InitSingleUploadCommand of(
        TenantId tenantId,
        Long organizationId,
        Long userContextId,
        String fileName,
        Long fileSize,
        String contentType,
        String checksum
    ) {
        return new InitSingleUploadCommand(
            tenantId,
            organizationId,
            userContextId,
            fileName,
            fileSize,
            contentType,
            checksum
        );
    }

}
