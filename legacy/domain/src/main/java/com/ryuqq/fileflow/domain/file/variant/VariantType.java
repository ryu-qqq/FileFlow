package com.ryuqq.fileflow.domain.file.variant;

/**
 * Variant Type Enum
 *
 * <p>파일 변형의 종류를 정의합니다.</p>
 *
 * <p><strong>변형 종류:</strong></p>
 * <ul>
 *   <li><strong>THUMBNAIL</strong>: 썸네일 (예: 200x200px)</li>
 *   <li><strong>PREVIEW</strong>: 미리보기 (예: 800x600px)</li>
 *   <li><strong>COMPRESSED</strong>: 압축 버전 (WebP, 품질 80%)</li>
 *   <li><strong>WATERMARKED</strong>: 워터마크 추가</li>
 *   <li><strong>CONVERTED</strong>: 포맷 변환 (예: PDF → PNG)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public enum VariantType {

    /**
     * 썸네일 (작은 미리보기 이미지)
     */
    THUMBNAIL,

    /**
     * 미리보기 (중간 크기)
     */
    PREVIEW,

    /**
     * 압축 버전
     */
    COMPRESSED,

    /**
     * 워터마크 추가
     */
    WATERMARKED,

    /**
     * 포맷 변환
     */
    CONVERTED
}
