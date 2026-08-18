package com.zoonza.coinalarm.member.internal.domain;

import com.zoonza.coinalarm.common.error.ErrorCode;

public enum AuthenticationErrorCode implements ErrorCode {
    INVALID_CREDENTIALS(
            "AUTH-001",
            "로그인 아이디 또는 비밀번호가 올바르지 않습니다.",
            401
    ),

    INVALID_REFRESH_TOKEN(
            "AUTH-002",
            "리프레시 토큰이 유효하지 않습니다.",
            401
    );

    private final String code;
    private final String message;
    private final int status;

    AuthenticationErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return status;
    }
}
