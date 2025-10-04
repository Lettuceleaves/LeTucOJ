package com.LetucOJ.common.mq.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private String topic;          // 主题
    private String tag;            // 标签
    private String key;            // 业务主键
    private String body;           // 消息体（JSON 字符串）
    private String timestamp;        // 消息时间戳
    private Integer delayLevel;    // 延迟等级（RocketMQ）
}

