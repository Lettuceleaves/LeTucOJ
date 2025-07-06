package com.LetucOJ.contest.model;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class problemlist {

    @TableField("contest_id")
    private int contestId;
    @TableField("problem_id")
    private int problemId;

    public problemlist(int contestId, int problemId) {
        this.contestId = contestId;
        this.problemId = problemId;
    }
}