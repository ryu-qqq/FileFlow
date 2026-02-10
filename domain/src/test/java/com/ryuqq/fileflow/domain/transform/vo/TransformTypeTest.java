package com.ryuqq.fileflow.domain.transform.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TransformTypeTest {

    @Test
    @DisplayName("모든 TransformType 값이 존재한다")
    void all_values_exist() {
        assertThat(TransformType.values())
                .containsExactly(
                        TransformType.RESIZE,
                        TransformType.CONVERT,
                        TransformType.COMPRESS,
                        TransformType.THUMBNAIL);
    }

    @ParameterizedTest
    @CsvSource({"RESIZE, 리사이즈", "CONVERT, 포맷 변환", "COMPRESS, 압축", "THUMBNAIL, 썸네일 생성"})
    @DisplayName("각 타입은 올바른 displayName을 반환한다")
    void display_name_matches(TransformType type, String expectedDisplayName) {
        assertThat(type.displayName()).isEqualTo(expectedDisplayName);
    }
}
