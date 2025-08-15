#!/bin/bash

echo "Loading Docker images from images folder..."

# 进入 images 文件夹
cd images || { echo "images folder not found"; exit 1; }

# 加载 practice 镜像
echo "Loading practice image..."
docker load -i practice_latest.tar

# 加载 gateway 镜像
echo "Loading gateway image..."
docker load -i gateway_latest.tar

# 加载 user 镜像
echo "Loading user image..."
docker load -i user_latest.tar

# 加载 run 镜像
echo "Loading run image..."
docker load -i run_latest.tar

# 加载 minio 镜像
echo "Loading minio image..."
docker load -i minio_minio_latest.tar

# 加载 mysql 镜像
echo "Loading mysql image..."
docker load -i mysql_latest.tar

# 加载 advice 镜像
echo "Loading advice image..."
docker load -i advice_latest.tar

# 加载 sys 镜像
echo "Loading sys image..."
docker load -i sys_latest.tar

# 加载 contest 镜像
echo "Loading contest image..."
docker load -i contest_latest.tar

# 退出 images 文件夹
cd ..

echo "Docker images loaded successfully."

echo "Building Docker Compose..."

# 启动 Docker Compose 服务
cd docker-compose || { echo "docker-compose folder not found"; exit 1; }
docker-compose up -d

echo "Docker Compose completed..."

# 暂停，等待用户按键继续
read -p "Press any key to continue..."
