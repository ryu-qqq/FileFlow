package com.ryuqq.fileflow.domain.asset.service;

import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.UploadCategory;
import java.util.List;

/**
 * 이미지 처리 정책 Domain Service.
 *
 * <p>파일이 이미지 처리(리사이징, 포맷 변환 등)가 필요한지 판단합니다.
 *
 * <ul>
 *   <li>Content-Type 기반 판단: 이미지 타입만 처리
 *   <li>UploadCategory 기반 판단: BANNER, PRODUCT_IMAGE, HTML만 처리
 *   <li>복합 조건: 둘 다 충족해야 처리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class ImageProcessingPolicy {

    /**
     * Content-Type 기반 이미지 처리 필요 여부 확인.
     *
     * @param contentType Content-Type
     * @return 이미지 타입이면 true
     */
    public boolean shouldProcess(ContentType contentType) {
        return contentType.isImage();
    }

    /**
     * UploadCategory 기반 이미지 처리 필요 여부 확인.
     *
     * @param category 업로드 카테고리
     * @return 이미지 처리가 필요한 카테고리면 true
     */
    public boolean shouldProcess(UploadCategory category) {
        return category.requiresImageProcessing();
    }

    /**
     * Content-Type과 UploadCategory 모두 기반 이미지 처리 필요 여부 확인.
     *
     * @param contentType Content-Type
     * @param category 업로드 카테고리
     * @return 둘 다 조건을 충족하면 true
     */
    public boolean shouldProcess(ContentType contentType, UploadCategory category) {
        return shouldProcess(contentType) && shouldProcess(category);
    }

    /**
     * 생성할 이미지 변형 목록 반환.
     *
     * <p>ORIGINAL을 제외한 리사이징 대상 변형(LARGE, MEDIUM, THUMBNAIL)을 반환합니다.
     *
     * @return 생성할 이미지 변형 목록 (불변)
     */
    public List<ImageVariant> getVariantsToGenerate() {
        return List.of(ImageVariant.LARGE, ImageVariant.MEDIUM, ImageVariant.THUMBNAIL);
    }

    /**
     * 생성할 이미지 포맷 목록 반환.
     *
     * <p>WebP를 기본 포맷으로 하고, 원본 확장자에 따른 폴백 포맷을 함께 반환합니다.
     *
     * <ul>
     *   <li>PNG 확장자 → [WEBP, PNG]
     *   <li>JPG/JPEG/기타 → [WEBP, JPEG]
     * </ul>
     *
     * @param originalExtension 원본 파일 확장자
     * @return 생성할 이미지 포맷 목록 (불변)
     */
    public List<ImageFormat> getFormatsToGenerate(String originalExtension) {
        ImageFormat fallbackFormat = ImageFormat.fromOriginal(originalExtension);
        return List.of(ImageFormat.WEBP, fallbackFormat);
    }
}
