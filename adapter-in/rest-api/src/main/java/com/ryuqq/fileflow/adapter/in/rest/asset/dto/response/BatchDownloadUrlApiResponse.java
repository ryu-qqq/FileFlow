package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import java.util.List;

/**
 * Presigned Download URL 일괄 생성 응답 DTO.
 *
 * <p>여러 파일에 대해 생성된 S3 Presigned URL 목록을 반환합니다.
 *
 * @param downloadUrls 생성된 Download URL 목록
 * @param successCount 성공 건수
 * @param failureCount 실패 건수
 * @param failures 실패한 파일 자산 ID 목록
 * @author development-team
 * @since 1.0.0
 */
public record BatchDownloadUrlApiResponse(
        List<DownloadUrlApiResponse> downloadUrls,
        int successCount,
        int failureCount,
        List<FailedDownloadUrl> failures) {

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
    public static BatchDownloadUrlApiResponse of(
            List<DownloadUrlApiResponse> downloadUrls, List<FailedDownloadUrl> failures) {
        return new BatchDownloadUrlApiResponse(
                downloadUrls, downloadUrls.size(), failures.size(), failures);
    }

    /**
     * 실패한 다운로드 URL 정보.
     *
     * @param fileAssetId 파일 자산 ID
     * @param errorCode 에러 코드
     * @param errorMessage 에러 메시지
     */
    public record FailedDownloadUrl(String fileAssetId, String errorCode, String errorMessage) {

        /**
         * 값 기반 생성.
         *
         * @param fileAssetId 파일 자산 ID
         * @param errorCode 에러 코드
         * @param errorMessage 에러 메시지
         * @return FailedDownloadUrl
         */
        public static FailedDownloadUrl of(
                String fileAssetId, String errorCode, String errorMessage) {
            return new FailedDownloadUrl(fileAssetId, errorCode, errorMessage);
        }
    }
}
