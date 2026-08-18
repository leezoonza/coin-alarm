package com.zoonza.coinalarm.member.internal.domain;

import com.zoonza.coinalarm.common.error.ErrorCode;

public enum MemberErrorCode implements ErrorCode {
    DUPLICATE_LOING_ID(
            "MEMBER-001",
            "이미 사용 중인 로그인 아이디입니다.",
            409
    );

    private final String code;
    private final String message;
    private final int status;

    MemberErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public int getStatus() {
        return this.status;
    }
}
