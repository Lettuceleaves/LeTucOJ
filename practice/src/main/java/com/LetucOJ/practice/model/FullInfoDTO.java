package com.LetucOJ.practice.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullInfoDTO {
    private String name;
    private String cnname;
    private Integer caseAmount;
    private Integer difficulty;
    private String tags;
    private String authors;
    private Date createtime;
    private Date updateat;
    private String content;
    private Float freq;
    private Boolean publicProblem;
    private String solution;
    private Boolean showsolution;

    // JSON 序列化/反序列化
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** 将本对象转换为 JSON 字符串 */
    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("FullInfoDTO 序列化失败", e);
        }
    }

    /** 从 JSON 字符串反序列化为 FullInfoDTO 实例 */
    public static FullInfoDTO fromJson(String json) {
        try {
            return MAPPER.readValue(json, FullInfoDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("FullInfoDTO 反序列化失败", e);
        }
    }
}