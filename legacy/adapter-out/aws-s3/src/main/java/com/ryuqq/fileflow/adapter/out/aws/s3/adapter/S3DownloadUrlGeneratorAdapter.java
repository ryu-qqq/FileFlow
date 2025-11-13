package com.ryuqq.fileflow.adapter.out.aws.s3.adapter;

import com.ryuqq.fileflow.adapter.out.aws.s3.exception.S3StorageException;
import com.ryuqq.fileflow.application.file.port.out.DownloadUrlGeneratorPort;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

/**
 * S3 Download URL Generator Adapter
 *
 * <p>Application Layer의 {@link DownloadUrlGeneratorPort}를 구현하는 AWS S3 Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>S3 Presigned GET URL 생성</li>
 *   <li>다운로드 URL 만료 시간 관리</li>
 *   <li>AWS SDK Exception을 Infrastructure Exception으로 변환</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴 준수</li>
 *   <li>✅ AWS SDK v2 사용</li>
 *   <li>✅ Presigned URL (AWS Signature V4)</li>
 *   <li>✅ Exception 변환 (S3Exception → S3StorageException)</li>
 * </ul>
 *
 * <p><strong>Presigned URL 특징:</strong></p>
 * <ul>
 *   <li>서명된 URL (AWS Signature V4)</li>
 *   <li>만료 시간 포함 (기본 1시간, 최대 24시간)</li>
 *   <li>일회성 사용 권장</li>
 *   <li>인증 없이 다운로드 가능</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class S3DownloadUrlGeneratorAdapter implements DownloadUrlGeneratorPort {

    private final S3Presigner s3Presigner;
    private final String defaultBucket;

    /**
     * 생성자
     *
     * @param s3Presigner S3 Presigner
     * @param defaultBucket 기본 S3 Bucket (설정에서 주입)
     */
    public S3DownloadUrlGeneratorAdapter(
        S3Presigner s3Presigner,
        String defaultBucket
    ) {
        this.s3Presigner = s3Presigner;
        this.defaultBucket = defaultBucket;
    }

    /**
     * S3 Presigned Download URL 생성
     *
     * <p><strong>URL 형식:</strong></p>
     * <pre>
     * https://bucket-name.s3.region.amazonaws.com/storage-key?
     *   X-Amz-Algorithm=AWS4-HMAC-SHA256&
     *   X-Amz-Credential=...&
     *   X-Amz-Date=...&
     *   X-Amz-Expires=3600&
     *   X-Amz-SignedHeaders=host&
     *   X-Amz-Signature=...
     * </pre>
     *
     * <p><strong>보안:</strong></p>
     * <ul>
     *   <li>AWS Signature V4 서명</li>
     *   <li>만료 시간 강제 (최대 24시간)</li>
     *   <li>인증 정보 노출 없음</li>
     * </ul>
     *
     * @param storageKey S3 Storage Key (예: tenant-123/org-456/file-789.pdf)
     * @param expirationDuration URL 만료 시간
     * @return Presigned Download URL
     * @throws S3StorageException Presigned URL 생성 실패 시
     * @throws IllegalArgumentException storageKey가 null이거나 비어있는 경우
     */
    @Override
    public String generateDownloadUrl(String storageKey, Duration expirationDuration) {
        // 입력 검증
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("Storage Key는 null이거나 비어있을 수 없습니다");
        }
        if (expirationDuration == null) {
            throw new IllegalArgumentException("Expiration Duration은 필수입니다");
        }

        try {
            // 1. GetObjectRequest 생성
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(defaultBucket)
                .key(storageKey)
                .build();

            // 2. GetObjectPresignRequest 생성
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expirationDuration)
                .getObjectRequest(getObjectRequest)
                .build();

            // 3. Presigned GET URL 생성
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            // 4. URL 반환
            return presignedRequest.url().toString();

        } catch (S3Exception e) {
            throw new S3StorageException(
                "Failed to generate presigned download URL: bucket=" + defaultBucket +
                ", storageKey=" + storageKey +
                ", expirationDuration=" + expirationDuration,
                e
            );
        } catch (Exception e) {
            throw new S3StorageException(
                "Unexpected error while generating download URL: bucket=" + defaultBucket +
                ", storageKey=" + storageKey,
                e
            );
        }
    }
}
