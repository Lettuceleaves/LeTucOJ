package com.LetucOJ.sys.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtInfoVO {
    private String username;
    private String cnname;
    private String role;
    private long millis;
}
