echo modify the following line and set the default compiler and JVM to be 1.2
set PATH=c:\MyPrograms\jdk1.2.2\bin;%PATH%

java -version
pause check now that the JVM is actually 1.2. Otherwise abort the testing

idlj
pause check that idlj is an unrecognized command such that the FIPa classes will remain clean. Otherwise abort the testing

echo modify the following 3 lines to set your classpath
REM set CLASSPATH=..\..\lib\jade.jar;..\..\lib\jadeTools.jar;..\..\lib\iiop.jar;..\..\lib\Base64.jar;..\..\classes
set JESS51=c:\myprograms\jess51
set CLASSPATH=..\..\classes;%JESS51%
set JADEJAR=..\..\lib\jade.jar

goto :SKIPCOMPILATION
echo compile JADE and the examples
cd ..\..
CALL makejade
CALL makeexamples
CALL makejessexample
cd ..\test
echo compile the Test code
javac -d ..\..\classes -classpath ..\..\classes TestAgent.java jsp\TestDanielExample.java

:SKIPCOMPILATION

echo Starting the Agent Platform
START java -cp %CLASSPATH% jade.Boot -gui
pause Press a key when the platform is ready

goto :STARTHERE 

echo Running Base64 example
java -cp %CLASSPATH% jade.Boot -container a:examples.Base64.ObjectReaderAgent b:examples.Base64.ObjectWriterAgent

echo Running behaviours example
java -cp %CLASSPATH% jade.Boot -container a:examples.behaviours.ComplexBehaviourAgent
java -cp %CLASSPATH% jade.Boot -container a:examples.behaviours.FSMAgent
java -cp %CLASSPATH% jade.Boot -container a:examples.behaviours.TestReceiverBehaviourAgent
java -cp %CLASSPATH% jade.Boot -container a:examples.behaviours.WakerAgent

echo Running inProcess example
java -cp %CLASSPATH% examples.inprocess.InProcessTest
java -cp %CLASSPATH% examples.inprocess.InProcessTest -container

echo Running JadeJessProtege example FIXME

echo Running jess example
cd ..
java -cp %CLASSPATH%;..\classes jade.Boot -container jess:examples.jess.JessAgent
cd test

echo Running jsp example. The test works is a message arrives to buffer
START java -cp %CLASSPATH% jade.Boot -container buffer:jade.tools.DummyAgent.DummyAgent 
pause press a Key when the container with buffer is ready
java -cp %CLASSPATH% TestDanielExample

echo Running MessageTemplate example.
echo Remind to send a REQUEST message from the DummyAgent
java -cp %CLASSPATH% jade.Boot -container a:examples.MessageTemplate.WaitAgent

echo Running the Mobile example
java -cp %CLASSPATH% jade.Boot -container a:examples.mobile.MobileAgent

echo Running the Ontoloogy example
java -cp %CLASSPATH% jade.Boot -container a:examples.ontology.EngagerAgent b:examples.ontology.RequesterAgent

echo Running the PingAgent example
java -cp %CLASSPATH% jade.Boot -container a:examples.PingAgent.PingAgent

echo Running the protocols example
echo Everytime the initiator is blocked, it might be waiting forever for
echo an INFORM/FAILURE message that closes the protocol.
echo In such a case, just send it by using the DummyAgent (remind to match
echo both conversation-id and in-reply-to field values)
java -cp %CLASSPATH% examples.protocols.ProtocolTester r1 r2 r3

echo Running the receivers example
java -cp %CLASSPATH% jade.Boot -container a:examples.receivers.AgentReceiver b:examples.receivers.AgentSender

echo Running the subdf example
START java -cp %CLASSPATH% jade.Boot -container subDF:examples.subdf.SubDF

echo Running the thanksAgent example
java -cp %CLASSPATH% jade.Boot -container t:examples.thanksAgent.ThanksAgent

:STARTHERE

echo Running the demo
pause shutdown the current Agent Platform before continuing
cd ..\demo\MeetingScheduler
CALL run

echo Running the TestAgent (testing the messages)
echo type as input file testmessages.msg
cd test
java -cp %CLASSPATH% test.TestAgent

echo Running the LEAP Testsuite 
cd ..\..\..\leapTestSuite
CALL makeTestSuite.bat
cd ..\jade\src\test

echo Running the behaviours test FIXME (per Giovanni Caire)

echo Running the content test FIXME (per Federico Bergenti)

echo Testing the orbacus add-on FIXME (per Tiziana)

echo Testing the HTTP-MTP add-on FIXME (per Tiziana)

echo Testing the XML ACLCodec add-on FIXME (per Tiziana)

echo Testing the bit-efficient ACLCodec add-on FIXME (per Tiziana)

echo Test inter-platform communication FIXME

echo Test if calling jade.jar works. Just the RMA GUI must appear properly
cd ..\..
CALL makelib
java -cp %JADEJAR% jade.Boot -nomtp rma2:jade.tools.rma.rma