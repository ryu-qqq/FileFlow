package com.ryuqq.fileflow.application.download.port.in.command;

import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;

public interface CreateDownloadTaskUseCase {

    DownloadTaskResponse execute(CreateDownloadTaskCommand command);
}
