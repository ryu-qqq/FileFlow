package com.ryuqq.fileflow.adapter.in.rest.session.error;

import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import com.ryuqq.fileflow.domain.session.exception.SessionException;
import java.net.URI;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SessionErrorMapper implements ErrorMapper {

    @Override
    public boolean supports(DomainException ex) {
        return ex instanceof SessionException;
    }

    @Override
    public MappedError map(DomainException ex, Locale locale) {
        HttpStatus status = HttpStatus.valueOf(ex.httpStatus());
        return new MappedError(status, ex.code(), ex.getMessage(), URI.create("/errors/session"));
    }
}
