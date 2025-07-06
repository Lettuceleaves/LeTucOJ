package com.LetucOJ.contest.service.impl;

import com.LetucOJ.contest.client.practiceClient;
import com.LetucOJ.contest.model.contest;
import com.LetucOJ.contest.model.problemlist;
import com.LetucOJ.contest.repos.contestMybatisRepos;
import com.LetucOJ.contest.repos.problemlistMybatisRepos;
import com.LetucOJ.contest.service.contestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class contestServiceImpl implements contestService {

    @Autowired
    private contestMybatisRepos contestMybatisRepos;

    @Autowired
    private problemlistMybatisRepos problemlistMybatisRepos;

    @Autowired
    private practiceClient practiceClient;

    @Override
    public String submit(String problemName, byte[] userFile) throws Exception {
        return practiceClient.submitFeign(problemName, userFile);
    }

    @Override
    public String putContest(contest newContest, int[] problemIds) {
        try {
            contestMybatisRepos.insert(newContest);
            for (var problemId : problemIds) {
                problemlist pl = new problemlist(newContest.getId(), problemId);
                problemlistMybatisRepos.insert(pl);
            }
            return "Contest added successfully.";
        } catch (Exception e) {
            return "Error adding contest: " + e.getMessage();
        }
    }

    @Override
    public String deleteContestById(int id) {
        try {
            problemlistMybatisRepos.deleteProblemById(id);
            contestMybatisRepos.deleteById(id);
            return "Contest deleted successfully.";
        } catch (Exception e) {
            return "Error deleting contest: " + e.getMessage();
        }
    }

    @Override
    public String deleteContestByName(String name) {
        try {
            contest oldContest = contestMybatisRepos.getByName(name);
            if (oldContest == null) {
                return "Contest not found.";
            }
            problemlistMybatisRepos.deleteProblemById(oldContest.getId());
            contestMybatisRepos.deleteByName(name);
            return "Contest deleted successfully.";
        } catch (Exception e) {
            return "Error deleting contest: " + e.getMessage();
        }
    }

    @Override
    public String updateContest(contest newContest, int[] problemIds) {
        try {
            System.out.println(newContest);
            contestMybatisRepos.updateById(newContest);
            problemlistMybatisRepos.deleteProblemById(newContest.getId());
            for (var problemId : problemIds) {
                problemlist pl = new problemlist(newContest.getId(), problemId);
                problemlistMybatisRepos.insert(pl);
            }
            contestMybatisRepos.updateById(newContest);
            return "Contest updated successfully.";
        } catch (Exception e) {
            return "Error updating contest: " + e.getMessage();
        }
    }

    @Override
    public contest getContestInfo(String name) {
        contest contestInfo = contestMybatisRepos.getByName(name);
        System.out.println(contestInfo);
        return contestInfo;
    } // 没获取题目信息 TODO
}
