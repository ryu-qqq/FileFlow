package com.ryuqq.fileflow.adapter.sqs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * S3 Event Notification DTO
 *
 * S3에서 발생한 이벤트 정보를 표현합니다.
 * Jackson을 사용하여 JSON 메시지를 역직렬화합니다.
 *
 * @author sangwon-ryu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class S3EventNotification {

    @JsonProperty("Records")
    private List<S3EventRecord> records;

    public S3EventNotification() {
    }

    public S3EventNotification(List<S3EventRecord> records) {
        this.records = records;
    }

    public List<S3EventRecord> getRecords() {
        return records;
    }

    public void setRecords(List<S3EventRecord> records) {
        this.records = records;
    }

    /**
     * S3 이벤트 레코드
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class S3EventRecord {

        @JsonProperty("eventVersion")
        private String eventVersion;

        @JsonProperty("eventSource")
        private String eventSource;

        @JsonProperty("awsRegion")
        private String awsRegion;

        @JsonProperty("eventTime")
        private String eventTime;

        @JsonProperty("eventName")
        private String eventName;

        @JsonProperty("s3")
        private S3Entity s3;

        public S3EventRecord() {
        }

        public String getEventVersion() {
            return eventVersion;
        }

        public void setEventVersion(String eventVersion) {
            this.eventVersion = eventVersion;
        }

        public String getEventSource() {
            return eventSource;
        }

        public void setEventSource(String eventSource) {
            this.eventSource = eventSource;
        }

        public String getAwsRegion() {
            return awsRegion;
        }

        public void setAwsRegion(String awsRegion) {
            this.awsRegion = awsRegion;
        }

        public String getEventTime() {
            return eventTime;
        }

        public void setEventTime(String eventTime) {
            this.eventTime = eventTime;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public S3Entity getS3() {
            return s3;
        }

        public void setS3(S3Entity s3) {
            this.s3 = s3;
        }
    }

    /**
     * S3 엔티티 정보
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class S3Entity {

        @JsonProperty("bucket")
        private S3BucketEntity bucket;

        @JsonProperty("object")
        private S3ObjectEntity object;

        public S3Entity() {
        }

        public S3BucketEntity getBucket() {
            return bucket;
        }

        public void setBucket(S3BucketEntity bucket) {
            this.bucket = bucket;
        }

        public S3ObjectEntity getObject() {
            return object;
        }

        public void setObject(S3ObjectEntity object) {
            this.object = object;
        }
    }

    /**
     * S3 버킷 정보
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class S3BucketEntity {

        @JsonProperty("name")
        private String name;

        @JsonProperty("arn")
        private String arn;

        public S3BucketEntity() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getArn() {
            return arn;
        }

        public void setArn(String arn) {
            this.arn = arn;
        }
    }

    /**
     * S3 객체 정보
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class S3ObjectEntity {

        @JsonProperty("key")
        private String key;

        @JsonProperty("size")
        private Long size;

        @JsonProperty("eTag")
        private String eTag;

        @JsonProperty("sequencer")
        private String sequencer;

        public S3ObjectEntity() {
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }

        public String geteTag() {
            return eTag;
        }

        public void seteTag(String eTag) {
            this.eTag = eTag;
        }

        public String getSequencer() {
            return sequencer;
        }

        public void setSequencer(String sequencer) {
            this.sequencer = sequencer;
        }
    }
}
