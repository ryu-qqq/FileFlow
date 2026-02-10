package com.ryuqq.fileflow.domain.session.id;

/** SingleUploadSession 식별자. String 타입 - Application Factory에서 UUID v7으로 생성하여 주입. */
public record SingleUploadSessionId(String value) {

    public static SingleUploadSessionId of(String value) {
        return new SingleUploadSessionId(value);
    }
}
