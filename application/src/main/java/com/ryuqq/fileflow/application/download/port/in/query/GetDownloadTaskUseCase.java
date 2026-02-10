package com.ryuqq.fileflow.application.download.port.in.query;

import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;

public interface GetDownloadTaskUseCase {

    DownloadTaskResponse execute(String downloadTaskId);
}
