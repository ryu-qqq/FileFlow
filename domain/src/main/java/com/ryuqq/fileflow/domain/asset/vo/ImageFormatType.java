package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 이미지 포맷 타입.
 *
 * <p>이미지 처리 시 사용 가능한 출력 포맷을 정의한다.
 *
 * <ul>
 *   <li>WEBP: 최신 웹 포맷 (높은 압축률, 투명도 지원)
 *   <li>JPEG: 손실 압축 포맷 (사진에 적합)
 *   <li>PNG: 무손실 압축 포맷 (투명도 필요 시)
 * </ul>
 */
public enum ImageFormatType {

    /** WebP 포맷. 높은 압축률과 품질, 투명도 지원. */
    WEBP,

    /** JPEG 포맷. 손실 압축, 사진에 적합. */
    JPEG,

    /** PNG 포맷. 무손실 압축, 투명도 지원. */
    PNG
}
