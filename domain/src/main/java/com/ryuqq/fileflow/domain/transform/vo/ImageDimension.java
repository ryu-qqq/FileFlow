package com.ryuqq.fileflow.domain.transform.vo;

import com.ryuqq.fileflow.domain.transform.exception.TransformErrorCode;
import com.ryuqq.fileflow.domain.transform.exception.TransformException;

public record ImageDimension(int width, int height) {

    public ImageDimension {
        if (width <= 0) {
            throw new TransformException(
                    TransformErrorCode.INVALID_TRANSFORM_PARAMS,
                    "width must be positive, got: " + width);
        }
        if (height <= 0) {
            throw new TransformException(
                    TransformErrorCode.INVALID_TRANSFORM_PARAMS,
                    "height must be positive, got: " + height);
        }
    }

    public static ImageDimension of(int width, int height) {
        return new ImageDimension(width, height);
    }
}
