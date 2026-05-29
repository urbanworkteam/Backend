package kr.farmily.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.farmily.api.common.exception.ErrorCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, ErrorBody error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(ErrorCode code, String message, String field) {
        return new ApiResponse<>(false, null, new ErrorBody(code.name(), message, field, null));
    }

    public static ApiResponse<Void> fail(ErrorCode code, String message, String field, Object details) {
        return new ApiResponse<>(false, null, new ErrorBody(code.name(), message, field, details));
    }
}
