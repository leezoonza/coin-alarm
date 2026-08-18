package com.zoonza.coinalarm.common.response;

import com.zoonza.coinalarm.common.error.ErrorCode;
import com.zoonza.coinalarm.common.error.ValidationError;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<ValidationError> errors
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                List.of()
        );
    }

    public static ErrorResponse validation(
            ErrorCode errorCode,
            List<ValidationError> errors
    ) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                List.copyOf(errors)
        );
    }
}
