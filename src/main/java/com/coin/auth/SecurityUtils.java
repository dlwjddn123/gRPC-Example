package com.coin.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
public class SecurityUtils {

    public static String getLoggedUserLoginId() {
        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        return principal.getName();
    }
}

