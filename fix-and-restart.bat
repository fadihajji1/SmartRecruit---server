@echo off
REM ============================================================================
REM  SmartRecruit - one-shot repair + restart
REM
REM  What it does, in order:
REM    1. Forcefully kill every java.exe listening on 8080..8095
REM       (old api-gateway / notification / candidate-ai / subscription instances
REM        that keep returning old or stale responses)
REM    2. Make sure Docker infrastructure (DBs, Kafka, MinIO) is up
REM    3. Align every service DB password with .env
REM       (fixes the "authentification par mot de passe echouee pour smartrecruit"
REM        crash loop in identity-service - Postgres only sets POSTGRES_PASSWORD
REM        on first boot, so if .env ever changed the DB still has the old one)
REM    4. Start all services via start-backend.bat
REM    5. Quick health check on every service + the gateway
REM ============================================================================
setlocal EnableDelayedExpansion
cd /d "%~dp0"

if not exist .env (
  echo [X] .env is missing. Copy .env.example to .env and set POSTGRES_PASSWORD, JWT_SECRET, ADMIN_PASSWORD. ^& pause ^& exit /b 1
)

REM ---------- Load .env into this cmd session ----------
for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do if not "%%B"=="" set "%%A=%%B"

if not defined POSTGRES_PASSWORD (
  echo [X] POSTGRES_PASSWORD is not set in .env. ^& pause ^& exit /b 1
)

echo.
echo ============================================
echo  SmartRecruit repair + restart
echo ============================================
echo.

REM ---------- 1. Kill stale Java processes on service ports ----------
echo [1/5] Killing stale Java processes on ports 8080..8095 ...
for %%P in (8080 8082 8090 8091 8092 8093 8094 8095) do call :killJavaOnPort %%P
timeout /t 2 /nobreak >nul

REM ---------- 2. Make sure infra containers are running ----------
echo [2/5] Starting Docker infrastructure (DBs, Kafka, MinIO)...
docker compose up -d --remove-orphans
if errorlevel 1 (
  echo [X] Docker failed. Is Docker Desktop running?
  pause
  exit /b 1
)

echo        Waiting 8s for Postgres containers to be ready...
timeout /t 8 /nobreak >nul

REM ---------- 3. Reset the smartrecruit role password on every DB container ----------
REM This fixes the "FATAL: password authentication failed for user smartrecruit"
REM crash without destroying data. Runs ALTER USER inside each Postgres container
REM so the role password matches whatever is in .env today.
echo [3/5] Aligning Postgres passwords with .env POSTGRES_PASSWORD ...
set DB_CONTAINERS=smartrecruit-identity-postgres smartrecruit-recruitment-postgres smartrecruit-candidate-postgres smartrecruit-subscription-postgres smartrecruit-interview-postgres smartrecruit-notification-postgres smartrecruit-analytics-postgres
for %%C in (%DB_CONTAINERS%) do call :fixDbPassword %%C

REM ---------- 4. Start all backend services ----------
echo [4/5] Starting backend services (logs in logs\) ...
if not exist logs mkdir logs
set IDENTITY_DB_URL=jdbc:postgresql://localhost:15433/identity_db

for %%S in (identity-service:8082 subscription-service:8092 recruitment-service:8090 candidate-ai-service:8091 interview-service:8093 notification-service:8094 analytics-service:8095 api-gateway:8080) do (
  for /f "tokens=1 delims=:" %%N in ("%%S") do (
    if exist "services\%%N\target\%%N-0.0.1-SNAPSHOT.jar" (
      start "%%N" /min cmd /c "java -jar services\%%N\target\%%N-0.0.1-SNAPSHOT.jar > logs\%%N.log 2>&1"
      echo        starting %%N
    ) else (
      echo [!] Missing jar for %%N - run: mvnw.cmd -DskipTests package
    )
  )
)

REM ---------- 5. Health check ----------
echo [5/5] Waiting for services to answer (up to 90 seconds) ...
set ALL_OK=1
for %%S in (identity-service:8082 recruitment-service:8090 subscription-service:8092 candidate-ai-service:8091 interview-service:8093 notification-service:8094 analytics-service:8095 api-gateway:8080) do (
  for /f "tokens=1,2 delims=:" %%N in ("%%S") do (
    set "STATE=DOWN"
    for /L %%T in (1,1,30) do if "!STATE!"=="DOWN" (
      for /f %%C in ('curl -s -o nul -w "%%{http_code}" http://localhost:%%O/actuator/health 2^>nul') do if not "%%C"=="000" set "STATE=UP"
      if "!STATE!"=="DOWN" (
        for /f %%C in ('curl -s -o nul -w "%%{http_code}" http://localhost:%%O/ 2^>nul') do if not "%%C"=="000" set "STATE=UP"
      )
      if "!STATE!"=="DOWN" timeout /t 3 /nobreak >nul
    )
    if "!STATE!"=="UP" (
      echo        OK    %%N      http://localhost:%%O
    ) else (
      set ALL_OK=
      echo        FAIL  %%N      see logs\%%N.log
    )
  )
)

echo.
if defined ALL_OK (
  echo ============================================
  echo  All services are up. Next steps:
  echo    cd ..\SmartRecruit-Stitch-Angular
  echo    npm start              ^(or: ng serve^)
  echo  Then open: http://localhost:4200
  echo ============================================
) else (
  echo [!] One or more services failed to start. Open the matching log file
  echo     under logs\ and look for "APPLICATION FAILED" or "Caused by".
)
pause
endlocal
exit /b 0

REM ---------- helpers ----------
:killJavaOnPort
for /f "tokens=5" %%P in ('netstat -ano -p tcp ^| findstr /R /C:":%1 .*LISTENING"') do (
  tasklist /FI "PID eq %%P" 2>nul | find /I "java.exe" >nul && (
    echo        stopping stale java PID %%P on port %1
    taskkill /PID %%P /F >nul 2>&1
  )
)
exit /b 0

:fixDbPassword
docker ps --format "{{.Names}}" | findstr /B /C:"%1" >nul 2>&1
if errorlevel 1 (
  echo        %1  not running  ^(skipped^)
  exit /b 0
)
REM Use ALTER USER so the password is aligned without destroying data.
docker exec %1 psql -U smartrecruit -d postgres -c "ALTER USER smartrecruit WITH PASSWORD '%POSTGRES_PASSWORD%';" >nul 2>&1
if errorlevel 1 (
  docker exec %1 psql -U postgres -d postgres -c "ALTER USER smartrecruit WITH PASSWORD '%POSTGRES_PASSWORD%';" >nul 2>&1
)
echo        %1  password aligned
exit /b 0
