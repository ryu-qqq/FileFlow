package com.ryuqq.fileflow.application.common.port.out;

/**
 * ID 생성 포트 (출력 포트)
 *
 * <p>UUID v7 기반의 고유 식별자를 생성합니다. Domain Layer의 ID Value Object에 사용됩니다.
 */
public interface IdGeneratorPort {

    /**
     * 새 ID 생성 (UUID v7 형식)
     *
     * @return 생성된 ID 문자열
     */
    String generate();
}
