package com.ryuqq.fileflow.domain.asset.vo;

/** FileAsset 상태. */
public enum FileAssetStatus {

    /** 생성됨, 가공 대기 중. */
    PENDING,

    /** 가공 처리 중. */
    PROCESSING,

    /** 완료됨. */
    COMPLETED,

    /** 실패. */
    FAILED,

    /** 삭제됨 (Soft Delete). */
    DELETED,

    /** 이미지 리사이징 완료. */
    RESIZED,

    /** N8N 워크플로우 처리 중. */
    N8N_PROCESSING,

    /** N8N 워크플로우 완료. */
    N8N_COMPLETED
}
