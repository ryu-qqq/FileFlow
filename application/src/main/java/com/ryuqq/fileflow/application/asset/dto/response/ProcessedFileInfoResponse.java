package com.ryuqq.fileflow.application.asset.dto.response;

/**
 * 처리된 파일 정보 DTO.
 *
 * <p>리사이징/포맷 변환된 개별 파일 정보를 담습니다.
 *
 * @param processedFileAssetId 처리된 파일 에셋 ID
 * @param variant 이미지 변형 (ORIGINAL, THUMBNAIL, MEDIUM, LARGE)
 * @param format 이미지 포맷 (JPEG, PNG, WEBP, GIF)
 * @param fileName 파일명
 * @param fileSize 파일 크기 (bytes)
 * @param width 이미지 너비 (px)
 * @param height 이미지 높이 (px)
 * @param bucket S3 버킷명
 * @param s3Key S3 키
 */
public record ProcessedFileInfoResponse(
        String processedFileAssetId,
        String variant,
        String format,
        String fileName,
        long fileSize,
        Integer width,
        Integer height,
        String bucket,
        String s3Key) {}
