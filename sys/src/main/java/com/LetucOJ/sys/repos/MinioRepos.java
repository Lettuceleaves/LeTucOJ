package com.LetucOJ.sys.repos;

import com.LetucOJ.sys.model.FileDTO;

public interface MinioRepos {
    String get();
    String update(byte[] doc);
}
