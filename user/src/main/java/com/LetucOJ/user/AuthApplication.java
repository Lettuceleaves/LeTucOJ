package com.LetucOJ.user;

import com.LetucOJ.common.unique.TaskIdContext;
import lombok.AllArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(
        scanBasePackages = {"com.LetucOJ.user", "com.LetucOJ.common"}
)
@MapperScan(basePackages = {"com.LetucOJ.user.repos", "com.LetucOJ.common.anno"})
@AllArgsConstructor
public class AuthApplication implements CommandLineRunner {

    private final testSubmit testSubmit;

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            TaskIdContext.setTaskId("test-task-id");
            testSubmit.test("java", "userName", "problemName");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
