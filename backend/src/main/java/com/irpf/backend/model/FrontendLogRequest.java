package com.irpf.backend.model;

public record FrontendLogRequest(
        String timestamp,
        String level,
        String type,
        String message,
        String context,
        String url,
        String method,
        Integer statusCode,
        String username,
        String userAgent,
        String stackTrace
) {}
