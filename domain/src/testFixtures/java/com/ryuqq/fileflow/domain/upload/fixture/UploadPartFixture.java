package com.ryuqq.fileflow.domain.upload.fixture;

import com.ryuqq.fileflow.domain.upload.Checksum;
import com.ryuqq.fileflow.domain.upload.ETag;
import com.ryuqq.fileflow.domain.upload.FileSize;
import com.ryuqq.fileflow.domain.upload.PartNumber;
import com.ryuqq.fileflow.domain.upload.UploadPart;

/**
 * UploadPart Test Fixture
 *
 * <p>테스트에서 UploadPart 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author Sangwon Ryu
 * @since 2025-10-31
 */
public class UploadPartFixture {

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     */
    private UploadPartFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 UploadPart 생성 (partNumber=1, 5MB)
     *
     * @return UploadPart 인스턴스
     */
    public static UploadPart createDefault() {
        return UploadPart.of(PartNumber.of(1), ETag.of("etag-default-123"), FileSize.of(5242880L));
    }

    /**
     * 특정 partNumber와 크기로 UploadPart 생성
     *
     * @param partNumber 파트 번호
     * @param size 파트 크기
     * @return UploadPart 인스턴스
     */
    public static UploadPart create(Integer partNumber, Long size) {
        return UploadPart.of(PartNumber.of(partNumber), ETag.of("etag-" + partNumber), FileSize.of(size));
    }

    /**
     * 모든 속성을 지정하여 UploadPart 생성
     *
     * @param partNumber 파트 번호
     * @param etag ETag 값
     * @param size 파트 크기
     * @return UploadPart 인스턴스
     */
    public static UploadPart create(Integer partNumber, String etag, Long size) {
        return UploadPart.of(PartNumber.of(partNumber), ETag.of(etag), FileSize.of(size));
    }

    /**
     * 체크섬을 포함한 UploadPart 생성
     *
     * @param partNumber 파트 번호
     * @param etag ETag 값
     * @param size 파트 크기
     * @param checksum 체크섬
     * @return UploadPart 인스턴스
     */
    public static UploadPart createWithChecksum(
        Integer partNumber,
        String etag,
        Long size,
        String checksum
    ) {
        return UploadPart.of(
            PartNumber.of(partNumber),
            ETag.of(etag),
            FileSize.of(size),
            checksum != null ? Checksum.of(checksum) : null
        );
    }

    /**
     * 최소 크기 (5MB) UploadPart 생성
     *
     * @param partNumber 파트 번호
     * @return UploadPart 인스턴스
     */
    public static UploadPart createMinimumSize(Integer partNumber) {
        return UploadPart.of(PartNumber.of(partNumber), ETag.of("etag-" + partNumber), FileSize.of(5242880L)); // 5MB
    }

    /**
     * 큰 크기 (100MB) UploadPart 생성
     *
     * @param partNumber 파트 번호
     * @return UploadPart 인스턴스
     */
    public static UploadPart createLargeSize(Integer partNumber) {
        return UploadPart.of(PartNumber.of(partNumber), ETag.of("etag-" + partNumber), FileSize.of(104857600L)); // 100MB
    }

    /**
     * 여러 UploadPart 생성
     *
     * @param count 생성할 개수
     * @return UploadPart 리스트
     */
    public static java.util.List<UploadPart> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> UploadPart.of(PartNumber.of(i), ETag.of("etag-" + i), FileSize.of(5242880L)))
            .toList();
    }
}
