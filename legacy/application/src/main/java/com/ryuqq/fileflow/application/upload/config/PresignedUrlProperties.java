package com.ryuqq.fileflow.application.upload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;

/**
 * Presigned URL 설정 Properties
 *
 * <p>S3 Presigned URL의 만료 시간을 관리하는 Type-Safe Configuration 클래스입니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ @ConfigurationProperties: Type-Safe 설정</li>
 *   <li>✅ @Validated: Bean Validation 지원</li>
 *   <li>✅ Immutable Properties: Setter를 통한 주입 (생성 후 불변)</li>
 *   <li>✅ Default Values: 설정 누락 시 안전한 기본값</li>
 * </ul>
 *
 * <p><strong>application.yml 예시:</strong></p>
 * <pre>{@code
 * fileflow:
 *   s3:
 *     presigned-url:
 *       single-upload-duration: 1h      # 단일 업로드 (1시간)
 *       multipart-part-duration: 24h    # Multipart 파트 (24시간)
 * }</pre>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * @Service
 * public class InitSingleUploadService {
 *     private final PresignedUrlProperties properties;
 *
 *     public void execute(Command command) {
 *         Duration ttl = properties.getSingleUploadDuration();
 *         // ...
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Validation 규칙:</strong></p>
 * <ul>
 *   <li>단일 업로드: 최소 1분, 최대 7일</li>
 *   <li>Multipart 파트: 최소 1시간, 최대 7일</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "fileflow.s3.presigned-url")
@Validated
public class PresignedUrlProperties {

    /**
     * 단일 업로드 Presigned URL 만료 시간
     *
     * <p><strong>기본값:</strong> 1시간</p>
     * <p><strong>권장 범위:</strong> 1분 ~ 7일</p>
     * <p><strong>사용처:</strong> InitSingleUploadService</p>
     */
    @NotNull
    private Duration singleUploadDuration = Duration.ofHours(1);

    /**
     * Multipart 업로드 파트별 Presigned URL 만료 시간
     *
     * <p><strong>기본값:</strong> 24시간</p>
     * <p><strong>권장 범위:</strong> 1시간 ~ 7일</p>
     * <p><strong>사용처:</strong> InitMultipartUploadService, GeneratePartUploadUrlService</p>
     */
    @NotNull
    private Duration multipartPartDuration = Duration.ofHours(24);

    /**
     * 단일 업로드 Presigned URL 만료 시간 조회
     *
     * @return 단일 업로드 만료 시간
     */
    public Duration getSingleUploadDuration() {
        return singleUploadDuration;
    }

    /**
     * 단일 업로드 Presigned URL 만료 시간 설정
     *
     * <p>Spring Boot가 application.yml에서 자동 주입합니다.</p>
     *
     * @param singleUploadDuration 단일 업로드 만료 시간
     * @throws IllegalArgumentException duration이 1분 미만이거나 7일 초과인 경우
     */
    public void setSingleUploadDuration(Duration singleUploadDuration) {
        validateDuration(singleUploadDuration, "singleUploadDuration", Duration.ofMinutes(1), Duration.ofDays(7));
        this.singleUploadDuration = singleUploadDuration;
    }

    /**
     * Multipart 업로드 파트별 Presigned URL 만료 시간 조회
     *
     * @return Multipart 파트 만료 시간
     */
    public Duration getMultipartPartDuration() {
        return multipartPartDuration;
    }

    /**
     * Multipart 업로드 파트별 Presigned URL 만료 시간 설정
     *
     * <p>Spring Boot가 application.yml에서 자동 주입합니다.</p>
     *
     * @param multipartPartDuration Multipart 파트 만료 시간
     * @throws IllegalArgumentException duration이 1시간 미만이거나 7일 초과인 경우
     */
    public void setMultipartPartDuration(Duration multipartPartDuration) {
        validateDuration(multipartPartDuration, "multipartPartDuration", Duration.ofHours(1), Duration.ofDays(7));
        this.multipartPartDuration = multipartPartDuration;
    }

    /**
     * Duration 유효성 검증
     *
     * @param duration 검증할 Duration
     * @param fieldName 필드명 (에러 메시지용)
     * @param min 최소값
     * @param max 최대값
     * @throws IllegalArgumentException duration이 범위를 벗어난 경우
     */
    private void validateDuration(Duration duration, String fieldName, Duration min, Duration max) {
        if (duration == null) {
            throw new IllegalArgumentException(fieldName + "은(는) null일 수 없습니다");
        }
        if (duration.compareTo(min) < 0) {
            throw new IllegalArgumentException(
                String.format("%s은(는) %s 이상이어야 합니다: %s", fieldName, min, duration)
            );
        }
        if (duration.compareTo(max) > 0) {
            throw new IllegalArgumentException(
                String.format("%s은(는) %s 이하여야 합니다: %s", fieldName, max, duration)
            );
        }
    }
}
