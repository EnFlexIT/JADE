# Makefile for JADE project - src directory

# The following targets are not file names
.PHONY: all clean

export MAKE = make
export STUBGEN = rmic
export PARSERGEN = javacc

export JC = javac
export JFLAGS = -deprecation


ACLFLAGS    =   -OUTPUT_DIRECTORY:jade/lang/acl
ACLSPEC     = 	jade/lang/acl/ACLParser.jj

AGENTMANFLAGS =	-OUTPUT_DIRECTORY:jade/domain
AGENTMANSPEC  =	jade/domain/AgentManagementParser.jj

EXECS       = 	jade/Boot.java
ROOTCLASSES =	jade/core/ComplexBehaviour.java \
		jade/core/NonDeterministicBehaviour.java \
		jade/core/SequentialBehaviour.java \
		jade/core/SimpleBehaviour.java \
		jade/core/SenderBehaviour.java \
		jade/core/ReceiverBehaviour.java \
		jade/domain/rma.java \
		jade/proto/ProtocolDrivenBehaviour.java \
		jade/proto/Standard.java \
		jade/gui/AMSMainFrame.java
STUBDIR     =	.
STUBS       = 	jade.core.AgentContainerImpl \
		jade.core.AgentPlatformImpl


all: parsers classes stubs
	@echo
	@echo Build complete.
	@echo

parsers: ACLParser AgentManParser
	@echo Parsers generated

ACLParser:
	$(PARSERGEN) $(ACLFLAGS) $(ACLSPEC)

AgentManParser:
	$(PARSERGEN) $(AGENTMANFLAGS) $(AGENTMANSPEC)

classes:
	$(JC) $(JFLAGS) $(EXECS)
	$(JC) $(JFLAGS) $(ROOTCLASSES)

stubs:
	@export CLASSPATH=$(SRCDIR);       \
	$(STUBGEN) -d $(STUBDIR) $(STUBS)

clean:
	rm -f `find . -name '*~' -print`
	rm -f `find . -name '#*#' -print`
	rm -f `find . -name '*.class' -print`

	rm -f jade/lang/acl/ACLParser.java
	rm -f jade/lang/acl/ACLParserConstants.java
	rm -f jade/lang/acl/ACLParserTokenManager.java
	rm -f jade/lang/acl/ASCII_CharStream.java
	rm -f jade/lang/acl/ParseException.java
	rm -f jade/lang/acl/Token.java
	rm -f jade/lang/acl/TokenMgrError.java

	rm -f jade/domain/AgentManagementParser.java
	rm -f jade/domain/AgentManagementParserConstants.java
	rm -f jade/domain/AgentManagementParserTokenManager.java
	rm -f jade/domain/ASCII_CharStream.java
	rm -f jade/domain/ParseException.java
	rm -f jade/domain/Token.java
	rm -f jade/domain/TokenMgrError.java




