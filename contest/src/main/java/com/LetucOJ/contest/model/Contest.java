package com.LetucOJ.contest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contest {
    String contestName;
    String contestNickName;
    String mode;
    LocalDateTime start;
    LocalDateTime  end;
    boolean publicContest;
    String note;
    String password;
}
