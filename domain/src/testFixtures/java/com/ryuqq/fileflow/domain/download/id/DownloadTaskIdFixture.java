package com.ryuqq.fileflow.domain.download.id;

public class DownloadTaskIdFixture {

    public static DownloadTaskId aDownloadTaskId() {
        return DownloadTaskId.of("download-001");
    }

    public static DownloadTaskId aDownloadTaskId(String value) {
        return DownloadTaskId.of(value);
    }
}
