package com.ryuqq.fileflow.adapter.in.rest.config.properties;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.in.rest.config.properties.ApiEndpointProperties.ExternalDownloadEndpoints;
import com.ryuqq.fileflow.adapter.in.rest.config.properties.ApiEndpointProperties.FileAssetEndpoints;
import com.ryuqq.fileflow.adapter.in.rest.config.properties.ApiEndpointProperties.UploadSessionEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ApiEndpointProperties 단위 테스트.
 *
 * <p>API 엔드포인트 경로 설정이 올바르게 동작하는지 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ApiEndpointProperties 단위 테스트")
class ApiEndpointPropertiesTest {

    private ApiEndpointProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ApiEndpointProperties();
    }

    @Nested
    @DisplayName("기본값 테스트")
    class DefaultValueTest {

        @Test
        @DisplayName("baseV1의 기본값은 /api/v1 이다")
        void baseV1_DefaultValue_ShouldBeApiV1() {
            // then
            assertThat(properties.getBaseV1()).isEqualTo("/api/v1");
        }

        @Test
        @DisplayName("uploadSession 기본 객체가 생성된다")
        void uploadSession_DefaultValue_ShouldNotBeNull() {
            // then
            assertThat(properties.getUploadSession()).isNotNull();
        }

        @Test
        @DisplayName("fileAsset 기본 객체가 생성된다")
        void fileAsset_DefaultValue_ShouldNotBeNull() {
            // then
            assertThat(properties.getFileAsset()).isNotNull();
        }

        @Test
        @DisplayName("externalDownload 기본 객체가 생성된다")
        void externalDownload_DefaultValue_ShouldNotBeNull() {
            // then
            assertThat(properties.getExternalDownload()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Setter/Getter 테스트")
    class SetterGetterTest {

        @Test
        @DisplayName("baseV1을 변경할 수 있다")
        void setBaseV1_ShouldUpdateValue() {
            // when
            properties.setBaseV1("/api/v2");

            // then
            assertThat(properties.getBaseV1()).isEqualTo("/api/v2");
        }

        @Test
        @DisplayName("uploadSession을 변경할 수 있다")
        void setUploadSession_ShouldUpdateValue() {
            // given
            UploadSessionEndpoints newEndpoints = new UploadSessionEndpoints();
            newEndpoints.setBase("/custom-upload-sessions");

            // when
            properties.setUploadSession(newEndpoints);

            // then
            assertThat(properties.getUploadSession().getBase()).isEqualTo("/custom-upload-sessions");
        }

        @Test
        @DisplayName("fileAsset을 변경할 수 있다")
        void setFileAsset_ShouldUpdateValue() {
            // given
            FileAssetEndpoints newEndpoints = new FileAssetEndpoints();
            newEndpoints.setBase("/custom-file-assets");

            // when
            properties.setFileAsset(newEndpoints);

            // then
            assertThat(properties.getFileAsset().getBase()).isEqualTo("/custom-file-assets");
        }

        @Test
        @DisplayName("externalDownload를 변경할 수 있다")
        void setExternalDownload_ShouldUpdateValue() {
            // given
            ExternalDownloadEndpoints newEndpoints = new ExternalDownloadEndpoints();
            newEndpoints.setBase("/custom-downloads");

            // when
            properties.setExternalDownload(newEndpoints);

            // then
            assertThat(properties.getExternalDownload().getBase()).isEqualTo("/custom-downloads");
        }
    }

    @Nested
    @DisplayName("UploadSessionEndpoints 테스트")
    class UploadSessionEndpointsTest {

        private UploadSessionEndpoints endpoints;

        @BeforeEach
        void setUp() {
            endpoints = new UploadSessionEndpoints();
        }

        @Test
        @DisplayName("base 기본값은 /upload-sessions 이다")
        void base_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getBase()).isEqualTo("/upload-sessions");
        }

        @Test
        @DisplayName("singleInit 기본값은 /single 이다")
        void singleInit_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getSingleInit()).isEqualTo("/single");
        }

        @Test
        @DisplayName("singleComplete 기본값은 /{sessionId}/single/complete 이다")
        void singleComplete_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getSingleComplete()).isEqualTo("/{sessionId}/single/complete");
        }

        @Test
        @DisplayName("multipartInit 기본값은 /multipart 이다")
        void multipartInit_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getMultipartInit()).isEqualTo("/multipart");
        }

        @Test
        @DisplayName("multipartComplete 기본값은 /{sessionId}/multipart/complete 이다")
        void multipartComplete_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getMultipartComplete()).isEqualTo("/{sessionId}/multipart/complete");
        }

        @Test
        @DisplayName("parts 기본값은 /{sessionId}/parts 이다")
        void parts_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getParts()).isEqualTo("/{sessionId}/parts");
        }

        @Test
        @DisplayName("cancel 기본값은 /{sessionId}/cancel 이다")
        void cancel_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getCancel()).isEqualTo("/{sessionId}/cancel");
        }

        @Test
        @DisplayName("byId 기본값은 /{sessionId} 이다")
        void byId_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getById()).isEqualTo("/{sessionId}");
        }

        @Test
        @DisplayName("모든 속성을 설정하고 조회할 수 있다")
        void settersAndGetters_ShouldWork() {
            // when
            endpoints.setBase("/sessions");
            endpoints.setSingleInit("/s");
            endpoints.setSingleComplete("/{id}/s/complete");
            endpoints.setMultipartInit("/m");
            endpoints.setMultipartComplete("/{id}/m/complete");
            endpoints.setParts("/{id}/p");
            endpoints.setCancel("/{id}/c");
            endpoints.setById("/{id}");

            // then
            assertThat(endpoints.getBase()).isEqualTo("/sessions");
            assertThat(endpoints.getSingleInit()).isEqualTo("/s");
            assertThat(endpoints.getSingleComplete()).isEqualTo("/{id}/s/complete");
            assertThat(endpoints.getMultipartInit()).isEqualTo("/m");
            assertThat(endpoints.getMultipartComplete()).isEqualTo("/{id}/m/complete");
            assertThat(endpoints.getParts()).isEqualTo("/{id}/p");
            assertThat(endpoints.getCancel()).isEqualTo("/{id}/c");
            assertThat(endpoints.getById()).isEqualTo("/{id}");
        }
    }

    @Nested
    @DisplayName("FileAssetEndpoints 테스트")
    class FileAssetEndpointsTest {

        private FileAssetEndpoints endpoints;

        @BeforeEach
        void setUp() {
            endpoints = new FileAssetEndpoints();
        }

        @Test
        @DisplayName("base 기본값은 /file-assets 이다")
        void base_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getBase()).isEqualTo("/file-assets");
        }

        @Test
        @DisplayName("byId 기본값은 /{id} 이다")
        void byId_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getById()).isEqualTo("/{id}");
        }

        @Test
        @DisplayName("delete 기본값은 /{id}/delete 이다")
        void delete_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getDelete()).isEqualTo("/{id}/delete");
        }

        @Test
        @DisplayName("downloadUrl 기본값은 /{id}/download-url 이다")
        void downloadUrl_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getDownloadUrl()).isEqualTo("/{id}/download-url");
        }

        @Test
        @DisplayName("batchDownloadUrl 기본값은 /batch-download-url 이다")
        void batchDownloadUrl_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getBatchDownloadUrl()).isEqualTo("/batch-download-url");
        }

        @Test
        @DisplayName("모든 속성을 설정하고 조회할 수 있다")
        void settersAndGetters_ShouldWork() {
            // when
            endpoints.setBase("/assets");
            endpoints.setById("/asset/{id}");
            endpoints.setDelete("/asset/{id}/remove");
            endpoints.setDownloadUrl("/asset/{id}/url");
            endpoints.setBatchDownloadUrl("/assets/urls");

            // then
            assertThat(endpoints.getBase()).isEqualTo("/assets");
            assertThat(endpoints.getById()).isEqualTo("/asset/{id}");
            assertThat(endpoints.getDelete()).isEqualTo("/asset/{id}/remove");
            assertThat(endpoints.getDownloadUrl()).isEqualTo("/asset/{id}/url");
            assertThat(endpoints.getBatchDownloadUrl()).isEqualTo("/assets/urls");
        }
    }

    @Nested
    @DisplayName("ExternalDownloadEndpoints 테스트")
    class ExternalDownloadEndpointsTest {

        private ExternalDownloadEndpoints endpoints;

        @BeforeEach
        void setUp() {
            endpoints = new ExternalDownloadEndpoints();
        }

        @Test
        @DisplayName("base 기본값은 /external-downloads 이다")
        void base_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getBase()).isEqualTo("/external-downloads");
        }

        @Test
        @DisplayName("byId 기본값은 /{id} 이다")
        void byId_DefaultValue_ShouldBeCorrect() {
            assertThat(endpoints.getById()).isEqualTo("/{id}");
        }

        @Test
        @DisplayName("모든 속성을 설정하고 조회할 수 있다")
        void settersAndGetters_ShouldWork() {
            // when
            endpoints.setBase("/downloads");
            endpoints.setById("/download/{id}");

            // then
            assertThat(endpoints.getBase()).isEqualTo("/downloads");
            assertThat(endpoints.getById()).isEqualTo("/download/{id}");
        }
    }
}
