package com.ryuqq.fileflow.domain.download.id;

/**
 * DownloadTask 식별자.
 * String 타입 - Application Factory에서 UUID v7으로 생성하여 주입.
 */
public record DownloadTaskId(String value) {

    public static DownloadTaskId of(String value) {
        return new DownloadTaskId(value);
    }
}
