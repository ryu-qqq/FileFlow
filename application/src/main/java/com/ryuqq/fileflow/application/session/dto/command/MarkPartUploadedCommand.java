package com.ryuqq.fileflow.application.session.dto.command;

/**
 * Part 업로드 완료 표시 Command.
 *
 * <p>Multipart 업로드의 각 Part 업로드 완료를 세션에 기록합니다.
 *
 * <p><strong>검증 규칙</strong>:
 *
 * <ul>
 *   <li>sessionId: null 불가, 빈 문자열 불가
 *   <li>partNumber: 1 ~ totalParts 범위
 *   <li>etag: null 불가, 빈 문자열 불가 (S3가 반환한 Part ETag)
 *   <li>size: 5MB ~ 5GB (마지막 Part는 5MB 미만 가능)
 * </ul>
 *
 * @param sessionId 세션 ID
 * @param partNumber Part 번호 (1-based)
 * @param etag S3가 반환한 Part ETag
 * @param size Part 크기 (바이트)
 */
public record MarkPartUploadedCommand(String sessionId, int partNumber, String etag, long size) {

    /**
     * 값 기반 생성.
     *
     * @param sessionId 세션 ID
     * @param partNumber Part 번호
     * @param etag Part ETag
     * @param size Part 크기
     * @return MarkPartUploadedCommand
     */
    public static MarkPartUploadedCommand of(
            String sessionId, int partNumber, String etag, long size) {
        return new MarkPartUploadedCommand(sessionId, partNumber, etag, size);
    }
}
