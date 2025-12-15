package com.LetucOJ.sys.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Log {
    String logId;
    String traceId;
    String content;
}
