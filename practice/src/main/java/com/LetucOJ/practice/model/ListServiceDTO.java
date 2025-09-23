package com.LetucOJ.practice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListServiceDTO {
    Long start;
    Long limit;
    String order;
    String like;
}
