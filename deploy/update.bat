@echo off
REM ===========================================
REM Configuration: adjust these two paths as needed
REM Note: do NOT include a trailing backslash
REM ===========================================
set "SRC_ROOT=E:\projects\LeTucOJ"
set "DST_ROOT=E:\projects\LeTucOJ\deploy"

REM ===========================================
REM Copy each module: delete old, copy new & rename
REM ===========================================

echo ----- Processing advice -----
if exist "%DST_ROOT%\advice\advice.jar" del /Q "%DST_ROOT%\advice\advice.jar"
copy /Y "%SRC_ROOT%\advice\target\advice-0.0.1-SNAPSHOT.jar" "%DST_ROOT%\advice\advice.jar"
if errorlevel 1 (
    echo [ERROR] advice copy failed
) else (
    echo advice.jar updated
)

echo ----- Processing contest -----
if exist "%DST_ROOT%\contest\contest.jar" del /Q "%DST_ROOT%\contest\contest.jar"
copy /Y "%SRC_ROOT%\contest\target\contest-0.0.1-SNAPSHOT.jar" "%DST_ROOT%\contest\contest.jar"
if errorlevel 1 (
    echo [ERROR] contest copy failed
) else (
    echo contest.jar updated
)

echo ----- Processing gateway -----
if exist "%DST_ROOT%\gateway\gateway.jar" del /Q "%DST_ROOT%\gateway\gateway.jar"
copy /Y "%SRC_ROOT%\gateway\target\gateway-0.0.1-SNAPSHOT.jar" "%DST_ROOT%\gateway\gateway.jar"
if errorlevel 1 (
    echo [ERROR] gateway copy failed
) else (
    echo gateway.jar updated
)

echo ----- Processing practice -----
if exist "%DST_ROOT%\practice\practice.jar" del /Q "%DST_ROOT%\practice\practice.jar"
copy /Y "%SRC_ROOT%\practice\target\practice-0.0.1-SNAPSHOT.jar" "%DST_ROOT%\practice\practice.jar"
if errorlevel 1 (
    echo [ERROR] practice copy failed
) else (
    echo practice.jar updated
)

echo ----- Processing run -----
if exist "%DST_ROOT%\run\run.jar" del /Q "%DST_ROOT%\run\run.jar"
copy /Y "%SRC_ROOT%\run\target\run-0.0.1-SNAPSHOT.jar" "%DST_ROOT%\run\run.jar"
if errorlevel 1 (
    echo [ERROR] run copy failed
) else (
    echo run.jar updated
)

echo ----- Processing sys -----
if exist "%DST_ROOT%\sys\sys.jar" del /Q "%DST_ROOT%\sys\sys.jar"
copy /Y "%SRC_ROOT%\sys\target\sys-0.0.1-SNAPSHOT.jar" "%DST_ROOT%\sys\sys.jar"
if errorlevel 1 (
    echo [ERROR] sys copy failed
) else (
    echo sys.jar updated
)

echo ----- Processing user -----
if exist "%DST_ROOT%\user\user.jar" del /Q "%DST_ROOT%\user\user.jar"
copy /Y "%SRC_ROOT%\user\target\user-0.0.1-SNAPSHOT.jar" "%DST_ROOT%\user\user.jar"
if errorlevel 1 (
    echo [ERROR] user copy failed
) else (
    echo user.jar updated
)

echo.
echo All modules processed.
pause
