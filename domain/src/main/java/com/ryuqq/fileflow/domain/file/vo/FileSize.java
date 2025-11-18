package com.ryuqq.fileflow.domain.file.vo;

import com.ryuqq.fileflow.domain.file.support.FileSizeUnit;

/**
 * 파일 크기 Value Object.
 */
public record FileSize(long bytes) {

    public FileSize {
        if (bytes <= 0) {
            throw new IllegalArgumentException("FileSize는 0보다 커야 합니다: " + bytes);
        }
    }

    public static FileSize of(long bytes) {
        return new FileSize(bytes);
    }

    public void validateForUploadType(UploadType uploadType) {
        if (uploadType == null) {
            throw new IllegalArgumentException("UploadType은 null일 수 없습니다.");
        }
        if (bytes > uploadType.getMaxSize()) {
            throw new IllegalArgumentException(
                "파일 크기가 업로드 타입 제한을 초과했습니다: " + bytes + " > " + uploadType.getMaxSize()
            );
        }
    }

    public boolean isLargerThan(long threshold) {
        return bytes > threshold;
    }

    public double toMB() {
        return (double) bytes / FileSizeUnit.MB_IN_BYTES;
    }

    public double toGB() {
        return (double) bytes / FileSizeUnit.GB_IN_BYTES;
    }
}

