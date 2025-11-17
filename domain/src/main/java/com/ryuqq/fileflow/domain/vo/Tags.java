package com.ryuqq.fileflow.domain.vo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tags Value Object
 * <p>
 * 파일 태그 목록을 검증하고 캡슐화합니다.
 * </p>
 *
 * <p>
 * 검증 규칙:
 * - 최대 10개 태그
 * - 각 태그 최대 20자
 * - 콤마 구분 문자열 또는 List<String> 입력 지원
 * </p>
 */
public record Tags(List<String> value) {

    /**
     * 최대 태그 개수
     */
    private static final int MAX_TAGS = 10;

    /**
     * 각 태그의 최대 길이
     */
    private static final int MAX_TAG_LENGTH = 20;

    /**
     * Compact Constructor (Record 검증 패턴)
     */
    public Tags {
        // null 안전성: null인 경우 빈 리스트로 변환
        value = (value == null) ? Collections.emptyList() : List.copyOf(value);
        validateMaxTags(value);
        validateTagLength(value);
    }

    /**
     * 콤마 구분 문자열로 Tags 생성
     *
     * @param commaSeparatedTags 콤마로 구분된 태그 문자열 (예: "tag1,tag2,tag3")
     * @return Tags VO
     */
    public static Tags of(String commaSeparatedTags) {
        if (commaSeparatedTags == null || commaSeparatedTags.isBlank()) {
            return new Tags(Collections.emptyList());
        }

        List<String> tagList = Arrays.stream(commaSeparatedTags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());

        return new Tags(tagList);
    }

    /**
     * List<String>으로 Tags 생성
     *
     * @param tagList 태그 리스트
     * @return Tags VO
     */
    public static Tags of(List<String> tagList) {
        return new Tags(tagList);
    }

    /**
     * 빈 Tags 생성
     *
     * @return 빈 태그 리스트를 가진 Tags
     */
    public static Tags empty() {
        return new Tags(Collections.emptyList());
    }

    /**
     * 최대 태그 개수 검증
     */
    private static void validateMaxTags(List<String> tags) {
        if (tags.size() > MAX_TAGS) {
            throw new IllegalArgumentException(
                    String.format("태그는 최대 %d개까지 허용됩니다 (현재: %d개)", MAX_TAGS, tags.size())
            );
        }
    }

    /**
     * 각 태그의 길이 검증
     */
    private static void validateTagLength(List<String> tags) {
        for (String tag : tags) {
            if (tag.length() > MAX_TAG_LENGTH) {
                throw new IllegalArgumentException(
                        String.format("각 태그는 최대 %d자까지 허용됩니다 (현재 태그 '%s': %d자)",
                                MAX_TAG_LENGTH, tag, tag.length())
                );
            }
        }
    }

    /**
     * 태그를 콤마 구분 문자열로 반환
     *
     * @return 콤마로 구분된 태그 문자열
     */
    public String getTagsAsString() {
        return String.join(",", value);
    }

    /**
     * 태그 리스트 조회
     *
     * @return 태그 리스트 (Immutable)
     */
    public List<String> getValue() {
        return value;
    }
}
