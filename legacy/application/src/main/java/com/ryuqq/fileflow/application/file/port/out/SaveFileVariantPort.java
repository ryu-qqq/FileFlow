package com.ryuqq.fileflow.application.file.port.out;

import com.ryuqq.fileflow.domain.file.variant.FileVariant;

/**
 * Save FileVariant Port (출력 포트)
 *
 * <p><strong>역할</strong>: FileVariant 저장을 위한 출력 인터페이스</p>
 * <p><strong>위치</strong>: application/file/port/out/</p>
 * <p><strong>구현</strong>: adapter-out/persistence-mysql/file/adapter/</p>
 *
 * <h3>헥사고날 아키텍처 패턴</h3>
 * <ul>
 *   <li>✅ Application Layer가 정의하는 인터페이스</li>
 *   <li>✅ Adapter Layer에서 구현</li>
 *   <li>✅ 의존성 방향: Adapter → Application</li>
 *   <li>❌ Application이 Adapter에 의존하지 않음</li>
 * </ul>
 *
 * <h3>구현 예시</h3>
 * <pre>
 * {@literal @}Component
 * public class FileVariantCommandAdapter implements SaveFileVariantPort {
 *     {@literal @}Override
 *     public FileVariant save(FileVariant fileVariant) {
 *         // JPA Entity로 변환 후 저장
 *     }
 * }
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see com.ryuqq.fileflow.domain.file.variant.FileVariant
 */
public interface SaveFileVariantPort {

    /**
     * FileVariant 저장
     *
     * <p>신규 생성 또는 기존 Variant 업데이트</p>
     *
     * @param fileVariant 저장할 FileVariant (Domain)
     * @return 저장된 FileVariant (ID 할당됨)
     */
    FileVariant save(FileVariant fileVariant);
}
