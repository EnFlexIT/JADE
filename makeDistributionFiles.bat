@REM =======================================================================
@REM This batch file creates the distribution files
@REM =======================================================================
cd ..
jar c0vf jade\distribution\jadeBin.zip jade\lib\jade.jar jade\lib\jadeTools.jar jade\lib\Base64.jar jade\src\starlight
echo "Remind to put in PDF the Programmer' Guide!"
jar c0vf jade\distribution\jadeDoc.zip jade\doc
jar c0vf jade\distribution\jadeSrc.zip jade\src\*.java jade\src\*.idl jade\src\Makefile jade\src\*.html jade\src\jade
jar c0vf jade\distribution\jadeExamples.zip jade\src\examples jade\src\demo
pause