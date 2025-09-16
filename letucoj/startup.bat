@echo off
echo buiding Docker images...

:: 构建 practice 镜像
cd practice
docker build -t practice:latest .
cd ..

:: 构建 gateway 镜像
cd gateway
docker build -t gateway:latest .
cd ..

:: 构建 user 镜像
cd user
docker build -t user:latest .
cd ..

:: 构建 run 镜像
cd run
docker build -t run:latest .
cd ..

:: 构建 advice 镜像
cd advice
docker build -t advice:latest .
cd ..

:: 构建 sys 镜像
cd sys
docker build -t sys:latest .
cd ..

:: 构建 contest 镜像
cd contest
docker build -t contest:latest .
cd ..

:: 构建 nginx 镜像
cd nginx
docker build -t nginx:latest .
cd ..

echo building Docker Compose...

:: 启动 Docker Compose 服务
cd docker-compose
docker-compose up -d

echo Docker Compose completed...
pause
