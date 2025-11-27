package com.ryuqq.fileflow.adapter.in.rest.asset.dto.command;

/**
 * 파일 자산 삭제 API Request.
 *
 * <p>Soft Delete를 위한 요청 DTO입니다. 실제로는 상태를 ARCHIVED로 변경합니다.
 *
 * @param reason 삭제 사유 (Optional)
 * @author development-team
 * @since 1.0.0
 */
public record DeleteFileAssetApiRequest(String reason) {

    /**
     * 기본 생성자 - 사유 없이 삭제
     *
     * @return DeleteFileAssetApiRequest
     */
    public static DeleteFileAssetApiRequest empty() {
        return new DeleteFileAssetApiRequest(null);
    }
}
