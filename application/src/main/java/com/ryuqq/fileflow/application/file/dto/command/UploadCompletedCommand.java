package com.ryuqq.fileflow.application.file.dto.command;

import com.ryuqq.fileflow.domain.iam.tenant.TenantId;
import com.ryuqq.fileflow.domain.upload.Checksum;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

/**
 * Upload 완료 Command
 *
 * <p>Upload Session 완료 이벤트로부터 전달받는 정보를 담은 Command입니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용 (Lombok 금지)</li>
 *   <li>✅ 불변성 보장</li>
 *   <li>✅ 검증 로직 포함</li>
 * </ul>
 *
 * @param uploadSessionId 업로드 세션 ID
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID (optional)
 * @param ownerUserId 파일 소유자 사용자 ID
 * @param fileName 파일명
 * @param fileSize 파일 크기
 * @param mimeType MIME 타입
 * @param storageKey S3 저장 키
 * @param checksum SHA-256 체크섬
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record UploadCompletedCommand(
    UploadSessionId uploadSessionId,
    TenantId tenantId,
    Long organizationId,
    Long ownerUserId,
    FileName fileName,
    FileSize fileSize,
    MimeType mimeType,
    StorageKey storageKey,
    Checksum checksum
) {

    /**
     * Compact Constructor - 검증 로직
     */
    public UploadCompletedCommand {
        if (uploadSessionId == null) {
            throw new IllegalArgumentException("UploadSession ID는 필수입니다");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID는 필수입니다");
        }
        if (ownerUserId == null || ownerUserId <= 0) {
            throw new IllegalArgumentException("Owner User ID는 필수입니다");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("FileName은 필수입니다");
        }
        if (fileSize == null) {
            throw new IllegalArgumentException("FileSize는 필수입니다");
        }
        if (storageKey == null) {
            throw new IllegalArgumentException("StorageKey는 필수입니다");
        }
        if (checksum == null) {
            throw new IllegalArgumentException("Checksum은 필수입니다");
        }
    }

    /**
     * UploadCompletedCommand 생성 (Static Factory Method)
     *
     * @param uploadSessionId 업로드 세션 ID
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param ownerUserId 파일 소유자 사용자 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param mimeType MIME 타입
     * @param storageKey S3 저장 키
     * @param checksum 체크섬
     * @return UploadCompletedCommand
     */
    public static UploadCompletedCommand of(
        UploadSessionId uploadSessionId,
        TenantId tenantId,
        Long organizationId,
        Long ownerUserId,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        StorageKey storageKey,
        Checksum checksum
    ) {
        return new UploadCompletedCommand(
            uploadSessionId,
            tenantId,
            organizationId,
            ownerUserId,
            fileName,
            fileSize,
            mimeType,
            storageKey,
            checksum
        );
    }
}
