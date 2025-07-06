package com.LetucOJ.auth.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
public class user {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @TableField("user_name")
    private String userName;

    private String password;

    private String role;
}
