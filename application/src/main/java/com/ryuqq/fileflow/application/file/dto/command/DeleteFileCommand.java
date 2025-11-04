package com.ryuqq.fileflow.application.file.dto.command;

import com.ryuqq.fileflow.domain.file.asset.FileId;
import com.ryuqq.fileflow.domain.iam.tenant.TenantId;

/**
 * DeleteFile Command DTO
 *
 * <p>CQRS Command Side - 파일 삭제 요청 (Soft Delete)</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>파일 삭제 요청 파라미터 캡슐화</li>
 *   <li>테넌트/조직 스코프 검증</li>
 *   <li>삭제 권한 검증 정보 제공</li>
 * </ul>
 *
 * <p><strong>사용 시나리오</strong>:</p>
 * <ul>
 *   <li>사용자 파일 삭제</li>
 *   <li>관리자 파일 정리</li>
 *   <li>만료된 파일 자동 삭제 (Batch Job)</li>
 * </ul>
 *
 * <p><strong>삭제 규칙</strong>:</p>
 * <ul>
 *   <li>Soft Delete만 수행 (deleted_at 타임스탬프)</li>
 *   <li>물리 삭제는 별도 Batch Job에서 처리</li>
 *   <li>삭제된 파일은 조회 불가 (deleted_at IS NULL 필터)</li>
 * </ul>
 *
 * <p><strong>보안</strong>:</p>
 * <ul>
 *   <li>테넌트/조직 스코프 강제 검증</li>
 *   <li>삭제 권한 검증 (SELF 또는 TENANT 권한)</li>
 *   <li>이미 삭제된 파일 재삭제 방지</li>
 * </ul>
 *
 * @param fileId 파일 ID
 * @param tenantId 테넌트 ID (보안 스코프)
 * @param organizationId 조직 ID (선택)
 * @param requesterId 요청자 User ID (권한 검증용)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record DeleteFileCommand(
    FileId fileId,
    TenantId tenantId,
    Long organizationId,
    Long requesterId
) {
    /**
     * Compact Constructor (검증 로직)
     */
    public DeleteFileCommand {
        if (fileId == null) {
            throw new IllegalArgumentException("FileId는 필수입니다");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId는 필수입니다");
        }
        if (requesterId == null) {
            throw new IllegalArgumentException("RequesterId는 필수입니다 (권한 검증)");
        }
    }

    /**
     * Static Factory Method - 기본
     *
     * @param fileId 파일 ID
     * @param tenantId 테넌트 ID
     * @param requesterId 요청자 User ID
     * @return DeleteFileCommand
     */
    public static DeleteFileCommand of(
        FileId fileId,
        TenantId tenantId,
        Long requesterId
    ) {
        return new DeleteFileCommand(fileId, tenantId, null, requesterId);
    }

    /**
     * Static Factory Method - 조직 스코프 포함
     *
     * @param fileId 파일 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param requesterId 요청자 User ID
     * @return DeleteFileCommand
     */
    public static DeleteFileCommand of(
        FileId fileId,
        TenantId tenantId,
        Long organizationId,
        Long requesterId
    ) {
        return new DeleteFileCommand(fileId, tenantId, organizationId, requesterId);
    }
}
