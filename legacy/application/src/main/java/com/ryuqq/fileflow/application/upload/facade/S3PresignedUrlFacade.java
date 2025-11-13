package com.ryuqq.fileflow.application.upload.facade;

import com.ryuqq.fileflow.application.iam.context.IamContext;
import com.ryuqq.fileflow.application.upload.port.out.S3StoragePort;
import com.ryuqq.fileflow.domain.upload.StorageContext;
import com.ryuqq.fileflow.domain.upload.StorageKey;

import java.time.Duration;

import org.springframework.stereotype.Component;

/**
 * S3 Presigned URL Facade
 *
 * <p>S3 Presigned URL 생성을 전담하는 Facade 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>단일 업로드용 Presigned PUT URL 생성</li>
 *   <li>Multipart 업로드용 Presigned Upload Part URL 생성</li>
 *   <li>StorageContext 기반 Bucket 이름 결정</li>
 *   <li>S3StoragePort 호출 캡슐화</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>InitSingleUploadService: 단일 업로드 Presigned URL 발급</li>
 *   <li>InitMultipartUploadService: Multipart 파트 업로드 Presigned URL 발급</li>
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
public class S3PresignedUrlFacade {

    private static final Duration DEFAULT_PRESIGNED_URL_DURATION = Duration.ofHours(1);

    private final S3StoragePort s3StoragePort;

    /**
     * 생성자
     *
     * @param s3StoragePort S3 Storage Port
     */
    public S3PresignedUrlFacade(S3StoragePort s3StoragePort) {
        this.s3StoragePort = s3StoragePort;
    }

    /**
     * 단일 업로드용 Presigned PUT URL 생성
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>IamContext로부터 StorageContext 재구성</li>
     *   <li>StorageContext에게 Bucket 이름 생성 위임</li>
     *   <li>S3StoragePort 호출: presignPutObject()</li>
     *   <li>Presigned PUT URL 반환</li>
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
     * @param contentType Content Type
     * @return Presigned PUT URL (1시간 유효)
     * @throws IllegalArgumentException iamContext 또는 storageKey가 null인 경우
     */
    public String generateSingleUploadUrl(
        IamContext iamContext,
        StorageKey storageKey,
        String contentType
    ) {
        if (iamContext == null) {
            throw new IllegalArgumentException("IamContext는 필수입니다");
        }
        if (storageKey == null) {
            throw new IllegalArgumentException("StorageKey는 필수입니다");
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

        // 4. Presigned PUT URL 생성
        return s3StoragePort.presignPutObject(
            bucket,
            key,
            contentType,
            DEFAULT_PRESIGNED_URL_DURATION
        );
    }

    /**
     * Multipart 파트 업로드용 Presigned URL 생성
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>IamContext로부터 StorageContext 재구성</li>
     *   <li>StorageContext에게 Bucket 이름 생성 위임</li>
     *   <li>S3StoragePort 호출: presignUploadPart()</li>
     *   <li>Presigned Upload Part URL 반환</li>
     * </ol>
     *
     * @param iamContext IAM Context (Tenant, Organization, UserContext)
     * @param storageKey Storage Key
     * @param uploadId S3 Multipart Upload ID
     * @param partNumber 파트 번호
     * @return Presigned Upload Part URL (1시간 유효)
     * @throws IllegalArgumentException 필수 파라미터가 null인 경우
     */
    public String generateMultipartUploadUrl(
        IamContext iamContext,
        StorageKey storageKey,
        String uploadId,
        Integer partNumber
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
        if (partNumber == null || partNumber <= 0) {
            throw new IllegalArgumentException("Part Number는 양수여야 합니다");
        }

        // 1. StorageContext 재구성
        StorageContext storageContext = StorageContext.from(
            iamContext.tenant(),
            iamContext.organization(),
            iamContext.userContext()
        );

        // 2. Bucket 이름 생성
        String bucket = storageContext.generateBucketName();

        // 3. StorageKey 값 추출
        String key = storageKey.value();

        // 4. Presigned Upload Part URL 생성
        return s3StoragePort.presignUploadPart(
            bucket,
            key,
            uploadId,
            partNumber,
            DEFAULT_PRESIGNED_URL_DURATION
        );
    }
}
