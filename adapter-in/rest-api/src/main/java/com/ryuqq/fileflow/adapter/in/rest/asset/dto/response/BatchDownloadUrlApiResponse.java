package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import java.util.ArrayList;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Presigned Download URL 일괄 생성 응답 DTO.
 *
 * <p>
 * 여러 파일에 대해 생성된 S3 Presigned URL 목록을 반환합니다.
 *
 * @param downloadUrls 생성된 Download URL 목록
 * @param successCount 성공 건수
 * @param failureCount 실패 건수
 * @param failures 실패한 파일 자산 ID 목록
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "다운로드 URL 일괄 생성 응답")
public record BatchDownloadUrlApiResponse(
        @Schema(description = "생성된 Download URL 목록") List<DownloadUrlApiResponse> downloadUrls,
        @Schema(description = "성공 건수", example = "5") int successCount,
        @Schema(description = "실패 건수", example = "1") int failureCount,
        @Schema(description = "실패한 파일 자산 목록") List<FailedDownloadUrl> failures) {
    /**
     * 생성자에서 방어적 복사 수행.
     */
    public BatchDownloadUrlApiResponse {
        if (downloadUrls != null) {
            downloadUrls = new ArrayList<>(downloadUrls);
        }
        if (failures != null) {
            failures = new ArrayList<>(failures);
        }
    }

    /**
     * 성공한 URL 목록으로 응답 생성.
     *
     * @param downloadUrls 성공한 URL 목록
     * @return BatchDownloadUrlApiResponse
     */
    public static BatchDownloadUrlApiResponse ofSuccess(List<DownloadUrlApiResponse> downloadUrls) {
        return new BatchDownloadUrlApiResponse(downloadUrls, downloadUrls.size(), 0, List.of());
    }

    /**
     * 성공/실패 혼합 응답 생성.
     *
     * @param downloadUrls 성공한 URL 목록
     * @param failures 실패 정보 목록
     * @return BatchDownloadUrlApiResponse
     */
    public static BatchDownloadUrlApiResponse of(List<DownloadUrlApiResponse> downloadUrls,
            List<FailedDownloadUrl> failures) {
        return new BatchDownloadUrlApiResponse(downloadUrls, downloadUrls.size(), failures.size(),
                failures);
    }

    /**
     * 실패한 다운로드 URL 정보.
     *
     * @param fileAssetId 파일 자산 ID
     * @param errorCode 에러 코드
     * @param errorMessage 에러 메시지
     */
    @Schema(description = "실패한 다운로드 URL 정보")
    public record FailedDownloadUrl(
            @Schema(description = "파일 자산 ID", example = "asset-123") String fileAssetId,
            @Schema(description = "에러 코드", example = "NOT_FOUND") String errorCode,
            @Schema(description = "에러 메시지", example = "파일 자산을 찾을 수 없습니다") String errorMessage) {

        /**
         * 값 기반 생성.
         *
         * @param fileAssetId 파일 자산 ID
         * @param errorCode 에러 코드
         * @param errorMessage 에러 메시지
         * @return FailedDownloadUrl
         */
        public static FailedDownloadUrl of(String fileAssetId, String errorCode,
                String errorMessage) {
            return new FailedDownloadUrl(fileAssetId, errorCode, errorMessage);
        }
    }
}
