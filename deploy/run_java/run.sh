#!/bin/bash

# 获取命令行参数中的测试案例总数
N=$1

# 定义超时时间 (秒)
TIMEOUT=5

# 检查是否提供了参数
if [ -z "$N" ]; then
    echo "5" > status.txt
    echo "Error: Number of test cases (N) not provided." >&2
    exit 1
fi

# 切换到提交目录
cd /submission || { echo "5" > status.txt; exit 1; }

# 清空之前的输出文件
rm -f out_*.txt err.txt compile.txt status.txt

echo "start" > status.txt

# 1. 编译用户程序
# 使用 javac 编译 prog.java 文件
javac prog.java 2> compile.txt

# 检查编译是否成功
if [ $? -ne 0 ]; then
    echo "2" > status.txt
    exit 0
fi

# 2. 循环运行每个测试用例
for i in $(seq 1 $N); do
    # 检查输入文件是否存在
    if [ ! -f "in_$i.txt" ]; then
        echo "5" > status.txt
        echo "Error: Test case in_$i.txt not found." >&2
        exit 0
    fi
    
    # 运行程序，并将输入和输出重定向
    # 使用 'java prog' 来执行编译后的 Java 类
    timeout $TIMEOUT java prog < "in_$i.txt" >> ou_$i.txt 2>> err.txt
    
    # 获取退出状态码
    exit_code=$?
    
    # 检查退出状态码
    if [ $exit_code -eq 124 ]; then
        # 超时
        echo "4" > status.txt
        exit 0
    elif [ $exit_code -ne 0 ]; then
        # 运行时错误
        echo "3" > status.txt
        exit 0
    fi
done

# 3. 如果所有测试用例都成功通过
echo "0" > status.txt

exit 0

sleep infinity
