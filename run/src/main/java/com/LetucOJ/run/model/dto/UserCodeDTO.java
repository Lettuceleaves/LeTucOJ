package com.LetucOJ.run.model.dto;

import lombok.Data;

@Data
public class UserCodeDTO {
    private String userFile;
    private String problemName;
    private String language;

    public UserCodeDTO(String userFile, String problemName, String language) {
        this.userFile = userFile;
        this.problemName = problemName;
        this.language = language;
    }
}
