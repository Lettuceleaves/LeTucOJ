package com.LetucOJ.practice.model;

import lombok.Data;

@Data
public class ListServiceDTO {
    Long start;
    Long limit;
    String order;
    String like;

    public ListServiceDTO() {
        this.start = null;
        this.limit = null;
        this.order = "id";
        this.like = "";
    }

    public ListServiceDTO(Long start, Long limit, String order, String like) {
        this.start = start;
        this.limit = limit;
        this.order = order;
        this.like = like;
    }
}
