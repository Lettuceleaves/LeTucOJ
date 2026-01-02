package com.LetucOJ.practice.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListConditionDTO {
    Long start;
    Long limit;
    String order;
    String like;
    Boolean onlyPublic;
}
