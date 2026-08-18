package com.zoonza.coinalarm.common.response;

import com.zoonza.coinalarm.common.error.CommonErrorCode;
import com.zoonza.coinalarm.common.error.ValidationError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    @DisplayName("검증 오류 목록을 응답에 복사한다")
    void validationCopiesErrors() {
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError("loginId", "required"));

        ErrorResponse response = ErrorResponse.validation(
                CommonErrorCode.VALIDATION_FAILED,
                errors
        );
        errors.clear();

        assertThat(response.code()).isEqualTo("COMMON-002");
        assertThat(response.message()).isEqualTo("요청 값이 올바르지 않습니다.");
        assertThat(response.errors()).containsExactly(
                new ValidationError("loginId", "required")
        );
    }
}
