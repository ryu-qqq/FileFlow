package com.ryuqq.fileflow.application.image.port.in;

import com.ryuqq.fileflow.application.image.dto.ConvertToWebPCommand;
import com.ryuqq.fileflow.application.image.dto.ImageConversionResult;

/**
 * WebP 변환 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 이미지를 WebP 포맷으로 변환하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. 지원 포맷: JPEG, PNG, GIF
 * 2. WebP 압축 품질: 기본 90% (고품질)
 * 3. 투명도 보존: PNG, GIF의 알파 채널 유지
 * 4. 메타데이터 처리: 선택적 보존 가능
 * 5. 프로그레시브 인코딩: 자동 적용
 *
 * 성능 목표:
 * - JPEG → WebP: 25-35% 파일 크기 감소
 * - PNG → WebP: 26-45% 파일 크기 감소
 * - 변환 시간: < 2초 (10MB 이미지 기준)
 *
 * @author sangwon-ryu
 */
public interface ConvertToWebPUseCase {

    /**
     * 이미지를 WebP 포맷으로 변환합니다.
     *
     * 처리 흐름:
     * 1. Command 검증
     * 2. 포맷 변환 가능 여부 확인
     * 3. 최적화 전략 결정
     * 4. 이미지 변환 수행
     * 5. 결과 반환
     *
     * @param command WebP 변환 Command
     * @return 이미지 변환 결과
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.application.image.ImageConversionException 변환 중 오류 발생 시
     */
    ImageConversionResult convertToWebP(ConvertToWebPCommand command);
}
