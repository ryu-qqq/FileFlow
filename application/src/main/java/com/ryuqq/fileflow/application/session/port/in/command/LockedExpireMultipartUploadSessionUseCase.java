package com.ryuqq.fileflow.application.session.port.in.command;

/**
 * 분산락 기반 멀티파트 업로드 세션 만료 UseCase (Command)
 *
 * <p>Redis keyspace notification에 의해 호출되며, 분산락으로 중복 처리를 방지합니다.
 */
public interface LockedExpireMultipartUploadSessionUseCase {

    void execute(String sessionId);
}
