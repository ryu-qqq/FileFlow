package com.ryuqq.fileflow.adapter.out.client.http.client;

import com.ryuqq.fileflow.application.download.dto.response.RawDownloadedFile;
import com.ryuqq.fileflow.application.download.port.out.client.FileDownloadClient;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FileDownloadHttpClient implements FileDownloadClient {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadHttpClient.class);

    private final RestClient restClient;

    public FileDownloadHttpClient(@Qualifier("fileDownloadRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public RawDownloadedFile download(String sourceUrl) {
        log.info("HTTP 파일 다운로드 시작: sourceUrl={}", sourceUrl);

        byte[] fileBytes =
                restClient.get().uri(URI.create(sourceUrl)).retrieve().body(byte[].class);

        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalStateException("다운로드된 파일이 비어있습니다: " + sourceUrl);
        }

        String fileName = extractFileName(sourceUrl);
        String contentType = detectContentType(fileName);

        log.info("HTTP 파일 다운로드 완료: fileName={}, size={}", fileName, fileBytes.length);

        return RawDownloadedFile.of(fileName, contentType, fileBytes);
    }

    private String extractFileName(String url) {
        String path = URI.create(url).getPath();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }

    private String detectContentType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "application/octet-stream";
        }
        String ext = fileName.substring(dotIndex + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "zip" -> "application/zip";
            case "txt" -> "text/plain";
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "mp4" -> "video/mp4";
            case "mp3" -> "audio/mpeg";
            default -> "application/octet-stream";
        };
    }
}
