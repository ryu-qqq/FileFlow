package com.ryuqq.fileflow.application.upload.dto.command;

/**
 * 파트 업로드 URL 생성 Command
 *
 * @param sessionKey 세션 키
 * @param partNumber 파트 번호
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record GeneratePartUrlCommand(
    String sessionKey,
    Integer partNumber
) {
    /**
     * Static Factory Method
     *
     * @param sessionKey 세션 키
     * @param partNumber 파트 번호
     * @return GeneratePartUrlCommand 인스턴스
     */
    public static GeneratePartUrlCommand of(String sessionKey, Integer partNumber) {
        return new GeneratePartUrlCommand(sessionKey, partNumber);
    }
}
