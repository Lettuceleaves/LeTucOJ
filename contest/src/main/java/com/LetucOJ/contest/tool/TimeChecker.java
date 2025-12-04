package com.LetucOJ.contest.tool;

import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.ContestErrorCode;
import com.LetucOJ.contest.model.Contest;
import com.LetucOJ.contest.model.VO.TestTaskVO;
import org.bouncycastle.crypto.agreement.kdf.ConcatenationKDFGenerator;

import java.time.LocalDateTime;

public class TimeChecker {
    public static ResultVO<TestTaskVO> checkTime(Contest contest){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = contest.getStart();
        LocalDateTime end = contest.getEnd();
        if (start != null && end != null) {
            if (now.isBefore(start)) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_START, null);
            } else if (now.isAfter(end)) {
                return Result.failure(ContestErrorCode.CONTEST_FINISHED, null);
            }
        } else {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
        return Result.success(null);
    }
}
