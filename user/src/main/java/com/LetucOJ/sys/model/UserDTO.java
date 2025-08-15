package com.LetucOJ.sys.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class UserDTO {

    @TableField("user_name")
    private String userName;

    private String cnname;

    private String password;

    private String role;

    private boolean enabled;

    public UserDTO() {}

    public UserDTO(String userName, String password, String cnname) {
        this.userName = userName;
        this.password = password;
        this.role = "USER";
        this.enabled = false;
        this.cnname = cnname;
    }

    public boolean enabled() {
        return enabled;
    }
}
