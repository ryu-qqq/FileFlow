package com.ryuqq.fileflow.application.session.manager.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.session.port.out.client.MultipartUploadClient;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.CompletedPartFixture;
import com.ryuqq.fileflow.domain.session.vo.PartPresignedUrlSpec;
import com.ryuqq.fileflow.domain.session.vo.PartPresignedUrlSpecFixture;
import java.util.List;
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
@DisplayName("MultipartUploadManager 단위 테스트")
class MultipartUploadManagerTest {

    @InjectMocks private MultipartUploadManager sut;
    @Mock private MultipartUploadClient multipartUploadClient;

    @Nested
    @DisplayName("createMultipartUpload 메서드")
    class CreateMultipartUploadTest {

        @Test
        @DisplayName("클라이언트에 위임하여 uploadId를 반환한다")
        void createMultipartUpload_DelegatesToClient() {
            // given
            String s3Key = "public/2026/01/session-001.jpg";
            String contentType = "image/jpeg";

            given(multipartUploadClient.createMultipartUpload(s3Key, contentType))
                    .willReturn("upload-id-001");

            // when
            String result = sut.createMultipartUpload(s3Key, contentType);

            // then
            assertThat(result).isEqualTo("upload-id-001");
            then(multipartUploadClient).should().createMultipartUpload(s3Key, contentType);
        }
    }

    @Nested
    @DisplayName("generatePresignedPartUrl 메서드")
    class GeneratePresignedPartUrlTest {

        @Test
        @DisplayName("PartPresignedUrlSpec을 받아 클라이언트에 위임하여 파트 Presigned URL을 반환한다")
        void generatePresignedPartUrl_DelegatesToClient() {
            // given
            PartPresignedUrlSpec spec = PartPresignedUrlSpecFixture.aPartPresignedUrlSpec();
            String expectedUrl = "https://s3.presigned-part-url.com/test";

            given(
                            multipartUploadClient.generatePresignedPartUrl(
                                    spec.s3Key(), spec.uploadId(), spec.partNumber(), spec.ttl()))
                    .willReturn(expectedUrl);

            // when
            String result = sut.generatePresignedPartUrl(spec);

            // then
            assertThat(result).isEqualTo(expectedUrl);
            then(multipartUploadClient)
                    .should()
                    .generatePresignedPartUrl(
                            spec.s3Key(), spec.uploadId(), spec.partNumber(), spec.ttl());
        }
    }

    @Nested
    @DisplayName("completeMultipartUpload 메서드")
    class CompleteMultipartUploadTest {

        @Test
        @DisplayName("클라이언트에 위임하여 ETag를 반환한다")
        void completeMultipartUpload_DelegatesToClient() {
            // given
            String s3Key = "public/2026/01/session-001.jpg";
            String uploadId = "upload-id-001";
            List<CompletedPart> parts = List.of(CompletedPartFixture.aCompletedPart());

            given(multipartUploadClient.completeMultipartUpload(s3Key, uploadId, parts))
                    .willReturn("etag-final");

            // when
            String result = sut.completeMultipartUpload(s3Key, uploadId, parts);

            // then
            assertThat(result).isEqualTo("etag-final");
            then(multipartUploadClient).should().completeMultipartUpload(s3Key, uploadId, parts);
        }
    }

    @Nested
    @DisplayName("abortMultipartUpload 메서드")
    class AbortMultipartUploadTest {

        @Test
        @DisplayName("클라이언트에 위임하여 업로드를 중단한다")
        void abortMultipartUpload_DelegatesToClient() {
            // given
            String s3Key = "public/2026/01/session-001.jpg";
            String uploadId = "upload-id-001";

            // when
            sut.abortMultipartUpload(s3Key, uploadId);

            // then
            then(multipartUploadClient).should().abortMultipartUpload(s3Key, uploadId);
        }
    }
}
