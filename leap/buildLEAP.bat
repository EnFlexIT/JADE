@echo off
rem echo on

:: buildLEAP batch file
:: sets up environment for calling LEAP ant build file
:: @author Steffen Rusitschka, Siemens AG

set OLD_ANT_HOME=%ANT_HOME%
set OLD_JAVA_HOME=%JAVA_HOME%
set OLD_LEAP_HOME=%LEAP_HOME%


if not "%OS%"=="Windows_NT" goto win9xStart

if "%LEAP_HOME%"=="" set LEAP_HOME=%~dp0
if not "%LEAP_HOME%"=="" echo Automatically set LEAP_HOME to %LEAP_HOME%

if not "%JAVA_HOME%"=="" goto nt_javaHomeOk

  for /f %%i in ("javac.exe") do set JAVA_HOME=%%~dp$PATH:i
  if not "%JAVA_HOME%"=="" for /f %%i in ("%JAVA_HOME%..") do set JAVA_HOME=%%~fi
  if not "%JAVA_HOME%"=="" echo Automatically set JAVA_HOME to %JAVA_HOME%

:nt_javaHomeOk

:win9xStart

if not "%LEAP_HOME%"=="" goto leapHomeOk

  echo please set LEAP_HOME environment variable to your leap directory.
  goto quit

:leapHomeOk

if not "%JAVA_HOME%"=="" goto javaHomeOk

  echo please set JAVA_HOME environment variable to your java directory.
  goto quit

:javaHomeOk

set ARGS=
:argsLoop
set ARGS=%ARGS% %1
shift
if not "%1"=="" goto argsLoop

set ANT_HOME=%LEAP_HOME%\resources\build\ant

echo.
echo Building LEAP in "%LEAP_HOME%" using Java in "%JAVA_HOME%".
echo.

call %ANT_HOME%\bin\ant.bat -buildfile %LEAP_HOME%\resources\build\build.xml %ARGS%

:quit

set ARGS=

set ANT_HOME=%OLD_ANT_HOME%
set LEAP_HOME=%OLD_LEAP_HOME%
set JAVA_HOME=%OLD_JAVA_HOME%

set OLD_ANT_HOME=
set OLD_LEAP_HOME=
set OLD_JAVA_HOME=
