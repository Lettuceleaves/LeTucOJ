package com.LetucOJ.practice.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitRecord {
    private String userName;
    private String nickName;
    private String problemName;
    private String language;
    private String code;
    private String result;
    private long timeUsed;
    private long memoryUsed;
    private long submitTime;
}
