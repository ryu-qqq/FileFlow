package com.ryuqq.fileflow.application.upload.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Upload Configuration
 *
 * <p>Upload 관련 설정을 활성화하는 Configuration 클래스입니다.</p>
 *
 * <p><strong>활성화되는 Properties:</strong></p>
 * <ul>
 *   <li>PresignedUrlProperties: S3 Presigned URL 만료 시간 설정</li>
 * </ul>
 *
 * <p><strong>역할:</strong></p>
 * <ul>
 *   <li>@EnableConfigurationProperties로 Properties Bean 등록</li>
 *   <li>Type-Safe Configuration 활성화</li>
 *   <li>application.yml → Properties 자동 매핑</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties({
    PresignedUrlProperties.class
})
public class UploadConfiguration {
    // Properties 활성화만 담당 (Bean 정의 불필요)
}
