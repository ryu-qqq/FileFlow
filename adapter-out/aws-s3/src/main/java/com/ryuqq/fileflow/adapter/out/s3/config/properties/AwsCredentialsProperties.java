package com.ryuqq.fileflow.adapter.out.s3.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AWS Credentials Properties.
 *
 * <p>AWS 인증 정보를 담당하는 Properties 클래스입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "aws.credentials")
public class AwsCredentialsProperties {

    private String accessKey;
    private String secretKey;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
