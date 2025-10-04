package com.LetucOJ.contest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestInfoDTO {
    String name;
    String cnname;
    String mode;
    LocalDateTime start;
    LocalDateTime  end;
    boolean publicContest;
    String note;
}
