package com.LetucOJ.common.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ResultVO<T> {
    public static final String SUCCESS_CODE = "0";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String code;
    private T data;
    private String message;
     private String taskId;

}
