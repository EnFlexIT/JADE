@echo off
@setlocal

set LEAP_HOME=%~dp0
set ANT_HOME=%LEAP_HOME%\resources\build\ant
	
set JAR_NAME=%1
set SRC_PATH=%2
set MANIFEST=%LEAP_HOME%\resources\build\sample.mf
	
if "%JAR_NAME%"=="" goto print_usage

call %ANT_HOME%\bin\ant.bat -buildfile %LEAP_HOME%\resources\build\midpApp.xml build

goto end
	
:print_usage
echo "Usage: buildMIDPApp <app-source-path> <jar-filename>"
	
:end

@endlocal
