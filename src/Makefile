# Makefile for JADE project - src directory

# The following targets are not file names
.PHONY: all idl parsers classes stubs images realclean idlclean jjclean batch

MAKE = make
IDLCC = idlj -fall
STUBGEN = rmic
PARSERGEN = javacc

ACLFLAGS    =   -OUTPUT_DIRECTORY:jade/lang/acl
ACLSPEC     = 	jade/lang/acl/ACLParser.jj

SL0FLAGS    =   -OUTPUT_DIRECTORY:jade/lang/sl
SL0SPEC     =   jade/lang/sl/SL0Parser.jj

SLFLAGS     =   -OUTPUT_DIRECTORY:jade/content/lang/sl
SLSPEC      =   jade/content/lang/sl/SLParser.jj

EXECS       = 	jade/Boot.java jade/core/Runtime.java
IDLSPEC     =   fipa.idl
ROOTCLASSES =	jade/core/FullAcc.java \
		jade/core/RealMobilityManager.java \
		jade/core/DummyMobilityManager.java \
		jade/core/DummyNotificationManager.java \
		jade/core/LightAcc.java \
		jade/core/LightResourceManager.java \
		jade/core/behaviours/CompositeBehaviour.java \
		jade/core/behaviours/NonDeterministicBehaviour.java \
		jade/core/behaviours/SequentialBehaviour.java \
		jade/core/behaviours/SimpleBehaviour.java \
		jade/core/behaviours/SenderBehaviour.java \
		jade/core/behaviours/ReceiverBehaviour.java \
		jade/core/behaviours/WakerBehaviour.java \
		jade/core/behaviours/FSMBehaviour.java \
		jade/domain/MobilityOntology.java \
		jade/domain/DFServiceCommunicator.java \
		jade/domain/AMSServiceCommunicator.java \
		jade/domain/FIPANames.java \
		jade/proto/FipaRequestInitiatorBehaviour.java \
		jade/proto/FipaRequestResponderBehaviour.java \
		jade/proto/FipaContractNetInitiatorBehaviour.java \
		jade/proto/FipaContractNetResponderBehaviour.java \
		jade/proto/FipaQueryInitiatorBehaviour.java \
		jade/proto/FipaQueryResponderBehaviour.java \
		jade/proto/AchieveREInitiator.java \
		jade/proto/AchieveREResponder.java \
		jade/proto/ContractNetInitiator.java \
		jade/proto/ContractNetResponder.java \
		jade/proto/states/HandlerSelector.java \
		jade/security/dummy/DummyAuthority.java \
		jade/tools/rma/rma.java \
		jade/tools/DummyAgent/DummyAgent.java \
		jade/mtp/iiop/MessageTransportProtocol.java \
		jade/tools/sniffer/Sniffer.java \
		jade/tools/introspector/Introspector.java \
		jade/tools/SocketProxyAgent/SocketProxyAgent.java \
		jade/tools/applet/DFApplet.java \
		jade/tools/sl/SLFormatter.java \
		starlight/util/Base64.java \
		jade/imtp/rmi/MainContainerRMIImpl.java \
		jade/imtp/rmi/RMIIMTPManager.java \

ROOTCLASSES2 =	jade/content/abs/AbsAgentAction.java \
		jade/content/abs/AbsAggregate.java \
		jade/content/abs/AbsConcept.java \
		jade/content/abs/AbsContentElement.java \
		jade/content/abs/AbsContentElementList.java \
		jade/content/abs/AbsIRE.java \
		jade/content/abs/AbsObject.java \
		jade/content/abs/AbsPredicate.java \
		jade/content/abs/AbsPrimitive.java \
		jade/content/abs/AbsTerm.java \
		jade/content/abs/AbsVariable.java \
		jade/content/abs/AbsHelper.java \
		jade/content/acl/Inform.java \
		jade/content/acl/Request.java \
		jade/content/acl/CommunicativeActBase.java \
		jade/content/AgentAction.java \
		jade/content/Concept.java \
		jade/content/ContentElement.java \
		jade/content/ContentElementList.java \
		jade/content/ContentManager.java \
		jade/content/OntoAID.java \
		jade/content/Predicate.java \
		jade/content/Term.java

ROOTCLASSES3 =	jade/content/lang/j/JCodec.java \
		jade/content/lang/leap/LEAPCodec.java \
		jade/content/lang/sl/SLCodec.java \
		jade/content/onto/ACLOntology.java \
		jade/content/onto/BasicOntology.java \
		jade/content/onto/Introspector.java \
		jade/content/onto/Introspectable.java \
		jade/content/onto/Ontology.java \
		jade/content/onto/OntologyException.java \
		jade/content/onto/ReflectiveIntrospector.java \
		jade/content/onto/MicroIntrospector.java \
		jade/content/onto/BasicIntrospector.java \
		jade/content/onto/UngroundedException.java \
		jade/content/onto/UnknownSchemaException.java \
		jade/content/schema/AgentActionSchema.java \
		jade/content/schema/AggregateSchema.java \
		jade/content/schema/ConceptSchema.java \
		jade/content/schema/ContentElementListSchema.java \
		jade/content/schema/ContentElementSchema.java \
		jade/content/schema/IRESchema.java \
		jade/content/schema/ObjectSchema.java \
		jade/content/schema/PredicateSchema.java \
		jade/content/schema/PrimitiveSchema.java \
		jade/content/schema/TermSchema.java \
		jade/content/schema/VariableSchema.java \
		jade/content/onto/basic/Done.java \
		jade/content/onto/basic/Equals.java \
		jade/content/onto/basic/Result.java \


