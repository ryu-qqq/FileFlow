package com.ryuqq.fileflow.application.monitoring.dto.response;

/** 단일 큐의 아웃박스 상태별 카운트 */
public record OutboxQueueStatusResponse(long pending, long sent, long failed) {}
