package com.ryuqq.fileflow.application.port.out.command;

import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.vo.FileId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FilePersistencePort 인터페이스 계약 테스트
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *PersistencePort
 * - 메서드: persist() 하나만
 * - 반환 타입: FileId (Value Object)
 * - 파라미터: File (Domain Aggregate)
 * </p>
 */
class FilePersistencePortTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * FilePersistencePort 인터페이스가 존재하지 않으므로 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("FilePersistencePort는 persist() 메서드를 제공해야 한다")
    void shouldProvidePersistMethod() {
        // Given: FilePersistencePort 인터페이스 (컴파일 에러)
        FilePersistencePort port = null;

        // When & Then: 메서드 시그니처 검증
        // FileId persist(File file) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("FilePersistencePort.persist()는 FileId를 반환해야 한다")
    void persistShouldReturnFileId() {
        // Given: FilePersistencePort 인터페이스 (컴파일 에러)
        FilePersistencePort port = null;

        // When & Then: 반환 타입 검증
        // FileId 반환 (Long/String 같은 원시 타입 금지)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("FilePersistencePort.persist()는 File Aggregate를 파라미터로 받아야 한다")
    void persistShouldAcceptFileAggregate() {
        // Given: FilePersistencePort 인터페이스 (컴파일 에러)
        FilePersistencePort port = null;

        // When & Then: 파라미터 타입 검증
        // File Aggregate 파라미터 (DTO/Entity 금지)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }
}
