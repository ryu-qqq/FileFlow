package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 이미지 변형 타입.
 *
 * <p>이미지 처리 시 생성되는 다양한 크기의 변형 이미지를 정의한다.
 *
 * <ul>
 *   <li>ORIGINAL: 원본 이미지 (리사이징 없음)
 *   <li>LARGE: 대형 이미지 (예: 1200px)
 *   <li>MEDIUM: 중형 이미지 (예: 600px)
 *   <li>THUMBNAIL: 썸네일 이미지 (예: 150px)
 * </ul>
 */
public enum ImageVariantType {

    /** 원본 이미지. 리사이징 없이 원본 그대로 저장. */
    ORIGINAL,

    /** 대형 이미지. 상세 보기용. */
    LARGE,

    /** 중형 이미지. 목록 보기용. */
    MEDIUM,

    /** 썸네일 이미지. 미리보기용. */
    THUMBNAIL
}
