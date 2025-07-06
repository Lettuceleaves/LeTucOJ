#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>
#include <unistd.h>
#include <time.h>
#include <errno.h>
#include <signal.h>
#include <dirent.h>

#define MAX_FILENAME_LENGTH 256
#define MAX_LINE_LENGTH 1024

FILE* errfile;

void handle_alarm(int sig) {
    // 超时处理函数
    fprintf(stderr, "程序执行超时，终止执行\n");
    fprintf(errfile, "程序执行超时，终止执行\n");
    // 直接退出
    exit(1);
}

int compare(const void* a, const void* b) {
    const char* str1 = *(const char**)a;
    const char* str2 = *(const char**)b;
    int num1, num2;
    sscanf(str1, "in%d", &num1);
    sscanf(str2, "in%d", &num2);
    return num1 - num2;
}

int execute_test_program(FILE* infile, FILE* outfile, int timeout, const char* filename) {
    pid_t pid = fork();
    if (pid == -1) {
        perror("fork");
        fprintf(errfile, "创建子进程失败\n");
        return -1;
    } else if (pid == 0) {
        if (infile) dup2(fileno(infile), STDIN_FILENO);
        dup2(fileno(outfile), STDOUT_FILENO);
        dup2(fileno(errfile), STDERR_FILENO);
        // 添加环境变量以启用 AddressSanitizer 和其他 Sanitizer
        setenv("ASAN_OPTIONS", "detect_leaks=1:detect_stack_use_after_return=1", 1);
        
        setenv("LSAN_OPTIONS", "suppressions=asan_suppressions.txt", 1);

        // 添加环境变量以启用 UndefinedBehaviorSanitizer
        setenv("UBSAN_OPTIONS", "print_stacktrace=1", 1);

        // 添加环境变量以启用 CFISanitizer
        setenv("CFISAN_OPTIONS", "print_stacktrace=1", 1);

        // 添加环境变量以启用 SafeStackSanitizer
        setenv("SAFESTACK_OPTIONS", "print_stacktrace=1", 1);

        execlp("./test", "test", NULL);
        perror("execlp");
        exit(1);
    } else {
        signal(SIGALRM, handle_alarm);
        alarm(timeout);
        int status;
        pid_t ret = waitpid(pid, &status, 0);
        alarm(0);

        if (ret == -1) {
            perror("waitpid");
            return -1;
        }

        if (WIFEXITED(status)) {
            int exit_code = WEXITSTATUS(status);
            if (exit_code != 0) {
                fprintf(errfile, "文件%s处理失败，退出码：%d\n", filename, exit_code);
                return -1;
            }
        } else if (WIFSIGNALED(status)) {
            fprintf(errfile, "文件%s处理失败，被信号%d终止\n", filename, WTERMSIG(status));
            return -1;
        }
    }
    return 0;
}

int main(int argc, char* argv[]) {

    // 打开err.txt文件用于记录错误信息
    errfile = fopen("err.txt", "w");
    if (!errfile) {
        perror("fopen err.txt");
        return 1;
    }
    
    int timeout;
    if (argc != 2) {
        fprintf(errfile, "用法: %s <超时时间（秒）>\n", argv[0]);
        return 1;
    }

    // 从命令行参数获取超时时间
    timeout = atoi(argv[1]);
    if (timeout <= 0) {
        fprintf(errfile, "超时时间必须为正整数\n");
        return 1;
    }

    // 编译test.c
    printf("正在编译test.c...\n");
    FILE* compile_fp = popen("gcc -o test test.c -fsanitize=address -g 2>&1", "r");
    //     FILE* compile_fp = popen("gcc -o test test.c -fsanitize=address,undefined,cfi,safe-stack,fuzzer -g 2>&1", "r");
    if (!compile_fp) {
        perror("popen");
        fprintf(errfile, "编译失败\n");
        fclose(errfile);
        return 1;
    }
    
    char compile_output[MAX_LINE_LENGTH];
    while (fgets(compile_output, sizeof(compile_output), compile_fp)) {
        fprintf(errfile, "%s", compile_output);
    }
    pclose(compile_fp);

    if (access("./test", X_OK) != 0) {
        fprintf(errfile, "test程序不可执行\n");
        fclose(errfile);
        return 1;
    }

    // 收集输入文件
    DIR *dir = opendir(".");
    char **input_files = NULL;
    int num_files = 0;
    struct dirent *entry;

    while ((entry = readdir(dir)) != NULL) {
        if (strncmp(entry->d_name, "in", 2) == 0) {
            input_files = realloc(input_files, (num_files + 1) * sizeof(char*));
            input_files[num_files] = strdup(entry->d_name);
            num_files++;
        }
    }
    closedir(dir);
    qsort(input_files, num_files, sizeof(char*), compare);

    if (num_files > 0) {
        for (int i = 0; i < num_files; i++) {
            printf("正在处理文件：%s\n", input_files[i]);
            FILE* infile = fopen(input_files[i], "r");
            if (!infile) {
                fprintf(errfile, "无法打开%s\n", input_files[i]);
                free(input_files[i]);
                return 1;
            }

            // 打开对应的输出文件 out(i + 1).txt
            char output_filename[MAX_FILENAME_LENGTH];
            snprintf(output_filename, sizeof(output_filename), "out%d.txt", i + 1);
            FILE* outfile = fopen(output_filename, "w");
            if (!outfile) {
                fprintf(errfile, "无法创建输出文件%s\n", output_filename);
                fclose(infile);
                free(input_files[i]);
                return 1;
            }

            if (execute_test_program(infile, outfile, timeout, input_files[i]) != 0) {
                fclose(infile);
                fclose(outfile);
                free(input_files[i]);
                return 1;
            }

            fclose(infile);
            fclose(outfile);
            free(input_files[i]);
        }
        free(input_files);
    } else {
        printf("无输入文件，直接执行test\n");
        FILE* outfile = fopen("out1.txt", "w");
        if (!outfile) {
            fprintf(errfile, "无法创建输出文件out1.txt\n");
            fclose(errfile);
            return 1;
        }
        if (execute_test_program(NULL, outfile, timeout, NULL) != 0) {
            fclose(outfile);
            fclose(errfile);
            return 1;
        }
        fclose(outfile);
    }

    fclose(errfile);
    printf("处理完成\n");
    return 0;
}
