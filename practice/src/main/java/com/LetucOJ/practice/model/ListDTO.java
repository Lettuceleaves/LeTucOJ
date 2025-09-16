package com.LetucOJ.practice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListDTO {
    private String name;
    private String cnname;
    private String tags;
    private int difficulty;
    private int accepted;
}
