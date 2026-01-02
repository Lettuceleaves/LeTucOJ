@echo off
setlocal enabledelayedexpansion

if "%~1"=="build_worker" goto :BUILD_WORKER_ENTRY

set "SRC_ROOT=E:\projects\LeTucOJ"
set "DIST_SOURCE=%SRC_ROOT%\dist"
set "DST_ROOT=E:\projects\LeTucOJ\deploy"
set "REGISTRY_HOST=localhost:5000"
set "FLAG_DIR_FULL=%DST_ROOT%\__BUILD_FLAGS__"

set "JAR_MODULES=advice contest gateway practice run sys user"
set "IMAGES=practice gateway user run run_c run_cpp run_js run_java run_py advice sys contest nginx"

if exist "%FLAG_DIR_FULL%" rd /s /q "%FLAG_DIR_FULL%"
mkdir "%FLAG_DIR_FULL%"

echo === Step 0.1: Copying Jars ===
for %%i in (%JAR_MODULES%) do (
    set "FOUND="
    for /f "delims=" %%f in ('dir /b "%DIST_SOURCE%\%%i*.jar" 2^>nul') do set "FOUND=%%f"

    if "!FOUND!"=="" (
        echo [ERROR] Missing: %%i.jar in %DIST_SOURCE%
        pause
        exit /b 1
    )

    if not exist "%DST_ROOT%\%%i" mkdir "%DST_ROOT%\%%i"
    copy /Y "%DIST_SOURCE%\!FOUND!" "%DST_ROOT%\%%i\%%i.jar" >nul
    echo [OK] Updated %%i
)

cd /d "%DST_ROOT%\docker-compose"
docker compose up -d registry mysql redis minio
cd /d "%DST_ROOT%"

for %%i in (%IMAGES%) do (
    start "Build %%i" cmd /c call "%~f0" build_worker %%i "%DST_ROOT%" "%FLAG_DIR_FULL%" "%REGISTRY_HOST%"
)

:WAIT_LOOP
timeout /t 3 /nobreak >nul
set /a FINISHED_COUNT=0
for %%i in (%IMAGES%) do (
    if exist "%FLAG_DIR_FULL%\%%i_FAIL.flag" exit /b 1
    if exist "%FLAG_DIR_FULL%\%%i_DONE.flag" set /a FINISHED_COUNT+=1
)
if !FINISHED_COUNT! equ 13 ( goto :PROCEED )
echo Waiting... !FINISHED_COUNT! / 13
goto :WAIT_LOOP

:PROCEED
cd /d "%DST_ROOT%\docker-compose"
docker compose up -d
echo === ALL DONE ===
pause
exit /b 0

:BUILD_WORKER_ENTRY
set "W_IMAGE=%2"
set "W_ROOT=%~3"
set "W_FLAGS=%~4"
set "W_REGISTRY=%~5"
cd /d "%W_ROOT%\%W_IMAGE%" || (echo FAIL > "%W_FLAGS%\%W_IMAGE%_FAIL.flag" & exit 1)
docker build -t %W_REGISTRY%/%W_IMAGE%:latest . || (echo FAIL > "%W_FLAGS%\%W_IMAGE%_FAIL.flag" & exit 1)
docker push %W_REGISTRY%/%W_IMAGE%:latest || (echo FAIL > "%W_FLAGS%\%W_IMAGE%_FAIL.flag" & exit 1)
echo DONE > "%W_FLAGS%\%W_IMAGE%_DONE.flag"
exit 0