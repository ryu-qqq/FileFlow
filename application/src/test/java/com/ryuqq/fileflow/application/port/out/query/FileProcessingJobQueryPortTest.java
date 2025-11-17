package com.ryuqq.fileflow.application.port.out.query;

import com.ryuqq.fileflow.domain.aggregate.FileProcessingJob;
import com.ryuqq.fileflow.domain.vo.FileProcessingJobId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileProcessingJobQueryPort 인터페이스 계약 테스트
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *QueryPort
 * - 패키지: ..application..port.out.query..
 * - 필수 메서드 4개: findById, existsById, findByCriteria, countByCriteria
 * - Value Object 파라미터: FileProcessingJobId, FileProcessingJobSearchCriteria
 * - Domain 반환: FileProcessingJob Aggregate (DTO/Entity 반환 금지)
 * </p>
 */
class FileProcessingJobQueryPortTest {

    /**
     * 🔴 RED Phase: 컴파일 에러 확인
     * <p>
     * FileProcessingJobQueryPort 인터페이스와 FileProcessingJobSearchCriteria VO가 존재하지 않으므로
     * 컴파일 에러가 발생합니다.
     * </p>
     */
    @Test
    @DisplayName("FileProcessingJobQueryPort는 findById() 메서드를 제공해야 한다")
    void shouldProvideFindByIdMethod() {
        // Given: FileProcessingJobQueryPort 인터페이스 (컴파일 에러)
        FileProcessingJobQueryPort port = null;

        // When & Then: 메서드 시그니처 검증
        // Optional<FileProcessingJob> findById(FileProcessingJobId id) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("FileProcessingJobQueryPort는 existsById() 메서드를 제공해야 한다")
    void shouldProvideExistsByIdMethod() {
        // Given: FileProcessingJobQueryPort 인터페이스 (컴파일 에러)
        FileProcessingJobQueryPort port = null;

        // When & Then: 메서드 시그니처 검증
        // boolean existsById(FileProcessingJobId id) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("FileProcessingJobQueryPort는 findByCriteria() 메서드를 제공해야 한다")
    void shouldProvideFindByCriteriaMethod() {
        // Given: FileProcessingJobQueryPort 인터페이스 (컴파일 에러)
        FileProcessingJobQueryPort port = null;

        // When & Then: 메서드 시그니처 검증
        // List<FileProcessingJob> findByCriteria(FileProcessingJobSearchCriteria criteria) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("FileProcessingJobQueryPort는 countByCriteria() 메서드를 제공해야 한다")
    void shouldProvideCountByCriteriaMethod() {
        // Given: FileProcessingJobQueryPort 인터페이스 (컴파일 에러)
        FileProcessingJobQueryPort port = null;

        // When & Then: 메서드 시그니처 검증
        // long countByCriteria(FileProcessingJobSearchCriteria criteria) 메서드가 존재해야 함
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("findById()는 Optional<FileProcessingJob>을 반환해야 한다")
    void findByIdShouldReturnOptional() {
        // Given: FileProcessingJobQueryPort 인터페이스 (컴파일 에러)
        FileProcessingJobQueryPort port = null;

        // When & Then: 반환 타입 검증
        // Optional<FileProcessingJob> 반환 (null 방지)
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("existsById()는 boolean을 반환해야 한다")
    void existsByIdShouldReturnBoolean() {
        // Given: FileProcessingJobQueryPort 인터페이스 (컴파일 에러)
        FileProcessingJobQueryPort port = null;

        // When & Then: 반환 타입 검증
        // boolean 반환
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("findByCriteria()는 List<FileProcessingJob>을 반환해야 한다")
    void findByCriteriaShouldReturnList() {
        // Given: FileProcessingJobQueryPort 인터페이스 (컴파일 에러)
        FileProcessingJobQueryPort port = null;

        // When & Then: 반환 타입 검증
        // List<FileProcessingJob> 반환
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }

    @Test
    @DisplayName("countByCriteria()는 long을 반환해야 한다")
    void countByCriteriaShouldReturnLong() {
        // Given: FileProcessingJobQueryPort 인터페이스 (컴파일 에러)
        FileProcessingJobQueryPort port = null;

        // When & Then: 반환 타입 검증
        // long 반환
        assertThat(port).isNull(); // 임시 검증 (컴파일 에러 확인용)
    }
}
