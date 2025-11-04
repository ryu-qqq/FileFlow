package com.ryuqq.fileflow.application.file.port.in;

import com.ryuqq.fileflow.application.file.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.file.dto.response.DownloadUrlResponse;

/**
 * GenerateDownloadUrl UseCase
 *
 * <p>CQRS Command Side - 파일 다운로드 URL 생성</p>
 *
 * <p><strong>책임</strong>:</p>
 * <ul>
 *   <li>파일 다운로드 서명 URL 생성</li>
 *   <li>파일 존재 및 상태 검증</li>
 *   <li>테넌트/조직 스코프 검증</li>
 *   <li>파일 가시성 검사</li>
 * </ul>
 *
 * <p><strong>사용 시나리오</strong>:</p>
 * <ul>
 *   <li>사용자 파일 다운로드 링크 생성</li>
 *   <li>API 응답에 포함할 서명된 URL</li>
 *   <li>임시 공유 링크 생성</li>
 * </ul>
 *
 * <p><strong>비즈니스 규칙</strong>:</p>
 * <ul>
 *   <li>파일 상태가 AVAILABLE이어야 다운로드 가능</li>
 *   <li>삭제된 파일(deleted_at != null)은 다운로드 불가</li>
 *   <li>만료된 파일은 다운로드 불가</li>
 *   <li>PRIVATE 파일은 소유자 또는 권한 있는 사용자만 다운로드</li>
 * </ul>
 *
 * <p><strong>트랜잭션 전략</strong>:</p>
 * <ul>
 *   <li>@Transactional(readOnly = true) - 읽기 전용 최적화</li>
 *   <li>외부 S3 API 호출은 트랜잭션 밖에서 수행</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface GenerateDownloadUrlUseCase {

    /**
     * 파일 다운로드 URL 생성
     *
     * @param command 다운로드 URL 생성 Command
     * @return DownloadUrlResponse (서명된 URL, 만료 시간)
     * @throws com.ryuqq.fileflow.domain.exception.FileNotFoundException 파일이 존재하지 않는 경우
     * @throws com.ryuqq.fileflow.domain.exception.AccessDeniedException 테넌트/조직 스코프 위반
     * @throws IllegalStateException 파일 상태가 AVAILABLE이 아닌 경우
     */
    DownloadUrlResponse execute(GenerateDownloadUrlCommand command);
}
