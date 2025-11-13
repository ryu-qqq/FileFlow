package com.ryuqq.fileflow.domain.policy;

import com.ryuqq.fileflow.domain.upload.FileName;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.MimeType;

/**
 * File Metadata Value Object
 * 파일 메타데이터를 표현하는 불변 객체
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class FileMetadata {

    private final FileName name;
    private final MimeType mimeType;
    private final FileSize size;

    /**
     * Private 생성자
     *
     * @param name 파일명
     * @param mimeType MIME 타입
     * @param size 파일 크기
     */
    private FileMetadata(FileName name, MimeType mimeType, FileSize size) {
        this.name = name;
        this.mimeType = mimeType;
        this.size = size;
    }

    /**
     * Static Factory Method
     *
     * @param name 파일명
     * @param mimeType MIME 타입
     * @param size 파일 크기
     * @return FileMetadata 인스턴스
     */
    public static FileMetadata of(FileName name, MimeType mimeType, FileSize size) {
        return new FileMetadata(name, mimeType, size);
    }

    /**
     * 파일명을 반환합니다.
     *
     * @return 파일명
     */
    public FileName getName() {
        return name;
    }

    /**
     * MIME 타입을 반환합니다.
     *
     * @return MIME 타입
     */
    public MimeType getMimeType() {
        return mimeType;
    }

    /**
     * 파일 크기를 반환합니다.
     *
     * @return 파일 크기 (bytes)
     */
    public FileSize getSize() {
        return size;
    }

    /**
     * 동등성을 비교합니다.
     *
     * @param o 비교 대상 객체
     * @return 동등 여부
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileMetadata)) {
            return false;
        }
        FileMetadata that = (FileMetadata) o;
        return java.util.Objects.equals(name, that.name) &&
               java.util.Objects.equals(mimeType, that.mimeType) &&
               java.util.Objects.equals(size, that.size);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, mimeType, size);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return FileMetadata 정보 문자열
     */
    @Override
    public String toString() {
        return "FileMetadata{" +
            "name='" + name.value() + '\'' +
            ", mimeType='" + mimeType.value() + '\'' +
            ", size=" + size.bytes() +
            '}';
    }
}
