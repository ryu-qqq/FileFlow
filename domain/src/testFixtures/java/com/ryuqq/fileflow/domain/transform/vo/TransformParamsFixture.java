package com.ryuqq.fileflow.domain.transform.vo;

public class TransformParamsFixture {

    public static TransformParams aResizeParams() {
        return TransformParams.forResize(800, 600, true);
    }

    public static TransformParams aConvertParams() {
        return TransformParams.forConvert("webp");
    }

    public static TransformParams aCompressParams() {
        return TransformParams.forCompress(80);
    }

    public static TransformParams aThumbnailParams() {
        return TransformParams.forThumbnail(150, 150);
    }
}
