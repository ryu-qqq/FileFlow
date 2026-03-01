package com.ryuqq.fileflow.adapter.out.client.s3.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.ryuqq.fileflow.adapter.out.client.s3.config.S3ClientProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("StorageBucketAdapter 단위 테스트")
class StorageBucketAdapterTest {

    private final S3ClientProperties properties = mock(S3ClientProperties.class);
    private final StorageBucketAdapter adapter = new StorageBucketAdapter(properties);

    @Test
    @DisplayName("getBucket()은 S3ClientProperties에서 bucket을 반환한다")
    void shouldReturnBucketFromProperties() {
        // given
        given(properties.bucket()).willReturn("fileflow-bucket");

        // when
        String result = adapter.getBucket();

        // then
        assertThat(result).isEqualTo("fileflow-bucket");
    }
}
