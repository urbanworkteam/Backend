package kr.farmily.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorBody(String code, String message, String field, Object details) {}