ACLCLASSES = 	jade/lang/acl/ACLParser.java \
		jade/lang/acl/ACLParserConstants.java \
		jade/lang/acl/ACLParserTokenManager.java \
		jade/lang/acl/ASCII_CharStream.java \
		jade/lang/acl/ParseException.java \
		jade/lang/acl/Token.java \
		jade/lang/acl/TokenMgrError.java

SL0CLASSES =	jade/lang/sl/SL0Parser.java \
		jade/lang/sl/SL0ParserConstants.java \
		jade/lang/sl/SL0ParserTokenManager.java \
		jade/lang/sl/ASCII_UCodeESC_CharStream.java \
		jade/lang/sl/ParseException.java \
		jade/lang/sl/Token.java \
		jade/lang/sl/TokenMgrError.java

SLCLASSES =	jade/content/lang/sl/SLParser.java \
		jade/content/lang/sl/SLParserConstants.java \
		jade/content/lang/sl/SLParserTokenManager.java \
		jade/content/lang/sl/ASCII_UCodeESC_CharStream.java \
		jade/content/lang/sl/ParseException.java \
		jade/content/lang/sl/Token.java \
		jade/content/lang/sl/TokenMgrError.java

IDLSTUBS    =   FIPA/AgentID.java \
		FIPA/AgentIDHelper.java \
		FIPA/AgentIDHolder.java \
		FIPA/AgentIDsHelper.java \
		FIPA/AgentIDsHolder.java \
		FIPA/DateTime.java \
		FIPA/DateTimeHelper.java \
		FIPA/DateTimeHolder.java \
		FIPA/Envelope.java \
		FIPA/EnvelopeHelper.java \
		FIPA/EnvelopeHolder.java \
		FIPA/EnvelopesHelper.java \
		FIPA/EnvelopesHolder.java \
		FIPA/FipaMessage.java \
		FIPA/FipaMessageHelper.java \
		FIPA/FipaMessageHolder.java \
		FIPA/MTS.java \
		FIPA/MTSHelper.java \
		FIPA/MTSHolder.java \
		FIPA/OptAgentIDHelper.java \
		FIPA/OptAgentIDHolder.java \
		FIPA/OptDateTimeHelper.java \
		FIPA/OptDateTimeHolder.java \
		FIPA/OptReceivedObjectHelper.java \
		FIPA/OptReceivedObjectHolder.java \
		FIPA/OptTransportBehaviourTypeHelper.java \
		FIPA/OptTransportBehaviourTypeHolder.java \
		FIPA/PayloadHelper.java \
		FIPA/PayloadHolder.java \
		FIPA/Property.java \
		FIPA/PropertyHelper.java \
		FIPA/PropertyHolder.java \
		FIPA/ReceivedObject.java \
		FIPA/ReceivedObjectHelper.java \
		FIPA/ReceivedObjectHolder.java \
		FIPA/TransportBehaviourTypeHelper.java \
		FIPA/TransportBehaviourTypeHolder.java \
		FIPA/URLHelper.java \
		FIPA/_MTSImplBase.java \
		FIPA/_MTSStub.java \
		FIPA/stringsHelper.java \
		FIPA/stringsHolder.java

STUBS       = 	jade.imtp.rmi.AgentContainerRMIImpl \
		jade.imtp.rmi.MainContainerRMIImpl

all: $(IDLSTUBS) parsers classes stubs images
	@echo
	@echo Build complete.
	@echo

idl: $(IDLSTUBS)
	@echo IDL stubs and skeletons generated

$(IDLSTUBS): $(IDLSPEC)
	$(IDLCC) $<
	@echo IDL files generated

parsers: ACLParser SL0Parser SLParser
	@echo Parsers generated

ACLParser: $(ACLCLASSES)
	@echo ACL Parser generated.

SL0Parser: $(SL0CLASSES)
	@echo SL0 Parser generated.

SLParser: $(SLCLASSES)
	@echo SL Parser generated.

$(ACLCLASSES): $(ACLSPEC)
	$(PARSERGEN) $(ACLFLAGS) $(ACLSPEC)

$(SL0CLASSES): $(SL0SPEC)
	$(PARSERGEN) $(SL0FLAGS) $(SL0SPEC)

$(SLCLASSES): $(SLSPEC)
	$(PARSERGEN) $(SLFLAGS) $(SLSPEC)

classes:
	$(JC) $(JFLAGS) $(EXECS)
	$(JC) -classpath $(CLSDIR):$(SRCDIR) $(JFLAGS) $(ROOTCLASSES) $(ROOTCLASSES2) $(ROOTCLASSES3)

