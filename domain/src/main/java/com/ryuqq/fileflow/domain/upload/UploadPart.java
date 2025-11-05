package com.ryuqq.fileflow.domain.upload;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Upload Part Value Object
 * Multipart Upload의 개별 파트를 표현하는 불변 객체
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>파트 번호는 1-10000 범위</li>
 *   <li>파트 크기는 최소 5MB (마지막 파트 제외)</li>
 *   <li>ETag는 필수 (S3에서 반환)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadPart {

    private final PartNumber partNumber;
    private final ETag etag;
    private final FileSize size;
    private final Checksum checksum;
    private final LocalDateTime uploadedAt;

    /**
     * Private 생성자 (직접 생성 불가)
     *
     * @param partNumber 파트 번호
     * @param etag S3 ETag
     * @param size 파트 크기
     * @param checksum SHA256 체크섬 (Optional)
     */
    private UploadPart(
        PartNumber partNumber,
        ETag etag,
        FileSize size,
        Checksum checksum
    ) {
        if (partNumber == null) {
            throw new IllegalArgumentException("Part number는 필수입니다");
        }
        if (etag == null) {
            throw new IllegalArgumentException("ETag는 필수입니다");
        }
        if (size == null) {
            throw new IllegalArgumentException("Size는 필수입니다");
        }

        this.partNumber = partNumber;
        this.etag = etag;
        this.size = size;
        this.checksum = checksum;
        this.uploadedAt = LocalDateTime.now();
    }

    /**
     * Static Factory Method
     *
     * @param partNumber 파트 번호
     * @param etag S3 ETag
     * @param size 파트 크기
     * @return UploadPart 인스턴스
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    public static UploadPart of(PartNumber partNumber, ETag etag, FileSize size) {
        return new UploadPart(partNumber, etag, size, null);
    }

    /**
     * Static Factory Method (with checksum)
     *
     * @param partNumber 파트 번호
     * @param etag S3 ETag
     * @param size 파트 크기
     * @param checksum SHA256 체크섬
     * @return UploadPart 인스턴스
     * @throws IllegalArgumentException 파라미터가 유효하지 않은 경우
     */
    public static UploadPart of(
        PartNumber partNumber,
        ETag etag,
        FileSize size,
        Checksum checksum
    ) {
        return new UploadPart(partNumber, etag, size, checksum);
    }

    /**
     * 파트 번호를 반환합니다.
     *
     * @return 파트 번호
     */
    public PartNumber getPartNumber() {
        return partNumber;
    }

    /**
     * ETag를 반환합니다.
     *
     * @return ETag
     */
    public ETag getEtag() {
        return etag;
    }

    /**
     * 파트 크기를 반환합니다.
     *
     * @return 파트 크기
     */
    public FileSize getSize() {
        return size;
    }

    /**
     * 체크섬을 반환합니다.
     *
     * @return 체크섬 (Optional)
     */
    public Checksum getChecksum() {
        return checksum;
    }

    /**
     * 업로드 완료 시간을 반환합니다.
     *
     * @return 업로드 완료 시간
     */
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
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
        if (!(o instanceof UploadPart)) {
            return false;
        }
        UploadPart that = (UploadPart) o;
        return Objects.equals(partNumber, that.partNumber) &&
               Objects.equals(etag, that.etag);
    }

    /**
     * 해시코드를 반환합니다.
     *
     * @return 해시코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(partNumber, etag);
    }

    /**
     * 문자열 표현을 반환합니다.
     *
     * @return UploadPart 정보 문자열
     */
    @Override
    public String toString() {
        return String.format(
            "UploadPart{partNumber=%d, etag='%s', size=%s, uploadedAt=%s}",
            partNumber.value(), etag.value(), size.toHumanReadable(), uploadedAt
        );
    }
}
