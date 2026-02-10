package com.ryuqq.fileflow.domain.download.vo;

/** 콜백 아웃박스 처리 상태. PENDING → SENT | FAILED */
public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
}
