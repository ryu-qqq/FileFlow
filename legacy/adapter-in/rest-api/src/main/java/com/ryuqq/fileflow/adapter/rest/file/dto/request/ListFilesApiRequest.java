package com.ryuqq.fileflow.adapter.rest.file.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * List Files API Request (Stub)
 *
 * <p>TODO: 실제 구현 필요</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ListFilesApiRequest {

    private Long ownerUserId;
    private String status;
    private String visibility;

    @Min(0)
    private Integer page = 0;

    @Min(1)
    @Max(100)
    private Integer size = 20;

    /**
     * 생성자
     */
    public ListFilesApiRequest() {
    }

    /**
     * @return 소유자 사용자 ID
     */
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * @param ownerUserId 소유자 사용자 ID
     */
    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    /**
     * @return 파일 상태
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status 파일 상태
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return 가시성
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * @param visibility 가시성
     */
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    /**
     * @return 페이지 번호
     */
    public Integer getPage() {
        return page;
    }

    /**
     * @param page 페이지 번호
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * @return 페이지 크기
     */
    public Integer getSize() {
        return size;
    }

    /**
     * @param size 페이지 크기
     */
    public void setSize(Integer size) {
        this.size = size;
    }
}
