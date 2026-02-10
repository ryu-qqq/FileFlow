package com.ryuqq.fileflow.adapter.in.rest.common.mapper;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.net.URI;
import java.util.Locale;
import org.springframework.http.HttpStatus;

/**
 * 도메인 예외를 HTTP 응답으로 변환하는 매퍼 인터페이스
 *
 * <p>OCP(Open-Closed Principle) 준수를 위해 도메인별로 구현체를 생성합니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>DomainException 전체를 받아 유연한 매칭 지원
 *   <li>예외 타입, 에러 코드, 도메인별 조건 매칭 가능
 *   <li>i18n 지원을 위한 Locale 파라미터
 * </ul>
 *
 * <p><strong>구현 예시:</strong>
 *
 * <pre>{@code
 * @Component
 * public class OrderErrorMapper implements ErrorMapper {
 *
 *     @Override
 *     public boolean supports(DomainException ex) {
 *         // 방법 1: 예외 타입으로 매칭
 *         return ex instanceof OrderException;
 *
 *         // 방법 2: 에러 코드 prefix로 매칭
 *         // return ex.code().startsWith("ORDER_");
 *     }
 *
 *     @Override
 *     public MappedError map(DomainException ex, Locale locale) {
 *         return new MappedError(
 *             HttpStatus.BAD_REQUEST,
 *             "Order Error",
 *             ex.getMessage(),
 *             URI.create("/errors/order")
 *         );
 *     }
 * }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 * @see com.ryuqq.marketplace.adapter.in.rest.common.error.ErrorMapperRegistry
 */
public interface ErrorMapper {

    /**
     * 이 매퍼가 주어진 예외를 처리할 수 있는지 확인
     *
     * <p>매칭 전략:
     *
     * <ul>
     *   <li>예외 타입: {@code ex instanceof OrderException}
     *   <li>에러 코드: {@code ex.code().startsWith("ORDER_")}
     *   <li>복합 조건: 타입 + 코드 조합
     * </ul>
     *
     * @param ex 도메인 예외
     * @return 처리 가능하면 true
     */
    boolean supports(DomainException ex);

    /**
     * DomainException을 HTTP 응답용 MappedError로 변환
     *
     * @param ex 도메인 예외
     * @param locale 다국어 지원을 위한 로케일
     * @return RFC 7807 호환 에러 정보
     */
    MappedError map(DomainException ex, Locale locale);

    /**
     * RFC 7807 호환 에러 응답 DTO
     *
     * @param status HTTP 상태 코드
     * @param title 에러 유형 요약 (사람이 읽을 수 있는 형태)
     * @param detail 에러 상세 설명
     * @param type 에러 유형 URI (문서 링크)
     */
    record MappedError(HttpStatus status, String title, String detail, URI type) {}
}
