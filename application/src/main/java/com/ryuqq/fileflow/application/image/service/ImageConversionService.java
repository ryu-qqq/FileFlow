package com.ryuqq.fileflow.application.image.service;

import com.ryuqq.fileflow.application.image.ImageConversionException;
import com.ryuqq.fileflow.application.image.dto.ConvertToWebPCommand;
import com.ryuqq.fileflow.application.image.dto.ImageConversionResult;
import com.ryuqq.fileflow.application.image.port.in.ConvertToWebPUseCase;
import com.ryuqq.fileflow.application.image.port.out.ImageConversionPort;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import com.ryuqq.fileflow.domain.image.vo.ImageOptimizationRequest;
import com.ryuqq.fileflow.domain.image.vo.ImageOptimizationResult;
import com.ryuqq.fileflow.domain.image.vo.OptimizationStrategy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

/**
 * 이미지 변환 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * WebP 이미지 변환 비즈니스 로직을 처리합니다.
 *
 * 처리 흐름:
 * 1. Command 검증
 * 2. 포맷 변환 가능 여부 확인
 * 3. 최적화 요청 생성
 * 4. 변환 수행 (ImageConversionPort 위임)
 * 5. 결과 반환
 *
 * @author sangwon-ryu
 */
@Service
public class ImageConversionService implements ConvertToWebPUseCase {

    private final ImageConversionPort imageConversionPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param imageConversionPort 이미지 변환 Port
     */
    public ImageConversionService(ImageConversionPort imageConversionPort) {
        this.imageConversionPort = Objects.requireNonNull(
                imageConversionPort,
                "ImageConversionPort must not be null"
        );
    }

    /**
     * 이미지를 WebP 포맷으로 변환합니다.
     *
     * 비즈니스 로직:
     * 1. Command 검증
     * 2. 포맷 지원 여부 확인
     * 3. WebP 변환 가능 여부 확인
     * 4. 최적화 전략 결정
     * 5. ImageOptimizationRequest 생성
     * 6. 변환 수행
     * 7. ImageConversionResult 변환 및 반환
     *
     * @param command WebP 변환 Command
     * @return 이미지 변환 결과
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws ImageConversionException 변환 중 오류 발생 시
     */
    @Override
    public ImageConversionResult convertToWebP(ConvertToWebPCommand command) {
        Objects.requireNonNull(command, "ConvertToWebPCommand must not be null");

        // 1. 포맷 지원 여부 확인
        ImageFormat sourceFormat = command.sourceFormat();
        if (!imageConversionPort.supports(sourceFormat)) {
            throw ImageConversionException.unsupportedFormat(sourceFormat.getMimeType());
        }

        // 2. WebP 변환 가능 여부 확인
        if (!imageConversionPort.canConvertToWebP(sourceFormat)) {
            throw new ImageConversionException(
                    "Cannot convert " + sourceFormat + " to WebP format"
            );
        }

        // 3. 최적화 전략 결정
        OptimizationStrategy strategy = command.determineStrategy();

        // 4. ImageOptimizationRequest 생성
        ImageOptimizationRequest request = ImageOptimizationRequest.of(
                command.sourceS3Uri(),
                sourceFormat,
                strategy,
                command.getQualityOrDefault(),
                null, // targetDimension은 현재 사용하지 않음
                command.preserveMetadata()
        );

        // 5. 변환 수행
        Instant startTime = Instant.now();
        try {
            ImageOptimizationResult optimizationResult = imageConversionPort.convertToWebP(request);

            // 6. ImageConversionResult로 변환
            return ImageConversionResult.success(
                    command.fileId(),
                    optimizationResult.getResultS3Uri(),
                    optimizationResult.getOriginalFormat(),
                    optimizationResult.getResultFormat(),
                    optimizationResult.getAppliedStrategy(),
                    optimizationResult.getOriginalSizeBytes(),
                    optimizationResult.getOptimizedSizeBytes(),
                    optimizationResult.getProcessingTime()
            );

        } catch (Exception e) {
            // 7. 실패 시 ImageConversionResult 반환
            Instant endTime = Instant.now();
            return ImageConversionResult.failure(
                    command.fileId(),
                    command.sourceS3Uri(),
                    sourceFormat,
                    strategy,
                    0L, // 실패 시 원본 크기를 알 수 없음
                    java.time.Duration.between(startTime, endTime)
            );
        }
    }
}
