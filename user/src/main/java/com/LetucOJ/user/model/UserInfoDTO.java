package com.LetucOJ.user.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    @TableField("user_name")
    private String userName;
    private String cnname;
    private String email;
    private String phone;
    private String description;
}
