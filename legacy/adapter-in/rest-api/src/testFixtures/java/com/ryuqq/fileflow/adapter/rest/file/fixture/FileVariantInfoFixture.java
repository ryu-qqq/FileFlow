package com.ryuqq.fileflow.adapter.rest.file.fixture;

import com.ryuqq.fileflow.adapter.rest.file.dto.response.FileVariantInfo;

/**
 * FileVariantInfo 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see FileVariantInfo
 */
public class FileVariantInfoFixture {

    /**
     * 기본 원본 이미지 FileVariantInfo 생성
     *
     * @return 원본 이미지 FileVariantInfo
     */
    public static FileVariantInfo createOriginal() {
        return FileVariantInfo.of(
            "ORIGINAL",
            "tenant-1/org-2/original/image.jpg",
            "https://cdn.example.com/original/image.jpg",
            2000,
            1500,
            1048576L
        );
    }

    /**
     * 썸네일 FileVariantInfo 생성
     *
     * @return 썸네일 FileVariantInfo
     */
    public static FileVariantInfo createThumbnail() {
        return FileVariantInfo.of(
            "THUMBNAIL",
            "tenant-1/org-2/thumbnail/image.jpg",
            "https://cdn.example.com/thumbnail/image.jpg",
            200,
            150,
            51200L
        );
    }

    /**
     * 커스텀 FileVariantInfo 생성
     *
     * @param type 변형본 타입
     * @param storageKey 저장 키
     * @param url 접근 URL
     * @param width 너비
     * @param height 높이
     * @param fileSize 파일 크기
     * @return FileVariantInfo
     */
    public static FileVariantInfo createWith(
        String type,
        String storageKey,
        String url,
        Integer width,
        Integer height,
        Long fileSize
    ) {
        return FileVariantInfo.of(type, storageKey, url, width, height, fileSize);
    }

    // Private 생성자
    private FileVariantInfoFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
