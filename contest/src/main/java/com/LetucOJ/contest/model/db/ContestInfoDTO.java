package com.LetucOJ.contest.model.db;

import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class ContestInfoDTO {
    String name;
    String cnname;
    String mode;
    LocalDateTime start;
    LocalDateTime  end;
    boolean publicContest;
    String note;
}
