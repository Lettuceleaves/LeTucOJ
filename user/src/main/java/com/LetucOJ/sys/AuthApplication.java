package com.LetucOJ.sys;

import com.LetucOJ.sys.util.PasswordUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(
        scanBasePackages = {"com.LetucOJ.sys", "com.LetucOJ.common"}
)
public class AuthApplication {

    public static void main(String[] args) {
        System.out.println(PasswordUtil.encrypt("aaa"));

        SpringApplication.run(AuthApplication.class, args);
    }

}
