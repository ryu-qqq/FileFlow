package com.ryuqq.fileflow.domain.session.vo;

public class UploadPurposeFixture {

    public static UploadPurpose aProductImagePurpose() {
        return UploadPurpose.of("product-image");
    }

    public static UploadPurpose aUserAvatarPurpose() {
        return UploadPurpose.of("user-avatar");
    }

    public static UploadPurpose anUploadPurpose(String value) {
        return UploadPurpose.of(value);
    }
}
