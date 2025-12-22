package com.ryuqq.fileflow.application.session.dto.command;

/**
 * Multipart 파일 업로드 세션 초기화 Command.
 *
 * <p>대용량 파일을 Part 단위로 나누어 업로드하기 위한 세션 생성 요청 정보를 담습니다.
 *
 * <p><strong>검증 규칙</strong>:
 *
 * <ul>
 *   <li>fileName: null 불가, 빈 문자열 불가
 *   <li>fileSize: 5MB ~ 5TB
 *   <li>contentType: 허용된 MIME 타입만 가능
 *   <li>partSize: 5MB ~ 5GB (기본값: 5MB)
 *   <li>uploadCategory: Admin/Seller 필수, Customer는 null
 *   <li>customPath: SYSTEM 전용, uploadCategory와 동시 사용 불가
 * </ul>
 *
 * <p><strong>참고</strong>: tenantId, organizationId, userId, userEmail은 UserContext(ThreadLocal)에서
 * 가져옵니다.
 *
 * @param fileName 파일명 (확장자 포함)
 * @param fileSize 파일 크기 (바이트)
 * @param contentType Content-Type (MIME 타입)
 * @param partSize 각 Part 크기 (바이트, 기본: 5MB)
 * @param uploadCategory 업로드 카테고리 (Admin/Seller 필수, Customer는 null)
 * @param customPath 커스텀 S3 경로 (SYSTEM 전용, uploadCategory와 배타적)
 */
public record InitMultipartUploadCommand(
        String fileName,
        long fileSize,
        String contentType,
        long partSize,
        String uploadCategory,
        String customPath) {

    /**
     * 값 기반 생성.
     *
     * @param fileName 파일명
     * @param fileSize 파일 크기 (바이트)
     * @param contentType Content-Type
     * @param partSize 각 Part 크기 (바이트)
     * @param uploadCategory 업로드 카테고리 (null 가능)
     * @param customPath 커스텀 S3 경로 (null 가능)
     * @return InitMultipartUploadCommand
     */
    public static InitMultipartUploadCommand of(
            String fileName,
            long fileSize,
            String contentType,
            long partSize,
            String uploadCategory,
            String customPath) {
        return new InitMultipartUploadCommand(
                fileName, fileSize, contentType, partSize, uploadCategory, customPath);
    }
}
