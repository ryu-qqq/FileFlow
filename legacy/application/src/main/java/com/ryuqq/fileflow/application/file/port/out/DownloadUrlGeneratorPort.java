package com.ryuqq.fileflow.application.file.port.out;

import java.time.Duration;

/**
 * DownloadUrlGenerator Port
 *
 * <p>S3 Presigned URL 생성 전용 Port - Infrastructure Layer 구현</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>S3 Presigned URL 생성 (다운로드 전용)</li>
 *   <li>URL 만료 시간 관리</li>
 *   <li>서명 검증 (AWS Signature V4)</li>
 * </ul>
 *
 * <p><strong>구현 가이드</strong>:</p>
 * <ul>
 *   <li>AWS SDK S3Presigner 사용</li>
 *   <li>GetObjectRequest 생성</li>
 *   <li>Presigned URL 생성 (서명 포함)</li>
 *   <li>URL 만료 시간 설정 (기본 1시간)</li>
 * </ul>
 *
 * <p><strong>보안</strong>:</p>
 * <ul>
 *   <li>서명된 URL (AWS Signature V4)</li>
 *   <li>만료 시간 포함 (기본 1시간, 최대 24시간)</li>
 *   <li>일회성 사용 권장</li>
 * </ul>
 *
 * <p><strong>트랜잭션 주의</strong>:</p>
 * <ul>
 *   <li>외부 API 호출 → 트랜잭션 밖에서 수행</li>
 *   <li>@Transactional(readOnly = true) 내에서 호출 가능</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface DownloadUrlGeneratorPort {

    /**
     * S3 Presigned Download URL 생성
     *
     * <p><strong>URL 형식</strong>:</p>
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
     * @param storageKey S3 Storage Key (예: tenant-123/org-456/file-789.pdf)
     * @param expirationDuration URL 만료 시간 (기본: 1시간, 최대: 24시간)
     * @return Presigned Download URL
     * @throws IllegalArgumentException storageKey가 null이거나 비어있는 경우
     * @throws IllegalArgumentException expirationDuration이 음수이거나 24시간 초과
     */
    String generateDownloadUrl(String storageKey, Duration expirationDuration);
}
