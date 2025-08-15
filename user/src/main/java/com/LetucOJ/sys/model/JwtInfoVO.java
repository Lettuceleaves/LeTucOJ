package com.LetucOJ.sys.model;

import lombok.Data;

@Data
public class JwtInfoVO {
    private String username;
    private String cnname;
    private String role;
    private long millis;

    public JwtInfoVO() {}

    public JwtInfoVO(String username, String cnname, String role, long millis) {
        this.username = username;
        this.cnname = cnname;
        this.role = role;
        this.millis = millis;
    }
}
