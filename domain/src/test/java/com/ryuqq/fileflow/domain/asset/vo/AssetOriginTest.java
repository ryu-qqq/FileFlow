package com.ryuqq.fileflow.domain.asset.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AssetOrigin 열거형")
class AssetOriginTest {

    @Test
    @DisplayName("SINGLE_UPLOAD의 displayName은 '단건 업로드'이다")
    void shouldHaveSingleUploadDisplayName() {
        assertThat(AssetOrigin.SINGLE_UPLOAD.displayName()).isEqualTo("단건 업로드");
    }

    @Test
    @DisplayName("MULTIPART_UPLOAD의 displayName은 '멀티파트 업로드'이다")
    void shouldHaveMultipartUploadDisplayName() {
        assertThat(AssetOrigin.MULTIPART_UPLOAD.displayName()).isEqualTo("멀티파트 업로드");
    }

    @Test
    @DisplayName("EXTERNAL_DOWNLOAD의 displayName은 '외부 다운로드'이다")
    void shouldHaveExternalDownloadDisplayName() {
        assertThat(AssetOrigin.EXTERNAL_DOWNLOAD.displayName()).isEqualTo("외부 다운로드");
    }

    @Test
    @DisplayName("TRANSFORM의 displayName은 '이미지 변환'이다")
    void shouldHaveTransformDisplayName() {
        assertThat(AssetOrigin.TRANSFORM.displayName()).isEqualTo("이미지 변환");
    }

    @Test
    @DisplayName("총 4개의 enum 값이 존재한다")
    void shouldHaveFourValues() {
        assertThat(AssetOrigin.values()).hasSize(4);
    }
}
