echo modify the following line and set the default compiler and JVM to be 1.2
set PATH=c:\myprograms\jdk1.2.2\bin;c:\myprograms\javacc\bin;c:\winnt\system32
echo modify the following lines to set your JESS classpath
set JESS51=c:\myprograms\jess51
set JESS60=c:\myprograms\jess60a6\jess.jar
set ORBACUSJIDLPATH=C:\ORBacus\bin
set LOCALHOST=IBM10308

java -version
pause check now that the JVM is actually 1.2. Otherwise abort the testing

idlj
pause check that idlj is an unrecognized command such that the FIPa classes will remain clean. Otherwise abort the testing

echo off

set JADEJAR=..\..\lib\jade.jar
set JADEIIOP=..\..\lib\iiop.jar
set JADETOOLSJAR=..\..\lib\jadeTools.jar
set BASE64JAR=..\..\lib\Base64.jar
set ALLJADEJARS=%JADEJAR%;%JADEIIOP%;%JADETOOLSJAR%;%BASE64JAR%
set JADECLASSES=..\..\classes
set JAVA=java -Djava.compiler=""
echo 

set CLASSPATH=%ALLJADEJARS%;%JADECLASSES%

goto :SKIPCOMPILATION
echo compile JADE and the examples
cd ..\..
CALL makejade
pause
echo generating the jade jar
CALL makelib
pause 
CALL makeexamples
pause
goto :SKIPADDONS
echo remember to generate the batch files to compile the add-ons.
pause
echo start compiling the BEFipaMessage add-ons
cd add-ons\BEFipaMessage
CALL makebe
CALL makebelib
cd ..\http
echo start compiling the http add-ons
pause
CALL make
CALL makelib
echo start compiling the xmlacl add-ons
pause
cd ..\xmlacl
CALL make
CALL makelib
echo start compiling the ORBacusMTP add-ons remember to copy the OB.jar into jade\lib
pause
cd ..\ORBAcusMTP
set PATH=%PATH%;%ORBACUSJIDLPATH%
CALL makeORBacusMTP
pause
:SKIPADDONS
set CLASSPATH=%ALLJADEJARS%;%JADECLASSES%;%JESS51%
CALL makejessexample
pause
set CLASSPATH=%ALLJADEJARS%;%JADECLASSES%;%JESS60%
CALL makejadejessprotegeexample
pause
echo compile the Test code
cd src\test
javac -d %JADECLASSES% -classpath %JADECLASSES%;%JADEJAR% TestAgent.java jsp\TestDanielExample.java content\*.java
pause
:SKIPCOMPILATION

echo Starting the Agent Platform
START %JAVA% -cp %CLASSPATH% jade.Boot -gui -nomtp
echo Press a key when the platform is ready
pause

REM goto :STARTHERE 

echo Each example will be executed into a remote container. To pass to the
echo next example, just kill the container (NOT the platform) from the RMA GUI

echo Running the Party example. Select 1000 agents and check everything is ok
%JAVA% -cp %CLASSPATH% jade.Boot -container host:examples.party.HostAgent

echo Running Base64 example. Federate the DF where the reader is running
echo with the DF where the writer is running.
echo Test: DFFederation, DFSearch and registration, inter-platform IIOP Sun
echo communication, XML,bit-efficient,and String ACLCodecs, ACLMessage.content
echo both for setContentObject and setContent
%JAVA% -cp %CLASSPATH% jade.Boot -container reader:examples.Base64.ObjectReaderAgent 
START %JAVA% -cp %CLASSPATH% jade.Boot -gui -port 1200 writer:examples.Base64.ObjectWriterAgent

echo Running behaviours example
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.behaviours.ComplexBehaviourAgent
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.behaviours.FSMAgent
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.behaviours.TestReceiverBehaviourAgent
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.behaviours.WakerAgent

echo Running inProcess example
%JAVA% -cp %CLASSPATH% examples.inprocess.InProcessTest
%JAVA% -cp %CLASSPATH% examples.inprocess.InProcessTest -container

echo Running JadeJessProtege example FIXME. to do

echo Running jess example
echo Use a DummyAgent to send a CFP message and you should receive a PROPOSE
echo message back
cd ..
%JAVA% -cp %CLASSPATH%;%JESS51% jade.Boot -container jess:examples.jess.JessAgent
cd test

echo Running jsp example. The test works is a message arrives to buffer
START %JAVA% -cp %CLASSPATH% jade.Boot -container buffer:jade.tools.DummyAgent.DummyAgent 
pause press a Key when the container with buffer is ready
%JAVA% -cp %CLASSPATH% TestDanielExample

echo Running MessageTemplate example.
echo Remind to send a REQUEST message from the DummyAgent
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.MessageTemplate.WaitAgent

echo Running the Mobile example
echo Try also to clone, migrate and all the lifecycle of the mobile agent
echo by using the RMA GUI. Try also to create new agents from that RMA GUI.
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.mobile.MobileAgent

echo Running the Ontoloogy example
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.ontology.EngagerAgent b:examples.ontology.RequesterAgent

echo Running the PingAgent example
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.PingAgent.PingAgent

