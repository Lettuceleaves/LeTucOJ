package com.LetucOJ.sys.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LogListVO {
    List<Log> logList;
    int total;
}
