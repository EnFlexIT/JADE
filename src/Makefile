# Makefile for JADE project - src directory

# The following targets are not file names
.PHONY: all parsers classes stubs images realclean idlclean jjclean

MAKE = make
IDLCC = idltojava -fno-cpp
STUBGEN = rmic
PARSERGEN = javacc

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

ACLCLASSES = 	jade/lang/acl/ACLParser.java \
		jade/lang/acl/ACLParserConstants.java \
		jade/lang/acl/ACLParserTokenManager.java \
		jade/lang/acl/ASCII_CharStream.java \
		jade/lang/acl/ParseException.java \
		jade/lang/acl/Token.java \
		jade/lang/acl/TokenMgrError.java

AGENTMANCLASSES = jade/domain/AgentManagementParser.java \
		jade/domain/AgentManagementParserConstants.java \
		jade/domain/AgentManagementParserTokenManager.java \
		jade/domain/ASCII_CharStream.java \
		jade/domain/ParseException.java \
		jade/domain/Token.java \
		jade/domain/TokenMgrError.java 

SL0CLASSES =	jade/lang/sl/SL0Parser.java \
		jade/lang/sl/SL0ParserConstants.java \
		jade/lang/sl/SL0ParserTokenManager.java \
		jade/lang/sl/ASCII_UCodeESC_CharStream.java \
		jade/lang/sl/ParseException.java \
		jade/lang/sl/Token.java \
		jade/lang/sl/TokenMgrError.java

IDLSTUBS    =   _FIPA_Agent_97Stub.java \
		FIPA_Agent_97.java \
		FIPA_Agent_97Holder.java \
		FIPA_Agent_97Helper.java \
		_FIPA_Agent_97ImplBase.java

STUBS       = 	jade.core.AgentContainerImpl \
		jade.core.AgentPlatformImpl


all: $(IDLSTUBS) parsers classes stubs images
	@echo
	@echo Build complete.
	@echo

_%Stub.java %.java %Holder.java %Helper.java _%ImplBase.java: %.idl
	$(IDLCC) $<

parsers: ACLParser AgentManParser SL0Parser
	@echo Parsers generated

ACLParser: $(ACLCLASSES)
	@echo ACL Parser generated.

AgentManParser: $(AGENTMANCLASSES)
	@echo Parser for fipa-agent-management ontology generated.

SL0Parser: $(SL0CLASSES)
	@echo SL0 Parser generated.

$(ACLCLASSES): $(ACLSPEC)
	$(PARSERGEN) $(ACLFLAGS) $(ACLSPEC)

$(AGENTMANCLASSES): $(AGENTMANSPEC)
	$(PARSERGEN) $(AGENTMANFLAGS) $(AGENTMANSPEC)

$(SL0CLASSES): $(SL0SPEC)
	$(PARSERGEN) $(SL0FLAGS) $(SL0SPEC)

classes:
	$(JC) $(JFLAGS) $(EXECS)
	$(JC) $(JFLAGS) $(ROOTCLASSES)

stubs:
	$(STUBGEN) -d $(CLSDIR) $(STUBS)

images:
	mkdir -p $(CLSDIR)/jade/tools/DummyAgent
	mkdir -p $(CLSDIR)/jade/tools/rma
	mkdir -p $(CLSDIR)/jade/tools/sniffer
	cp -R $(SRCDIR)/jade/tools/DummyAgent/images $(CLSDIR)/jade/tools/DummyAgent
	cp -R $(SRCDIR)/jade/tools/rma/images $(CLSDIR)/jade/tools/rma
	cp -R $(SRCDIR)/jade/tools/sniffer/images $(CLSDIR)/jade/tools/sniffer

idlclean:
	rm -f _FIPA_Agent_97Stub.java
	rm -f FIPA_Agent_97.java
	rm -f FIPA_Agent_97Holder.java
	rm -f FIPA_Agent_97Helper.java
	rm -f _FIPA_Agent_97ImplBase.java

jjclean:
	rm -f $(ACLCLASSES)
	rm -f $(AGENTMANCLASSES)
	rm -f $(SL0CLASSES)