echo Running the protocols example
echo Everytime the initiator is blocked, it might be waiting forever for
echo an INFORM/FAILURE message that closes the protocol.
echo In such a case, simply
echo press a key and the responder will unblock the initiator.
%JAVA% -Djava.compiler="" -cp %CLASSPATH% jade.Boot -container ini:examples.protocols.ComplexInitiator(r1 r2) r1:examples.protocols.Responder  r2:examples.protocols.Responder 
%JAVA% -cp %CLASSPATH% examples.protocols.ProtocolTester r1 r2 r3
%JAVA% -Djava.compiler="" -cp %CLASSPATH% jade.Boot -container ini:examples.protocols.InitiatorHandler(r1 r2) r1:examples.protocols.ResponderHandler  r2:examples.protocols.ResponderHandler

echo Running the receivers example
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.receivers.AgentReceiver b:examples.receivers.AgentSender

echo Running the subdf example
START %JAVA% -cp %CLASSPATH% jade.Boot -container subDF:examples.subdf.SubDF

echo Running the thanksAgent example
%JAVA% -cp %CLASSPATH% jade.Boot -container t:examples.thanksAgent.ThanksAgent

echo Running the content example
%JAVA% -cp %CLASSPATH% jade.Boot -container sender:examples.content.Sender receiver:examples.content.Receiver

echo Running the demo
pause shutdown the current Agent Platform before continuing
cd ..\demo\MeetingScheduler
CALL run

echo Running the TestAgent (testing the messages)
echo type as input file testmessages.msg
cd test
%JAVA% -cp %CLASSPATH% test.TestAgent

echo Running the LEAP Testsuite 
cd ..\..\..\leapTestSuite
CALL makeTestSuite.bat
cd ..\jade\src\test
:STARTHERE

echo Running the behaviours test FIXME (per Giovanni Caire)

echo Running the content test 
pause Please SHUTDOWN any platform running
echo starting one sender and one receiver.
%JAVA% -cp %CLASSPATH% jade.Boot sender:Sender receiver:Receiver

echo Running the MessageTemplate test
pause Please SHUTDOWN any platform running
%JAVA% -cp %CLASSPATH% test.MessageTemplate.MessageTester

echo Testing the orbacus add-on
pause Please SHUTDOWN any platform running
echo starting two platform using ORBacusMTP. Try to send messages between the two platforms using the DummyAgents
echo Testing also the addRemotePlatform via RMA.
START %JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;..\..\lib\OB.jar;..\..\add-ons\ORBacusMTP\lib\iiopOB.jar jade.Boot -gui -port 1200 -mtp orbacus.MessageTransportProtocol(corbaloc:iiop:%LOCALHOST%:3000/jade) da0:jade.tools.DummyAgent.DummyAgent
START %JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;..\..\lib\OB.jar;..\..\add-ons\ORBacusMTP\lib\iiopOB.jar jade.Boot -gui -port 1300 -mtp orbacus.MessageTransportProtocol(corbaloc:iiop:%LOCALHOST%:4000/jade) da0:jade.tools.DummyAgent.DummyAgent
pause 

echo Testing the HTTP-MTP add-on
pause Please SHUTDOWN any platform running and copy the crimson parser into the jade\lib directory.
echo starting two platform using HTTP MTP. Try to send messages between the two platforms using the DummyAgents
START %JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;..\..\add-ons\http\lib\http.jar;..\..\lib\crimson.jar jade.Boot -gui -port 1200 -mtp jamr.jademtp.http.MessageTransportProtocol da0:jade.tools.DummyAgent.DummyAgent
START %JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;..\..\add-ons\http\lib\http.jar;..\..\lib\crimson.jar jade.Boot -gui -port 1300 -mtp jamr.jademtp.http.MessageTransportProtocol(http://%LOCALHOST%:7779/acc) da0:jade.tools.DummyAgent.DummyAgent
pause

echo Testing the XML ACLCodec add-on AND IIOP inter platform communication 
pause Please SHUTDOWN any platform running and verify to have the crimson.jar into the jade\lib directory.
echo starting two platform using XMLACLCODEC. Try to send messages between the two dummyagent using the xml codec.
echo Remember to set the envelope field AclRepresentation to the value: "fipa.acl.rep.xml.std" 
START %JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;%JADEIIOP%;..\..\lib\crimson.jar;..\..\add-ons\xmlacl\lib\xmlacl.jar jade.Boot -gui -port 1300 -aclcodec jamr.jadeacl.xml.XMLACLCodec da0:jade.tools.DummyAgent.DummyAgent
START %JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;%JADEIIOP%;..\..\lib\crimson.jar;..\..\add-ons\xmlacl\lib\xmlacl.jar jade.Boot -gui -port 1200 -aclcodec jamr.jadeacl.xml.XMLACLCodec da0:jade.tools.DummyAgent.DummyAgent
pause 

echo Testing the bit-efficient ACLCodec add-on and IIOP inter platform communication
pause Please SHUTDOWN any platform running.
echo starting two platform using BE ACLCodec. Try to send message between the two platforms using the DummyAgents.
echo remember to set the envelope field AclRepresentation to the value:  fipa.acl.rep.bitefficient.std 
START %JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;%JADEIIOP%;..\..\add-ons\BEFipaMessage\lib\BEFipaMessage.jar jade.Boot -gui -port 1300 -aclcodec sonera.fipa.acl.BitEffACLCodec da0:jade.tools.DummyAgent.DummyAgent
START %JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;%JADEIIOP%;..\..\add-ons\BEFipaMessage\lib\BEFipaMessage.jar jade.Boot -gui -port 1200 -aclcodec sonera.fipa.acl.BitEffACLCodec da0:jade.tools.DummyAgent.DummyAgent
pause

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
%JAVA% -cp %JADEJAR% jade.Boot -nomtp rma2:jade.tools.rma.rma
