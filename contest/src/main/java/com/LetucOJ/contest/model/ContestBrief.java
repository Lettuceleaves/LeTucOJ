package com.LetucOJ.contest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ContestBrief {
    String name;
    String cnname;
    String mode;
    LocalDateTime start;
    LocalDateTime  end;
    boolean publicContest;
    String note;
}
