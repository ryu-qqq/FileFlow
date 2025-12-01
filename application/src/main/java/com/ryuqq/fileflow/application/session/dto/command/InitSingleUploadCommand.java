package com.ryuqq.fileflow.application.session.dto.command;

/**
 * 단일 파일 업로드 세션 초기화 Command.
 *
 * <p>Presigned URL 발급을 위한 세션 생성 요청 정보를 담습니다.
 *
 * <p><strong>검증 규칙</strong>:
 *
 * <ul>
 *   <li>idempotencyKey: UUID 형식, null 불가
 *   <li>fileName: null 불가, 빈 문자열 불가
 *   <li>fileSize: 1 ~ 5GB (5,368,709,120 bytes)
 *   <li>contentType: 허용된 MIME 타입만 가능
 *   <li>tenantId: 양수
 *   <li>organizationId: 양수
 *   <li>userId: 양수 (Customer), null 가능 (Admin/Seller는 email 사용)
 *   <li>userEmail: null 가능 (Customer는 userId 사용)
 *   <li>uploadCategory: Admin/Seller 필수, Customer는 null
 * </ul>
 *
 * @param idempotencyKey 멱등성 키 (클라이언트 제공 UUID)
 * @param fileName 파일명 (확장자 포함)
 * @param fileSize 파일 크기 (바이트)
 * @param contentType Content-Type (MIME 타입)
 * @param tenantId 테넌트 ID
 * @param organizationId 조직 ID
 * @param userId 사용자 ID (Customer 전용, null 가능)
 * @param userEmail 사용자 이메일 (Admin/Seller 전용, null 가능)
 * @param uploadCategory 업로드 카테고리 (Admin/Seller 필수, Customer는 null)
 */
public record InitSingleUploadCommand(
        String idempotencyKey,
        String fileName,
        long fileSize,
        String contentType,
        long tenantId,
        long organizationId,
        Long userId,
        String userEmail,
        String uploadCategory) {

    /**
     * 값 기반 생성.
     *
     * @param idempotencyKey 멱등성 키 (UUID 문자열)
     * @param fileName 파일명
     * @param fileSize 파일 크기 (바이트)
     * @param contentType Content-Type
     * @param tenantId 테넌트 ID
     * @param organizationId 조직 ID
     * @param userId 사용자 ID (null 가능)
     * @param userEmail 사용자 이메일 (null 가능)
     * @param uploadCategory 업로드 카테고리 (null 가능)
     * @return InitSingleUploadCommand
     */
    public static InitSingleUploadCommand of(
            String idempotencyKey,
            String fileName,
            long fileSize,
            String contentType,
            long tenantId,
            long organizationId,
            Long userId,
            String userEmail,
            String uploadCategory) {
        return new InitSingleUploadCommand(
                idempotencyKey,
                fileName,
                fileSize,
                contentType,
                tenantId,
                organizationId,
                userId,
                userEmail,
                uploadCategory);
    }
}
