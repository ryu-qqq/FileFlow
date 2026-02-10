package com.ryuqq.fileflow.application.download.port.out.client;

import com.ryuqq.fileflow.application.download.dto.response.RawDownloadedFile;

public interface FileDownloadClient {

    RawDownloadedFile download(String sourceUrl);
}
