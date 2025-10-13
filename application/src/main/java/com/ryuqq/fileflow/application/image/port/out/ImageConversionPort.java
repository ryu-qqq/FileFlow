package com.ryuqq.fileflow.application.image.port.out;

import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import com.ryuqq.fileflow.domain.image.vo.ImageOptimizationRequest;
import com.ryuqq.fileflow.domain.image.vo.ImageOptimizationResult;

/**
 * 이미지 변환 Outbound Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * 이미지 변환 및 최적화를 위한 외부 시스템 연동을 정의합니다.
 *
 * 역할:
 * - Application Layer와 외부 이미지 처리 라이브러리 간의 인터페이스
 * - 이미지 포맷 변환 (JPEG/PNG → WebP)
 * - 이미지 압축 및 최적화
 * - S3 스토리지 연동
 *
 * 구현체:
 * - ThumbnailatorImageConversionAdapter (adapter-out-image-conversion 모듈)
 *
 * @author sangwon-ryu
 */
public interface ImageConversionPort {

    /**
     * 이미지를 WebP 포맷으로 변환합니다.
     *
     * 처리 과정:
     * 1. S3에서 원본 이미지 다운로드
     * 2. 이미지 포맷 변환 (WebP)
     * 3. 압축 품질 적용
     * 4. 메타데이터 처리
     * 5. S3에 변환된 이미지 업로드
     *
     * @param request 이미지 최적화 요청
     * @return 이미지 최적화 결과
     * @throws ImageConversionException 변환 중 오류 발생 시
     */
    ImageOptimizationResult convertToWebP(ImageOptimizationRequest request);

    /**
     * 특정 이미지 포맷의 변환을 지원하는지 확인합니다.
     *
     * @param format 이미지 포맷
     * @return 지원 여부
     */
    boolean supports(ImageFormat format);

    /**
     * WebP 포맷으로 변환 가능한지 확인합니다.
     *
     * @param format 이미지 포맷
     * @return 변환 가능 여부
     */
    boolean canConvertToWebP(ImageFormat format);
}
