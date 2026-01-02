package com.LetucOJ.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserManagerDTO {
    private String userName;
    private String userNickName;
    private String password;
    private String role;
    private int status;
}
