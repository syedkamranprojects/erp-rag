package com.erprag.api.security;

/** Authenticated entity-scoped chat user: which entity they belong to, and as whom. */
public record EntityUserPrincipal(String username, String entityCode, String role) {
}
