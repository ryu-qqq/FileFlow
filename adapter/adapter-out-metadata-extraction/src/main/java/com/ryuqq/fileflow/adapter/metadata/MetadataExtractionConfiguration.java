package com.ryuqq.fileflow.adapter.metadata;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 메타데이터 추출 Adapter 설정 클래스
 *
 * @author sangwon-ryu
 */
@Configuration
public class MetadataExtractionConfiguration {

    @Bean
    public ImageMetadataExtractor imageMetadataExtractor() {
        return new ImageMetadataExtractor();
    }
}
