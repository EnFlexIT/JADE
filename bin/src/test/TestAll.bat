echo modify the following line and set the default compiler and JVM to be 1.2
set PATH=c:\myprograms\jdk1.2.2\bin;c:\myprograms\javacc\bin;c:\winnt\system32
echo modify the following lines to set your JESS classpath
set JESS51=c:\myprograms\jess51
set JESS60=c:\myprograms\jess60a6\jess.jar
set ORBACUSJIDLPATH=C:\ORBacus\bin
set LOCALHOST=fbellif
set JADEHOME=c:\projects\jade

java -version
pause check now that the JVM is actually 1.2. Otherwise abort the testing

idlj
pause check that idlj is an unrecognized command such that the FIPa classes will remain clean. Otherwise abort the testing

echo off

set JADEJAR=%JADEHOME%\lib\jade.jar
set JADEIIOP=%JADEHOME%\lib\iiop.jar
set JADETOOLSJAR=%JADEHOME%\lib\jadeTools.jar
set BASE64JAR=%JADEHOME%\lib\Base64.jar
set ALLJADEJARS=%JADEJAR%;%JADEIIOP%;%JADETOOLSJAR%;%BASE64JAR%
set HTTPJARS=%JADEHOME%\add-ons\http\lib\http.jar;%JADEHOME%\lib\crimson.jar
set ORBACUSJARS=%JADEHOME%\lib\OB.jar;%JADEHOME%\lib\OBNaming.jar;%JADEHOME%\add-ons\ORBacusMTP\lib\iiopOB.jar
set JADECLASSES=%JADEHOME%\classes
set JAVA=java -Djava.compiler=""
echo 

set CLASSPATH=%JADECLASSES%;%ALLJADEJARS%

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
pause
echo start compiling the RDFCodec add-on. Remember to copy rdf-api-2001-01-19.jar into jade\add-ons\RDFCodec\lib
pause
cd ..\RDFCodec
CALL makeRDF
CALL makelib
CALL makeexamples
echo start compiling the ORBacusMTP add-ons remember to copy the OB.jar and OBNaming.jar into jade\lib
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
cd src
javac -d %JADECLASSES% -classpath %JADECLASSES%;%JADEJAR%;. test\TestAgent.java test\content\*.java test\wrapper\*.java test\proto\*.java test\MessageTemplate\*.java test\common\*.java test\content\tests\*.java test\proto\tests\*.java test\proto\tests\contractNet\*.java test\proto\responderBehaviours\*.java test\proto\responderBehaviours\achieveRE\*.java test\proto\responderBehaviours\contractNet\*.java test\roundTripTime\*.java
cd test
pause
:SKIPCOMPILATION

REM End compilation task. Start testing task. WorkingDir = src\test

goto :STARTHERE

echo Starting the Agent Platform
START %JAVA% -cp %CLASSPATH%;..\..\lib\crimson.jar;..\..\add-ons\xmlacl\lib\xmlacl.jar;..\..\add-ons\BEFipaMessage\lib\BEFipaMessage.jar jade.Boot -gui -aclcodec jamr.jadeacl.xml.XMLACLCodec;sonera.fipa.acl.BitEffACLCodec 
echo Press a key when the platform is ready
pause

echo Each example will be executed into a remote container. To pass to the
echo next example, just kill the container (NOT the platform) from the RMA GUI
echo
echo Running the Party example. Select 1000 agents and check everything is ok
%JAVA% -cp %CLASSPATH% jade.Boot -container host:examples.party.HostAgent

echo Running Base64 example. Federate the DF where the reader is running
echo with the DF where the writer is running.
echo Test: DFFederation, DFSearch and registration, inter-platform IIOP Sun
echo communication, XML,bit-efficient,and String ACLCodecs, ACLMessage.content
echo both for setContentObject and setContent
START %JAVA% -cp %CLASSPATH%;..\..\lib\crimson.jar;..\..\add-ons\xmlacl\lib\xmlacl.jar;..\..\add-ons\BEFipaMessage\lib\BEFipaMessage.jar jade.Boot -gui -port 1200 -aclcodec jamr.jadeacl.xml.XMLACLCodec;sonera.fipa.acl.BitEffACLCodec writer:examples.Base64.ObjectWriterAgent
%JAVA% -cp %CLASSPATH%;..\..\lib\crimson.jar;..\..\add-ons\xmlacl\lib\xmlacl.jar;..\..\add-ons\BEFipaMessage\lib\BEFipaMessage.jar jade.Boot -aclcodec jamr.jadeacl.xml.XMLACLCodec;sonera.fipa.acl.BitEffACLCodec -container reader:examples.Base64.ObjectReaderAgent 


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
REM FIXME %JAVA% -cp %CLASSPATH%;%JESS51% jade.Boot -container jess:examples.jess.JessAgent
cd test

echo Running jsp example. The test works is a message arrives to buffer
echo FIXME. Still to do

echo Running MessageTemplate example.
echo Remind to send a REQUEST message from the DummyAgent
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.MessageTemplate.WaitAgent

echo Running the Mobile example
echo Try also to clone, migrate and all the lifecycle of the mobile agent
echo by using the RMA GUI. Try also to create new agents from that RMA GUI.
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.mobile.MobileAgent

