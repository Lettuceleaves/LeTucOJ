package com.LetucOJ.sys.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileDTO implements Serializable {
    byte status; // 0: normal 1: not found 2: error
    String[] file;
    public enum fileType {
        INPUT,
        OUTPUT
    }

    public FileDTO(byte b, String s) {
        this.status = b;
        if (s == null) {
            this.file = new String[0];
        } else {
            this.file = new String[]{s};
        }
    }

    public FileDTO() {
    }
}
