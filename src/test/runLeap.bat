@echo off

%JAVA_HOME%\bin\java -cp ..\..\leap\j2se\classes;..\..\add-ons\testSuite\lib\testSuite.jar;..\..\classes test.common.testSuite.TestSuiteAgent %1 %2 %3 %4 %5 %6 %7 %8 %9
pause

