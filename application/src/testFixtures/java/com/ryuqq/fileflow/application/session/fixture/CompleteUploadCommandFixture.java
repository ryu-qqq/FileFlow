package com.ryuqq.fileflow.application.session.fixture;

import com.ryuqq.fileflow.application.session.dto.command.CompleteUploadCommand;
import com.ryuqq.fileflow.domain.session.vo.SessionId;

/**
 * CompleteUploadCommand TestFixture (Object Mother 패턴)
 * <p>
 * MVP Scope: Single Presigned URL Upload
 * </p>
 */
public class CompleteUploadCommandFixture {

    public static CompleteUploadCommand aCommand() {
        return new CompleteUploadCommand(SessionId.generate());
    }

    public static CompleteUploadCommand create() {
        return aCommand();
    }

    public static CompleteUploadCommand withSessionId(SessionId sessionId) {
        return new CompleteUploadCommand(sessionId);
    }
}
