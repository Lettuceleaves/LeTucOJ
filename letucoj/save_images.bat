@echo off
echo Saving Docker images to tar files...

:: 保存 practice 镜像
docker save -o images/practice_latest.tar practice:latest
echo Saved practice image...

:: 保存 gateway 镜像
docker save -o images/gateway_latest.tar gateway:latest
echo Saved gateway image...

:: 保存 user 镜像
docker save -o images/user_latest.tar user:latest
echo Saved user image...

:: 保存 run 镜像
docker save -o images/run_latest.tar run:latest
echo Saved run image...

:: 保存 minio 镜像
docker save -o images/minio_minio_latest.tar minio/minio:latest
echo Saved minio image...

:: 保存 mysql 镜像
docker save -o images/mysql_latest.tar mysql:latest
echo Saved mysql image...

:: 保存 advice 镜像
docker save -o images/advice_latest.tar advice:latest
echo Saved advice image...

:: 保存 sys 镜像
docker save -o images/sys_latest.tar sys:latest
echo Saved sys image...

:: 保存 contest 镜像
docker save -o images/contest_latest.tar contest:latest
echo Saved contest image...

:: 保存 nginx 镜像
docker save -o images/nginx_latest.tar nginx:latest
echo Saved nginx image...

:: 保存 run 镜像
docker save -o images/run_latest.tar run:latest
echo Saved run image...

:: 保存 run_c 镜像
docker save -o images/run_c_latest.tar run_c:latest
echo Saved run_c image...

:: 保存 run_cpp 镜像
docker save -o images/run_cpp_latest.tar run_cpp:latest
echo Saved run_cpp image...

:: 保存 run_js 镜像
docker save -o images/run_js_latest.tar run_js:latest
echo Saved run_js image...

:: 保存 run_py 镜像
docker save -o images/run_py_latest.tar run_py:latest
echo Saved run_py image...

:: 保存 run_java 镜像
docker save -o images/run_java_latest.tar run_java:latest
echo Saved run_java image...

echo Docker images saved successfully.
pause
