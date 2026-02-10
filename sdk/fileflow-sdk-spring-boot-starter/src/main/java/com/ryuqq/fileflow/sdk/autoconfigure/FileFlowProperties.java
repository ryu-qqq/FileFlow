package com.ryuqq.fileflow.sdk.autoconfigure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fileflow")
public class FileFlowProperties {

    private String baseUrl;

    private String serviceName;

    private String serviceToken;

    private final Timeout timeout = new Timeout();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public static class Timeout {

        private Duration connect = Duration.ofSeconds(5);

        private Duration read = Duration.ofSeconds(30);

        public Duration getConnect() {
            return connect;
        }

        public void setConnect(Duration connect) {
            this.connect = connect;
        }

        public Duration getRead() {
            return read;
        }

        public void setRead(Duration read) {
            this.read = read;
        }
    }
}
