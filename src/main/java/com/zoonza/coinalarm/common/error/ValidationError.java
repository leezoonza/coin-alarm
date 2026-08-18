package com.zoonza.coinalarm.common.error;

public record ValidationError(
        String field,
        String message
) {
}
