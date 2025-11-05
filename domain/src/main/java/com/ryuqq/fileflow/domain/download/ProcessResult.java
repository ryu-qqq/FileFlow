package com.ryuqq.fileflow.domain.download;

/**
 * 처리 결과 Enum
 */
public enum ProcessResult {
    SUCCESS,           // 성공적으로 처리됨
    RETRY,            // 재시도 필요
    PERMANENT_FAILURE // 영구 실패
}
