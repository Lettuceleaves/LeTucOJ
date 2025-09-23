package com.LetucOJ.contest.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordDTO {
    private String userName;
    private String cnname;
    private String problemName;
    private String language;
    private String code;
    private String result;
    private long timeUsed;
    private long memoryUsed;
    private long submitTime;
}
