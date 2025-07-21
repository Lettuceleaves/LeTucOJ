package com.LetucOJ.auth.model;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String username;
    private String password;
}
