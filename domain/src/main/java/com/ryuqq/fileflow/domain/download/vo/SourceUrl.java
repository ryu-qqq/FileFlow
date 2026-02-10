package com.ryuqq.fileflow.domain.download.vo;

import com.ryuqq.fileflow.domain.download.exception.DownloadErrorCode;
import com.ryuqq.fileflow.domain.download.exception.DownloadException;
import java.util.Objects;

public record SourceUrl(String value) {

    public SourceUrl {
        Objects.requireNonNull(value, "sourceUrl must not be null");
        if (value.isBlank()) {
            throw new DownloadException(
                    DownloadErrorCode.INVALID_SOURCE_URL, "sourceUrl must not be blank");
        }
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            throw new DownloadException(
                    DownloadErrorCode.INVALID_SOURCE_URL,
                    "sourceUrl must start with http:// or https://: " + value);
        }
    }

    public static SourceUrl of(String value) {
        return new SourceUrl(value);
    }
}
