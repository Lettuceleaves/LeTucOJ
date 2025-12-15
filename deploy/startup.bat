@echo off
setlocal enabledelayedexpansion

:: =======================================================
:: [核心机制] 检查是否为子进程调用
:: =======================================================
if "%~1"=="build_worker" goto :BUILD_WORKER_ENTRY

:: =======================================================
:: 主进程配置
:: =======================================================
set "SRC_ROOT=E:\projects\LeTucOJ"
set "DST_ROOT=E:\projects\LeTucOJ\deploy"
set REGISTRY_HOST=localhost:5000
set "FLAG_DIR_NAME=__BUILD_FLAGS__"

cd /d "%DST_ROOT%"
set "FLAG_DIR_FULL=%DST_ROOT%\%FLAG_DIR_NAME%"

set JAR_MODULES=advice contest gateway practice run sys user
set IMAGES=practice gateway user run run_c run_cpp run_js run_java run_py advice sys contest nginx

:: -------------------------------------------------------
:: 0. 准备工作
:: -------------------------------------------------------
echo =======================================================
echo 0. Preparation
echo =======================================================
if exist "%FLAG_DIR_FULL%" rd /s /q "%FLAG_DIR_FULL%"
mkdir "%FLAG_DIR_FULL%"

:: -------------------------------------------------------
:: 0.1 复制 JARs
:: -------------------------------------------------------
echo.
echo =======================================================
echo 0.1 Copying JARs...
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
echo 1. Starting Registry ^& Middleware
echo =======================================================

cd docker-compose
docker compose up -d registry mysql redis namesrv broker minio
cd ..

echo.
echo =======================================================
echo 2. Building ^& Pushing (CONCURRENT MODE)
echo =======================================================

set /a TOTAL_IMAGES=0
for %%i in (%IMAGES%) do set /a TOTAL_IMAGES+=1

:: ---------------------------------------------
:: 循环触发并发构建
:: ---------------------------------------------
for %%i in (%IMAGES%) do (
    echo [MAIN] Triggering build task for: %%i
    
    :: [修改点 1] 使用 cmd /c，表示命令执行完毕后自动关闭窗口
    :: 如果子脚本里有 pause，窗口会等待用户按键；如果没有 pause，窗口直接消失
    start "Build %%i" cmd /c call "%~f0" build_worker %%i "%DST_ROOT%" "%FLAG_DIR_FULL%" "%REGISTRY_HOST%"
)

:: ---------------------------------------------
:: 等待循环
:: ---------------------------------------------
echo.
echo [MAIN] Waiting for %TOTAL_IMAGES% builds to complete...

:WAIT_LOOP
timeout /t 3 /nobreak >nul

set /a FINISHED_COUNT=0
set "PENDING_LIST="

for %%i in (%IMAGES%) do (
    if exist "%FLAG_DIR_FULL%\%%i_FAIL.flag" (
        echo.
        echo [FATAL ERROR] Build process reported failure for: %%i
        echo Check the remaining open command windows for details.
        cd /d "%DST_ROOT%"
        pause
        exit /b 1
    )

    if exist "%FLAG_DIR_FULL%\%%i_DONE.flag" (
        set /a FINISHED_COUNT+=1
    ) else (
        set "PENDING_LIST=!PENDING_LIST! %%i"
    )
)

if %FINISHED_COUNT% equ %TOTAL_IMAGES% (
    echo.
    echo [SUCCESS] All images built and pushed successfully!
    goto :PROCEED
)

echo [WAITING] Completed: %FINISHED_COUNT%/%TOTAL_IMAGES%. Pending:!PENDING_LIST!
goto :WAIT_LOOP


:PROCEED
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

rd /s /q "%FLAG_DIR_FULL%"
docker image prune -f

echo.
echo [DEPLOYMENT COMPLETE]
pause
exit /b 0


:: =======================================================
::           Worker 子进程逻辑
:: =======================================================
:BUILD_WORKER_ENTRY
set "W_IMAGE=%2"
set "W_ROOT=%~3"
set "W_FLAGS=%~4"
set "W_REGISTRY=%~5"

setlocal enabledelayedexpansion

title Building %W_IMAGE%...

cd /d "%W_ROOT%\%W_IMAGE%"
if errorlevel 1 (
    echo [WORKER] Dir not found: %W_ROOT%\%W_IMAGE%
    echo FAIL > "%W_FLAGS%\%W_IMAGE%_FAIL.flag"
    
    :: [修改点 2] 失败时保留 pause，让你能看到报错
    color 4f
    echo [ERROR] Directory missing!
    pause
    exit 1
)

set "FULL_TAG=%W_REGISTRY%/%W_IMAGE%:latest"
echo [WORKER] Building %FULL_TAG%...

docker rmi %W_IMAGE%:latest 2>nul

docker build -t %FULL_TAG% .
if errorlevel 1 (
    echo [WORKER] Build failed for %W_IMAGE%
    echo FAIL > "%W_FLAGS%\%W_IMAGE%_FAIL.flag"
    
    :: [修改点 2] 失败时保留 pause，让你能看到报错
    color 4f
    echo [ERROR] Docker Build Failed!
    pause
    exit 1
)

echo [WORKER] Pushing...
docker push %FULL_TAG%
if errorlevel 1 (
    echo [WORKER] Push failed for %W_IMAGE%
    echo FAIL > "%W_FLAGS%\%W_IMAGE%_FAIL.flag"
    
    :: [修改点 2] 失败时保留 pause，让你能看到报错
    color 4f
    echo [ERROR] Docker Push Failed!
    pause
    exit 1
)

echo [WORKER] Success!
echo DONE > "%W_FLAGS%\%W_IMAGE%_DONE.flag"

:: [修改点 3] 成功时移除 pause，配合 cmd /c 实现自动关闭
:: 窗口会立即关闭，表示这个任务完成了
exit 0