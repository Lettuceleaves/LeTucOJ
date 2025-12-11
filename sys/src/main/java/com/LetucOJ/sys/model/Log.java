package com.LetucOJ.sys.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Log {
    String id;
    String traceId;
    String content;
}
