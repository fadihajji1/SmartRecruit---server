@echo off
REM ============================================================================
REM  SmartRecruit - local backend launcher
REM    run-all.bat           build everything, then start
REM    run-all.bat nobuild   start the jars that are already built
REM
REM  What it does, in order:
REM    1. loads settings from .env (same values Docker uses)
REM    2. stops OLD copies of the services: app containers started by
REM       "docker compose up" and java processes still holding the ports.
REM       Old copies answer with old code and cause 404/500 errors in the UI.
REM    3. starts the infrastructure containers (databases, kafka, minio)
REM    4. builds (unless nobuild) and starts every service, logs in logs\
REM    5. checks that each service answers and prints a status table
REM ============================================================================
setlocal EnableDelayedExpansion
cd /d "%~dp0"

REM ---------- 1. settings ----------
set JWT_ISSUER=smartrecruit-identity
set DOMAIN_EVENTS_TOPIC=smartrecruit.domain-events
set MINIO_BUCKET=smartrecruit-private
set MINIO_CV_BUCKET=smartrecruit-cvs
if exist .env (
  for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do if not "%%B"=="" set "%%A=%%B"
) else (
  echo [!] .env not found - copy .env.example to .env first. & pause & exit /b 1
)
REM identity database is published on 15433 (5433 is often used by a local PostgreSQL install)
set IDENTITY_DB_URL=jdbc:postgresql://localhost:15433/identity_db
REM Set to true once if you forgot the admin password: it is reset to ADMIN_PASSWORD at startup.
if not defined ADMIN_RESET_PASSWORD set ADMIN_RESET_PASSWORD=false
if exist "C:\Program Files\Java\jdk-21.0.12.1\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

set SERVICES=identity-service:8082 subscription-service:8092 recruitment-service:8090 candidate-ai-service:8091 interview-service:8093 notification-service:8094 analytics-service:8095

REM ---------- 2. stop old copies ----------
echo.
echo [1/5] Stopping old copies of the services...
docker compose --profile app stop identity-service candidate-ai-service subscription-service notification-service analytics-service api-gateway >nul 2>&1
docker compose --profile app rm -f identity-service candidate-ai-service subscription-service notification-service analytics-service api-gateway >nul 2>&1
for %%P in (8080 8082 8083 8090 8091 8092 8093 8094 8095) do call :stopJavaOnPort %%P
timeout /t 2 /nobreak >nul
set PORT_PROBLEM=
for %%P in (8080 8082 8090 8091 8092 8093 8094 8095) do call :checkPortFree %%P
if defined PORT_PROBLEM ( echo Close those programs, then run this script again. & pause & exit /b 1 )

REM ---------- 3. infrastructure ----------
echo [2/5] Starting databases, Kafka and MinIO (docker compose)...
docker compose up -d
if errorlevel 1 ( echo [!] Docker failed. Is Docker Desktop running? & pause & exit /b 1 )

REM ---------- 4. build ----------
if /I not "%1"=="nobuild" (
  echo [3/5] Building all services - this takes a few minutes...
  REM mvnw.cmd breaks when the folder path contains an apostrophe, as in l'Enseignement.
  REM The project folder is mapped to a temporary drive letter so Maven sees a clean path.
  set "BUILD_DRIVE="
  for %%D in (S T U V W X Y Z R Q P) do if not defined BUILD_DRIVE if not exist %%D:\ set "BUILD_DRIVE=%%D:"
  if not defined BUILD_DRIVE ( echo No free drive letter to build from. & pause & exit /b 1 )
  subst !BUILD_DRIVE! "%CD%"
  pushd !BUILD_DRIVE!\
  REM the build output is also kept in build.log so errors can be read after the window closes
  call mvnw.cmd -q -DskipTests package > build.log 2>&1
  set "BUILD_RESULT=!errorlevel!"
  echo EXIT=!BUILD_RESULT!>> build.log
  popd
  subst !BUILD_DRIVE! /d
  if not "!BUILD_RESULT!"=="0" ( type build.log & echo BUILD FAILED - see the errors above and in build.log. & pause & exit /b 1 )
  echo       Build OK.
) else (
  echo [3/5] Skipping build.
)

REM ---------- 5. start ----------
echo [4/5] Starting services...
if not exist logs mkdir logs
for %%S in (%SERVICES%) do (
  for /f "tokens=1 delims=:" %%N in ("%%S") do (
    if not exist "services\%%N\target\%%N-0.0.1-SNAPSHOT.jar" ( echo [!] missing jar for %%N - run without nobuild & pause & exit /b 1 )
    start "%%N" /min cmd /c "java -jar services\%%N\target\%%N-0.0.1-SNAPSHOT.jar > logs\%%N.log 2>&1"
  )
)
start "api-gateway" /min cmd /c "java -jar services\api-gateway\target\api-gateway-0.0.1-SNAPSHOT.jar > logs\api-gateway.log 2>&1"

echo [5/5] Waiting for every service to answer (up to 2 minutes)...
set ALL_OK=1
for %%S in (%SERVICES% api-gateway:8080) do (
  for /f "tokens=1,2 delims=:" %%N in ("%%S") do (
    set "STATE=DOWN"
    for /L %%T in (1,1,40) do if "!STATE!"=="DOWN" (
      for /f %%C in ('curl -s -o nul -w "%%{http_code}" http://localhost:%%O/api/v1/subscriptions/plans 2^>nul') do if not "%%C"=="000" set "STATE=UP"
      if "!STATE!"=="DOWN" timeout /t 3 /nobreak >nul
    )
    if "!STATE!"=="UP" ( echo       OK    %%N  http://localhost:%%O ) else (
      set ALL_OK=
      echo       FAIL  %%N  - open logs\%%N.log and look for "APPLICATION FAILED" or "Caused by"
    )
  )
)
echo.
if defined ALL_OK (
  echo All services are running. Frontend: run "ng serve" and open http://localhost:4200
  echo Demo data: seed-demo.bat
) else (
  echo Some services did not start. Fix the error in their log, then run: run-all.bat nobuild
)
pause
endlocal
exit /b 0

REM ---------- helpers ----------
REM netstat columns: proto, LOCAL address, remote address, state, PID. Only the local port is matched,
REM and the state column is ignored so this also works on non-English Windows.
:stopJavaOnPort
for /f "tokens=2,5" %%A in ('netstat -ano -p tcp ^| findstr /C:":%1 "') do (
  echo %%A| findstr /E /C:":%1" >nul && tasklist /FI "PID eq %%B" 2>nul | find /I "java.exe" >nul && (
    echo       stopping old java process %%B on port %1
    taskkill /PID %%B /F >nul 2>&1
  )
)
exit /b 0

:checkPortFree
for /f "tokens=2,5" %%A in ('netstat -ano -p tcp ^| findstr /C:":%1 "') do (
  echo %%A| findstr /E /C:":%1" >nul && if not "%%B"=="0" (
    echo [!] Port %1 is still used by process %%B:
    tasklist /FI "PID eq %%B" /FO LIST | findstr /B /C:"Image" /C:"Nom"
    set PORT_PROBLEM=1
  )
)
exit /b 0
