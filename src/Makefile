# Makefile for JADE project - src directory

# The following targets are not file names
.PHONY: all clean

export MAKE = make
export IDLCC = idltojava
export STUBGEN = rmic
export PARSERGEN = javacc

export JC = javac
export JFLAGS = -deprecation


ACLFLAGS    =   -OUTPUT_DIRECTORY:jade/lang/acl
ACLSPEC     = 	jade/lang/acl/ACLParser.jj

AGENTMANFLAGS =	-OUTPUT_DIRECTORY:jade/domain
AGENTMANSPEC  =	jade/domain/AgentManagementParser.jj

EXECS       = 	jade/Boot.java
IDLSPEC     =   FIPA_Agent_97.idl
ROOTCLASSES =	jade/core/ComplexBehaviour.java \
		jade/core/NonDeterministicBehaviour.java \
		jade/core/SequentialBehaviour.java \
		jade/core/SimpleBehaviour.java \
		jade/core/SenderBehaviour.java \
		jade/core/ReceiverBehaviour.java \
		jade/domain/rma.java \
		jade/domain/SearchDFBehaviour.java \
		jade/proto/ProtocolDrivenBehaviour.java \
		jade/proto/Standard.java \
		jade/proto/Waker.java \
		jade/proto/FipaRequestInitiatorBehaviour.java \
		jade/proto/FipaRequestResponderBehaviour.java \
		jade/proto/FipaContractNetInitiatorBehaviour.java \
		jade/proto/FipaContractNetResponderBehaviour.java \
		jade/proto/FipaQueryResponderBehaviour.java \
		jade/gui/AMSMainFrame.java
STUBDIR     =	.
IDLSTUBS    =   _FIPA_Agent_97Stub.java \
		FIPA_Agent_97.java \
		FIPA_Agent_97Holder.java \
		FIPA_Agent_97Helper.java \
		_FIPA_Agent_97ImplBase.java

STUBS       = 	jade.core.AgentContainerImpl \
		jade.core.AgentPlatformImpl


all: idl parsers classes stubs
	@echo
	@echo Build complete.
	@echo

idl:
	$(IDLCC) $(IDLSPEC)

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
	@set CLASSPATH=$(SRCDIR); export CLASSPATH; \
	$(STUBGEN) -d $(STUBDIR) $(STUBS)

clean:
	rm -f `find . -name '*~' -print`
	rm -f `find . -name '#*#' -print`
	rm -f `find . -name '*.class' -print`

	rm -f _FIPA_Agent_97Stub.java
	rm -f FIPA_Agent_97.java
	rm -f FIPA_Agent_97Holder.java
	rm -f FIPA_Agent_97Helper.java
	rm -f _FIPA_Agent_97ImplBase.java


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




