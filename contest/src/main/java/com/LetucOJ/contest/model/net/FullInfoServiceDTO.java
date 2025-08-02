package com.LetucOJ.contest.model.net;

import com.LetucOJ.contest.model.db.FullInfoDTO;
import lombok.Data;

@Data
public class FullInfoServiceDTO {
    String name;
    String cnname;
    Long limit;
    FullInfoDTO data;

    public FullInfoServiceDTO() {
        this.name = null;
        this.cnname = null;
        this.limit = null;
        this.data = null;
    }

    public FullInfoServiceDTO(String name, String cnname, Long limit, FullInfoDTO data) {
        this.name = name;
        this.cnname = null;
        this.limit = limit;
        this.data = data;
    }
}
