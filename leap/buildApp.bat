@echo off
@setlocal

set LEAP_HOME=%~dp0
set ANT_HOME=%LEAP_HOME%\resources\build\ant
	
set TYPE=%1
set JAR_NAME=%2
set SRC_PATH=%3
	
if "%SRC_PATH%"=="" goto print_usage

call %ANT_HOME%\bin\ant.bat -buildfile %LEAP_HOME%\resources\build\app.xml %type% build

goto end
	
:print_usage
echo "Usage: buildApp <java-type> <app-source-path> <jar-filename>"
	
:end

@endlocal
