package com.ryuqq.fileflow.application.upload.assembler;

import com.ryuqq.fileflow.application.upload.dto.command.InitMultipartCommand;
import com.ryuqq.fileflow.application.upload.dto.response.InitMultipartResponse;
import com.ryuqq.fileflow.domain.iam.organization.Organization;
import com.ryuqq.fileflow.domain.iam.tenant.Tenant;
import com.ryuqq.fileflow.domain.iam.usercontext.UserContext;
import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.ProviderUploadId;
import com.ryuqq.fileflow.domain.upload.StorageContext;
import com.ryuqq.fileflow.domain.upload.TotalParts;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;

/**
 * Multipart Upload Assembler
 * DTO ↔ Domain 변환을 담당하는 Assembler
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Command → Domain 객체 변환 (UploadSession, MultipartUpload)</li>
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
public final class MultipartUploadAssembler {

    /**
     * InitMultipartCommand와 IAM 컨텍스트로부터 UploadSession 생성
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>StorageContext 생성 (IAM 컨텍스트 기반)</li>
     *   <li>UploadSession.createForMultipartUpload() 호출</li>
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
     * @return 생성된 UploadSession (MULTIPART_UPLOAD 타입)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static UploadSession toUploadSession(
        InitMultipartCommand command,
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
        // Note: Multipart는 Single Upload와 동일한 방식으로 생성
        return UploadSession.createForSingleUpload(
            storageContext,
            fileName,
            fileSize
        );
    }

    /**
     * UploadSession과 S3 UploadId로부터 MultipartUpload 생성
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>MultipartUpload.forNew() 호출</li>
     *   <li>initiate() 메서드로 상태 초기화</li>
     *   <li>Domain 객체 반환</li>
     * </ol>
     *
     * <p><strong>Law of Demeter 준수:</strong></p>
     * <ul>
     *   <li>Domain 팩토리 메서드에 위임</li>
     *   <li>Domain 메서드로 상태 변경 (Tell, Don't Ask)</li>
     * </ul>
     *
     * @param session UploadSession Domain 객체
     * @param providerUploadId S3 UploadId (Provider Upload ID)
     * @param totalParts 전체 파트 수
     * @return 생성된 MultipartUpload (INIT → IN_PROGRESS)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static MultipartUpload toMultipartUpload(
        UploadSession session,
        String providerUploadId,
        Integer totalParts
    ) {
        if (session == null) {
            throw new IllegalArgumentException("UploadSession은 필수입니다");
        }
        if (providerUploadId == null || providerUploadId.isBlank()) {
            throw new IllegalArgumentException("Provider Upload ID는 필수입니다");
        }
        if (totalParts == null || totalParts <= 0) {
            throw new IllegalArgumentException("Total Parts는 양수여야 합니다");
        }

        // 1. UploadSessionId 추출
        Long sessionIdValue = session.getIdValue();
        if (sessionIdValue == null) {
            throw new IllegalStateException("UploadSession ID가 없습니다 (저장되지 않은 세션)");
        }

        // 2. MultipartUpload 생성 (Domain 팩토리 메서드)
        MultipartUpload multipartUpload = MultipartUpload.forNew(
            UploadSessionId.of(sessionIdValue)
        );

        // 3. 상태 초기화 (INIT → IN_PROGRESS, Tell Don't Ask)
        multipartUpload.initiate(
            ProviderUploadId.of(providerUploadId),
            TotalParts.of(totalParts)
        );

        return multipartUpload;
    }

    /**
     * UploadSession과 MultipartUpload로부터 InitMultipartResponse 생성
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>UploadSession과 MultipartUpload에서 필요한 값 추출</li>
     *   <li>Response DTO 생성</li>
     * </ol>
     *
     * <p><strong>Law of Demeter 준수:</strong></p>
     * <ul>
     *   <li>UploadSession의 직접 getter만 사용</li>
     *   <li>MultipartUpload의 직접 getter만 사용</li>
     *   <li>Value Object의 value() 메서드 사용 (체이닝 1단계만)</li>
     * </ul>
     *
     * @param session UploadSession Domain 객체
     * @param multipartUpload MultipartUpload Domain 객체
     * @return InitMultipartResponse DTO
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static InitMultipartResponse toInitMultipartResponse(
        UploadSession session,
        MultipartUpload multipartUpload
    ) {
        if (session == null) {
            throw new IllegalArgumentException("UploadSession은 필수입니다");
        }
        if (multipartUpload == null) {
            throw new IllegalArgumentException("MultipartUpload는 필수입니다");
        }

        // 1. UploadSession에서 필요한 값 추출
        String sessionKey = session.getSessionKey().value();
        String storageKey = session.getStorageKey().value();

        // 2. MultipartUpload에서 필요한 값 추출
        String uploadId = multipartUpload.getProviderUploadIdValue();
        Integer totalParts = multipartUpload.getTotalParts().value();

        // 3. Response DTO 생성
        return InitMultipartResponse.of(
            sessionKey,
            uploadId,
            totalParts,
            storageKey
        );
    }
}
