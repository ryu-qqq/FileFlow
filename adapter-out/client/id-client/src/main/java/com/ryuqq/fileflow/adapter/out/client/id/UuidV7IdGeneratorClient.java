package com.ryuqq.fileflow.adapter.out.client.id;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import org.springframework.stereotype.Component;

/**
 * UuidV7IdGeneratorClient - UUIDv7 기반 ID 생성 Client
 *
 * <p>uuid-creator 라이브러리를 사용하여 시간 순서가 보장되는 UUIDv7을 생성합니다.
 *
 * <p><strong>UUIDv7 특징:</strong>
 *
 * <ul>
 *   <li>시간 기반 정렬 가능 - 생성 순서대로 정렬됨
 *   <li>분산 환경에서 충돌 없는 고유 ID 생성
 *   <li>데이터베이스 인덱스 성능 최적화
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UuidV7IdGeneratorClient implements IdGeneratorPort {

    @Override
    public String generate() {
        return UuidCreator.getTimeOrderedEpoch().toString();
    }
}
