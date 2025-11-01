package com.LetucOJ.practice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CasePairDTO {
    private String name;
    private String input;
    private String output;
    private byte[] config;
}
