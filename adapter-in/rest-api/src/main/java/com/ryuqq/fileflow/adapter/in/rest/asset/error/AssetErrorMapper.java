package com.ryuqq.fileflow.adapter.in.rest.asset.error;

import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.domain.asset.exception.AssetException;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.net.URI;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AssetErrorMapper implements ErrorMapper {

    @Override
    public boolean supports(DomainException ex) {
        return ex instanceof AssetException;
    }

    @Override
    public MappedError map(DomainException ex, Locale locale) {
        HttpStatus status = HttpStatus.valueOf(ex.httpStatus());
        return new MappedError(status, ex.code(), ex.getMessage(), URI.create("/errors/asset"));
    }
}
