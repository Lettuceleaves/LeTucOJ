package com.LetucOJ.contest.model.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.sql.Date;

@Data
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
    private Boolean ispublic;
    private String solution;
    private Boolean showsolution;

    public FullInfoDTO() {
        this.name = null;
        this.cnname = null;
        this.caseAmount = null;
        this.difficulty = null;
        this.tags = null;
        this.authors = null;
        this.createtime = null;
        this.updateat = null;
        this.content = null;
        this.freq = null;
        this.solution = null;
        this.ispublic = null;
        this.showsolution = null;
    }

    public FullInfoDTO(String name, String cnname, int caseAmount, int difficulty, String tags, String authors,
                       Date createtime, Date updateat, String content, Float freq, Boolean ispublic,
                       String solution, Boolean showsolution) {
        this.name = name;
        this.cnname = cnname;
        this.caseAmount = caseAmount;
        this.difficulty = difficulty;
        this.tags = tags;
        this.authors = authors;
        this.createtime = createtime;
        this.updateat = updateat;
        this.content = content;
        this.freq = freq;
        this.ispublic = ispublic;
        this.solution = solution;
        this.showsolution = showsolution;
    }

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