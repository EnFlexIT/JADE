@echo off
@setlocal
	
set ENABLE_CLDC_PROTOCOLS=true
set WTK_HOME=c:\java\WTK20

set APP_NAME=%1
set JAD_NAME=%APP_NAME%.jad

rem Uncomment the following to use the P800 skin
rem %WTK_HOME%\bin\emulator -Xdevice:SonyEricsson_P800 -Xdescriptor:%JAD_NAME% -Xverbose:class

rem Uncomment the following to use the DefaultGrayPhone skin
rem %WTK_HOME%\bin\emulator -Xdevice:DefaultGrayPhone -Xdescriptor:%JAD_NAME% -Xverbose:class
%WTK_HOME%\bin\emulator -Xdevice:DefaultGrayPhone -Xdescriptor:%JAD_NAME% 

@endlocal
