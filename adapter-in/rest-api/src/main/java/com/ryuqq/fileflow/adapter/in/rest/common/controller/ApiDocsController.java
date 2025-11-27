package com.ryuqq.fileflow.adapter.in.rest.common.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API Documentation Controller
 *
 * <p>Spring REST Docs로 생성된 API 문서를 제공합니다.
 *
 * <p>빌드 시 asciidoctor 태스크가 생성한 HTML 문서를 static resource로 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@RestController
public class ApiDocsController {

    /**
     * API 문서 페이지 제공
     *
     * <p>Spring REST Docs로 생성된 index.html을 반환합니다.
     *
     * @return API 문서 HTML
     */
    @GetMapping(value = "/docs", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> getApiDocs() {
        Resource resource = new ClassPathResource("static/docs/index.html");
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(resource);
    }
}
