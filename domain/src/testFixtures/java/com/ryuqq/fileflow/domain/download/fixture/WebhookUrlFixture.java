package com.ryuqq.fileflow.domain.download.fixture;

import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;

/** WebhookUrl 테스트 Fixture. */
public final class WebhookUrlFixture {

    private WebhookUrlFixture() {}

    /** 기본 WebhookUrl 생성. */
    public static WebhookUrl defaultWebhookUrl() {
        return WebhookUrl.of("https://example.com/webhook");
    }

    /** API Webhook URL 생성. */
    public static WebhookUrl apiWebhookUrl() {
        return WebhookUrl.of("https://api.example.com/callbacks/download");
    }

    /** 특정 URL로 WebhookUrl 생성. */
    public static WebhookUrl customWebhookUrl(String url) {
        return WebhookUrl.of(url);
    }
}
