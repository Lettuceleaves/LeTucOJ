package com.LetucOJ.practice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)  // 序列化时忽略 null 字段
public class ListDTO {
    private String name;
    private String cnname;

    public ListDTO(String name, String cnname) {
        this.name = name;
        this.cnname = cnname;
    }

    public ListDTO() {
        this.name = null;
        this.cnname = null;
    }

    // JSON 序列化/反序列化
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** 将本对象转换为 JSON 字符串 */
    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("BasicInfoDTO 序列化失败", e);
        }
    }

    /** 从 JSON 字符串反序列化为 BasicInfoDTO 实例 */
    public static ListDTO fromJson(String json) {
        try {
            return MAPPER.readValue(json, ListDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("BasicInfoDTO 反序列化失败", e);
        }
    }
}
