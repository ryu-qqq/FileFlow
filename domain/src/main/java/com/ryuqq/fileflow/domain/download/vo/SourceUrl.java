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

    /**
     * URL에서 파일 확장자를 추출합니다.
     *
     * <p>쿼리스트링을 제거한 뒤, 경로의 마지막 '.' 이후를 확장자로 반환합니다. 확장자가 없으면 빈 문자열을 반환합니다.
     *
     * @return 확장자 (예: "jpg"), 확장자가 없으면 빈 문자열
     */
    public String extractExtension() {
        String path = value;

        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }

        int fragmentIndex = path.indexOf('#');
        if (fragmentIndex >= 0) {
            path = path.substring(0, fragmentIndex);
        }

        int lastSlash = path.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(dotIndex + 1).toLowerCase();
    }
}
