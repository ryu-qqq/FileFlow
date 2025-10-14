package com.ryuqq.fileflow.application.image.port.in;

import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand;
import com.ryuqq.fileflow.application.image.dto.ThumbnailGenerationResult;

import java.util.List;

/**
 * 썸네일 생성 UseCase
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 이미지 썸네일 생성 비즈니스 로직을 정의합니다.
 *
 * 비즈니스 규칙:
 * 1. 지원 크기: SMALL(300x300), MEDIUM(800x800), CUSTOM
 * 2. 업스케일링 방지: 원본보다 큰 썸네일 생성 안 함
 * 3. Aspect Ratio 유지: 기본적으로 비율 유지
 * 4. 고품질 리샘플링: Lanczos3 알고리즘 사용
 * 5. WebP 포맷 출력: 최적 압축
 *
 * 성능 목표:
 * - Small 썸네일 생성: < 500ms (5MB 이미지 기준)
 * - Medium 썸네일 생성: < 1초 (5MB 이미지 기준)
 * - 다중 크기 일괄 생성: < 2초 (5MB 이미지 기준)
 *
 * 사용 사례:
 * - 상품 이미지 썸네일 생성 (목록/상세 페이지)
 * - 프로필 사진 썸네일 생성
 * - 갤러리 썸네일 생성
 *
 * @author sangwon-ryu
 */
public interface GenerateThumbnailUseCase {

    /**
     * 단일 썸네일을 생성합니다.
     *
     * 처리 흐름:
     * 1. Command 검증
     * 2. S3에서 원본 이미지 다운로드
     * 3. 썸네일 전략 선택
     * 4. 썸네일 생성 (리사이징)
     * 5. S3에 썸네일 업로드
     * 6. 결과 반환
     *
     * @param command 썸네일 생성 Command
     * @return 썸네일 생성 결과
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.application.image.ImageConversionException 생성 중 오류 발생 시
     */
    ThumbnailGenerationResult generateThumbnail(GenerateThumbnailCommand command);

    /**
     * 다중 크기의 썸네일을 일괄 생성합니다.
     * 하나의 원본 이미지로부터 여러 크기의 썸네일을 한 번에 생성합니다.
     *
     * 처리 흐름:
     * 1. Commands 검증
     * 2. S3에서 원본 이미지 다운로드 (한 번만)
     * 3. 각 크기별 썸네일 생성
     * 4. S3에 썸네일들 업로드
     * 5. 결과 목록 반환
     *
     * 장점:
     * - 원본 다운로드를 한 번만 수행하여 효율적
     * - 트랜잭션 단위로 전체 성공/실패 처리
     * - 네트워크 비용 절감
     *
     * @param commands 썸네일 생성 Command 목록
     * @return 썸네일 생성 결과 목록
     * @throws IllegalArgumentException commands가 null이거나 비어있거나 유효하지 않은 경우
     * @throws com.ryuqq.fileflow.application.image.ImageConversionException 생성 중 오류 발생 시
     */
    List<ThumbnailGenerationResult> generateThumbnails(List<GenerateThumbnailCommand> commands);
}
