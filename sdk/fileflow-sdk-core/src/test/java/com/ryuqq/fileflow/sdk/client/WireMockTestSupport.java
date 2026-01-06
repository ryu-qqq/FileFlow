package com.ryuqq.fileflow.sdk.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * WireMock 테스트 지원 유틸리티.
 *
 * <p>공통 스텁 생성 메서드를 제공합니다.
 *
 * <p>Note: 각 테스트 클래스에 @WireMockTest 어노테이션을 추가해야 합니다.
 */
abstract class WireMockTestSupport {

    protected static final String SERVICE_TOKEN = "test-service-token";
    protected static final String SERVICE_TOKEN_HEADER = "X-Service-Token";

    /**
     * 성공 응답 스텁을 생성합니다.
     *
     * @param responseBody JSON 응답 본문
     * @return WireMock 응답 객체
     */
    protected com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder successResponse(
            String responseBody) {
        return aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(responseBody);
    }

    /**
     * ApiResponse 래퍼로 감싼 성공 응답 JSON을 생성합니다.
     *
     * @param data 응답 데이터 JSON
     * @return 전체 응답 JSON
     */
    protected String wrapSuccessResponse(String data) {
        return String.format(
                """
                {
                    "success": true,
                    "data": %s,
                    "timestamp": "2025-01-15T10:00:00",
                    "requestId": "test-request-id"
                }
                """,
                data);
    }

    /**
     * 에러 응답 스텁을 생성합니다.
     *
     * @param status HTTP 상태 코드
     * @param errorCode 에러 코드
     * @param detail 에러 상세 메시지
     * @return WireMock 응답 객체
     */
    protected com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder errorResponse(
            int status, String errorCode, String detail) {
        String body =
                String.format(
                        """
                        {
                            "type": "about:blank",
                            "title": "%s",
                            "status": %d,
                            "detail": "%s",
                            "instance": "/api/v1/file/test"
                        }
                        """,
                        errorCode, status, detail);
        return aResponse()
                .withStatus(status)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(body);
    }

    /**
     * FileFlow 동기 클라이언트를 생성합니다.
     *
     * @param wmRuntimeInfo WireMock 런타임 정보
     * @return FileFlowClient 인스턴스
     */
    protected FileFlowClient createClient(WireMockRuntimeInfo wmRuntimeInfo) {
        return FileFlowClient.builder()
                .baseUrl(wmRuntimeInfo.getHttpBaseUrl())
                .serviceToken(SERVICE_TOKEN)
                .build();
    }

    /**
     * FileFlow 비동기 클라이언트를 생성합니다.
     *
     * @param wmRuntimeInfo WireMock 런타임 정보
     * @return FileFlowAsyncClient 인스턴스
     */
    protected FileFlowAsyncClient createAsyncClient(WireMockRuntimeInfo wmRuntimeInfo) {
        return FileFlowClient.builder()
                .baseUrl(wmRuntimeInfo.getHttpBaseUrl())
                .serviceToken(SERVICE_TOKEN)
                .buildAsync();
    }

    /**
     * 인증 헤더를 검증하는 매처를 추가합니다.
     *
     * @param builder MappingBuilder
     * @return 인증 헤더가 추가된 MappingBuilder
     */
    protected MappingBuilder withAuth(MappingBuilder builder) {
        return builder.withHeader(SERVICE_TOKEN_HEADER, equalTo(SERVICE_TOKEN));
    }
}
