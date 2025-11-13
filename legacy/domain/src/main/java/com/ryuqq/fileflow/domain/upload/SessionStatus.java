package com.ryuqq.fileflow.domain.upload;

/**
 * 세션 상태 Enum
 */
public enum SessionStatus {
    PENDING,      // 초기 생성 상태
    IN_PROGRESS,  // 업로드 진행 중
    COMPLETED,    // 업로드 완료
    FAILED,       // 업로드 실패
    EXPIRED       // Presigned URL 만료
}
