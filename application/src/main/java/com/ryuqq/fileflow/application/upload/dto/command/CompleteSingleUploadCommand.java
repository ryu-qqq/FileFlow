package com.ryuqq.fileflow.application.upload.dto.command;

/**
 * Single 업로드 완료 Command
 *
 * <p>Client가 S3 Presigned URL로 직접 업로드를 완료한 후,
 * FileAsset을 생성하기 위해 호출하는 Complete API의 Command입니다.</p>
 *
 * <p><strong>플로우:</strong></p>
 * <ol>
 *   <li>initSingleUpload → Presigned URL 반환</li>
 *   <li>Client → S3에 직접 PUT</li>
 *   <li>completeSingleUpload (이 Command 사용) → FileAsset 생성</li>
 * </ol>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Record 패턴 사용 (Java 21)</li>
 *   <li>✅ Lombok 금지</li>
 *   <li>✅ Static Factory Method 제공</li>
 * </ul>
 *
 * @param sessionKey 세션 키
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record CompleteSingleUploadCommand(
    String sessionKey
) {
    /**
     * Static Factory Method
     *
     * @param sessionKey 세션 키
     * @return CompleteSingleUploadCommand 인스턴스
     */
    public static CompleteSingleUploadCommand of(String sessionKey) {
        return new CompleteSingleUploadCommand(sessionKey);
    }
}
