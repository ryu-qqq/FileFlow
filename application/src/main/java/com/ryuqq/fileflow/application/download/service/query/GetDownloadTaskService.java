package com.ryuqq.fileflow.application.download.service.query;

import com.ryuqq.fileflow.application.download.assembler.DownloadAssembler;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.application.download.manager.query.DownloadReadManager;
import com.ryuqq.fileflow.application.download.port.in.query.GetDownloadTaskUseCase;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import org.springframework.stereotype.Service;

@Service
public class GetDownloadTaskService implements GetDownloadTaskUseCase {

    private final DownloadReadManager downloadReadManager;
    private final DownloadAssembler downloadAssembler;

    public GetDownloadTaskService(
            DownloadReadManager downloadReadManager, DownloadAssembler downloadAssembler) {
        this.downloadReadManager = downloadReadManager;
        this.downloadAssembler = downloadAssembler;
    }

    @Override
    public DownloadTaskResponse execute(String downloadTaskId) {
        DownloadTask downloadTask = downloadReadManager.getDownloadTask(downloadTaskId);
        return downloadAssembler.toResponse(downloadTask);
    }
}
