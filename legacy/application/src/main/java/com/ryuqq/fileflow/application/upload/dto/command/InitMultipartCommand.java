package com.ryuqq.fileflow.application.upload.dto.command;

import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

/**
 * Multipart 업로드 초기화 Command
 *
 * <p>대용량 파일(100MB 이상)의 Multipart 업로드를 위한 Command DTO입니다.</p>
 *
 * <p><strong>IAM 컨텍스트 추가 (리팩토링):</strong></p>
 * <ul>
 *   <li>organizationId, userContextId 추가</li>
 *   <li>StorageContext 생성을 위한 IAM 정보 제공</li>
 * </ul>
 *
 * @param tenantId 테넌트 ID (필수)
 * @param organizationId 조직 ID (Optional, null 가능)
 * @param userContextId 유저 컨텍스트 ID (Optional, null 가능)
 * @param fileName 파일명
 * @param fileSize 파일 크기 (bytes)
 * @param contentType Content Type
 * @param checksum 체크섬 (nullable)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record InitMultipartCommand(
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
     * @param fileSize 파일 크기
     * @param contentType Content Type
     * @return InitMultipartCommand 인스턴스
     */
    public static InitMultipartCommand of(
        TenantId tenantId,
        String fileName,
        Long fileSize,
        String contentType
    ) {
        return new InitMultipartCommand(
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
     * @param fileSize 파일 크기
     * @param contentType Content Type
     * @return InitMultipartCommand 인스턴스
     */
    public static InitMultipartCommand of(
        TenantId tenantId,
        Long organizationId,
        Long userContextId,
        String fileName,
        Long fileSize,
        String contentType
    ) {
        return new InitMultipartCommand(
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
     * @param fileSize 파일 크기
     * @param contentType Content Type
     * @param checksum 체크섬
     * @return InitMultipartCommand 인스턴스
     */
    public static InitMultipartCommand of(
        TenantId tenantId,
        Long organizationId,
        Long userContextId,
        String fileName,
        Long fileSize,
        String contentType,
        String checksum
    ) {
        return new InitMultipartCommand(
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
