// PasswordUtil.java
package com.LetucOJ.sys.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    public static String encrypt(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}