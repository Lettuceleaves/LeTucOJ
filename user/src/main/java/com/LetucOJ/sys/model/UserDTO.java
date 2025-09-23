package com.LetucOJ.sys.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    @TableField("user_name")
    private String userName;

    private String cnname;

    private String password;

    private String role;

    private boolean enabled;
}