echo Running the Ontology example
echo remind to enter 'a', i.e. the local name of the EngagerAgent
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.ontology.EngagerAgent b:examples.ontology.RequesterAgent

echo Running the PingAgent example
echo Start a DummyAgent and send Ping messages
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.PingAgent.PingAgent

echo Running the protocols example
echo Everytime the initiator is blocked, it might be waiting forever for
echo an INFORM/FAILURE message that closes the protocol.
echo In such a case, simply
echo press a key and the responder will unblock the initiator.
%JAVA% -Djava.compiler="" -cp %CLASSPATH% jade.Boot -container ini:examples.protocols.ComplexInitiator(r1 r2) r1:examples.protocols.Responder  r2:examples.protocols.Responder 
%JAVA% -cp %CLASSPATH% examples.protocols.ProtocolTester r1 r2 r3
%JAVA% -Djava.compiler="" -cp %CLASSPATH% jade.Boot -container ini:examples.protocols.InitiatorHandler(r1 r2) r1:examples.protocols.ResponderHandler  r2:examples.protocols.ResponderHandler

echo Running the receivers example. a is the responder and b is the sender
%JAVA% -cp %CLASSPATH% jade.Boot -container a:examples.receivers.AgentReceiver b:examples.receivers.AgentSender

echo Running the subdf example
START %JAVA% -cp %CLASSPATH% jade.Boot -container subDF:examples.subdf.SubDF

echo Running the thanksAgent example
%JAVA% -cp %CLASSPATH% jade.Boot -container t:examples.thanksAgent.ThanksAgent

echo Running the content example
%JAVA% -cp %CLASSPATH% jade.Boot -container sender:examples.content.Sender receiver:examples.content.Receiver

echo Running the demo
pause shutdown the current Agent Platform before continuing
cd ..\..\..\demo
CALL runDemo

echo Running the TestAgent (testing the messages)
echo type as input file testmessages.msg
cd test
%JAVA% -cp %CLASSPATH% test.TestAgent

echo Running the Test of the Wrapper
%JAVA% -cp %CLASSPATH% test.wrapper.TestListener

echo Running the roundTripTime test on a single container. 10 couples=5.34msec (JADE 2.4)
echo This test tests also the configuration file and setting arguments to agents
echo Kill all running platforms
%JAVA% -cp %CLASSPATH% jade.Boot -conf roundTripTime\SingleContainer.conf
pause

:STARTHERE	

echo Running the roundTripTime test on 2 containers. 10 couples=33.52msec (JADE 2.4)
START %JAVA% -cp %CLASSPATH% jade.Boot -conf roundTripTime\Receiver.conf 
pause press a key when the platform is ready
%JAVA% -cp %CLASSPATH% jade.Boot -conf roundTripTime\RoundTripperContainer.conf 
pause

echo Running the roundTripTime test on 2 platforms. 
echo Edit RoundTripper.conf with the right IOR of the running platform and
echo set container=false
%JAVA% -cp %CLASSPATH% jade.Boot -conf roundTripTime\RoundTripper.conf 
pause

echo Running the roundTripTime test on 2 platforms with HTTP-based MTP
echo Kill any platform that is currently running
pause
START %JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;%HTTPJARS%;%JADECLASSES% jade.Boot -conf roundTripTime\RX_http.conf
%JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;%HTTPJARS%;%JADECLASSES% jade.Boot -conf roundTripTime\RT_http.conf
pause

echo Running the roundTripTime test on 2 platforms with ORBacus-based MTP
echo Kill any platform that is currently running
pause
START %JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;%ORBACUSJARS%;%JADECLASSES% jade.Boot -conf roundTripTime\RX_ORBacus.conf
%JAVA% -cp %JADEJAR%;%JADETOOLSJAR%;%ORBACUSJARS%;%JADECLASSES% jade.Boot -conf roundTripTime\RT_ORBacus.conf
pause




echo Running the tests on CL and ontology support for LEAP Codec
%JAVA% -cp %CLASSPATH% test.content.ContentTesterAgent
pause
echo Running the tests on CL and ontology support for SL Codec
%JAVA% -cp %CLASSPATH% test.content.ContentTesterAgent jade.content.lang.sl.SLCodec
pause
	
echo Running the tests on fipa-contract-net protocol
%JAVA% -cp %CLASSPATH% test.proto.ContractNetTesterAgent 
pause

echo Running the LEAP Testsuite 
cd ..\..\..\leapTestSuite
CALL makeTestSuite.bat
cd ..\jade\src\test

echo Running the behaviours test FIXME (per Giovanni Caire)

echo Running the content test 
pause Please SHUTDOWN any platform running
echo starting one sender and one receiver.
REM FIXME %JAVA% -cp %CLASSPATH% jade.Boot sender:Sender receiver:Receiver

echo Running the MessageTemplate test
pause Please SHUTDOWN any platform running
%JAVA% -cp %CLASSPATH% test.MessageTemplate.MessageTester

echo RDFCodec Test
dir
START %JAVA% -cp %ALLJADEJARS%;..\..\add-ons\http\lib\sax2\sax2.jar;..\..\lib\rdf-api-2001-19.jar;..\..\lib\xerces.jar;..\..\add-ons\RDFCodec\classes jade.Boot -gui sender:examples.rdfcontent.Sender receiver:examples.rdfcontent.Receiver
pause 

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
