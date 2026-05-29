package kr.farmily.api.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    NOT_RESOURCE_OWNER(HttpStatus.FORBIDDEN),
    ONBOARDING_REQUIRED(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    CROP_NOT_FOUND(HttpStatus.NOT_FOUND),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND),
    FARM_LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND),
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND),

    VALIDATION_ERROR(HttpStatus.UNPROCESSABLE_ENTITY),
    HANDLE_INVALID_FORMAT(HttpStatus.UNPROCESSABLE_ENTITY),
    FARM_LOCATION_REQUIRED(HttpStatus.UNPROCESSABLE_ENTITY),

    CREDIT_EXHAUSTED(HttpStatus.PAYMENT_REQUIRED),
    PLAN_LIMIT_EXCEEDED(HttpStatus.PAYMENT_REQUIRED),
    PAYMENT_FAILED(HttpStatus.PAYMENT_REQUIRED),
    PLAN_NOT_AVAILABLE(HttpStatus.BAD_REQUEST),

    DIARY_ALREADY_EXISTS_FOR_DATE(HttpStatus.CONFLICT),
    HANDLE_TAKEN(HttpStatus.CONFLICT),

    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY),
    KAKAO_API_ERROR(HttpStatus.BAD_GATEWAY),
    KMA_API_ERROR(HttpStatus.BAD_GATEWAY),
    PORTONE_API_ERROR(HttpStatus.BAD_GATEWAY),
    BEDROCK_API_ERROR(HttpStatus.BAD_GATEWAY),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
