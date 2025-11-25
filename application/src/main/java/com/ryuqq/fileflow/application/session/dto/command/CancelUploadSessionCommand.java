package com.ryuqq.fileflow.application.session.dto.command;

/**
 * 업로드 세션 취소 Command.
 *
 * <p>업로드를 중단하고 세션을 FAILED 상태로 전환합니다.
 *
 * <p><strong>검증 규칙</strong>:
 *
 * <ul>
 *   <li>sessionId: null 불가, 빈 문자열 불가
 *   <li>세션 상태: PREPARING 또는 ACTIVE (도메인에서 검증)
 * </ul>
 *
 * @param sessionId 세션 ID
 */
public record CancelUploadSessionCommand(String sessionId) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @return CancelUploadSessionCommand
     */
    public static CancelUploadSessionCommand of(String sessionId) {
        return new CancelUploadSessionCommand(sessionId);
    }
}
