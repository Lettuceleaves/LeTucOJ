package com.LetucOJ.practice.model;

import java.sql.Date;

public class FullInfoDTO {
        private String id;
        private String name;
        private int caseAmount;
        private int difficulty;
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
            this.id = "";
            this.name = "";
            this.caseAmount = 0;
            this.difficulty = 0;
            this.tags = "";
            this.authors = "";
            this.createTime = null;
            this.updateAt = null;
            this.content = "";
            this.freq = 0.0f;
            this.isPublic = 0;
            this.solution = "";
            this.showSolution = 0;
        }

        public FullInfoDTO(String id, String name, int caseAmount, int difficulty, String tags, String authors,
                           Date createTime, Date updateAt, String content, Float freq, Integer isPublic,
                           String solution, Integer showSolution) {
            this.id = id;
            this.name = name;
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

