package com.LetucOJ.contest.service;

import com.LetucOJ.contest.model.contest;

public interface contestService {
    String submit(String problemName, byte[] userFile) throws Exception;
    String putContest(contest newContest, int[] problemIds);
    String deleteContestById(int id);
    String deleteContestByName(String name);
    String updateContest(contest newContest, int[] problemIds);
    contest getContestInfo(String name);
}