stubs:
	$(STUBGEN) -classpath $(CLSDIR):$(SRCDIR) -d $(CLSDIR) $(STUBS)

images:
	mkdir -p $(CLSDIR)/jade/tools/DummyAgent
	mkdir -p $(CLSDIR)/jade/tools/rma
	mkdir -p $(CLSDIR)/jade/tools/sniffer
	mkdir -p $(CLSDIR)/jade/tools/dfgui
	cp -R $(SRCDIR)/jade/tools/DummyAgent/images $(CLSDIR)/jade/tools/DummyAgent
	cp -R $(SRCDIR)/jade/tools/sniffer/images $(CLSDIR)/jade/tools/sniffer
	cp -R $(SRCDIR)/jade/gui/images $(CLSDIR)/jade/gui
	cp -R $(SRCDIR)/jade/tools/dfgui/images $(CLSDIR)/jade/tools/dfgui
	cp -R $(SRCDIR)/jade/tools/rma/images $(CLSDIR)/jade/tools/rma
	cp -R $(SRCDIR)/jade/tools/introspector/gui/images $(CLSDIR)/jade/tools/introspector/gui/images
	mkdir -p $(CLSDIR)/jade/gui/help

idlclean:
	rm -f $(IDLSTUBS)

jjclean:
	rm -f $(ACLCLASSES)
	rm -f $(SL0CLASSES)
	rm -f $(SLCLASSES)

batch:
	rm -f $(BATCH_ALL)
	echo '@REM ===============================================================' > $(BATCH_ALL)
	echo '@REM =           Generated by JADE Makefile. DO NOT EDIT           =' >> $(BATCH_ALL)
	echo '@REM ===============================================================' >> $(BATCH_ALL)
	echo >> $(BATCH_ALL)
	echo 'cd src' >> $(BATCH_ALL)
	echo $(IDLCC) $(IDLSPEC) >> $(BATCH_ALL)
	echo >> $(BATCH_ALL)
	echo 'call $(PARSERGEN) $(subst /,\,$(ACLFLAGS)) $(subst /,\,$(ACLSPEC))' >> $(BATCH_ALL)
	echo 'call $(PARSERGEN) $(subst /,\,$(SL0FLAGS)) $(subst /,\,$(SL0SPEC))' >> $(BATCH_ALL)
	echo 'call $(PARSERGEN) $(subst /,\,$(SLFLAGS)) $(subst /,\,$(SLSPEC))' >> $(BATCH_ALL)
	echo >> $(BATCH_ALL)
	echo '$(JC) -classpath . $(subst /,\,$(subst $(ROOTDIR),..,$(JFLAGS))) $(subst /,\,$(EXECS))' >> $(BATCH_ALL)
	echo '$(JC) -classpath .;$(subst /,\,$(subst $(ROOTDIR),..,$(CLSDIR);$(SRCDIR))) $(subst /,\,$(subst $(ROOTDIR),..,$(JFLAGS))) $(subst /,\,$(ROOTCLASSES))' >> $(BATCH_ALL)
	echo '$(JC) -classpath .;$(subst /,\,$(subst $(ROOTDIR),..,$(CLSDIR);$(SRCDIR))) $(subst /,\,$(subst $(ROOTDIR),..,$(JFLAGS))) $(subst /,\,$(ROOTCLASSES2))' >> $(BATCH_ALL)
	echo '$(JC) -classpath .;$(subst /,\,$(subst $(ROOTDIR),..,$(CLSDIR);$(SRCDIR))) $(subst /,\,$(subst $(ROOTDIR),..,$(JFLAGS))) $(subst /,\,$(ROOTCLASSES3))' >> $(BATCH_ALL)
	echo >> $(BATCH_ALL)
	echo 'xcopy /E /I jade\tools\DummyAgent\images ..\classes\jade\tools\DummyAgent\images' >> $(BATCH_ALL)
	echo 'xcopy /E /I jade\tools\sniffer\images ..\classes\jade\tools\sniffer\images' >> $(BATCH_ALL)
	echo 'xcopy /E /I jade\tools\dfgui\images ..\classes\jade\tools\dfgui\images' >> $(BATCH_ALL)
	echo 'xcopy /E /I jade\tools\rma\images ..\classes\jade\tools\rma\images' >> $(BATCH_ALL)
	echo 'xcopy /E /I jade\gui\images ..\classes\jade\gui\images' >> $(BATCH_ALL)
	echo 'xcopy /E /I jade\tools\introspector\gui\images ..\classes\jade\tools\introspector\gui\images' >> $(BATCH_ALL)

	echo >> $(BATCH_ALL)

	echo 'xcopy /I jade\gui\help\*.html ..\classes\jade\gui\help' >> $(BATCH_ALL)
	echo >> $(BATCH_ALL)

	echo '$(STUBGEN) -classpath ..\classes -d ..\classes $(STUBS)' >> $(BATCH_ALL)
	echo >> $(BATCH_ALL)
	echo 'cd ..' >> $(BATCH_ALL)



