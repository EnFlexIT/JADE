@ECHO OFF

SET JAVA_BIN=D:\leap\Programme\jdk1.3.0_SE\bin
SET PRC=D:\1\lib
SET KJAVA_CLASSES=D:\leap\Programme\Sun_J2ME_CLDC\bin\api\classes

IF "%1" == "OBF" (

%JAVA_BIN%\java -classpath %KJAVA_CLASSES% palm.database.MakePalmApp -bootclasspath %KJAVA_CLASSES% -networking -v -name LEAP -o %PRC%\leap.prc -JARtoPRC %PRC%\palm.jar jade.a

) ELSE (

%JAVA_BIN%\java -classpath %KJAVA_CLASSES% palm.database.MakePalmApp -bootclasspath %KJAVA_CLASSES% -networking -v -name LEAP -o %PRC%\leap.prc -JARtoPRC %PRC%\palm.jar jade.Boot

)