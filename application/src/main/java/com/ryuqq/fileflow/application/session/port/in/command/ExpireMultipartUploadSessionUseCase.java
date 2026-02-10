package com.ryuqq.fileflow.application.session.port.in.command;

/**
 * 멀티파트 업로드 세션 만료 UseCase (Command)
 *
 * <p>Redis keyspace notification 또는 스케줄러에 의해 호출됩니다.
 */
public interface ExpireMultipartUploadSessionUseCase {

    void execute(String sessionId);
}
