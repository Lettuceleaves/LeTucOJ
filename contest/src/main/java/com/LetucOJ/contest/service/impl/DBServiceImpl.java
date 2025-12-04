package com.LetucOJ.contest.service.impl;

import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.ContestErrorCode;
import com.LetucOJ.contest.model.Contest;
import com.LetucOJ.contest.model.ContestBrief;
import com.LetucOJ.contest.model.DTO.*;
import com.LetucOJ.contest.model.ProblemBrief;
import com.LetucOJ.contest.model.Problem;
import com.LetucOJ.contest.model.VO.BoardVO;
import com.LetucOJ.contest.model.VO.ContestListVO;
import com.LetucOJ.contest.model.VO.ContestProblemListVO;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.DBService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Data
@AllArgsConstructor
public class DBServiceImpl implements DBService {

    private MybatisRepos mybatisRepos;

    @Override
    public ResultVO<ContestListVO> getContestList() {

        try {
            List<ContestBrief> list = mybatisRepos.getContestList();

            if (list == null || list.isEmpty()) {
                return Result.failure(ContestErrorCode.NO_CONTEST, null);
            }
            return Result.success(new ContestListVO(list));
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    @Override
    public ResultVO<ContestProblemListVO> getProblemList(String contestName, String role) {
        try {
            Contest contest = mybatisRepos.getContest(contestName);

            if (!contest.isPublicContest()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC, null);
            }

            // check time
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = contest.getStart();
            LocalDateTime end   = contest.getEnd();
            if (start != null && end != null) {
                if (now.isBefore(start)) {
                    return Result.failure(ContestErrorCode.CONTEST_NOT_START, null);
                } else if (now.isAfter(end)) {
                    return Result.failure(ContestErrorCode.CONTEST_FINISHED, null);
                }
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            }


            List<ProblemBrief> list = mybatisRepos.getProblemList(contestName);

            if (list == null || list.isEmpty()) {
                return Result.failure(ContestErrorCode.NO_PROBLEM_IN_CONTEST, null);
            }

            return Result.success(new ContestProblemListVO(list));
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    @Override
    public ResultVO<Problem> getProblem(String name, String contestName, String userName, String role) {
        try {

            ResultVO<Void> attended = attended(userName, contestName);
            if (!attended.getCode().equals("0")) {
                System.out.println(attended.getCode());
                return Result.failure(ContestErrorCode.USER_NOT_IN_CONTEST, null);
            }

            Contest dbDtoContest = mybatisRepos.getContest(contestName);

            if (!dbDtoContest.isPublicContest()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC, null);
            }

            Problem dbDto = mybatisRepos.getProblem(name);

            if (dbDto == null) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
            } else {
                dbDto.setSolution("题解已隐藏");
                return Result.success(dbDto);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }


    @Override
    public ResultVO<Void> attended(String userName, String contestName) {
        try {

            Integer inContest = mybatisRepos.getUserStatus(contestName, userName);

            if (inContest == null || inContest == 0) {
                return Result.failure(ContestErrorCode.USER_NOT_IN_CONTEST);
            } else {
                return Result.success();
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO<BoardVO> getBoard(String contestName, String role) {

        try {

            List<BoardDTO> boardDbDto = mybatisRepos.getBoard(contestName);

            ProblemStatusDTO statusDbDto = mybatisRepos.getStatus(contestName);

            if (boardDbDto == null || boardDbDto.isEmpty()) {
                return Result.failure(ContestErrorCode.EMPTY_BOARD, null);
            } else if (!statusDbDto.isIspublic()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC, null);
            } else {
                return Result.success(new BoardVO(boardDbDto, 0, 0));
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    @Override
    public ResultVO<Contest> getContest(String ctname, String role) {

        try {

            Contest dbDto = mybatisRepos.getContest(ctname);

            if (dbDto == null) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_EXIST, null);
            } else if (!dbDto.isPublicContest()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC, null);
            } else {
                return Result.success(dbDto);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR, null);
        }
    }

    @Override
    public ResultVO<Void> insertContest(Contest dto) {

        try {
            if (dto == null) {
                return Result.failure(BaseErrorCode.CLIENT_ERROR);
            }
            ResultVO<Void> response;
            try {
                Integer rows = mybatisRepos.insertContest(dto);
                if (rows != null && rows > 0) {
                    return Result.success();
                } else {
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
            } catch (Exception e) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO<Void> updateContest(Contest dto) {
        try {
            try {
                System.out.println(dto);
                Integer rows = mybatisRepos.updateContest(dto);

                if (rows != null && rows > 0) {
                    return Result.success();
                } else {
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
            } catch (Exception e) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO<Void> insertProblem(ContestProblemDTO dto) {
        try {
            if (dto == null) {
                return Result.failure(ContestErrorCode.EMPTY_DATA);
            } else if (dto.getScore() < 0) {
                return Result.failure(ContestErrorCode.INVALID_PARAM);
            }

            Integer check = mybatisRepos.problemExist(dto.getProblemName());

            if (check == null || check == 0) {
                return Result.failure(ContestErrorCode.INVALID_PARAM);
            }

            try {
                Integer rows = mybatisRepos.insertProblem(dto);
                if (rows != null && rows > 0) {
                    return Result.success();
                } else {
                    return Result.failure(BaseErrorCode.PROBLEM_NOT_EXIST);
                }
            } catch (Exception e) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO<Void> deleteProblem(ContestProblemDTO dto) {
        try {
            if (dto == null) {
                return Result.failure(ContestErrorCode.EMPTY_DATA);
            }
            try {
                Integer rows = mybatisRepos.deleteProblem(dto.getContestName(), dto.getProblemName());

                if (rows != null && rows == 1) {
                    return Result.success();
                } else {
                    return Result.failure(BaseErrorCode.SERVICE_ERROR);
                }
            } catch (Exception e) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO<Void> attend(String name, String cnname, String contestName) {
        try {
            Contest dbDtoContest = mybatisRepos.getContest(contestName);

            if (!dbDtoContest.isPublicContest()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC);
            }
            if (name == null || name.isEmpty()) {
                return Result.failure(ContestErrorCode.EMPTY_DATA);
            }
            Integer rows = mybatisRepos.insertContestUser(contestName, name, cnname);
            if (rows != null && rows > 0) {
                return Result.success();
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }
}
