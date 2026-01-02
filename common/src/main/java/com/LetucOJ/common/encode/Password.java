package com.LetucOJ.common.encode;

import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Password {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    public static String encrypt(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (ENCODER.matches(rawPassword, encodedPassword)) {
            return true;
        } else {
            Logger.log(Type.CLIENT, LogLevel.INFO, "raw: " +  rawPassword + " encoded: " + encodedPassword);
            return false;
        }
    }
}