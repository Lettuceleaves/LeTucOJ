package com.LetucOJ.auth.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

@Data
public class UserDTO {

    @TableField("user_name")
    private String userName;

    private String password;

    private String role;

    public UserDTO() {}

    public UserDTO(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.role = "USER";
    }
}
