package com.LetucOJ.practice.service;

import com.LetucOJ.common.result.ResultVO;
import com.LetucOJ.practice.model.CaseFile;
import com.LetucOJ.practice.model.DTO.SubmitRecord;
import com.LetucOJ.practice.model.Problem;
import com.LetucOJ.practice.model.DTO.ListConditionDTO;
import com.LetucOJ.practice.model.TestCaseDTO;
import com.LetucOJ.practice.model.VO.ProblemListVO;
import com.LetucOJ.practice.model.VO.SubmitRecordListVO;
import com.LetucOJ.practice.model.VO.TestTaskVO;

import java.util.List;

public interface DBService {
    ResultVO<ProblemListVO> getList(ListConditionDTO listConditionDTO, String user_name);
    ResultVO<ProblemListVO> getListInRoot(ListConditionDTO listConditionDTO, String user_name);
    ResultVO<ProblemListVO> searchList(ListConditionDTO listConditionDTO, String user_name);
    ResultVO<ProblemListVO> searchListInRoot(ListConditionDTO dto, String user_name);
    ResultVO<Problem> getProblem(String user_name);
    ResultVO<Problem> getProblemInRoot(String user_name);
    ResultVO<Void> insertProblem(Problem problem);
    ResultVO<Void> updateProblem(Problem problem);
    ResultVO<Void> deleteProblem(String user_name);
    ResultVO<TestTaskVO> testCase(TestCaseDTO testCaseDTO, String language);
    ResultVO<Void> saveCase(CaseFile caseFile);
    ResultVO<CaseFile> getCase(String problemName, Integer id);
    ResultVO<SubmitRecordListVO> submitRecordListByName(String userName, int start, int limit);
    ResultVO<SubmitRecordListVO> submitRecordListAll(int start, int limit);
    ResultVO<byte[]> getConfigFile(String problemName);
}
