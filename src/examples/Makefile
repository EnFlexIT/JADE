# Makefile for JADE project - examples directory

# The following targets are not file names
.PHONY: all clean

JC = javac
JFLAGS = -deprecation

TARGETS = ex1/Agent1.java ex1/Agent2.java ex1/Agent3.java ex1/Agent4.java \
	  ex2/AgentSender.java ex2/AgentReceiver.java ex2/AgentBroadcaster.java \
	  ex2/AgentTimeout.java \
	  ex2/AboutDialog.java ex2/SentReceivedGUI.java ex2/FilebasedSenderAgent.java \
	  ex3/AgentMessageFilter.java ex3/AgentThread.java \
	  ex4/AgentRequester.java ex4/AgentResponder.java \
	  ex5/amsTester.java ex5/dfTester.java ex5/SubDF.java ex5/MobileAgent.java \
	  ex6/AgQueryInit.java ex6/AgQueryResp.java \
	  ex7/ObjectReaderAgent.java ex7/ObjectWriterAgent.java ex7/OntologyTest.java

all: 
	@echo Making test examples
	set CLASSPATH=$(CLASSPATH):$(LIBDIR)/$(LIBNAME); export CLASSPATH; \
	$(JC) $(JFLAGS) $(TARGETS)

clean:
	rm -f `find . -name '*~' -print`
	rm -f `find . -name '#*#' -print`
	rm -f `find . -name '*.class' -print`


