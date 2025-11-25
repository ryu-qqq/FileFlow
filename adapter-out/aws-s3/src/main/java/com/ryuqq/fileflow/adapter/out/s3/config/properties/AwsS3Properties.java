package com.ryuqq.fileflow.adapter.out.s3.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AWS S3 Properties.
 *
 * <p>S3 설정을 담당하는 Properties 클래스입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {

    private String region;
    private String bucket;
    private PresignedUrl presignedUrl = new PresignedUrl();

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public PresignedUrl getPresignedUrl() {
        return presignedUrl;
    }

    public void setPresignedUrl(PresignedUrl presignedUrl) {
        this.presignedUrl = presignedUrl;
    }

    public static class PresignedUrl {
        private int expirationMinutes = 60;

        public int getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(int expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }
    }
}
