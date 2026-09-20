package com.erprag.api.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static EntityUserPrincipal currentEntityUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof EntityUserPrincipal entityUserPrincipal)) {
            throw new AccessDeniedException("Not authenticated as an entity user");
        }
        return entityUserPrincipal;
    }

    /** Belt-and-suspenders: reject if the JWT's entityCode doesn't match the path's entityCode. */
    public static void requireEntityMatch(String pathEntityCode) {
        EntityUserPrincipal principal = currentEntityUser();
        if (!principal.entityCode().equals(pathEntityCode)) {
            throw new AccessDeniedException("Token is not scoped to entity '" + pathEntityCode + "'");
        }
    }
}
