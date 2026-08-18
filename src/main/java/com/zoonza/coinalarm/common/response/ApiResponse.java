package com.zoonza.coinalarm.common.response;

public final class ApiResponse<T> {
    private final boolean success;
    private final T data;

    private ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null);
    }

    public static <T> ApiResponse<T> failure(T error) {
        return new ApiResponse<>(false, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }
}
