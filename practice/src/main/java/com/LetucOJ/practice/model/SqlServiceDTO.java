package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class SqlServiceDTO {
    byte type; // 0: insert, 1: update, 2: delete, 3: select
    String id;
    Long limit;
}