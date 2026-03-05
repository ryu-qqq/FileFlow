package com.ryuqq.fileflow.adapter.out.client.http.client;

import com.ryuqq.fileflow.application.download.dto.response.RawDownloadedFile;
import com.ryuqq.fileflow.application.download.exception.PermanentDownloadFailureException;
import com.ryuqq.fileflow.application.download.port.out.client.FileDownloadClient;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
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

        URI safeUri = toEncodedUri(sourceUrl);

        ResponseEntity<byte[]> response;
        try {
            response = restClient.get().uri(safeUri).retrieve().toEntity(byte[].class);
        } catch (HttpClientErrorException e) {
            throw new PermanentDownloadFailureException(
                    "HTTP " + e.getStatusCode().value() + ": " + sourceUrl, e);
        }

        byte[] fileBytes = response.getBody();
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalStateException("다운로드된 파일이 비어있습니다: " + sourceUrl);
        }

        String fileName = extractFileName(safeUri);
        String contentType = resolveContentType(response, fileName);

        log.info(
                "HTTP 파일 다운로드 완료: fileName={}, contentType={}, size={}",
                fileName,
                contentType,
                fileBytes.length);

        return RawDownloadedFile.of(fileName, contentType, fileBytes);
    }

    @SuppressWarnings("deprecation")
    private URI toEncodedUri(String sourceUrl) {
        try {
            URL url = new URL(sourceUrl.strip());
            return new URI(
                    url.getProtocol(),
                    url.getUserInfo(),
                    url.getHost(),
                    url.getPort(),
                    url.getPath(),
                    url.getQuery(),
                    url.getRef());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new PermanentDownloadFailureException("유효하지 않은 다운로드 URL: " + sourceUrl, e);
        }
    }

    private String resolveContentType(ResponseEntity<byte[]> response, String fileName) {
        MediaType mediaType = response.getHeaders().getContentType();
        if (mediaType != null && !isGenericContentType(mediaType)) {
            return mediaType.getType() + "/" + mediaType.getSubtype();
        }
        return detectContentTypeFromFileName(fileName);
    }

    private boolean isGenericContentType(MediaType mediaType) {
        return MediaType.APPLICATION_OCTET_STREAM.equalsTypeAndSubtype(mediaType)
                || MediaType.ALL.equalsTypeAndSubtype(mediaType);
    }

    private String extractFileName(URI uri) {
        String path = uri.getPath();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }

    private String detectContentTypeFromFileName(String fileName) {
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
            case "bmp" -> "image/bmp";
            case "tiff", "tif" -> "image/tiff";
            case "ico" -> "image/x-icon";
            case "avif" -> "image/avif";
            case "heic" -> "image/heic";
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
