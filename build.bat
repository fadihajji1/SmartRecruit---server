@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
if exist "C:\Program Files\Java\jdk-21.0.12.1\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
set "BUILD_DRIVE="
for %%D in (S T U V W X Y Z R Q P) do if not defined BUILD_DRIVE if not exist %%D:\ set "BUILD_DRIVE=%%D:"
subst !BUILD_DRIVE! "%CD%"
pushd !BUILD_DRIVE!\
echo Building... output in build.log
call mvnw.cmd -q -DskipTests -fae package > build.log 2>&1
echo EXIT=!errorlevel!>> build.log
popd
subst !BUILD_DRIVE! /d
echo Build finished - see build.log
timeout /t 3 >nul
