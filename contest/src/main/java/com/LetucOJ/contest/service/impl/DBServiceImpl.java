package com.LetucOJ.contest.service.impl;

import com.LetucOJ.contest.client.RunClient;
import com.LetucOJ.contest.model.db.BoardDTO;
import com.LetucOJ.contest.model.db.ContestInfoDTO;
import com.LetucOJ.contest.model.db.FullInfoDTO;
import com.LetucOJ.contest.model.db.ProblemStatusDTO;
import com.LetucOJ.contest.model.net.*;
import com.LetucOJ.contest.repos.MinioRepos;
import com.LetucOJ.contest.repos.MybatisRepos;
import com.LetucOJ.contest.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DBServiceImpl implements DBService {

    @Autowired
    private MybatisRepos mybatisRepos;

    @Autowired
    private MinioRepos minioRepos;

    @Autowired
    private RunClient runClient;

    @Override
    public ResultVO getContestList() {

        try {
            List<ContestInfoDTO> list = mybatisRepos.getContestList();

            if (list == null || list.isEmpty()) {
                return new ResultVO((byte)1, null, "practice/getContestList: No problems found in Mybatis");
            }
            return new ResultVO((byte)0, list, null);
        } catch (Exception e) {
            return new ResultVO((byte)5, null, "practice/getContestList: Error retrieving contest list from Mybatis: " + e.getMessage());
        }
    }

    @Override
    public ResultVO getProblemList(String contestName) {
        try {
            ContestInfoDTO dbDtoContest = mybatisRepos.getContest(contestName);

            if (!dbDtoContest.isPublicContest()) {
                return new ResultVO((byte) 5, null, "practice/getProblemList: Not Public");
            }

            // check time
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = dbDtoContest.getStart();
            LocalDateTime end   = dbDtoContest.getEnd();
            if (start != null && end != null) {
                if (now.isBefore(start)) {
                    long secondsToStart = Duration.between(now, start).getSeconds();
                    return new ResultVO((byte)5, null,
                            "practice/submit: Contest has not started yet, start in "
                                    + secondsToStart + " seconds");
                } else if (now.isAfter(end)) {
                    return new ResultVO((byte)5, null, "practice/submit: Contest has already ended");
                }
            } else {
                return new ResultVO((byte)5, null, "practice/submit: Contest start or end time is not set");
            }


            List<ContestProblemDTO> list = mybatisRepos.getProblemList(contestName);

            if (list == null || list.isEmpty()) {
                return new ResultVO((byte) 1, null, "practice/getProblemList: No problems found in Mybatis");
            }

            return new ResultVO((byte) 0, list, null);
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/getProblemList: Error retrieving problem list from Mybatis: " + e.getMessage());
        }
    }

    @Override
    public ResultVO getProblemListInRoot(String contestName) {
        try {
            List<ContestProblemDTO> list = mybatisRepos.getProblemList(contestName);

            if (list == null || list.isEmpty()) {
                return new ResultVO((byte) 1, null, "practice/getProblemList: No problems found in Mybatis");
            }

            return new ResultVO((byte) 0, list, null);
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/getProblemList: Error retrieving problem list from Mybatis: " + e.getMessage());
        }
    }

    @Override
    public ResultVO getProblem(String name, String contestName) {
        try {
            ContestInfoDTO dbDtoContest = mybatisRepos.getContest(contestName);

            if (!dbDtoContest.isPublicContest()) {
                return new ResultVO((byte) 5, null, "practice/getProblem: Not Public");
            }

            FullInfoDTO dbDto = mybatisRepos.getProblem(name);

            if (dbDto == null) {
                return new ResultVO((byte) 5, null, "practice/getProblem: Problem not found in Mybatis");
            } else {
                dbDto.setSolution("题解已隐藏");
                return new ResultVO((byte) 0, dbDto, null);
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/getProblem: Error retrieving problem from Mybatis: " + e.getMessage());
        }
    }
    @Override
    public ResultVO getProblemInRoot(String name, String contestName) {
        try {
            FullInfoDTO dbDto = mybatisRepos.getProblem(name);

            if (dbDto == null) {
                return new ResultVO((byte) 5, null, "practice/getProblem: Problem not found in Mybatis");
            } else {
                dbDto.setSolution("题解已隐藏");
                return new ResultVO((byte) 0, dbDto, null);
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/getProblem: Error retrieving problem from Mybatis: " + e.getMessage());
        }
    }


    @Override
    public ResultVO getUserStatus(String name, String contestName) {
        try {
            if (name == null || name.isEmpty()) {
                return new ResultVO((byte)5, null, "contest/getUserStatus: userName is null or empty");
            }

            Integer inContest = mybatisRepos.getUserStatus(name, contestName);

            if (inContest == null) {
                return new ResultVO((byte)5, null, "contest/getUserStatus: User not found in contest");
            } else if (inContest == 0) {
                return new ResultVO((byte)1, null, "contest/getUserStatus: User has not joined the contest");
            } else {
                return new ResultVO((byte)0, inContest, null);
            }
        } catch (Exception e) {
            return new ResultVO((byte)5, null, "contest/getUserStatus: Error retrieving user status from Mybatis: " + e.getMessage());
        }
    }

    @Override
    public ResultVO getBoard(String contestName) {

        try {

            List<BoardDTO> boardDbDto = mybatisRepos.getBoard(contestName);

            ProblemStatusDTO statusDbDto = mybatisRepos.getStatus(contestName);

            if (boardDbDto == null || boardDbDto.isEmpty()) {
                return new ResultVO((byte) 1, null, "contest/getBoard: Board not found in Mybatis");
            } else if (!statusDbDto.isIspublic()) {
                return new ResultVO((byte) 2, null, "contest/getBoard: Not Public");
            } else {
                return new ResultVO((byte) 0, boardDbDto, null);
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "contest/getBoard: Error retrieving board from Mybatis: " + e.getMessage());
        }
    }

    @Override
    public ResultVO getBoardInRoot(String contestName) {

        try {

            List<BoardDTO> boardDbDto = mybatisRepos.getBoard(contestName);

            ProblemStatusDTO statusDbDto = mybatisRepos.getStatus(contestName);

            if (boardDbDto == null || boardDbDto.isEmpty()) {
                return new ResultVO((byte) 1, null, "contest/getBoard: Board not found in Mybatis");
            } else {
                return new ResultVO((byte) 0, boardDbDto, null);
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "contest/getBoard: Error retrieving board from Mybatis: " + e.getMessage());
        }
    }

    @Override
    public ResultVO getContest(String ctname) {

        try {

            ContestInfoDTO dbDto = mybatisRepos.getContest(ctname);

            if (dbDto == null) {
                return new ResultVO((byte) 5, null, "contest/getContest: Contest not found in Mybatis");
            } else if (!dbDto.isPublicContest()) {
                return new ResultVO((byte) 5, null, "contest/getContest: Not Public");
            } else {
                return new ResultVO((byte) 0, dbDto, null);
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "contest/getContest: Error retrieving contest from Mybatis: " + e.getMessage());
        }
    }

    @Override
    public ResultVO getContestInRoot(String ctname) {

        try {

            ContestInfoDTO dbDto = mybatisRepos.getContest(ctname);

            System.out.println(dbDto);

            if (dbDto == null) {
                return new ResultVO((byte) 5, null, "contest/getContest: Contest not found in Mybatis");
            } else {
                return new ResultVO((byte) 0, dbDto, null);
            }
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "contest/getContest: Error retrieving contest from Mybatis: " + e.getMessage());
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
                    response = new ResultVO(0, null, null);
                } else {
                    response = new ResultVO(5, null, "practice/insertContest: Mybatis return not >=0 or is null");
                }
            } catch (Exception e) {
                return new ResultVO(5, null, "practice/insertContest: " + e.getMessage());
            }
            return response;
        } catch (Exception e) {
            return new ResultVO((byte)5, null, "practice/insertContest: Error inserting contest into Mybatis: " + e.getMessage());
        }
    }

    @Override
    public ResultVO updateContest(ContestInfoDTO dto) {
        try {
            if (dto == null) {
                return new ResultVO();
            }
            ResultVO response;
            try {
                System.out.println(dto);
                Integer rows = mybatisRepos.updateContest(dto);

                if (rows != null && rows > 0) {
                    response = new ResultVO((byte) 0, null, null);
                } else {
                    response = new ResultVO((byte) 5, null, "practice/updateContest: Mybatis return not >=0 or is null");
                }
            } catch (Exception e) {
                return new ResultVO((byte) 5, null, "practice/updateContest: " + e.getMessage());
            }
            return response;
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/updateContest: Error updating contest in Mybatis: " + e.getMessage());
        }
    }

    @Override
    public ResultVO deleteContest(String ctname) {
        return new ResultVO((byte)5, null, "practice/deleteContest: Delete operation is not supported yet");
    }

    @Override
    public ResultVO insertProblem(ContestProblemDTO dto) {
        try {
            if (dto == null) {
                return new ResultVO(5, null, "practice/insertProblem: DTO is null");
            } else if (dto.getScore() <= 0) {
                return new ResultVO(5, null, "practice/insertProblem: Score not available");
            }

            Integer check = mybatisRepos.problemExist(dto.getProblemName());

            if (check == null || check == 0) {
                return new ResultVO(1, null, "practice/insertProblem: Problem does not exist in Mybatis");
            }

            ResultVO response;

            try {
                Integer rows = mybatisRepos.insertProblem(dto);
                if (rows != null && rows > 0) {
                    response = new ResultVO(0, null, null);
                } else {
                    response = new ResultVO(5, null, "practice/insertProblem: Mybatis return not >=0 or is null");
                }
            } catch (Exception e) {
                return new ResultVO(5, null, "practice/insertProblem: " + e.getMessage());
            }
            return response;
        } catch (Exception e) {
            return new ResultVO((byte)5, null, "practice/insertProblem: Error inserting problem into Mybatis: " + e.getMessage());
        }
    }

    @Override
    public ResultVO deleteProblem(ContestProblemDTO dto) {
        try {
            if (dto == null) {
                return new ResultVO(5, null, "practice/deleteProblem: DTO is null");
            }

            ResultVO response;
            try {
                Integer rows = mybatisRepos.deleteProblem(dto.getContestName(), dto.getProblemName());

                if (rows != null && rows == 1) {
                    response = new ResultVO((byte) 0, null, null);
                } else {
                    response = new ResultVO((byte) 5, null, "practice/deleteProblem: Mybatis return not >=0 or is null");
                }
            } catch (Exception e) {
                return new ResultVO((byte) 5, null, "practice/deleteProblem: " + e.getMessage());
            }
            return response;
        } catch (Exception e) {
            return new ResultVO((byte) 5, null, "practice/deleteProblem: Error deleting problem from Mybatis: " + e.getMessage());
        }
    }

    @Override
    public ResultVO attend(String name, String cnname, String contestName) {
        try {
            if (name == null || name.isEmpty()) {
                return new ResultVO((byte)5, null, "practice/attend: userName is null or empty");
            }
            // 调用新加的 Mapper 接口
            Integer rows = mybatisRepos.insertContestUser(contestName, name, cnname);
            if (rows != null && rows > 0) {
                return new ResultVO((byte)0, null, null);
            } else {
                return new ResultVO((byte)5, null, "practice/attend: failed to register user");
            }
        } catch (Exception e) {
            return new ResultVO((byte)5, null, "practice/attend: " + e.getMessage());
        }
    }
}
