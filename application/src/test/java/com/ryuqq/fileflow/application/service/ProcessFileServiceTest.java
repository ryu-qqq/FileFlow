package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.ProcessFileCommand;
import com.ryuqq.fileflow.application.fixture.ProcessFileCommandFixture;
import com.ryuqq.fileflow.application.port.out.query.LoadFilePort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.fixture.FileFixture;
import com.ryuqq.fileflow.domain.vo.FileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * ProcessFileService 테스트
 * <p>
 * Application Layer Service 테스트 규칙:
 * - Mock Port 사용 (Outbound Port)
 * - TestFixture 필수 사용 (Command, Domain)
 * - Transaction 경계 검증 (@Transactional 내 외부 API 호출 금지)
 * - CQRS 준수 검증 (Command UseCase)
 * </p>
 * <p>
 * ProcessFileService Zero-Tolerance 규칙:
 * - 파일 가공은 COMPLETED 상태에서만 가능
 * - PENDING, UPLOADING, FAILED 상태에서는 가공 불가
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ProcessFileServiceTest {

    @Mock
    private LoadFilePort loadFilePort;

    private ProcessFileService processFileService;

    @BeforeEach
    void setUp() {
        processFileService = new ProcessFileService(loadFilePort);
    }

    /**
     * 🔴 RED Phase: PENDING 상태 검증 테스트
     * <p>
     * PENDING 상태 파일은 아직 업로드되지 않았으므로 가공할 수 없습니다.
     * IllegalStateException 발생해야 합니다.
     * </p>
     */
    @Test
    @DisplayName("PENDING 상태 파일은 가공할 수 없다")
    void shouldThrowExceptionWhenFileNotCompleted_Pending() {
        // Given: PENDING 상태 파일
        ProcessFileCommand command = ProcessFileCommandFixture.create();
        File pendingFile = FileFixture.aPendingFile();

        given(loadFilePort.loadById(any(FileId.class)))
                .willReturn(Optional.of(pendingFile));

        // When & Then: IllegalStateException 발생
        assertThatThrownBy(() -> processFileService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMPLETED");
    }

    /**
     * 🔴 RED Phase: UPLOADING 상태 검증 테스트
     * <p>
     * UPLOADING 상태 파일은 업로드 진행 중이므로 가공할 수 없습니다.
     * </p>
     */
    @Test
    @DisplayName("UPLOADING 상태 파일은 가공할 수 없다")
    void shouldThrowExceptionWhenFileNotCompleted_Uploading() {
        // Given: UPLOADING 상태 파일
        ProcessFileCommand command = ProcessFileCommandFixture.create();
        File uploadingFile = FileFixture.aUploadingFile();

        given(loadFilePort.loadById(any(FileId.class)))
                .willReturn(Optional.of(uploadingFile));

        // When & Then: IllegalStateException 발생
        assertThatThrownBy(() -> processFileService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMPLETED");
    }

    /**
     * 🔴 RED Phase: FAILED 상태 검증 테스트
     * <p>
     * FAILED 상태 파일은 업로드 실패했으므로 가공할 수 없습니다.
     * </p>
     */
    @Test
    @DisplayName("FAILED 상태 파일은 가공할 수 없다")
    void shouldThrowExceptionWhenFileNotCompleted_Failed() {
        // Given: FAILED 상태 파일
        ProcessFileCommand command = ProcessFileCommandFixture.create();
        File failedFile = FileFixture.aFailedFile();

        given(loadFilePort.loadById(any(FileId.class)))
                .willReturn(Optional.of(failedFile));

        // When & Then: IllegalStateException 발생
        assertThatThrownBy(() -> processFileService.execute(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("COMPLETED");
    }

    /**
     * 🔴 RED Phase: 파일 존재 확인 테스트
     * <p>
     * 파일이 존재하지 않으면 가공할 수 없습니다.
     * RuntimeException 또는 FileNotFoundException 발생해야 합니다.
     * </p>
     */
    @Test
    @DisplayName("파일이 존재하지 않으면 가공할 수 없다")
    void shouldThrowExceptionWhenFileNotFound() {
        // Given: 파일이 존재하지 않음
        ProcessFileCommand command = ProcessFileCommandFixture.create();

        given(loadFilePort.loadById(any(FileId.class)))
                .willReturn(Optional.empty());

        // When & Then: RuntimeException 발생
        assertThatThrownBy(() -> processFileService.execute(command))
                .isInstanceOf(RuntimeException.class);
    }
}
