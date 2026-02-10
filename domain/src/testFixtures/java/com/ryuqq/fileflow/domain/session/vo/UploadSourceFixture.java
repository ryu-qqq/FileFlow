package com.ryuqq.fileflow.domain.session.vo;

public class UploadSourceFixture {

    public static UploadSource aCommerceServiceSource() {
        return UploadSource.of("commerce-service");
    }

    public static UploadSource anAdminServiceSource() {
        return UploadSource.of("admin-service");
    }

    public static UploadSource anUploadSource(String value) {
        return UploadSource.of(value);
    }
}
