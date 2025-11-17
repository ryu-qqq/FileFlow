package com.ryuqq.fileflow.domain.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UuidV7Generator 유틸리티 테스트")
class UuidV7GeneratorTest {

    @Test
    @DisplayName("유효한 UUID v7 형식을 생성해야 한다")
    void shouldGenerateValidUuidV7Format() {
        // Given & When
        String uuid = UuidV7Generator.generate();

        // Then
        assertThat(uuid).isNotNull();
        assertThat(uuid).hasSize(36); // UUID 표준 길이 (8-4-4-4-12)
        assertThat(UUID.fromString(uuid)).isNotNull(); // 유효한 UUID 형식
    }

    @Test
    @DisplayName("생성된 UUID는 시간 순서대로 정렬되어야 한다")
    void shouldGenerateTimeOrderedUuids() throws InterruptedException {
        // Given
        List<String> uuids = new ArrayList<>();

        // When - 여러 UUID 생성 (시간 간격 두기)
        for (int i = 0; i < 5; i++) {
            uuids.add(UuidV7Generator.generate());
            Thread.sleep(2); // 2ms 대기하여 타임스탬프 차이 보장
        }

        // Then - 생성 순서대로 정렬되어야 함 (사전순 정렬 = 시간순 정렬)
        List<String> sorted = new ArrayList<>(uuids);
        sorted.sort(String::compareTo);
        assertThat(uuids).isEqualTo(sorted);
    }

    @Test
    @DisplayName("여러 번 호출해도 중복되지 않는 UUID를 생성해야 한다")
    void shouldGenerateUniqueUuids() {
        // Given
        List<String> uuids = new ArrayList<>();

        // When - 1000개 UUID 생성
        for (int i = 0; i < 1000; i++) {
            uuids.add(UuidV7Generator.generate());
        }

        // Then - 모두 유니크해야 함
        long distinctCount = uuids.stream().distinct().count();
        assertThat(distinctCount).isEqualTo(1000);
    }
}
