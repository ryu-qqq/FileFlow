package com.ryuqq.fileflow.application.download.internal;

import com.ryuqq.fileflow.application.download.dto.response.FileDownloadResult;
import com.ryuqq.fileflow.application.download.dto.response.RawDownloadedFile;
import com.ryuqq.fileflow.application.download.exception.PermanentDownloadFailureException;
import com.ryuqq.fileflow.application.download.manager.client.FileDownloadManager;
import com.ryuqq.fileflow.application.download.manager.client.FileStorageUploadManager;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileTransferFacade {

    private static final Logger log = LoggerFactory.getLogger(FileTransferFacade.class);

    private final FileDownloadManager fileDownloadManager;
    private final FileStorageUploadManager fileStorageUploadManager;

    public FileTransferFacade(
            FileDownloadManager fileDownloadManager,
            FileStorageUploadManager fileStorageUploadManager) {
        this.fileDownloadManager = fileDownloadManager;
        this.fileStorageUploadManager = fileStorageUploadManager;
    }

    public FileDownloadResult transfer(DownloadTask downloadTask) {
        try {
            RawDownloadedFile rawFile = fileDownloadManager.download(downloadTask.sourceUrlValue());

            String etag =
                    fileStorageUploadManager.upload(
                            downloadTask.bucket(),
                            downloadTask.s3Key(),
                            rawFile.data(),
                            rawFile.contentType());

            return FileDownloadResult.success(
                    rawFile.fileName(), rawFile.contentType(), rawFile.fileSize(), etag);
        } catch (PermanentDownloadFailureException e) {
            log.warn(
                    "파일 전송 영구 실패 (재시도 불가): taskId={}, sourceUrl={}, error={}",
                    downloadTask.idValue(),
                    downloadTask.sourceUrlValue(),
                    e.getMessage());
            return FileDownloadResult.permanentFailure(e.getMessage());
        } catch (Exception e) {
            log.error(
                    "파일 전송 실패: taskId={}, sourceUrl={}, error={}",
                    downloadTask.idValue(),
                    downloadTask.sourceUrlValue(),
                    e.getMessage(),
                    e);
            return FileDownloadResult.failure(e.getMessage());
        }
    }
}
