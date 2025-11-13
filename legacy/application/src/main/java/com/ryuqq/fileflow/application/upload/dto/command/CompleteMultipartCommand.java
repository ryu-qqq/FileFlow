package com.ryuqq.fileflow.application.upload.dto.command;

/**
 * Multipart 업로드 완료 Command
 *
 * @param sessionKey 세션 키
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record CompleteMultipartCommand(
    String sessionKey
) {
    /**
     * Static Factory Method
     *
     * @param sessionKey 세션 키
     * @return CompleteMultipartCommand 인스턴스
     */
    public static CompleteMultipartCommand of(String sessionKey) {
        return new CompleteMultipartCommand(sessionKey);
    }
}
