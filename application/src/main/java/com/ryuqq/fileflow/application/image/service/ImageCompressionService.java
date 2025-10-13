package com.ryuqq.fileflow.application.image.service;

import com.ryuqq.fileflow.application.image.ImageConversionException;
import com.ryuqq.fileflow.application.image.dto.CompressImageCommand;
import com.ryuqq.fileflow.application.image.dto.ImageConversionResult;
import com.ryuqq.fileflow.application.image.port.in.CompressImageUseCase;
import com.ryuqq.fileflow.application.image.port.out.ImageConversionPort;
import com.ryuqq.fileflow.domain.image.vo.ImageFormat;
import com.ryuqq.fileflow.domain.image.vo.ImageOptimizationRequest;
import com.ryuqq.fileflow.domain.image.vo.ImageOptimizationResult;
import com.ryuqq.fileflow.domain.image.vo.OptimizationStrategy;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 이미지 압축 Service
 *
 * Hexagonal Architecture의 UseCase 구현체로서,
 * 이미지 압축(품질 90%) 비즈니스 로직을 처리합니다.
 *
 * 처리 흐름:
 * 1. Command 검증
 * 2. 압축 요청 생성
 * 3. 압축 수행 (ImageConversionPort 위임)
 * 4. 압축 효과 검증 (최소 10% 감소)
 * 5. 결과 반환
 *
 * @author sangwon-ryu
 */
@Service
public class ImageCompressionService implements CompressImageUseCase {

    private static final double MINIMUM_COMPRESSION_RATIO = 0.10; // 최소 10% 압축

    private final ImageConversionPort imageConversionPort;

    /**
     * Constructor Injection (NO Lombok)
     *
     * @param imageConversionPort 이미지 변환 Port
     */
    public ImageCompressionService(ImageConversionPort imageConversionPort) {
        this.imageConversionPort = Objects.requireNonNull(
                imageConversionPort,
                "ImageConversionPort must not be null"
        );
    }

    /**
     * 이미지를 품질 90%로 압축합니다.
     *
     * 비즈니스 로직:
     * 1. Command 검증
     * 2. 포맷 지원 여부 확인
     * 3. ImageOptimizationRequest 생성
     * 4. 압축 수행
     * 5. 압축 효과 검증 (최소 10%)
     * 6. ImageConversionResult 변환 및 반환
     *
     * @param command 이미지 압축 Command (압축 가능 포맷은 Command에서 이미 검증됨)
     * @return 이미지 압축 결과
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     * @throws ImageConversionException 압축 중 오류 발생 시
     */
    @Override
    public ImageConversionResult compressImage(CompressImageCommand command) {
        Objects.requireNonNull(command, "CompressImageCommand must not be null");

        // 1. 포맷 지원 여부 확인
        ImageFormat sourceFormat = command.sourceFormat();
        if (!imageConversionPort.supports(sourceFormat)) {
            throw ImageConversionException.unsupportedFormat(sourceFormat.getMimeType());
        }

        // 2. ImageOptimizationRequest 생성 (동일 포맷 유지)
        ImageOptimizationRequest request = ImageOptimizationRequest.of(
                command.sourceS3Uri(),
                sourceFormat,
                OptimizationStrategy.COMPRESS_ONLY, // 압축만 수행
                command.getQualityOrDefault(),
                null, // targetDimension은 현재 사용하지 않음
                command.preserveMetadata()
        );

        // 4. 압축 수행
        try {
            ImageOptimizationResult optimizationResult = imageConversionPort.compressImage(request);

            // 5. 압축 효과 검증
            validateCompressionEffectiveness(optimizationResult);

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

        } catch (ImageConversionException e) {
            // 7. ImageConversionException은 그대로 전파
            throw e;
        } catch (Exception e) {
            // 8. 기타 예외는 ImageConversionException으로 래핑하여 전파
            throw new ImageConversionException(
                    "Failed to compress image for command: " + command,
                    e
            );
        }
    }

    /**
     * 압축 효과를 검증합니다.
     * 최소 10% 이상 파일 크기가 감소했는지 확인합니다.
     *
     * @param result 최적화 결과
     * @throws ImageConversionException 압축 효과가 미미한 경우
     */
    private void validateCompressionEffectiveness(ImageOptimizationResult result) {
        long originalSize = result.getOriginalSizeBytes();
        long compressedSize = result.getOptimizedSizeBytes();

        // 압축된 파일이 원본보다 큰 경우
        if (compressedSize >= originalSize) {
            throw new ImageConversionException(
                    String.format(
                            "Compression failed: compressed size (%d bytes) is not smaller than original (%d bytes)",
                            compressedSize,
                            originalSize
                    )
            );
        }

        // 압축률 계산
        double compressionRatio = 1.0 - ((double) compressedSize / originalSize);

        // 최소 10% 압축률 검증
        if (compressionRatio < MINIMUM_COMPRESSION_RATIO) {
            throw new ImageConversionException(
                    String.format(
                            "Compression effectiveness too low: %.2f%% (minimum required: %.0f%%)",
                            compressionRatio * 100,
                            MINIMUM_COMPRESSION_RATIO * 100
                    )
            );
        }
    }
}
