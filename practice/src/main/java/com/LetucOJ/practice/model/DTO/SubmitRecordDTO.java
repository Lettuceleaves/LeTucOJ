package com.LetucOJ.practice.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitRecordDTO {
    private String traceId;
    private String userName;
    private String userNickName;
    private String problemName;
    private String language;
    private String code;
    private String result;
    private long timeUsed;
    private long memoryUsed;
    private long submitTime;
}
