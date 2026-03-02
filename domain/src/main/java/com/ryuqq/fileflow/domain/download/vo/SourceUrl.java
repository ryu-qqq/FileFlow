package com.ryuqq.fileflow.domain.download.vo;

import com.ryuqq.fileflow.domain.download.exception.DownloadErrorCode;
import com.ryuqq.fileflow.domain.download.exception.DownloadException;
import java.util.Objects;
import java.util.Set;

public record SourceUrl(String value) {

    private static final Set<String> KNOWN_EXTENSIONS =
            Set.of(
                    "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp", "tiff", "tif", "ico", "avif",
                    "heic", "pdf", "json", "xml", "zip", "txt", "html", "htm", "css", "js", "mp4",
                    "mp3");

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
     * <p>쿼리스트링/프래그먼트를 제거한 뒤, 각 path segment를 역순으로 탐색하여 알려진 파일 확장자가 포함된 segment를 찾습니다. CDN 이미지 리사이징
     * URL처럼 확장자가 중간 segment에 위치하는 경우도 처리합니다.
     *
     * <p>예시:
     *
     * <ul>
     *   <li>{@code https://example.com/image.jpg} → {@code "jpg"}
     *   <li>{@code https://cdn.example.com/abc.jpeg/_dims_/resize/300x300} → {@code "jpeg"}
     *   <li>{@code https://example.com/files/noextension} → {@code ""}
     * </ul>
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

        String[] segments = path.split("/");

        // 역순 탐색: 마지막 segment부터 확장자가 있는 segment를 찾음
        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i];
            int dotIndex = segment.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < segment.length() - 1) {
                String ext = segment.substring(dotIndex + 1).toLowerCase();
                if (KNOWN_EXTENSIONS.contains(ext)) {
                    return ext;
                }
            }
        }

        return "";
    }
}
