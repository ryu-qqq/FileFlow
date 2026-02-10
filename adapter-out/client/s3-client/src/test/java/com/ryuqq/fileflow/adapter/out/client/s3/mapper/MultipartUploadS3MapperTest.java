package com.ryuqq.fileflow.adapter.out.client.s3.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("MultipartUploadS3Mapper 단위 테스트")
class MultipartUploadS3MapperTest {

    private MultipartUploadS3Mapper sut;

    @BeforeEach
    void setUp() {
        sut = new MultipartUploadS3Mapper();
    }

    @Nested
    @DisplayName("toS3Parts 메서드")
    class ToS3Parts {

        @Test
        @DisplayName("성공: Domain CompletedPart 목록을 S3 CompletedPart 목록으로 변환한다")
        void shouldConvertDomainPartsToS3Parts() {
            // given
            Instant now = Instant.now();
            List<CompletedPart> domainParts =
                    List.of(
                            CompletedPart.of(1, "\"etag-1\"", 5_000_000, now),
                            CompletedPart.of(2, "\"etag-2\"", 3_000_000, now));

            // when
            List<software.amazon.awssdk.services.s3.model.CompletedPart> result =
                    sut.toS3Parts(domainParts);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).partNumber()).isEqualTo(1);
            assertThat(result.get(0).eTag()).isEqualTo("\"etag-1\"");
            assertThat(result.get(1).partNumber()).isEqualTo(2);
            assertThat(result.get(1).eTag()).isEqualTo("\"etag-2\"");
        }

        @Test
        @DisplayName("성공: 빈 목록을 변환하면 빈 결과를 반환한다")
        void shouldReturnEmptyListForEmptyInput() {
            // when
            List<software.amazon.awssdk.services.s3.model.CompletedPart> result =
                    sut.toS3Parts(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공: 단일 파트도 올바르게 변환한다")
        void shouldConvertSinglePart() {
            // given
            Instant now = Instant.now();
            List<CompletedPart> domainParts =
                    List.of(CompletedPart.of(1, "\"single-etag\"", 10_000_000, now));

            // when
            List<software.amazon.awssdk.services.s3.model.CompletedPart> result =
                    sut.toS3Parts(domainParts);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).partNumber()).isEqualTo(1);
            assertThat(result.get(0).eTag()).isEqualTo("\"single-etag\"");
        }
    }
}
