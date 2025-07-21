package com.LetucOJ.auth.model;

import lombok.Data;

@Data
public class JwtInfoVO {
    private String username;
    private String role;
    private long millis;

    public JwtInfoVO() {}

    public JwtInfoVO(String username, String role, long millis) {
        this.username = username;
        this.role = role;
        this.millis = millis;
    }
}
