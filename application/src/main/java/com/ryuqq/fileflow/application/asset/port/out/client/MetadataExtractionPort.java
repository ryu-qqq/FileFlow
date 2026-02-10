package com.ryuqq.fileflow.application.asset.port.out.client;

import com.ryuqq.fileflow.application.asset.dto.result.ImageMetadataResult;

public interface MetadataExtractionPort {

    ImageMetadataResult extract(byte[] imageBytes);
}
