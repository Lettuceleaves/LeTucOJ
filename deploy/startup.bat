@echo off
setlocal enabledelayedexpansion

:: =======================================================
:: 配置部分
:: =======================================================
set "SRC_ROOT=E:\projects\LeTucOJ"
set "DST_ROOT=E:\projects\LeTucOJ\deploy"
set REGISTRY_HOST=localhost:5000

cd /d "%DST_ROOT%"

:: 定义模块
set JAR_MODULES=advice contest gateway practice run sys user
set IMAGES=practice gateway user run run_c run_cpp run_js run_java run_py advice sys contest nginx

echo =======================================================
echo 0. Copying JARs...
echo =======================================================

for %%i in (%JAR_MODULES%) do (
    set "TARGET_DIR=%%i"
    set "DEST_DIR=%%i"
    set "SNAPSHOT_JAR=%%i-0.0.1-SNAPSHOT.jar"
    set "FINAL_JAR=%%i.jar"
    
    if exist "%DST_ROOT%\!DEST_DIR!\!FINAL_JAR!" del /Q "%DST_ROOT%\!DEST_DIR!\!FINAL_JAR!"
    copy /Y "%SRC_ROOT%\!TARGET_DIR!\target\!SNAPSHOT_JAR!" "%DST_ROOT%\!DEST_DIR!\!FINAL_JAR!" >nul
    
    if errorlevel 1 (
        echo [ERROR] !FINAL_JAR! copy failed.
        pause
        exit /b 1
    ) else (
        echo [OK] !FINAL_JAR! updated.
    )
)

echo.
echo =======================================================
echo 1. Starting Registry & Middleware
echo =======================================================

cd docker-compose
docker compose up -d registry mysql redis namesrv broker minio
cd ..

echo.
echo =======================================================
echo 2. Building & Pushing (With Auto-Cleanup)
echo =======================================================

for %%i in (%IMAGES%) do (
    echo.
    echo --- Processing %%i ---
    cd %%i
    
    set FULL_IMAGE_TAG=%REGISTRY_HOST%/%%i:latest
    
    docker rmi %%i:latest 2>nul
    
    echo Building !FULL_IMAGE_TAG!...
    docker build -t !FULL_IMAGE_TAG! .
    
    echo Pushing !FULL_IMAGE_TAG!...
    docker push !FULL_IMAGE_TAG!
    
    cd ..
)

echo.
echo =======================================================
echo 3. Starting Services
echo =======================================================

cd docker-compose
docker compose up -d
cd ..

echo.
echo =======================================================
echo 4. Cleanup
echo =======================================================

docker image prune -f

echo.
echo Done!
pause