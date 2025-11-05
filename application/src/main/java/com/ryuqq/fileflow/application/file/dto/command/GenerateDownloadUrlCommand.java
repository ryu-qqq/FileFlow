package com.ryuqq.fileflow.application.file.dto.command;

import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

import java.time.Duration;

/**
 * GenerateDownloadUrl Command DTO
 *
 * <p>CQRS Command Side - 파일 다운로드 URL 생성 요청</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>다운로드 URL 생성 요청 파라미터 캡슐화</li>
 *   <li>테넌트/조직 스코프 검증</li>
 *   <li>URL 만료 시간 관리</li>
 * </ul>
 *
 * <p><strong>사용 시나리오</strong>:</p>
 * <ul>
 *   <li>사용자 파일 다운로드 링크 생성</li>
 *   <li>임시 공유 링크 생성</li>
 *   <li>API 응답에 포함할 서명된 URL</li>
 * </ul>
 *
 * <p><strong>보안</strong>:</p>
 * <ul>
 *   <li>테넌트/조직 스코프 강제 검증</li>
 *   <li>파일 가시성 검사 (PRIVATE → 서명 필수)</li>
 *   <li>파일 상태 검증 (AVAILABLE만 다운로드 허용)</li>
 * </ul>
 *
 * @param fileId 파일 ID
 * @param tenantId 테넌트 ID (보안 스코프)
 * @param organizationId 조직 ID (선택)
 * @param expirationDuration URL 만료 시간 (기본: 1시간)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record GenerateDownloadUrlCommand(
    FileId fileId,
    TenantId tenantId,
    Long organizationId,
    Duration expirationDuration
) {
    /**
     * Compact Constructor (검증 로직)
     */
    public GenerateDownloadUrlCommand {
        if (fileId == null) {
            throw new IllegalArgumentException("FileId는 필수입니다");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId는 필수입니다");
        }
        if (expirationDuration == null) {
            expirationDuration = Duration.ofHours(1); // 기본 1시간
        }
        if (expirationDuration.isNegative() || expirationDuration.isZero()) {
            throw new IllegalArgumentException("Expiration Duration은 양수여야 합니다");
        }
        if (expirationDuration.toHours() > 24) {
            throw new IllegalArgumentException("Expiration Duration은 24시간을 초과할 수 없습니다");
        }
    }

    /**
     * Static Factory Method - 기본 만료 시간 (1시간)
     *
     * @param fileId 파일 ID
     * @param tenantId 테넌트 ID
     * @return GenerateDownloadUrlCommand
     */
    public static GenerateDownloadUrlCommand of(FileId fileId, TenantId tenantId) {
        return new GenerateDownloadUrlCommand(fileId, tenantId, null, null);
    }

    /**
     * Static Factory Method - 조직 스코프 포함
     *
     * @param fileId 파일 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @return GenerateDownloadUrlCommand
     */
    public static GenerateDownloadUrlCommand of(
        FileId fileId,
        TenantId tenantId,
        Long organizationId
    ) {
        return new GenerateDownloadUrlCommand(fileId, tenantId, organizationId, null);
    }

    /**
     * Static Factory Method - 커스텀 만료 시간
     *
     * @param fileId 파일 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param expirationDuration URL 만료 시간
     * @return GenerateDownloadUrlCommand
     */
    public static GenerateDownloadUrlCommand of(
        FileId fileId,
        TenantId tenantId,
        Long organizationId,
        Duration expirationDuration
    ) {
        return new GenerateDownloadUrlCommand(fileId, tenantId, organizationId, expirationDuration);
    }
}
