package com.erprag.core.auth;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Shared by both the REST and MCP adapters so credentials are hashed/verified identically
 * without either depending on Spring Security.
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public static boolean matches(String rawPassword, String hash) {
        return BCrypt.checkpw(rawPassword, hash);
    }
}
