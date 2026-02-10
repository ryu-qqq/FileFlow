package com.ryuqq.fileflow.domain.download.vo;

import com.ryuqq.fileflow.domain.download.exception.DownloadErrorCode;
import com.ryuqq.fileflow.domain.download.exception.DownloadException;

public record CallbackInfo(String callbackUrl) {

    private static final CallbackInfo EMPTY = new CallbackInfo(null);

    public CallbackInfo {
        if (callbackUrl != null && !callbackUrl.isBlank()) {
            if (!callbackUrl.startsWith("http://") && !callbackUrl.startsWith("https://")) {
                throw new DownloadException(
                        DownloadErrorCode.INVALID_CALLBACK_URL,
                        "callbackUrl must start with http:// or https://: " + callbackUrl);
            }
        }
    }

    public static CallbackInfo of(String callbackUrl) {
        if (callbackUrl == null || callbackUrl.isBlank()) {
            return EMPTY;
        }
        return new CallbackInfo(callbackUrl);
    }

    public static CallbackInfo empty() {
        return EMPTY;
    }

    public boolean hasCallback() {
        return callbackUrl != null && !callbackUrl.isBlank();
    }
}
