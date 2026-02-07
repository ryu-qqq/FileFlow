package com.ryuqq.fileflow.domain.asset.vo;

/**
 * 파일 유형별 메타데이터.
 * 비동기로 추출되며, Asset 생성 시점에는 null일 수 있습니다.
 *
 * @param type 파일 유형
 * @param width 이미지 너비 (nullable, IMAGE일 때)
 * @param height 이미지 높이 (nullable, IMAGE일 때)
 * @param durationSeconds 영상 길이 초 (nullable, VIDEO일 때)
 * @param pageCount 문서 페이지 수 (nullable, DOCUMENT일 때)
 */
public record FileTypeMetadata(
        FileType type,
        Integer width,
        Integer height,
        Integer durationSeconds,
        Integer pageCount
) {

    public static FileTypeMetadata ofImage(int width, int height) {
        return new FileTypeMetadata(FileType.IMAGE, width, height, null, null);
    }

    public static FileTypeMetadata ofVideo(int durationSeconds) {
        return new FileTypeMetadata(FileType.VIDEO, null, null, durationSeconds, null);
    }

    public static FileTypeMetadata ofDocument(int pageCount) {
        return new FileTypeMetadata(FileType.DOCUMENT, null, null, null, pageCount);
    }

    public static FileTypeMetadata ofSpreadsheet(int pageCount) {
        return new FileTypeMetadata(FileType.SPREADSHEET, null, null, null, pageCount);
    }

    public static FileTypeMetadata ofUnknown() {
        return new FileTypeMetadata(FileType.UNKNOWN, null, null, null, null);
    }
}
