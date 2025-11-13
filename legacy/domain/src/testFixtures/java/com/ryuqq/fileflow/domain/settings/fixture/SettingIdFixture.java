package com.ryuqq.fileflow.domain.settings.fixture;

import com.ryuqq.fileflow.domain.settings.*;

/**
 * SettingId Test Fixture
 *
 * <p>테스트에서 SettingId 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class SettingIdFixture {

    private static final Long DEFAULT_ID = 1L;
    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private SettingIdFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }


    public static SettingId create() {
        return SettingId.of(DEFAULT_ID);
    }

    public static SettingId create(Long value) {
        return SettingId.of(value);
    }

    public static java.util.List<SettingId> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.IntStream.rangeClosed(1, count)
            .mapToObj(i -> SettingId.of((long) i))
            .toList();
    }

    public static java.util.List<SettingId> createMultiple(Long startId, int count) {
        if (startId <= 0) {
            throw new IllegalArgumentException("startId는 양수여야 합니다");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return java.util.stream.LongStream.range(startId, startId + count)
            .mapToObj(SettingId::of)
            .toList();
    }
}
