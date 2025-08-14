package com.LetucOJ.contest.model.method;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckDTO {
    private int status; // 0: pass 1: fail 2: error
    private long caseIndex;
    private String message;
}
