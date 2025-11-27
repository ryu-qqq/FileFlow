package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import java.time.LocalDateTime;

/**
 * 파일 자산 삭제 API Response.
 *
 * <p>Soft Delete 처리 결과를 반환합니다.
 *
 * @param id 삭제된 파일 자산 ID
 * @param deletedAt 삭제 시각
 * @author development-team
 * @since 1.0.0
 */
public record DeleteFileAssetApiResponse(String id, LocalDateTime deletedAt) {

    /**
     * 값 기반 생성.
     *
     * @param id 파일 자산 ID
     * @param deletedAt 삭제 시각
     * @return DeleteFileAssetApiResponse
     */
    public static DeleteFileAssetApiResponse of(String id, LocalDateTime deletedAt) {
        return new DeleteFileAssetApiResponse(id, deletedAt);
    }
}
