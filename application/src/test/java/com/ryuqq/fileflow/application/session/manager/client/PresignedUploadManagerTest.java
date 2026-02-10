package com.ryuqq.fileflow.application.session.manager.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.session.port.out.client.PresignedUploadClient;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("PresignedUploadManager 단위 테스트")
class PresignedUploadManagerTest {

    @InjectMocks private PresignedUploadManager sut;
    @Mock private PresignedUploadClient presignedUploadClient;

    @Nested
    @DisplayName("getBucket 메서드")
    class GetBucketTest {

        @Test
        @DisplayName("클라이언트에 위임하여 버킷명을 반환한다")
        void getBucket_DelegatesToClient() {
            // given
            given(presignedUploadClient.getBucket()).willReturn("fileflow-bucket");

            // when
            String result = sut.getBucket();

            // then
            assertThat(result).isEqualTo("fileflow-bucket");
            then(presignedUploadClient).should().getBucket();
        }
    }

    @Nested
    @DisplayName("generatePresignedUploadUrl 메서드")
    class GeneratePresignedUploadUrlTest {

        @Test
        @DisplayName("클라이언트에 위임하여 Presigned URL을 반환한다")
        void generatePresignedUploadUrl_DelegatesToClient() {
            // given
            String s3Key = "public/2026/01/session-001.jpg";
            String contentType = "image/jpeg";
            Duration ttl = Duration.ofHours(1);
            String expectedUrl = "https://s3.presigned-url.com/test";

            given(presignedUploadClient.generatePresignedUploadUrl(s3Key, contentType, ttl))
                    .willReturn(expectedUrl);

            // when
            String result = sut.generatePresignedUploadUrl(s3Key, contentType, ttl);

            // then
            assertThat(result).isEqualTo(expectedUrl);
            then(presignedUploadClient)
                    .should()
                    .generatePresignedUploadUrl(s3Key, contentType, ttl);
        }
    }
}
