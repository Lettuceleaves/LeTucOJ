package com.LetucOJ.contest.model.db;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ContestInfoDTO {
    String name;
    String cnname;
    String mode;
    Timestamp start;
    Timestamp end;
    String note;
    boolean ispublic;

    public ContestInfoDTO() {
        this.name = null;
        this.cnname = null;
        this.mode = "add";
        this.start = null;
        this.end = null;
        this.note = null;
        this.ispublic = false;
    }

    public ContestInfoDTO(String name, String cnname, String mode, Timestamp start, Timestamp end, String note, boolean ispublic) {
        this.name = name;
        this.cnname = cnname;
        this.mode = mode;
        this.start = start;
        this.end = end;
        this.note = note;
        this.ispublic = ispublic;
    }
}
