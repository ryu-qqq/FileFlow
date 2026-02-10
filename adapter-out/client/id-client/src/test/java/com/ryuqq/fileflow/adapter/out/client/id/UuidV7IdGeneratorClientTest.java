package com.ryuqq.fileflow.adapter.out.client.id;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("UuidV7IdGeneratorClient 단위 테스트")
class UuidV7IdGeneratorClientTest {

    private static final Pattern UUID_V7_PATTERN =
            Pattern.compile(
                    "^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

    private IdGeneratorPort sut;

    @BeforeEach
    void setUp() {
        sut = new UuidV7IdGeneratorClient();
    }

    @Nested
    @DisplayName("generate 메서드")
    class Generate {

        @Test
        @DisplayName("성공: UUIDv7 형식 준수 (8-4-4-4-12, 버전 7)")
        void shouldReturnValidUuidV7Format() {
            String result = sut.generate();

            assertThat(result).isNotBlank();
            assertThat(result).matches(UUID_V7_PATTERN);
            assertThat(result).hasSize(36);
        }

        @RepeatedTest(10)
        @DisplayName("성공: 반복 호출 시 매번 고유한 ID 생성")
        void shouldGenerateUniqueIdOnEachCall() {
            String id1 = sut.generate();
            String id2 = sut.generate();

            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("성공: 대량 생성 시 모든 ID가 고유함")
        void shouldGenerateUniqueIdsInBulk() {
            int count = 100;
            Set<String> generatedIds = new HashSet<>();

            for (int i = 0; i < count; i++) {
                generatedIds.add(sut.generate());
            }

            assertThat(generatedIds).hasSize(count);
        }

        @Test
        @DisplayName("성공: IdGeneratorPort 인터페이스 구현")
        void shouldImplementIdGeneratorPort() {
            assertThat(sut).isInstanceOf(IdGeneratorPort.class);
        }
    }
}
