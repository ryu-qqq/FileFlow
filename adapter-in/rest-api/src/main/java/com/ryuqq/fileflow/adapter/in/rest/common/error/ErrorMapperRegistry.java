package com.ryuqq.fileflow.adapter.in.rest.common.error;

import com.ryuqq.fileflow.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * ErrorMapper 구현체들을 관리하고 적절한 매퍼를 선택하는 레지스트리
 *
 * <p>Spring이 모든 ErrorMapper 빈들을 자동으로 수집하여 주입합니다.
 *
 * <p><strong>매칭 우선순위:</strong>
 *
 * <ul>
 *   <li>첫 번째로 {@code supports(ex)}가 true를 반환하는 매퍼 사용
 *   <li>매칭되는 매퍼가 없으면 defaultMapping 사용
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 * @see ErrorMapper
 */
@Component
public class ErrorMapperRegistry {

    private static final Logger log = LoggerFactory.getLogger(ErrorMapperRegistry.class);
    private final List<ErrorMapper> mappers;

    public ErrorMapperRegistry(List<ErrorMapper> mappers) {
        this.mappers = mappers;
        log.info(
                "ErrorMapperRegistry initialized with {} mappers: {}",
                mappers.size(),
                mappers.stream().map(m -> m.getClass().getSimpleName()).toList());
    }

    /**
     * DomainException에 맞는 매퍼를 찾아 에러 응답으로 변환
     *
     * @param ex 도메인 예외
     * @param locale 다국어 지원을 위한 로케일
     * @return 매핑된 에러 (매칭 매퍼가 없으면 empty)
     */
    public Optional<ErrorMapper.MappedError> map(DomainException ex, Locale locale) {
        Optional<ErrorMapper> matchingMapper =
                mappers.stream().filter(mapper -> mapper.supports(ex)).findFirst();

        if (matchingMapper.isPresent()) {
            log.debug(
                    "Found matching ErrorMapper: {} for code={}",
                    matchingMapper.get().getClass().getSimpleName(),
                    ex.code());
        } else {
            log.debug(
                    "No matching ErrorMapper found for code={}, will use defaultMapping",
                    ex.code());
        }

        return matchingMapper.map(mapper -> mapper.map(ex, locale));
    }

    /**
     * 기본 에러 매핑 (매칭되는 ErrorMapper가 없을 때 사용)
     *
     * <p>DomainException의 ErrorCode에 설정된 httpStatus를 사용하여 적절한 HTTP 상태 코드를 반환합니다.
     *
     * @param ex 도메인 예외
     * @return RFC 7807 호환 기본 에러 응답
     */
    public ErrorMapper.MappedError defaultMapping(DomainException ex) {
        int rawStatus = ex.httpStatus();
        HttpStatus status = HttpStatus.valueOf(rawStatus);
        log.debug(
                "defaultMapping called: code={}, rawHttpStatus={}, resolvedStatus={}",
                ex.code(),
                rawStatus,
                status);
        return new ErrorMapper.MappedError(
                status,
                status.getReasonPhrase(),
                ex.getMessage() != null ? ex.getMessage() : "Invalid request",
                URI.create("about:blank"));
    }
}
