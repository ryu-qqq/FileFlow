package com.ryuqq.fileflow.application.session.dto.command;

/**
 * Multipart 파일 업로드 완료 Command.
 *
 * <p>모든 Part 업로드 완료 후 S3 병합을 요청합니다.
 *
 * <p><strong>검증 규칙</strong>:
 *
 * <ul>
 *   <li>sessionId: null 불가, 빈 문자열 불가
 *   <li>모든 Part가 업로드 완료 상태여야 함 (도메인에서 검증)
 * </ul>
 *
 * @param sessionId 세션 ID
 */
public record CompleteMultipartUploadCommand(String sessionId) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @return CompleteMultipartUploadCommand
     */
    public static CompleteMultipartUploadCommand of(String sessionId) {
        return new CompleteMultipartUploadCommand(sessionId);
    }
}
