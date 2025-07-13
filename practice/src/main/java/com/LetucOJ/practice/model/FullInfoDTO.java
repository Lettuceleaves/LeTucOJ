package com.LetucOJ.practice.model;

import lombok.Data;

import java.sql.Date;

@Data
public class FullInfoDTO {
        private String name;
        private String cnname;
        private Integer caseAmount;
        private Integer difficulty;
        private String tags;
        private String authors;
        private Date createTime;
        private Date updateAt;
        private String content;
        private Float freq;
        private Integer isPublic;
        private String solution;
        private Integer showSolution;

        public FullInfoDTO() {
            this.name = null;
            this.cnname = null;
            this.caseAmount = null;
            this.difficulty = null;
            this.tags = null;
            this.authors = null;
            this.createTime = null;
            this.updateAt = null;
            this.content = null;
            this.freq = null;
            this.isPublic = null;
            this.solution = null;
            this.showSolution = null;
        }

        public FullInfoDTO(String name, String cnname, int caseAmount, int difficulty, String tags, String authors,
                           Date createTime, Date updateAt, String content, Float freq, Integer isPublic,
                           String solution, Integer showSolution) {
            this.name = name;
            this.cnname = cnname;
            this.caseAmount = caseAmount;
            this.difficulty = difficulty;
            this.tags = tags;
            this.authors = authors;
            this.createTime = createTime;
            this.updateAt = updateAt;
            this.content = content;
            this.freq = freq;
            this.isPublic = isPublic;
            this.solution = solution;
            this.showSolution = showSolution;
        }
}

