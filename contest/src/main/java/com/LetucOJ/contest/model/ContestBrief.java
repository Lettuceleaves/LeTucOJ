package com.LetucOJ.contest.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ContestBrief {
    String contestName;
    String contestNickName;
    String mode;
    LocalDateTime start;
    LocalDateTime  end;
    boolean publicContest;
    String note;
}
