package com.zoonza.coinalarm.global.exception;

import com.zoonza.coinalarm.common.error.CommonErrorCode;
import com.zoonza.coinalarm.common.error.DomainException;
import com.zoonza.coinalarm.common.error.ErrorCode;
import com.zoonza.coinalarm.common.error.ValidationError;
import com.zoonza.coinalarm.common.response.ApiResponse;
import com.zoonza.coinalarm.common.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDomainException(
            DomainException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();
        ErrorResponse error = ErrorResponse.of(errorCode);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        List<ValidationError> errors = new ArrayList<>();

        exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .forEach(errors::add);

        exception.getBindingResult().getGlobalErrors().stream()
                .map(error -> new ValidationError(null, error.getDefaultMessage()))
                .forEach(errors::add);

        ErrorResponse response = ErrorResponse.validation(
                CommonErrorCode.VALIDATION_FAILED,
                errors
        );

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure(response));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnexpectedException(
            Exception exception
    ) {
        log.error("예상하지 못한 예외가 발생했습니다.", exception);

        ErrorResponse error = ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR);

        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.failure(error));
    }
}
