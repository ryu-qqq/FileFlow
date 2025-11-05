package com.ryuqq.fileflow.adapter.rest.file.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * List Files API Request
 *
 * <p>파일 목록 조회 요청 DTO입니다.</p>
 *
 * <p><strong>Query Parameters:</strong></p>
 * <ul>
 *   <li>ownerUserId: 소유자 ID (선택)</li>
 *   <li>status: 파일 상태 (AVAILABLE, PROCESSING, FAILED 등, 선택)</li>
 *   <li>visibility: 가시성 (PRIVATE, INTERNAL, PUBLIC, 선택)</li>
 *   <li>page: 페이지 번호 (기본: 0)</li>
 *   <li>size: 페이지 크기 (기본: 20, 최소: 1, 최대: 100)</li>
 * </ul>
 *
 * <p><strong>요청 예시:</strong></p>
 * <pre>{@code
 * GET /api/v1/files?ownerUserId=100&status=AVAILABLE&page=0&size=20
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class FilesSearchApiRequest {

    private Long ownerUserId;
    private String status;
    private String visibility;

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private Integer page = 0; // 기본 0페이지

    @Min(value = 1, message = "페이지 크기는 최소 1입니다")
    @Max(value = 100, message = "페이지 크기는 최대 100입니다")
    private Integer size = 20; // 기본 20개

    /**
     * Public 생성자 (Bean Validation용)
     */
    public FilesSearchApiRequest() {
    }

    /**
     * Owner User ID Getter
     *
     * @return 소유자 User ID
     */
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * Owner User ID Setter
     *
     * @param ownerUserId 소유자 User ID
     */
    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    /**
     * Status Getter
     *
     * @return 파일 상태 (문자열)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Status Setter
     *
     * @param status 파일 상태
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Visibility Getter
     *
     * @return 가시성 (문자열)
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * Visibility Setter
     *
     * @param visibility 가시성
     */
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    /**
     * Page Getter
     *
     * @return 페이지 번호
     */
    public Integer getPage() {
        return page;
    }

    /**
     * Page Setter
     *
     * @param page 페이지 번호
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * Size Getter
     *
     * @return 페이지 크기
     */
    public Integer getSize() {
        return size;
    }

    /**
     * Size Setter
     *
     * @param size 페이지 크기
     */
    public void setSize(Integer size) {
        this.size = size;
    }
}
