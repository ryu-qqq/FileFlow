package com.ryuqq.fileflow.application.download.service.command;

import com.ryuqq.fileflow.application.download.assembler.DownloadAssembler;
import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.application.download.factory.command.DownloadCommandFactory;
import com.ryuqq.fileflow.application.download.manager.cache.DownloadUrlBlacklistManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadCommandManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadQueueOutboxCommandManager;
import com.ryuqq.fileflow.application.download.port.in.command.CreateDownloadTaskUseCase;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.exception.DownloadErrorCode;
import com.ryuqq.fileflow.domain.download.exception.DownloadException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDownloadTaskService implements CreateDownloadTaskUseCase {

    private final DownloadCommandFactory downloadCommandFactory;
    private final DownloadCommandManager downloadCommandManager;
    private final DownloadQueueOutboxCommandManager downloadQueueOutboxCommandManager;
    private final DownloadAssembler downloadAssembler;
    private final DownloadUrlBlacklistManager downloadUrlBlacklistManager;

    public CreateDownloadTaskService(
            DownloadCommandFactory downloadCommandFactory,
            DownloadCommandManager downloadCommandManager,
            DownloadQueueOutboxCommandManager downloadQueueOutboxCommandManager,
            DownloadAssembler downloadAssembler,
            DownloadUrlBlacklistManager downloadUrlBlacklistManager) {
        this.downloadCommandFactory = downloadCommandFactory;
        this.downloadCommandManager = downloadCommandManager;
        this.downloadQueueOutboxCommandManager = downloadQueueOutboxCommandManager;
        this.downloadAssembler = downloadAssembler;
        this.downloadUrlBlacklistManager = downloadUrlBlacklistManager;
    }

    @Transactional
    @Override
    public DownloadTaskResponse execute(CreateDownloadTaskCommand command) {
        if (downloadUrlBlacklistManager.isBlacklisted(command.sourceUrl())) {
            throw new DownloadException(
                    DownloadErrorCode.SOURCE_URL_BLACKLISTED,
                    "영구 실패 이력이 있는 URL입니다: " + command.sourceUrl());
        }

        DownloadTask downloadTask = downloadCommandFactory.create(command);
        downloadCommandManager.persist(downloadTask);

        DownloadQueueOutbox outbox =
                downloadCommandFactory.createQueueOutbox(downloadTask.idValue());
        downloadQueueOutboxCommandManager.persist(outbox);

        return downloadAssembler.toResponse(downloadTask);
    }
}
