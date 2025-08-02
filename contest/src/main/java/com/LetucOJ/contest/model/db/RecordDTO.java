package com.LetucOJ.contest.model.db;

import lombok.Data;

@Data
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

    public RecordDTO() {}

    public RecordDTO(String userName, String cnname, String problemName, String language, String code) {
        this.userName = userName;
        this.cnname = cnname;
        this.problemName = problemName;
        this.language = language;
        this.code = code;
    }

    public RecordDTO(String name, String cnname, String name1, String c, String code, String s, long l, long l1, long l2) {
        this.userName = name;
        this.cnname = cnname;
        this.problemName = name1;
        this.language = c;
        this.code = code;
        this.result = s;
        this.timeUsed = l;
        this.memoryUsed = l1;
        this.submitTime = l2;
    }
}
