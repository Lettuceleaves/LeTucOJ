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

        List<ContestInfoDTO> list = mybatisRepos.getContestList();

        if (list == null || list.isEmpty()) {
            return new ResultVO((byte)5, null, "practice/getContestList: No problems found in Mybatis");
        }

        return new ResultVO((byte)0, list, null);
    }

    @Override
    public ResultVO getProblemList(String contestName) {
        ContestInfoDTO dbDtoContest = mybatisRepos.getContest(contestName);

        if (!dbDtoContest.isIspublic()) {
            return new ResultVO((byte)5, null, "practice/getProblem: Not Public");
        }

        // check time
        if (dbDtoContest.getStart() != null && dbDtoContest.getEnd() != null) {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (currentTime.before(dbDtoContest.getStart())) {
                return new ResultVO((byte) 5, null, "practice/submit: Contest has not started yet, start in "
                        + (dbDtoContest.getStart().getTime() - currentTime.getTime()) / 1000 + " seconds");
            } else if (currentTime.after(dbDtoContest.getEnd())) {
                return new ResultVO((byte) 5, null, "practice/submit: Contest has already ended");
            }
        } else {
            return new ResultVO((byte) 5, null, "practice/submit: Contest start or end time is not set");
        }


        List<ContestProblemDTO> list = mybatisRepos.getProblemList(contestName);

        if (list == null || list.isEmpty()) {
            return new ResultVO((byte)5, null, "practice/getProblemList: No problems found in Mybatis");
        }

        return new ResultVO((byte)0, list, null);
    }

    @Override
    public ResultVO getProblem(FullInfoServiceDTO dto, String contestName) {
        ContestInfoDTO dbDtoContest = mybatisRepos.getContest(contestName);

        if (!dbDtoContest.isIspublic()) {
            return new ResultVO((byte)5, null, "practice/getProblem: Not Public");
        }

        FullInfoDTO dbDto = mybatisRepos.getProblem(dto.getName());

        if (dbDto == null) {
            return new ResultVO((byte)5, null, "practice/getProblem: Problem not found in Mybatis");
        } else {
            dbDto.setSolution("题解已隐藏");
            return new ResultVO((byte)0, dbDto, null);
        }
    }

    @Override
    public ResultVO getBoard(String contestName) {

        List<BoardDTO> boardDbDto = mybatisRepos.getBoard(contestName);

        ProblemStatusDTO statusDbDto = mybatisRepos.getStatus(contestName);

        if (statusDbDto == null || boardDbDto == null) {
            return new ResultVO((byte)5, null, "contest/getBoard: Board not found in Mybatis");
        } else if (!statusDbDto.isIspublic()) {
            return new ResultVO((byte)5, null, "contest/getBoard: Not Public");
        } else {
            return new ResultVO((byte)0, boardDbDto, null);
        }
    }

    @Override
    public ResultVO getContest(ContestServiceDTO dto) {

        ContestInfoDTO dbDto = mybatisRepos.getContest(dto.getName());

        if (dbDto == null) {
            return new ResultVO((byte)5, null, "contest/getContest: Contest not found in Mybatis");
        } else if (!dbDto.isIspublic()){
            return new ResultVO((byte)5, null, "contest/getContest: Not Public");
        } else {
            return new ResultVO((byte)0, dbDto, null);
        }
    }

    @Override
    public ResultVO insertContest(ContestServiceDTO dto) {
        if (dto.getData() == null) {
            return new ResultVO();
        }
        ResultVO response;
        try {
            Integer rows = mybatisRepos.insertContest(dto.getData());
            if (rows != null && rows > 0) {
                response = new ResultVO(0, null, null);
            } else {
                response = new ResultVO(5, null, "practice/insertContest: Mybatis return not >=0 or is null");
            }
        } catch (Exception e) {
            return new ResultVO(5, null, "practice/insertContest: " + e.getMessage());
        }
        return response;
    }

    @Override
    public ResultVO updateContest(ContestServiceDTO dto) {
        if (dto.getData() == null) {
            return new ResultVO();
        }
        ResultVO response;
        try {
            Integer rows = mybatisRepos.updateContest(dto.getData());

            if (rows != null && rows > 0) {
                response = new ResultVO((byte)0, null, null);
            } else {
                response = new ResultVO((byte)5, null, "practice/updateContest: Mybatis return not >=0 or is null");
            }
        } catch (Exception e) {
            return new ResultVO((byte)5, null, "practice/updateContest: " + e.getMessage());
        }
        return response;
    }

    @Override
    public ResultVO deleteContest(ContestServiceDTO dto) {
//        ResultVO response;
//        try {
//            Integer rows = mybatisRepos.deleteProblem(dto.getName());
//
//            if (rows != null && rows > 0) {
//                minioRepos.deleteFile(dto.getName());
//                response = new ResultVO((byte)0, null, null);
//            } else {
//                response = new ResultVO((byte)5, null, "practice/deleteProblem: Mybatis return not >=0 or is null");
//            }
//        } catch (Exception e) {
//            return new ResultVO((byte)0, null, e.getMessage());
//        }
//        return response;
        return new ResultVO((byte)5, null, "practice/deleteContest: Delete operation is not supported yet");
    }

    @Override
    public ResultVO insertProblem(ContestProblemDTO dto) {
        if (dto == null) {
            return new ResultVO(5, null, "practice/insertProblem: DTO is null");
        } else if (dto.getScore() <= 0) {
            return new ResultVO(5, null, "practice/insertProblem: Score not available");
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
    }

    @Override
    public ResultVO deleteProblem(ContestProblemDTO dto) {
        if (dto == null) {
            return new ResultVO(5, null, "practice/deleteProblem: DTO is null");
        }

        ResultVO response;
        try {
            Integer rows = mybatisRepos.deleteProblem(dto);

            if (rows != null && rows == 1) {
                response = new ResultVO((byte)0, null, null);
            } else {
                response = new ResultVO((byte)5, null, "practice/deleteProblem: Mybatis return not >=0 or is null");
            }
        } catch (Exception e) {
            return new ResultVO((byte)5, null, "practice/deleteProblem: " + e.getMessage());
        }
        return response;
    }

    @Override
    public ResultVO attend(String cnname, String contestName) {
        if (cnname == null || cnname.isEmpty()) {
            return new ResultVO(5, null, "practice/attend: cnname is null or empty");
        }
        return new ResultVO(0, null, null);
    }
}
