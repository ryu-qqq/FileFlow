package com.ryuqq.fileflow.application.common.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.common.port.out.StorageBucketPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("StorageBucketManager 단위 테스트")
class StorageBucketManagerTest {

    @InjectMocks private StorageBucketManager sut;
    @Mock private StorageBucketPort storageBucketPort;

    @Test
    @DisplayName("StorageBucketPort에서 버킷명을 조회한다")
    void getBucket_ReturnsBucketFromPort() {
        // given
        given(storageBucketPort.getBucket()).willReturn("fileflow-bucket");

        // when
        String result = sut.getBucket();

        // then
        assertThat(result).isEqualTo("fileflow-bucket");
    }
}
