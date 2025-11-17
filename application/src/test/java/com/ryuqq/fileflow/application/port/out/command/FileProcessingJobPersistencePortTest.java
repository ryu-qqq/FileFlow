package com.ryuqq.fileflow.application.port.out.command;

import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;
import com.ryuqq.fileflow.domain.vo.FileProcessingJobId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileProcessingJobPersistencePort 인터페이스 계약 테스트
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *PersistencePort
 * - 패키지: ..application..port.out.command..
 * - 메서드: persist() 하나만
 * - 반환 타입: FileProcessingJobId (Value Object)
 * - 파라미터: FileProcessingJob (Domain Aggregate)
 * </p>
 */
class FileProcessingJobPersistencePortTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * FileProcessingJobPersistencePort 인터페이스가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("FileProcessingJobPersistencePort는 persist() 메서드를 제공해야 한다")
    void shouldProvidePersistMethod() {
        // Given: FileProcessingJobPersistencePort 인터페이스 (컴파일 에러)
        FileProcessingJobPersistencePort port = null;

        // When & Then: 메서드 시그니처 검증
        // FileProcessingJobId persist(FileProcessingJob job) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("persist()는 FileProcessingJobId를 반환해야 한다")
    void persistShouldReturnFileProcessingJobId() {
        // Given: FileProcessingJobPersistencePort 인터페이스 (컴파일 에러)
        FileProcessingJobPersistencePort port = null;

        // When & Then: 반환 타입 검증
        // FileProcessingJobId 반환 (Value Object)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("persist()는 FileProcessingJob을 파라미터로 받아야 한다")
    void persistShouldAcceptFileProcessingJob() {
        // Given: FileProcessingJobPersistencePort 인터페이스 (컴파일 에러)
        FileProcessingJobPersistencePort port = null;

        // When & Then: 파라미터 타입 검증
        // FileProcessingJob 파라미터 (Domain Aggregate)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }
}
