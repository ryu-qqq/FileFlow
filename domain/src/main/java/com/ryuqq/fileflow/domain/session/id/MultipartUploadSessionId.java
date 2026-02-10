package com.ryuqq.fileflow.domain.session.id;

/** MultipartUploadSession 식별자. String 타입 - Application Factory에서 UUID v7으로 생성하여 주입. */
public record MultipartUploadSessionId(String value) {

    public static MultipartUploadSessionId of(String value) {
        return new MultipartUploadSessionId(value);
    }
}
