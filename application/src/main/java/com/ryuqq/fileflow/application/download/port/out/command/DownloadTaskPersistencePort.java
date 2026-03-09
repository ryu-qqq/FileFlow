package com.ryuqq.fileflow.application.download.port.out.command;

import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;

public interface DownloadTaskPersistencePort {

    long persist(DownloadTask downloadTask);
}
