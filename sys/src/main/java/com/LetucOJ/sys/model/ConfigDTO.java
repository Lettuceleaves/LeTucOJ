package com.LetucOJ.sys.model;

import lombok.Data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class ConfigDTO {
    private String label;
    private Integer C;
    private Integer CPP;
    private Integer Python;
    private Integer JAVA;

    public List<List<String>> getConfigList() {
        List<List<String>> configList = new ArrayList<>();
        System.out.println(label + ": " + C + ", " + CPP + ", " + Python + ", " + JAVA);
        if (label == null || label.isEmpty()) {
            label = "Default";
        }
        if (C == null || C <= 0) {
            C = 0;
        }
        if (CPP == null || CPP <= 0) {
            CPP = 0;
        }
        if (Python == null || Python <= 0) {
            Python = 0;
        }
        if (JAVA == null || JAVA <= 0) {
            JAVA = 0;
        }
        configList.add(Arrays.asList("C", String.valueOf(C)));
        configList.add(Arrays.asList("C++", String.valueOf(CPP)));
        configList.add(Arrays.asList("Python", String.valueOf(Python)));
        configList.add(Arrays.asList("Java", String.valueOf(JAVA)));
        return configList;
    }
}
