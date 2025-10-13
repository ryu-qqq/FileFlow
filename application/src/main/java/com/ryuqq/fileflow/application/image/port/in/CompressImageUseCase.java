package com.ryuqq.fileflow.application.image.port.in;

import com.ryuqq.fileflow.application.image.dto.CompressImageCommand;
import com.ryuqq.fileflow.application.image.dto.ImageConversionResult;

/**
 * 이미지 압축 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 이미지 파일을 품질 90%로 압축하는 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. 지원 포맷: JPEG, PNG, WebP
 * 2. 목표 압축 품질: 90% (고품질 유지)
 * 3. 최소 압축률: 10% 이상 파일 크기 감소
 * 4. 메타데이터: 선택적 보존 가능
 * 5. 원본 포맷 유지: 압축 후에도 동일 포맷 유지
 *
 * 성능 목표:
 * - 압축 시간: < 2초 (10MB 이미지 기준)
 * - 압축률: 평균 30-50% 파일 크기 감소
 * - 품질 손실: 육안으로 구분 불가능한 수준 (SSIM > 0.95)
 *
 * @author sangwon-ryu
 */
public interface CompressImageUseCase {

    /**
     * 이미지를 품질 90%로 압축합니다.
     *
     * 처리 흐름:
     * 1. Command 검증
     * 2. 포맷별 압축 전략 선택
     * 3. 이미지 압축 수행
     * 4. 압축 효과 검증 (최소 10% 감소)
     * 5. 결과 반환
     *
     * @param command 이미지 압축 Command
     * @return 이미지 압축 결과
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.application.image.ImageConversionException 압축 중 오류 발생 시
     */
    ImageConversionResult compressImage(CompressImageCommand command);
}
