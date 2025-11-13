package com.ryuqq.fileflow.domain.download;

/**
 * External Download Outbox ID Value Object
 *
 * <p>Outbox ID를 Value Object로 래핑하여 타입 안전성을 보장합니다.</p>
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Java 21 Record 사용</li>
 *   <li>✅ 불변 객체 (Immutable)</li>
 *   <li>✅ Compact Constructor로 검증 로직 캡슐화</li>
 *   <li>❌ Lombok 사용 안함</li>
 * </ul>
 *
 * @param value Outbox ID 원시 값 (양수만 허용)
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record ExternalDownloadOutboxId(Long value) {

    /**
     * Compact Constructor - 검증 로직
     *
     * @throws IllegalArgumentException value가 null이거나 0 이하인 경우
     */
    public ExternalDownloadOutboxId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Outbox ID는 양수여야 합니다");
        }
    }
}
