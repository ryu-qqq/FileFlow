package com.ryuqq.fileflow.application.download.manager.client;

import com.ryuqq.fileflow.application.download.dto.response.RawDownloadedFile;
import com.ryuqq.fileflow.application.download.port.out.client.FileDownloadClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileDownloadManager {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadManager.class);

    private final FileDownloadClient fileDownloadClient;

    public FileDownloadManager(FileDownloadClient fileDownloadClient) {
        this.fileDownloadClient = fileDownloadClient;
    }

    public RawDownloadedFile download(String sourceUrl) {
        log.info("파일 다운로드 시작: sourceUrl={}", sourceUrl);
        RawDownloadedFile result = fileDownloadClient.download(sourceUrl);
        log.info("파일 다운로드 완료: fileName={}, size={}", result.fileName(), result.fileSize());
        return result;
    }
}
