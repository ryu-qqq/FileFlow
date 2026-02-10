package com.ryuqq.fileflow.domain.asset.vo;

public class FileInfoFixture {

    public static FileInfo anImageFileInfo() {
        return FileInfo.of("test.jpg", 1024L, "image/jpeg", "etag-123", "jpg");
    }

    public static FileInfo aPdfFileInfo() {
        return FileInfo.of("report.pdf", 2048L, "application/pdf", "etag-pdf", "pdf");
    }

    public static FileInfo aVideoFileInfo() {
        return FileInfo.of("video.mp4", 104857600L, "video/mp4", "etag-mp4", "mp4");
    }

    public static FileInfo aSpreadsheetFileInfo() {
        return FileInfo.of(
                "data.xlsx",
                4096L,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "etag-xlsx",
                "xlsx");
    }
}
