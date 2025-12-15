package com.LetucOJ.contest.model.DTO;

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
    private long useTime;
    private long useMemory;
    private long submitTime;
}
