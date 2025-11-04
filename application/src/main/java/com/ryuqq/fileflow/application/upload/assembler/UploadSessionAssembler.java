package com.ryuqq.fileflow.application.upload.assembler;

import com.ryuqq.fileflow.application.upload.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.response.SingleUploadResponse;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.StorageContext;
import com.ryuqq.fileflow.domain.upload.UploadSession;

/**
 * Upload Session Assembler
 * DTO ↔ Domain 변환을 담당하는 Assembler
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Command → Domain 객체 변환</li>
 *   <li>Domain 객체 → Response DTO 변환</li>
 *   <li>IAM 컨텍스트 기반 Domain 객체 생성</li>
 * </ul>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 *   <li>✅ Assembler 패턴 - DTO/Domain 변환 전담</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadSessionAssembler {

    /**
     * InitSingleUploadCommand와 IAM 컨텍스트로부터 UploadSession 생성
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>StorageContext 생성 (IAM 컨텍스트 기반)</li>
     *   <li>UploadSession.createForSingleUpload() 호출</li>
     *   <li>Domain 객체 반환</li>
     * </ol>
     *
     * <p><strong>Law of Demeter 준수:</strong></p>
     * <ul>
     *   <li>Command에서 직접 값 추출 (Getter 체이닝 없음)</li>
     *   <li>Domain 팩토리 메서드에 위임 (Tell, Don't Ask)</li>
     * </ul>
     *
     * @param command Command DTO
     * @param tenant Tenant Aggregate
     * @param organization Organization Aggregate (Optional, null 가능)
     * @param userContext UserContext Aggregate (Optional, null 가능)
     * @return 생성된 UploadSession
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static UploadSession toUploadSession(
        InitSingleUploadCommand command,
        Tenant tenant,
        Organization organization,
        UserContext userContext
    ) {
        if (command == null) {
            throw new IllegalArgumentException("Command는 필수입니다");
        }
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant는 필수입니다");
        }

        // 1. StorageContext 생성 (IAM 컨텍스트 기반)
        StorageContext storageContext = StorageContext.from(
            tenant,
            organization,
            userContext
        );

        // 2. Command에서 FileName, FileSize 추출
        FileName fileName = FileName.of(command.fileName());
        FileSize fileSize = FileSize.of(command.fileSize());

        // 3. UploadSession 생성 (Domain 팩토리 메서드에 위임)
        return UploadSession.createForSingleUpload(
            storageContext,
            fileName,
            fileSize
        );
    }

    /**
     * UploadSession과 Presigned URL로부터 SingleUploadResponse 생성
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>UploadSession에서 필요한 값 추출</li>
     *   <li>Response DTO 생성</li>
     * </ol>
     *
     * <p><strong>Law of Demeter 준수:</strong></p>
     * <ul>
     *   <li>UploadSession의 직접 getter만 사용</li>
     *   <li>Value Object의 value() 메서드 사용 (체이닝 1단계만)</li>
     * </ul>
     *
     * @param session UploadSession Domain 객체
     * @param presignedUrl S3 Presigned PUT URL
     * @return SingleUploadResponse DTO
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static SingleUploadResponse toSingleUploadResponse(
        UploadSession session,
        String presignedUrl
    ) {
        if (session == null) {
            throw new IllegalArgumentException("UploadSession은 필수입니다");
        }
        if (presignedUrl == null || presignedUrl.isBlank()) {
            throw new IllegalArgumentException("Presigned URL은 필수입니다");
        }

        // UploadSession에서 필요한 값 추출
        String sessionKey = session.getSessionKey().value();
        String storageKey = session.getStorageKey().value();

        // Response DTO 생성
        return SingleUploadResponse.of(
            sessionKey,
            presignedUrl,
            storageKey
        );
    }
}
