package com.ryuqq.fileflow.application.file.port.in;

import com.ryuqq.fileflow.application.file.dto.command.DeleteFileCommand;

/**
 * DeleteFile UseCase
 *
 * <p>CQRS Command Side - 파일 삭제 (Soft Delete)</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>파일 Soft Delete 수행</li>
 *   <li>삭제 권한 검증 (SELF 또는 TENANT)</li>
 *   <li>테넌트/조직 스코프 검증</li>
 *   <li>중복 삭제 방지</li>
 * </ul>
 *
 * <p><strong>사용 시나리오</strong>:</p>
 * <ul>
 *   <li>사용자 파일 삭제</li>
 *   <li>관리자 파일 정리</li>
 *   <li>만료된 파일 자동 삭제 (Batch Job)</li>
 * </ul>
 *
 * <p><strong>삭제 규칙</strong>:</p>
 * <ul>
 *   <li>Soft Delete만 수행 (deleted_at 타임스탬프 설정)</li>
 *   <li>물리 삭제는 별도 Batch Job에서 처리</li>
 *   <li>삭제된 파일은 조회 불가 (deleted_at IS NULL 필터)</li>
 *   <li>이미 삭제된 파일 재삭제 시 예외 발생</li>
 * </ul>
 *
 * <p><strong>권한 검증</strong>:</p>
 * <ul>
 *   <li>SELF: 파일 소유자만 삭제 가능</li>
 *   <li>TENANT: 테넌트 관리자 삭제 가능</li>
 *   <li>ORGANIZATION: 조직 관리자 삭제 가능</li>
 * </ul>
 *
 * <p><strong>트랜잭션 전략</strong>:</p>
 * <ul>
 *   <li>@Transactional - 쓰기 작업</li>
 *   <li>Domain 메서드 호출: FileAsset.softDelete()</li>
 *   <li>외부 API 호출 없음 (트랜잭션 내 완료)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface DeleteFileUseCase {

    /**
     * 파일 삭제 (Soft Delete)
     *
     * @param command 파일 삭제 Command
     * @throws com.ryuqq.fileflow.domain.exception.FileNotFoundException 파일이 존재하지 않는 경우
     * @throws com.ryuqq.fileflow.domain.exception.AccessDeniedException 삭제 권한 없음
     * @throws IllegalStateException 이미 삭제된 파일인 경우
     */
    void execute(DeleteFileCommand command);
}
