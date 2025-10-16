package com.sisinnov.pms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    LocalDateTime timestamp,
    Integer status,
    String error,
    String message,
    String path,
    String traceId
) {
    public static ErrorResponse of(Integer status, String error, String message, String path, String traceId) {
        return new ErrorResponse(
            LocalDateTime.now(),
            status,
            error,
            message,
            path,
            traceId
        );
    }
}