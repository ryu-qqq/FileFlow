package com.ryuqq.fileflow.application.upload.dto;

import com.ryuqq.fileflow.domain.upload.vo.UploadStatus;

/**
 * 업로드 완료 확인 응답 DTO
 *
 * 업로드 완료 확인 결과를 클라이언트에 전달하기 위한 응답 객체입니다.
 *
 * @param sessionId 세션 ID
 * @param status 세션 상태 (COMPLETED 예상)
 * @param message 클라이언트 안내 메시지
 * @author sangwon-ryu
 */
public record ConfirmUploadResponse(
        String sessionId,
        UploadStatus status,
        String message
) {
    /**
     * 업로드 확인 성공 응답을 생성합니다.
     *
     * @param sessionId 세션 ID
     * @param status 세션 상태
     * @return ConfirmUploadResponse 인스턴스
     */
    public static ConfirmUploadResponse success(String sessionId, UploadStatus status) {
        return new ConfirmUploadResponse(
                sessionId,
                status,
                "업로드가 성공적으로 확인되었습니다."
        );
    }

    /**
     * 커스텀 메시지와 함께 응답을 생성합니다.
     *
     * @param sessionId 세션 ID
     * @param status 세션 상태
     * @param message 커스텀 메시지
     * @return ConfirmUploadResponse 인스턴스
     */
    public static ConfirmUploadResponse of(String sessionId, UploadStatus status, String message) {
        return new ConfirmUploadResponse(sessionId, status, message);
    }
}
