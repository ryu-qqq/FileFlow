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
    FAILED
}
