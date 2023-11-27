rem Batch file to start JADE with the AMSProtectionService configured to read permissions from the local 
rem permissions.properties file and to apply checks to local agents too.
rem 2 RMA agents are started with different permissions.

echo off

set JADE_HOME=../../..
set LIB_DIR=%JADE_HOME%/lib
set CP=%LIB_DIR%/jade.jar;%LIB_DIR%/jadeExamples.jar

java -cp %CP% jade.Boot -gui ^
                      -services jade.core.event.NotificationService;examples.service.AMSProtectionService ^
					  -examples_service_AMSProtectionService_permissionsfile permissions.properties ^
					  -examples_service_AMSProtectionService_trustlocalagents false ^
					  -agents rma1:jade.tools.rma.rma

pause
