package com.LetucOJ.user;

import com.LetucOJ.common.anno.SubmitLimit;
import org.springframework.stereotype.Service;

import static java.lang.Thread.sleep;

@Service
public class testSubmit implements testService {

    @SubmitLimit
    public void test(String test, String userName, String problemName) throws InterruptedException {
        sleep(5000);
    }
}
