package com.ryuqq.fileflow.domain.fixture;

import com.ryuqq.fileflow.domain.util.UuidV7Generator;

/**
 * UuidV7Generator TestFixture (Object Mother 패턴)
 */
public class UuidV7GeneratorFixture {

    /**
     * UUID v7 생성
     */
    public static String aUuidV7() {
        return UuidV7Generator.generate();
    }

    /**
     * 고정된 테스트용 UUID v7 (타임스탬프 기반이므로 실제로는 동적 생성)
     * <p>
     * 주의: UUID v7은 타임스탬프를 포함하므로 완전히 고정된 값을 사용할 수 없습니다.
     * 테스트에서 특정 UUID가 필요한 경우 이 메서드 대신 직접 문자열을 사용하세요.
     * </p>
     */
    public static String aFixedUuidV7() {
        // UUID v7은 타임스탬프 기반이므로 고정값 대신 동적 생성
        // 필요 시 테스트에서 직접 UUID 문자열을 하드코딩하는 것이 더 명확함
        return aUuidV7();
    }
}
