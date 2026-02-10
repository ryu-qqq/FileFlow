package com.ryuqq.fileflow.application.common.response;

import java.util.List;

/**
 * 커서 기반 페이징 응답 (무한 스크롤)
 *
 * @param <T> 콘텐츠 타입
 * @param content 현재 슬라이스의 데이터 목록
 * @param size 슬라이스 크기
 * @param hasNext 다음 슬라이스 존재 여부
 * @param nextCursor 다음 슬라이스 조회용 커서 (optional)
 */
public record SliceResponse<T>(List<T> content, int size, boolean hasNext, String nextCursor) {

    public static <T> SliceResponse<T> of(
            List<T> content, int size, boolean hasNext, String nextCursor) {
        return new SliceResponse<>(content, size, hasNext, nextCursor);
    }

    public static <T> SliceResponse<T> of(List<T> content, int size, boolean hasNext) {
        return new SliceResponse<>(content, size, hasNext, null);
    }

    public static <T> SliceResponse<T> empty(int size) {
        return new SliceResponse<>(List.of(), size, false, null);
    }
}
