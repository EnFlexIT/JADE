echo modify the following line and set the default compiler and JVM to be 1.2
set PATH=c:\MyPrograms\jdk1.2.2\bin;c:\MyPrograms\javacc\bin;c:\winnt\system32

java -version
pause check now that the JVM is actually 1.2. Otherwise abort the testing

idlj
pause check that idlj is an unrecognized command such that the FIPa classes will remain clean. Otherwise abort the testing

set JADEJAR=..\..\lib\jade.jar
set ALLJADEJARS=%JADEJAR%;..\..\lib\jadeTools.jar;..\..\lib\iiop.jar;..\..\lib\Base64.jar
set JADECLASSES=..\..\classes

echo modify the following lines to set your JESS classpath
set JESS51=c:\myprograms\jess51
set JESS60=c:\myprograms\jess60a6\jess.jar
echo 

set CLASSPATH=%ALLJADEJARS%;%JADECLASSES%

REM goto :SKIPCOMPILATION
echo compile JADE and the examples
cd ..\..
CALL makejade
pause 
CALL makeexamples
pause
set CLASSPATH=%ALLJADEJARS%;%JADECLASSES%;%JESS51%
CALL makejessexample
pause
set CLASSPATH=%ALLJADEJARS%;%JADECLASSES%;%JESS60%
CALL makejadejessprotegeexample
pause
echo compile the Test code
cd src\test
javac -d %JADECLASSES% -classpath %JADECLASSES% TestAgent.java jsp\TestDanielExample.java
pause

:SKIPCOMPILATION

echo Starting the Agent Platform
START java -cp %CLASSPATH% jade.Boot -gui
pause Press a key when the platform is ready

REM goto :STARTHERE 

echo Each example will be executed into a remote container. To pass to the
echo next example, just kill the container (NOT the platform) from the RMA GUI
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

echo Running JadeJessProtege example FIXME. to do

echo Running jess example
echo Use a DummyAgent to send a CFP message and you should receive a PROPOSE
echo message back
cd ..
java -cp %CLASSPATH%;%JESS51% jade.Boot -container jess:examples.jess.JessAgent
cd test

echo Running jsp example. The test works is a message arrives to buffer
START java -cp %CLASSPATH% jade.Boot -container buffer:jade.tools.DummyAgent.DummyAgent 
pause press a Key when the container with buffer is ready
java -cp %CLASSPATH% TestDanielExample

echo Running MessageTemplate example.
echo Remind to send a REQUEST message from the DummyAgent
java -cp %CLASSPATH% jade.Boot -container a:examples.MessageTemplate.WaitAgent

echo Running the Mobile example
echo Try also to clone, migrate and all the lifecycle of the mobile agent
echo by using the RMA GUI. Try also to create new agents from that RMA GUI.
java -cp %CLASSPATH% jade.Boot -container a:examples.mobile.MobileAgent

echo Running the Ontoloogy example
java -cp %CLASSPATH% jade.Boot -container a:examples.ontology.EngagerAgent b:examples.ontology.RequesterAgent

echo Running the PingAgent example
java -cp %CLASSPATH% jade.Boot -container a:examples.PingAgent.PingAgent

echo Running the protocols example
echo Everytime the initiator is blocked, it might be waiting forever for
echo an INFORM/FAILURE message that closes the protocol.
echo In such a case, simply
echo press a key and the responder will unblock the initiator.
java -Djava.compiler="" -cp %CLASSPATH% jade.Boot -container ini:examples.protocols.ComplexInitiator(r1 r2) r1:examples.protocols.Responder  r2:examples.protocols.Responder 
java -cp %CLASSPATH% examples.protocols.ProtocolTester r1 r2 r3
java -Djava.compiler="" -cp %CLASSPATH% jade.Boot -container ini:examples.protocols.InitiatorHandler(r1 r2) r1:examples.protocols.ResponderHandler  r2:examples.protocols.ResponderHandler

echo Running the receivers example
java -cp %CLASSPATH% jade.Boot -container a:examples.receivers.AgentReceiver b:examples.receivers.AgentSender

echo Running the subdf example
START java -cp %CLASSPATH% jade.Boot -container subDF:examples.subdf.SubDF

echo Running the thanksAgent example
java -cp %CLASSPATH% jade.Boot -container t:examples.thanksAgent.ThanksAgent

echo Running the content example
java -cp %CLASSPATH% jade.Boot -container sender:examples.content.Sender receiver:examples.content.Receiver

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
:STARTHERE

echo Running the behaviours test FIXME (per Giovanni Caire)

echo Running the content test FIXME (per Federico Bergenti)

echo Testing the orbacus add-on FIXME (per Tiziana)

echo Testing the HTTP-MTP add-on FIXME (per Tiziana)

echo Testing the XML ACLCodec add-on FIXME (per Tiziana)

echo Testing the bit-efficient ACLCodec add-on FIXME (per Tiziana)

echo Test inter-platform communication FIXME

echo Test if calling jade.jar works. Just the RMA GUI must appear properly
echo Test also the Graphical Tools:
echo DummyAgent: all the menu items + all the image buttons + drag&drop of a message (use DragAndDropMessage.txt)
echo Sniffer: all the menu items + all the image buttons + click on the arrows
echo Introspector: FIXME
echo RMA: FIXME
echo DFGUI: FIXME
cd ..\..
CALL makelib
cd ..\src\test
java -cp %JADEJAR% jade.Boot -nomtp rma2:jade.tools.rma.rma
