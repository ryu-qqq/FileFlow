package com.ryuqq.fileflow.application.asset.port.out.client;

import com.ryuqq.fileflow.application.asset.dto.response.ImageMetadataResponse;
import com.ryuqq.fileflow.application.asset.dto.response.ImageProcessingResultResponse;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;

/**
 * 이미지 처리 포트.
 *
 * <p>이미지 리사이징 및 메타데이터 추출 기능을 제공한다.
 *
 * <p><strong>구현체 위치</strong>: infrastructure 모듈
 *
 * <p><strong>사용 예시</strong>:
 *
 * <ul>
 *   <li>원본 이미지 → 썸네일 생성
 *   <li>JPEG → WebP 포맷 변환
 *   <li>이미지 크기/포맷 정보 추출
 * </ul>
 */
public interface ImageProcessingPort {

    /**
     * 이미지를 리사이징한다.
     *
     * <p>지정된 variant와 format에 따라 이미지를 처리한다.
     *
     * @param imageData 원본 이미지 바이트 데이터
     * @param variant 대상 이미지 변형 타입 (LARGE, MEDIUM, THUMBNAIL 등)
     * @param format 대상 이미지 포맷 (WEBP, JPEG, PNG)
     * @return 처리 결과 (리사이징된 이미지 데이터, 너비, 높이)
     */
    ImageProcessingResultResponse resize(
            byte[] imageData, ImageVariant variant, ImageFormat format);

    /**
     * 이미지 메타데이터를 추출한다.
     *
     * <p>이미지의 너비, 높이, 포맷, 색상 공간 등의 정보를 추출한다.
     *
     * @param imageData 이미지 바이트 데이터
     * @return 이미지 메타데이터
     */
    ImageMetadataResponse extractMetadata(byte[] imageData);
}
