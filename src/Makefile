# Makefile for JADE project - src directory

# The following targets are not file names
.PHONY: all clean idlclean

export MAKE = make
export IDLCC = idltojava -fno-cpp
export STUBGEN = rmic
export PARSERGEN = javacc

export JC = javac
export JFLAGS = -deprecation


ACLFLAGS    =   -OUTPUT_DIRECTORY:jade/lang/acl
ACLSPEC     = 	jade/lang/acl/ACLParser.jj

AGENTMANFLAGS =	-OUTPUT_DIRECTORY:jade/domain
AGENTMANSPEC  =	jade/domain/AgentManagementParser.jj

SL0FLAGS    =   -OUTPUT_DIRECTORY:jade/lang/sl
SL0SPEC     =   jade/lang/sl/SL0Parser.jj

EXECS       = 	jade/Boot.java
IDLSPEC     =   FIPA_Agent_97.idl
ROOTCLASSES =	jade/core/behaviours/ComplexBehaviour.java \
		jade/core/behaviours/NonDeterministicBehaviour.java \
		jade/core/behaviours/SequentialBehaviour.java \
		jade/core/behaviours/SimpleBehaviour.java \
		jade/core/behaviours/SenderBehaviour.java \
		jade/core/behaviours/ReceiverBehaviour.java \
		jade/domain/SearchDFBehaviour.java \
		jade/proto/ProtocolDrivenBehaviour.java \
		jade/proto/Standard.java \
		jade/proto/FipaRequestInitiatorBehaviour.java \
		jade/proto/FipaRequestResponderBehaviour.java \
		jade/proto/FipaContractNetInitiatorBehaviour.java \
		jade/proto/FipaContractNetResponderBehaviour.java \
		jade/proto/FipaQueryInitiatorBehaviour.java \
		jade/proto/FipaQueryResponderBehaviour.java \
		jade/onto/DefaultOntology.java \
		jade/tools/rma/rma.java \
		jade/tools/sniffer/Sniffer.java \
		jade/tools/DummyAgent/DummyAgent.java \
		jade/tools/SocketProxyAgent/SocketProxyAgent.java

STUBDIR     =	.
IDLSTUBS    =   _FIPA_Agent_97Stub.java \
		FIPA_Agent_97.java \
		FIPA_Agent_97Holder.java \
		FIPA_Agent_97Helper.java \
		_FIPA_Agent_97ImplBase.java

STUBS       = 	jade.core.AgentContainerImpl \
		jade.core.AgentPlatformImpl


all: $(IDLSTUBS) parsers classes stubs
	@echo
	@echo Build complete.
	@echo

_%Stub.java %.java %Holder.java %Helper.java _%ImplBase.java: %.idl
	$(IDLCC) $<

parsers: ACLParser AgentManParser SL0Parser
	@echo Parsers generated

ACLParser:
	$(PARSERGEN) $(ACLFLAGS) $(ACLSPEC)

AgentManParser:
	$(PARSERGEN) $(AGENTMANFLAGS) $(AGENTMANSPEC)

SL0Parser:
	$(PARSERGEN) $(SL0FLAGS) $(SL0SPEC)

classes:
	$(JC) $(JFLAGS) $(EXECS)
	$(JC) $(JFLAGS) $(ROOTCLASSES)

stubs:
	@set CLASSPATH=$(SRCDIR); export CLASSPATH; \
	$(STUBGEN) -d $(STUBDIR) $(STUBS)

idlclean:
	rm -f _FIPA_Agent_97Stub.java
	rm -f FIPA_Agent_97.java
	rm -f FIPA_Agent_97Holder.java
	rm -f FIPA_Agent_97Helper.java
	rm -f _FIPA_Agent_97ImplBase.java



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

	rm -f jade/lang/sl/SL0Parser.java
	rm -f jade/lang/sl/SL0ParserConstants.java
	rm -f jade/lang/sl/SL0ParserTokenManager.java
	rm -f jade/lang/sl/ASCII_UCodeESC_CharStream.java
	rm -f jade/lang/sl/ParseException.java
	rm -f jade/lang/sl/Token.java
	rm -f jade/lang/sl/TokenMgrError.java

	rm -f jade/domain/AgentManagementParser.java
	rm -f jade/domain/AgentManagementParserConstants.java
	rm -f jade/domain/AgentManagementParserTokenManager.java
	rm -f jade/domain/ASCII_CharStream.java
	rm -f jade/domain/ParseException.java
	rm -f jade/domain/Token.java
	rm -f jade/domain/TokenMgrError.java




