@echo off
REM Fills every SmartRecruit service with demo data through the API gateway.
REM Start the backend first with run-all.bat (it must report every service OK).
REM Admin credentials are read from .env (ADMIN_EMAIL / ADMIN_PASSWORD) unless given:
REM    seed-demo.bat [admin-email] [admin-password]
setlocal
cd /d "%~dp0"
if exist .env for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do if not "%%B"=="" set "%%A=%%B"
if not "%~1"=="" set "ADMIN_EMAIL=%~1"
if not "%~2"=="" set "ADMIN_PASSWORD=%~2"
if not defined ADMIN_EMAIL set ADMIN_EMAIL=admin@admin.com
node seed-demo.mjs --base http://localhost:8080 --admin-email "%ADMIN_EMAIL%" --admin-password "%ADMIN_PASSWORD%"
pause
endlocal
