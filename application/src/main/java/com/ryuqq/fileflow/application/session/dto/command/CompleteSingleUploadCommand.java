package com.ryuqq.fileflow.application.session.dto.command;

/**
 * 단일 파일 업로드 완료 Command.
 *
 * <p>S3에 업로드 완료 후 세션 완료 처리를 요청합니다.
 *
 * <p><strong>검증 규칙</strong>:
 *
 * <ul>
 *   <li>sessionId: null 불가, 빈 문자열 불가
 *   <li>etag: null 불가, 빈 문자열 불가 (S3가 반환한 ETag)
 * </ul>
 *
 * @param sessionId 세션 ID
 * @param etag S3가 반환한 ETag
 */
public record CompleteSingleUploadCommand(String sessionId, String etag) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param etag S3 ETag
     * @return CompleteSingleUploadCommand
     */
    public static CompleteSingleUploadCommand of(String sessionId, String etag) {
        return new CompleteSingleUploadCommand(sessionId, etag);
    }
}
