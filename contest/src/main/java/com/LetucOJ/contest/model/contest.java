package com.LetucOJ.contest.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class contest {
    private int id; // 自增id TODO
    private String name;

    @TableField("organizer_name")
    private String organizerName;

    @TableField("start_time")
    private Timestamp startTime;

    @TableField("end_time")
    private Timestamp endTime;

    private String description;
}