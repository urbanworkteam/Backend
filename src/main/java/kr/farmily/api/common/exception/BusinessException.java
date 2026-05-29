package kr.farmily.api.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode code;
    private final String field;
    private final transient Object details;

    public BusinessException(ErrorCode code) {
        this(code, code.name(), null, null);
    }

    public BusinessException(ErrorCode code, String message) {
        this(code, message, null, null);
    }

    public BusinessException(ErrorCode code, String message, String field) {
        this(code, message, field, null);
    }

    public BusinessException(ErrorCode code, String message, String field, Object details) {
        super(message);
        this.code = code;
        this.field = field;
        this.details = details;
    }
}
