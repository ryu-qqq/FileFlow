package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.download.assembler.DownloadAssembler;
import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.application.download.factory.command.DownloadCommandFactory;
import com.ryuqq.fileflow.application.download.manager.client.DownloadQueueManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadCommandManager;
import com.ryuqq.fileflow.application.download.port.in.command.CreateDownloadTaskUseCase;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import org.springframework.stereotype.Service;

@Service
public class CreateDownloadTaskService implements CreateDownloadTaskUseCase {

    private final DownloadCommandFactory downloadCommandFactory;
    private final DownloadCommandManager downloadCommandManager;
    private final DownloadQueueManager downloadQueueManager;
    private final DownloadAssembler downloadAssembler;

    public CreateDownloadTaskService(
            DownloadCommandFactory downloadCommandFactory,
            DownloadCommandManager downloadCommandManager,
            DownloadQueueManager downloadQueueManager,
            DownloadAssembler downloadAssembler) {
        this.downloadCommandFactory = downloadCommandFactory;
        this.downloadCommandManager = downloadCommandManager;
        this.downloadQueueManager = downloadQueueManager;
        this.downloadAssembler = downloadAssembler;
    }

    @Override
    public DownloadTaskResponse execute(CreateDownloadTaskCommand command) {
        DownloadTask downloadTask = downloadCommandFactory.create(command);
        downloadCommandManager.persist(downloadTask);
        downloadQueueManager.enqueue(downloadTask.idValue());
        return downloadAssembler.toResponse(downloadTask);
    }
}
