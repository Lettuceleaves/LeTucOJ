#!/bin/bash

echo "Building Docker images..."

# 构建 practice 镜像
cd practice || { echo "practice folder not found"; exit 1; }
docker build -t practice:latest .
cd ..

# 构建 gateway 镜像
cd gateway || { echo "gateway folder not found"; exit 1; }
docker build -t gateway:latest .
cd ..

# 构建 user 镜像
cd user || { echo "user folder not found"; exit 1; }
docker build -t user:latest .
cd ..

# 构建 run 镜像
cd run || { echo "run folder not found"; exit 1; }
docker build -t run:latest .
cd ..

# 构建 run_c 镜像
cd run_c || { echo "run_c folder not found"; exit 1; }
docker build -t run_c:latest .
cd ..

# 构建 run_js 镜像
cd run_js || { echo "run_js folder not found"; exit 1; }
docker build -t run_js:latest .
cd ..

# 构建 run_java 镜像
cd run_java || { echo "run_java folder not found"; exit 1; }
docker build -t run_java:latest .
cd ..

# 构建 run_cpp 镜像
cd run_cpp || { echo "run_cpp folder not found"; exit 1; }
docker build -t run_cpp:latest .
cd ..

# 构建 run_py 镜像
cd run_py || { echo "run_py folder not found"; exit 1; }
docker build -t run_py:latest .
cd ..

# 构建 advice 镜像
cd advice || { echo "advice folder not found"; exit 1; }
docker build -t advice:latest .
cd ..

# 构建 sys 镜像
cd sys || { echo "sys folder not found"; exit 1; }
docker build -t sys:latest .
cd ..

# 构建 contest 镜像
cd contest || { echo "contest folder not found"; exit 1; }
docker build -t contest:latest .
cd ..

# 构建 nginx 镜像
cd nginx || { echo "nginx folder not found"; exit 1; }
docker build -t nginx:latest .
cd ..

echo "Building Docker Compose..."

# 启动 Docker Compose 服务
cd docker-compose || { echo "docker-compose folder not found"; exit 1; }
docker-compose up -d

echo "Docker Compose completed..."

# 暂停，等待用户按键继续
read -p "Press any key to continue..."
