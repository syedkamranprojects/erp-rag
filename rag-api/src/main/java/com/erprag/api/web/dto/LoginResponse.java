package com.erprag.api.web.dto;

public record LoginResponse(String token, long expiresInSeconds) {
}
