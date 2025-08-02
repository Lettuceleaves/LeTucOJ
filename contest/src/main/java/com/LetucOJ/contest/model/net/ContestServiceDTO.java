package com.LetucOJ.contest.model.net;

import com.LetucOJ.contest.model.db.ContestInfoDTO;
import lombok.Data;

@Data
public class ContestServiceDTO {
    String name;
    ContestInfoDTO data;
}
