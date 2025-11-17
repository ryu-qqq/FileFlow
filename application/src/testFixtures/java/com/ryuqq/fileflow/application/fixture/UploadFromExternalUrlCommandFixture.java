package com.ryuqq.fileflow.application.fixture;

import com.ryuqq.fileflow.application.dto.command.UploadFromExternalUrlCommand;

import java.util.List;

/**
 * UploadFromExternalUrlCommand TestFixture (Object Mother 패턴)
 * <p>
 * TestFixture 규칙:
 * - 패키지: ..application..fixture..
 * - 클래스명: *Fixture
 * - Object Mother 패턴 사용
 * - 팩토리 메서드: aCommand(), create()
 * </p>
 */
public class UploadFromExternalUrlCommandFixture {

    /**
     * 기본 UploadFromExternalUrlCommand 생성
     */
    public static UploadFromExternalUrlCommand aCommand() {
        return new UploadFromExternalUrlCommand(
                "https://example.com/image.jpg",
                1L,
                "EXTERNAL",
                List.of("external", "url"),
                "https://webhook.example.com/callback"
        );
    }

    /**
     * 기본 UploadFromExternalUrlCommand 생성 (alias)
     */
    public static UploadFromExternalUrlCommand create() {
        return aCommand();
    }

    /**
     * 커스텀 외부 URL로 Command 생성
     */
    public static UploadFromExternalUrlCommand withExternalUrl(String externalUrl) {
        return new UploadFromExternalUrlCommand(
                externalUrl,
                1L,
                "EXTERNAL",
                List.of("external", "url"),
                "https://webhook.example.com/callback"
        );
    }

    /**
     * 커스텀 업로더 ID로 Command 생성
     */
    public static UploadFromExternalUrlCommand withUploaderId(Long uploaderId) {
        return new UploadFromExternalUrlCommand(
                "https://example.com/image.jpg",
                uploaderId,
                "EXTERNAL",
                List.of("external", "url"),
                "https://webhook.example.com/callback"
        );
    }

    /**
     * 커스텀 카테고리로 Command 생성
     */
    public static UploadFromExternalUrlCommand withCategory(String category) {
        return new UploadFromExternalUrlCommand(
                "https://example.com/image.jpg",
                1L,
                category,
                List.of("external", "url"),
                "https://webhook.example.com/callback"
        );
    }

    /**
     * 커스텀 태그로 Command 생성
     */
    public static UploadFromExternalUrlCommand withTags(List<String> tags) {
        return new UploadFromExternalUrlCommand(
                "https://example.com/image.jpg",
                1L,
                "EXTERNAL",
                tags,
                "https://webhook.example.com/callback"
        );
    }

    /**
     * 커스텀 Webhook URL로 Command 생성
     */
    public static UploadFromExternalUrlCommand withWebhookUrl(String webhookUrl) {
        return new UploadFromExternalUrlCommand(
                "https://example.com/image.jpg",
                1L,
                "EXTERNAL",
                List.of("external", "url"),
                webhookUrl
        );
    }

    /**
     * 이미지 파일 외부 URL 업로드 Command (시나리오)
     */
    public static UploadFromExternalUrlCommand imageFromExternal() {
        return new UploadFromExternalUrlCommand(
                "https://cdn.example.com/photo.jpg",
                2L,
                "IMAGE",
                List.of("image", "cdn", "import"),
                "https://api.example.com/webhook/upload-complete"
        );
    }

    /**
     * 문서 파일 외부 URL 업로드 Command (시나리오)
     */
    public static UploadFromExternalUrlCommand documentFromExternal() {
        return new UploadFromExternalUrlCommand(
                "https://storage.example.com/report.pdf",
                3L,
                "DOCUMENT",
                List.of("document", "pdf", "report"),
                "https://api.example.com/webhook/upload-complete"
        );
    }
}
