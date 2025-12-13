package com.ryuqq.fileflow.application.asset.dto.response;

/**
 * 이미지 처리 결과 응답.
 *
 * <p>이미지 리사이징 후의 결과 데이터를 담는다.
 *
 * @param data 처리된 이미지 바이트 데이터
 * @param width 처리된 이미지 너비
 * @param height 처리된 이미지 높이
 */
public record ImageProcessingResultResponse(byte[] data, int width, int height) {

    /**
     * 정적 팩토리 메서드.
     *
     * @param data 이미지 바이트 데이터
     * @param width 이미지 너비
     * @param height 이미지 높이
     * @return ImageProcessingResultResponse
     */
    public static ImageProcessingResultResponse of(byte[] data, int width, int height) {
        return new ImageProcessingResultResponse(data, width, height);
    }

    /**
     * 이미지 데이터 크기를 반환한다.
     *
     * @return 바이트 크기
     */
    public long size() {
        return data != null ? data.length : 0;
    }
}
