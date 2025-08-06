package com.LetucOJ.sys.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class ResultVO {
    private int status; // 0: success, 1: fail, 2: error
    private Object data;
    private String error;

    public ResultVO() {}

    public ResultVO(int status, Object data, String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    // —— 静态工厂方法 ——
    public static ResultVO success(Object data) {
        return new ResultVO(0, data, null);
    }
    public static ResultVO fail(String errorMsg) {
        return new ResultVO(1, null, errorMsg);
    }
    public static ResultVO error(String errorMsg) {
        return new ResultVO(2, null, errorMsg);
    }

    // —— JSON 序列化/反序列化 ——
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** 将本对象转成 JSON 字符串 */
    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("ResultVO 序列化失败", e);
        }
    }

    /** 从 JSON 字符串反序列化出一个 ResultVO 实例 */
    public static ResultVO fromJson(String json) {
        try {
            return MAPPER.readValue(json, ResultVO.class);
        } catch (Exception e) {
            throw new RuntimeException("ResultVO 反序列化失败", e);
        }
    }
}
