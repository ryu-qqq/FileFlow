package com.ryuqq.fileflow.application.transform.port.in.command;

import com.ryuqq.fileflow.application.transform.dto.command.CreateTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;

public interface CreateTransformRequestUseCase {

    TransformRequestResponse execute(CreateTransformRequestCommand command);
}
