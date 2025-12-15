package com.LetucOJ.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtInfoVO {
    private String userName;
    private String nickName;
    private String role;
    private long millis;
}
