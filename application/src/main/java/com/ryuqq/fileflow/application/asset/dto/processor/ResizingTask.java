package com.ryuqq.fileflow.application.asset.dto.processor;

import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageResizingSpec;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;

/**
 * 이미지 리사이징 작업 정보.
 *
 * <p>단일 리사이징 작업을 위한 Application Layer DTO입니다.
 *
 * <p><strong>구조</strong>:
 *
 * <ul>
 *   <li>Domain의 ImageResizingSpec을 래핑
 *   <li>작업 식별자(taskId) 제공
 *   <li>편의 메서드(variant, format) 위임
 * </ul>
 *
 * @param spec 도메인 리사이징 명세
 */
public record ResizingTask(ImageResizingSpec spec) {

    /** Compact Constructor (검증 로직). */
    public ResizingTask {
        if (spec == null) {
            throw new IllegalArgumentException("리사이징 명세는 null일 수 없습니다.");
        }
    }

    /**
     * 정적 팩토리 메서드 (ImageResizingSpec 직접 사용).
     *
     * @param spec 리사이징 명세
     * @return ResizingTask
     */
    public static ResizingTask of(ImageResizingSpec spec) {
        return new ResizingTask(spec);
    }

    /**
     * 정적 팩토리 메서드 (variant, format으로 생성).
     *
     * @param variant 이미지 변형 타입
     * @param format 이미지 포맷
     * @return ResizingTask
     */
    public static ResizingTask of(ImageVariant variant, ImageFormat format) {
        return new ResizingTask(ImageResizingSpec.of(variant, format));
    }

    /**
     * 이미지 변형 타입을 반환합니다.
     *
     * @return ImageVariant
     */
    public ImageVariant variant() {
        return spec.variant();
    }

    /**
     * 이미지 포맷을 반환합니다.
     *
     * @return ImageFormat
     */
    public ImageFormat format() {
        return spec.format();
    }

    /**
     * 태스크 식별자를 반환합니다.
     *
     * @return "variant_format" 형식의 식별자
     */
    public String taskId() {
        return spec.specId();
    }
}
