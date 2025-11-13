package com.ryuqq.fileflow.application.upload.facade;

import com.ryuqq.fileflow.application.iam.context.IamContext;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.command.CompleteMultipartUploadResult;
import com.ryuqq.fileflow.application.upload.dto.command.CompletedPartCommand;
import com.ryuqq.fileflow.application.upload.dto.command.InitiateMultipartUploadCommand;
import com.ryuqq.fileflow.application.upload.dto.command.InitiateMultipartUploadResult;
import com.ryuqq.fileflow.application.upload.dto.response.S3CompleteResultResponse;
import com.ryuqq.fileflow.application.upload.dto.response.S3InitResultResponse;
import com.ryuqq.fileflow.application.upload.port.out.S3StoragePort;
import com.ryuqq.fileflow.domain.upload.StorageContext;
import com.ryuqq.fileflow.domain.upload.StorageKey;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * S3 Multipart Facade
 *
 * <p>S3 Multipart Upload 초기화를 전담하는 Facade 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>S3 Multipart Upload 초기화</li>
 *   <li>S3 Multipart Upload 완료</li>
 *   <li>StorageContext 기반 Bucket 이름 결정</li>
 *   <li>파트 개수 계산</li>
 *   <li>S3StoragePort 호출 캡슐화</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>InitMultipartUploadService: Multipart 초기화</li>
 *   <li>CompleteMultipartUploadService: Multipart 완료</li>
 * </ul>
 *
 * <p><strong>패턴:</strong></p>
 * <ul>
 *   <li>Facade Pattern: S3StoragePort 및 StorageContext 캡슐화</li>
 *   <li>Tell, Don't Ask: StorageContext에게 Bucket 생성 위임</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class S3MultipartFacade {

    private static final long PART_SIZE = 100 * 1024 * 1024L; // 100MB (AWS 권장)

    private final S3StoragePort s3StoragePort;

    /**
     * 생성자
     *
     * @param s3StoragePort S3 Storage Port
     */
    public S3MultipartFacade(S3StoragePort s3StoragePort) {
        this.s3StoragePort = s3StoragePort;
    }

    /**
     * Multipart Upload 초기화
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>IamContext로부터 StorageContext 재구성</li>
     *   <li>StorageContext에게 Bucket 이름 생성 위임</li>
     *   <li>S3StoragePort 호출: initiateMultipartUpload()</li>
     *   <li>파트 개수 계산</li>
     *   <li>S3InitResultResponse 반환</li>
     * </ol>
     *
     * <p><strong>Tell, Don't Ask 패턴:</strong></p>
     * <ul>
     *   <li>StorageContext에게 Bucket 이름 생성 위임</li>
     *   <li>Getter 체이닝 없음</li>
     * </ul>
     *
     * @param iamContext IAM Context (Tenant, Organization, UserContext)
     * @param storageKey Storage Key
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param contentType Content Type
     * @return S3 초기화 결과 (Upload ID, Part Count 포함)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     */
    public S3InitResultResponse initializeMultipart(
        IamContext iamContext,
        StorageKey storageKey,
        String fileName,
        Long fileSize,
        String contentType
    ) {
        if (iamContext == null) {
            throw new IllegalArgumentException("IamContext는 필수입니다");
        }
        if (storageKey == null) {
            throw new IllegalArgumentException("StorageKey는 필수입니다");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일명은 필수입니다");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("파일 크기는 양수여야 합니다");
        }

        // 1. StorageContext 재구성 (Tell, Don't Ask)
        StorageContext storageContext = StorageContext.from(
            iamContext.tenant(),
            iamContext.organization(),
            iamContext.userContext()
        );

        // 2. Bucket 이름 생성 (Tell, Don't Ask)
        String bucket = storageContext.generateBucketName();

        // 3. StorageKey 값 추출 (Law of Demeter 준수 - 1단계만)
        String key = storageKey.value();

        // 4. S3 Multipart Upload 초기화
        InitiateMultipartUploadCommand command = InitiateMultipartUploadCommand.of(
            bucket,
            key,
            contentType
        );

        InitiateMultipartUploadResult result = s3StoragePort.initiateMultipartUpload(command);

        // 5. 파트 개수 계산
        int partCount = calculatePartCount(fileSize);

        // 6. S3InitResultResponse 생성
        return new S3InitResultResponse(
            result.uploadId(),
            key,
            bucket,
            partCount
        );
    }

    /**
     * Multipart Upload 완료
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>IamContext로부터 StorageContext 재구성</li>
     *   <li>StorageContext에게 Bucket 이름 생성 위임</li>
     *   <li>S3StoragePort 호출: completeMultipartUpload()</li>
     *   <li>S3CompleteResultResponse 반환</li>
     * </ol>
     *
     * <p><strong>Tell, Don't Ask 패턴:</strong></p>
     * <ul>
     *   <li>StorageContext에게 Bucket 이름 생성 위임</li>
     *   <li>Getter 체이닝 없음</li>
     * </ul>
     *
     * @param iamContext IAM Context (Tenant, Organization, UserContext)
     * @param storageKey Storage Key
     * @param uploadId S3 Provider Upload ID
     * @param completedParts 완료된 파트 목록
     * @param fileSize 파일 크기
     * @return S3 완료 결과 (ETag, Location 포함)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     */
    public S3CompleteResultResponse completeMultipart(
        IamContext iamContext,
        StorageKey storageKey,
        String uploadId,
        List<CompletedPartCommand> completedParts,
        Long fileSize
    ) {
        if (iamContext == null) {
            throw new IllegalArgumentException("IamContext는 필수입니다");
        }
        if (storageKey == null) {
            throw new IllegalArgumentException("StorageKey는 필수입니다");
        }
        if (uploadId == null || uploadId.isBlank()) {
            throw new IllegalArgumentException("Upload ID는 필수입니다");
        }
        if (completedParts == null || completedParts.isEmpty()) {
            throw new IllegalArgumentException("완료된 파트 목록은 필수입니다");
        }
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("파일 크기는 양수여야 합니다");
        }

        // 1. StorageContext 재구성 (Tell, Don't Ask)
        StorageContext storageContext = StorageContext.from(
            iamContext.tenant(),
            iamContext.organization(),
            iamContext.userContext()
        );

        // 2. Bucket 이름 생성 (Tell, Don't Ask)
        String bucket = storageContext.generateBucketName();

        // 3. StorageKey 값 추출 (Law of Demeter 준수 - 1단계만)
        String key = storageKey.value();

        // 4. S3 Complete Multipart Upload Request
        CompleteMultipartUploadCommand command = CompleteMultipartUploadCommand.of(
            bucket,
            key,
            uploadId,
            completedParts
        );

        CompleteMultipartUploadResult result = s3StoragePort.completeMultipartUpload(command);

        // 5. S3CompleteResultResponse 생성
        return new S3CompleteResultResponse(
            result.etag(),
            result.location(),
            fileSize
        );
    }

    /**
     * 파트 개수 계산
     *
     * <p>파일 크기를 파트 크기(100MB)로 나누어 필요한 파트 개수를 계산합니다.</p>
     *
     * @param fileSize 파일 크기 (bytes)
     * @return 파트 개수
     */
    private int calculatePartCount(Long fileSize) {
        return (int) Math.ceil((double) fileSize / PART_SIZE);
    }
}
