package com.ryuqq.fileflow.domain.session.vo;

import com.ryuqq.fileflow.domain.common.vo.AccessType;

public class UploadTargetFixture {

    public static UploadTarget anUploadTarget() {
        return UploadTarget.of(
                "public/2026/01/file-001.jpg",
                "fileflow-bucket",
                AccessType.PUBLIC,
                "product-image.jpg",
                "image/jpeg");
    }

    public static UploadTarget anInternalUploadTarget() {
        return UploadTarget.of(
                "internal/2026/01/file-002.xlsx",
                "fileflow-bucket",
                AccessType.INTERNAL,
                "report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}
