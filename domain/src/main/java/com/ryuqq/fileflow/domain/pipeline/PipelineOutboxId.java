package com.ryuqq.fileflow.domain.pipeline;

import java.util.Objects;

/**
 * Pipeline Outbox ID Value Object
 *
 * <p>Pipeline Outbox Aggregate의 식별자입니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Immutable Value Object (final 필드)</li>
 *   <li>✅ Null 안전성 보장</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class PipelineOutboxId {

    private final Long value;

    /**
     * Pipeline Outbox ID 생성 (Static Factory Method)
     *
     * @param value ID 값
     * @return PipelineOutboxId
     * @throws IllegalArgumentException value가 null이거나 0 이하인 경우
     */
    public static PipelineOutboxId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Pipeline Outbox ID는 0보다 큰 양수여야 합니다");
        }
        return new PipelineOutboxId(value);
    }

    /**
     * Private 생성자
     *
     * @param value ID 값
     */
    private PipelineOutboxId(Long value) {
        this.value = value;
    }

    /**
     * ID 값 반환
     *
     * @return ID 값
     */
    public Long value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PipelineOutboxId that = (PipelineOutboxId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "PipelineOutboxId{" +
            "value=" + value +
            '}';
    }
}
