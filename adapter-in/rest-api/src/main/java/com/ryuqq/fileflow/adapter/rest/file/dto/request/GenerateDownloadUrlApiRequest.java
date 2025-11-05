package com.ryuqq.fileflow.adapter.rest.file.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Generate Download URL API Request
 *
 * <p>파일 다운로드 URL 생성 요청 DTO입니다.</p>
 *
 * <p><strong>검증 규칙:</strong></p>
 * <ul>
 *   <li>fileId: 필수</li>
 *   <li>expirationHours: 1-24 시간 (기본: 1시간)</li>
 * </ul>
 *
 * <p><strong>요청 예시:</strong></p>
 * <pre>{@code
 * {
 *   "fileId": 123,
 *   "expirationHours": 2
 * }
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class GenerateDownloadUrlApiRequest {

    @NotNull(message = "File ID는 필수입니다")
    private Long fileId;

    @Min(value = 1, message = "만료 시간은 최소 1시간입니다")
    @Max(value = 24, message = "만료 시간은 최대 24시간입니다")
    private Integer expirationHours = 1; // 기본 1시간

    /**
     * Public 생성자 (Bean Validation용)
     */
    public GenerateDownloadUrlApiRequest() {
    }

    /**
     * File ID Getter
     *
     * @return File ID
     */
    public Long getFileId() {
        return fileId;
    }

    /**
     * File ID Setter
     *
     * @param fileId File ID
     */
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    /**
     * Expiration Hours Getter
     *
     * @return 만료 시간 (시간 단위)
     */
    public Integer getExpirationHours() {
        return expirationHours;
    }

    /**
     * Expiration Hours Setter
     *
     * @param expirationHours 만료 시간 (시간 단위)
     */
    public void setExpirationHours(Integer expirationHours) {
        this.expirationHours = expirationHours;
    }
}
