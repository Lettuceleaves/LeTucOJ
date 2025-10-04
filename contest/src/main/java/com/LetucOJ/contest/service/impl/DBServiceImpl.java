package com.LetucOJ.contest.service.impl;

import com.LetucOJ.common.result.Result;
import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.common.result.errorcode.BaseErrorCode;
import com.LetucOJ.common.result.errorcode.ContestErrorCode;
import com.LetucOJ.contest.model.ContestProblemDTO;
import com.LetucOJ.contest.model.BoardDTO;
import com.LetucOJ.contest.model.ContestInfoDTO;
import com.LetucOJ.contest.model.FullInfoDTO;
import com.LetucOJ.contest.model.ProblemStatusDTO;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DBServiceImpl implements DBService {

    @Autowired
    private MybatisRepos mybatisRepos;

    @Override
    public ResultVO getContestList() {

        try {
            List<ContestInfoDTO> list = mybatisRepos.getContestList();

            if (list == null || list.isEmpty()) {
                return Result.failure(ContestErrorCode.NO_CONTEST);
            }
            return Result.success(list);
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getProblemList(String contestName) {
        try {
            ContestInfoDTO dbDtoContest = mybatisRepos.getContest(contestName);

            if (!dbDtoContest.isPublicContest()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC);
            }

            // check time
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = dbDtoContest.getStart();
            LocalDateTime end   = dbDtoContest.getEnd();
            if (start != null && end != null) {
                if (now.isBefore(start)) {
                    long secondsToStart = Duration.between(now, start).getSeconds();
                    return Result.failure(ContestErrorCode.CONTEST_NOT_START, secondsToStart);
                } else if (now.isAfter(end)) {
                    return Result.failure(ContestErrorCode.CONTEST_FINISHED);
                }
            } else {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            }


            List<ContestProblemDTO> list = mybatisRepos.getProblemList(contestName);

            if (list == null || list.isEmpty()) {
                return Result.failure(ContestErrorCode.NO_PROBLEM_IN_CONTEST);
            }

            return Result.success(list);
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getProblemListInRoot(String contestName) {
        try {
            List<ContestProblemDTO> list = mybatisRepos.getProblemList(contestName);

            if (list == null || list.isEmpty()) {
                return Result.failure(ContestErrorCode.NO_PROBLEM_IN_CONTEST);
            }

            return Result.success(list);
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getProblem(String name, String contestName) {
        try {
            ContestInfoDTO dbDtoContest = mybatisRepos.getContest(contestName);

            if (!dbDtoContest.isPublicContest()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC);
            }

            FullInfoDTO dbDto = mybatisRepos.getProblem(name);

            if (dbDto == null) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            } else {
                dbDto.setSolution("题解已隐藏");
                return Result.success(dbDto);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }
    @Override
    public ResultVO getProblemInRoot(String name, String contestName) {
        try {
            FullInfoDTO dbDto = mybatisRepos.getProblem(name);

            if (dbDto == null) {
                return Result.failure(BaseErrorCode.SERVICE_ERROR);
            } else {
                dbDto.setSolution("题解已隐藏");
                return Result.success(dbDto);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }


    @Override
    public ResultVO getUserStatus(String name, String contestName) {
        try {

            Integer inContest = mybatisRepos.getUserStatus(name, contestName);

            if (inContest == null || inContest == 0) {
                return Result.failure(ContestErrorCode.USER_NOT_IN_CONTEST);
            } else {
                return Result.success(inContest);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getBoard(String contestName) {

        try {

            List<BoardDTO> boardDbDto = mybatisRepos.getBoard(contestName);

            ProblemStatusDTO statusDbDto = mybatisRepos.getStatus(contestName);

            if (boardDbDto == null || boardDbDto.isEmpty()) {
                return Result.failure(ContestErrorCode.EMPTY_BOARD);
            } else if (!statusDbDto.isIspublic()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC);
            } else {
                return Result.success(boardDbDto);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getBoardInRoot(String contestName) {

        try {

            List<BoardDTO> boardDbDto = mybatisRepos.getBoard(contestName);

            ProblemStatusDTO statusDbDto = mybatisRepos.getStatus(contestName);

            if (boardDbDto == null || boardDbDto.isEmpty()) {
                return Result.failure(ContestErrorCode.EMPTY_BOARD);
            } else {
                return Result.success(boardDbDto);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getContest(String ctname) {

        try {

            ContestInfoDTO dbDto = mybatisRepos.getContest(ctname);

            if (dbDto == null) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_EXIST);
            } else if (!dbDto.isPublicContest()) {
                return Result.failure(ContestErrorCode.CONTEST_NOT_PUBLIC);
            } else {
                return Result.success(dbDto);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO getContestInRoot(String ctname) {

        try {

            ContestInfoDTO dbDto = mybatisRepos.getContest(ctname);

            System.out.println(dbDto);

            if (dbDto == null) {
                return Result.failure(ContestErrorCode.NO_CONTEST);
            } else {
                return Result.success(dbDto);
            }
        } catch (Exception e) {
            return Result.failure(BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Override
    public ResultVO insertContest(ContestInfoDTO dto) {

        try {
            if (dto == null) {
                return new ResultVO();
            }
            ResultVO response;
            try {
                Integer rows = mybatisRepos.insertContest(dto);
                if (rows != null && rows > 0) {
                    return Result.success(rows);
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
    public ResultVO updateContest(ContestInfoDTO dto) {
        try {
            try {
                System.out.println(dto);
                Integer rows = mybatisRepos.updateContest(dto);

                if (rows != null && rows > 0) {
                    return Result.success(rows);
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
    public ResultVO insertProblem(ContestProblemDTO dto) {
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
    public ResultVO deleteProblem(ContestProblemDTO dto) {
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
    public ResultVO attend(String name, String cnname, String contestName) {
        try {
            ContestInfoDTO dbDtoContest = mybatisRepos.getContest(contestName);

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
