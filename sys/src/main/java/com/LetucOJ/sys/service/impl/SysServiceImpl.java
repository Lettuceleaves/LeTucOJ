package com.LetucOJ.sys.service.impl;

import com.LetucOJ.sys.model.ResultVO;
import com.LetucOJ.sys.repos.MinioRepos;
import com.LetucOJ.sys.service.SysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysServiceImpl implements SysService {

    @Autowired
    private MinioRepos minioRepos;

    @Override
    public ResultVO getDoc() {
        try {
            String result = minioRepos.get();
            if (result == null || result.isEmpty()) {
                return new ResultVO(5, null, "sys/getDoc: Document not found in MinIO");
            } else {
                return new ResultVO(0, result, null);
            }
        } catch (Exception e) {
            return new ResultVO(5, null, "sys/getDoc: " + e.getMessage());
        }
    }

    @Override
    public ResultVO updateDoc(byte[] doc) {
        String result = minioRepos.update(doc);
        if (result == null || result.isEmpty()) {
            return new ResultVO(0, null, null);
        } else {
            return new ResultVO(5, null, result);
        }
    }
}
