# Makefile for JADE project - 'doc' directory

ZIP = jar
ZIPFLAGS = cMvf
ZIPUPDATEFLAGS = uvf
ZIPEXT = zip

UNZIP = jar
UNZIPFLAGS = xvf
UNZIPEXT = zip

DOC = javadoc

DOC_MAIN = $(SRCDIR)/jade-main.html
DOC_OUTDIR = $(DOCDIR)/api
DOC_WINTITLE = "$(PACKAGE) $(VERSION) API Reference"
TMPSRCDIR = $(DOCDIR)/__tmp

API_FILES1 = 	jade/core/behaviours/package.html \
		jade/core/behaviours/DataStore.java \
		jade/core/behaviours/Behaviour.java \
		jade/core/behaviours/CompositeBehaviour.java \
		jade/core/behaviours/CyclicBehaviour.java \
		jade/core/behaviours/FSMBehaviour.java \
		jade/core/behaviours/NonDeterministicBehaviour.java \
		jade/core/behaviours/OneShotBehaviour.java \
		jade/core/behaviours/ParallelBehaviour.java \
		jade/core/behaviours/ReceiverBehaviour.java \
		jade/core/behaviours/SenderBehaviour.java \
		jade/core/behaviours/SequentialBehaviour.java \
		jade/core/behaviours/SimpleBehaviour.java \
		jade/core/behaviours/WakerBehaviour.java \
		jade/core/package.html \
		jade/core/AID.java \
		jade/core/Agent.java \
		jade/core/BehaviourID.java \
		jade/core/CaseInsensitiveString.java \
		jade/core/ContainerID.java \
		jade/core/Location.java \
		jade/core/NameClashException.java \
		jade/core/NotFoundException.java \
		jade/core/Profile.java \
		jade/core/ProfileException.java \
		jade/core/ProfileImpl.java \
		jade/core/Runtime.java \
		jade/core/Specifier.java \
		jade/core/UnreachableException.java \
		jade/core/event/AgentEvent.java \
		jade/core/event/AgentListener.java \
		jade/core/event/JADEEvent.java \
		jade/core/event/MTPEvent.java \
		jade/core/event/MTPListener.java \
		jade/core/event/MessageEvent.java \
		jade/core/event/MessageListener.java \
		jade/core/event/PlatformEvent.java \
		jade/core/event/PlatformListener.java

API_FILES2 =	jade/domain/DFGUIManagement/DFAppletManagementOntology.java \
		jade/domain/DFGUIManagement/DeregisterFrom.java \
		jade/domain/DFGUIManagement/Federate.java \
		jade/domain/DFGUIManagement/GetDefaultDescription.java \
		jade/domain/DFGUIManagement/GetDescriptionUsed.java \
		jade/domain/DFGUIManagement/GetParent.java \
		jade/domain/DFGUIManagement/ModifyOn.java \
		jade/domain/DFGUIManagement/RegisterWith.java \
		jade/domain/DFGUIManagement/SearchOn.java \
		jade/domain/package.html \
		jade/domain/AMSServiceCommunicator.java \
		jade/domain/DFGUIAdapter.java \
		jade/domain/DFGUIInterface.java \
		jade/domain/DFServiceCommunicator.java \
		jade/domain/FIPAException.java \
		jade/domain/FIPANames.java \
		jade/domain/FIPAServiceCommunicator.java \
		jade/domain/MobilityOntology.java \
		jade/domain/RequestFIPAServiceBehaviour.java

API_FILES3 =	jade/domain/FIPAAgentManagement/package.html \
		jade/domain/FIPAAgentManagement/AMSAgentDescription.java \
		jade/domain/FIPAAgentManagement/APDescription.java \
		jade/domain/FIPAAgentManagement/APTransportDescription.java \
		jade/domain/FIPAAgentManagement/AlreadyRegistered.java \
		jade/domain/FIPAAgentManagement/DFAgentDescription.java \
		jade/domain/FIPAAgentManagement/Deregister.java \
		jade/domain/FIPAAgentManagement/Envelope.java \
		jade/domain/FIPAAgentManagement/FIPAAgentManagementOntology.java \
		jade/domain/FIPAAgentManagement/FailureException.java \
		jade/domain/FIPAAgentManagement/GetDescription.java \
		jade/domain/FIPAAgentManagement/InternalError.java \
		jade/domain/FIPAAgentManagement/MTPDescription.java \
		jade/domain/FIPAAgentManagement/MissingParameter.java \
		jade/domain/FIPAAgentManagement/Modify.java \
		jade/domain/FIPAAgentManagement/NotRegistered.java \
		jade/domain/FIPAAgentManagement/NotUnderstoodException.java \
		jade/domain/FIPAAgentManagement/Property.java \
		jade/domain/FIPAAgentManagement/Quit.java \
		jade/domain/FIPAAgentManagement/ReceivedObject.java \
		jade/domain/FIPAAgentManagement/RefuseException.java \
		jade/domain/FIPAAgentManagement/Register.java \
		jade/domain/FIPAAgentManagement/Search.java \
		jade/domain/FIPAAgentManagement/SearchConstraints.java \
		jade/domain/FIPAAgentManagement/ServiceDescription.java \
		jade/domain/FIPAAgentManagement/Unauthorised.java \
		jade/domain/FIPAAgentManagement/UnexpectedAct.java \
		jade/domain/FIPAAgentManagement/UnexpectedParameter.java \
		jade/domain/FIPAAgentManagement/UnrecognisedParameterValue.java \
		jade/domain/FIPAAgentManagement/UnrecognisedValue.java \
		jade/domain/FIPAAgentManagement/UnsupportedAct.java \
		jade/domain/FIPAAgentManagement/UnsupportedFunction.java \
		jade/domain/FIPAAgentManagement/UnsupportedValue.java

API_FILES4 =	jade/domain/JADEAgentManagement/CreateAgent.java \
		jade/domain/JADEAgentManagement/DebugOff.java \
		jade/domain/JADEAgentManagement/DebugOn.java \
		jade/domain/JADEAgentManagement/InstallMTP.java \
		jade/domain/JADEAgentManagement/JADEAgentManagementOntology.java \
		jade/domain/JADEAgentManagement/KillAgent.java \
		jade/domain/JADEAgentManagement/KillContainer.java \
		jade/domain/JADEAgentManagement/ShowGui.java \
		jade/domain/JADEAgentManagement/SniffOff.java \
		jade/domain/JADEAgentManagement/SniffOn.java \
		jade/domain/JADEAgentManagement/UninstallMTP.java \
		jade/domain/introspection/package.html \
		jade/domain/introspection/ACLMessage.java \
		jade/domain/introspection/AddedBehaviour.java \
		jade/domain/introspection/AddedContainer.java \
		jade/domain/introspection/AddedMTP.java \
		jade/domain/introspection/BornAgent.java \
		jade/domain/introspection/ChangedAgentState.java \
		jade/domain/introspection/ChangedBehaviourState.java \
		jade/domain/introspection/DeadAgent.java \
		jade/domain/introspection/Event.java \
		jade/domain/introspection/EventRecord.java \
		jade/domain/introspection/JADEIntrospectionOntology.java \
		jade/domain/introspection/MovedAgent.java \
		jade/domain/introspection/Occurred.java \
		jade/domain/introspection/PlatformDescription.java \
		jade/domain/introspection/PostedMessage.java \
		jade/domain/introspection/ReceivedMessage.java \
		jade/domain/introspection/RemovedBehaviour.java \
		jade/domain/introspection/RemovedContainer.java \
		jade/domain/introspection/RemovedMTP.java \
		jade/domain/introspection/RoutedMessage.java \
		jade/domain/introspection/SentMessage.java \
		jade/domain/introspection/StartNotify.java \
		jade/domain/introspection/StopNotify.java \
		jade/domain/AMSService.java \
		jade/domain/DFService.java

API_FILES5 =	jade/gui/package.html \
		jade/gui/AIDGui.java \
		jade/gui/APDescriptionPanel.java \
		jade/gui/AboutJadeAction.java \
		jade/gui/AclGui.java \
		jade/gui/AgentTree.java \
		jade/gui/AgentTreeModel.java \
		jade/gui/BrowserLauncher.java \
		jade/gui/ConstraintDlg.java \
		jade/gui/DFAgentDscDlg.java \
		jade/gui/GuiAgent.java \
		jade/gui/GuiEvent.java \
		jade/gui/GuiProperties.java \
		jade/gui/JadeLogoButton.java \
		jade/gui/MTPDescriptionPanel.java \
		jade/gui/MyFilterImage.java \
		jade/gui/NodeDescriptor.java \
		jade/gui/ServiceDscDlg.java \
		jade/gui/SingleProperty.java \
		jade/gui/StringDlg.java \
		jade/gui/TimeChooser.java \
		jade/gui/TreeHelp.java \
		jade/gui/TreeIconRenderer.java \
		jade/gui/UserPropertyGui.java \
		jade/gui/VisualAIDList.java \
		jade/gui/VisualAPTransportProfileList.java \
		jade/gui/VisualPropertiesList.java \
		jade/gui/VisualServicesList.java \
		jade/gui/VisualStringList.java \
		jade/lang/acl/package.html \
		jade/lang/acl/ACLCodec.java \
		jade/lang/acl/ACLMessage.java \
		jade/lang/acl/ISO8601.java \
		jade/lang/acl/MessageTemplate.java \
		jade/lang/acl/StringACLCodec.java \
		jade/lang/acl/UnreadableException.java \
		jade/lang/sl/package.html \
		jade/lang/sl/SL0Codec.java \
		jade/lang/sl/SL0Encoder.java \
		jade/lang/package.html \
		jade/lang/Codec.java \
		jade/mtp/iiop/MessageTransportProtocol.java \
		jade/mtp/InChannel.java \
		jade/mtp/MTP.java \
		jade/mtp/MTPException.java \
		jade/mtp/OutChannel.java \
		jade/mtp/TransportAddress.java \
		jade/onto/package.html \
		jade/onto/JADEMetaOntology/AnOntology.java \
		jade/onto/JADEMetaOntology/JADEMetaOntology.java \
		jade/onto/JADEMetaOntology/Role.java \
		jade/onto/JADEMetaOntology/Slot.java \
		jade/onto/DefaultOntology.java \
		jade/onto/Frame.java \
		jade/onto/FrameSchema.java \
		jade/onto/Ontology.java \
		jade/onto/OntologyException.java \
		jade/onto/SlotDescriptor.java \
		jade/onto/basic/Action.java \
		jade/onto/basic/BasicOntology.java \
		jade/onto/basic/DonePredicate.java \
		jade/onto/basic/FalseProposition.java \
		jade/onto/basic/Not.java \
		jade/onto/basic/ResultPredicate.java \
		jade/onto/basic/TrueProposition.java

API_FILES6 =	jade/proto/package.html \
		jade/proto/states/HandlerSelector.java \
		jade/proto/states/MsgReceiver.java \
		jade/proto/AchieveREInitiator.java \
		jade/proto/AchieveREResponder.java \
		jade/proto/ContractNetInitiator.java \
		jade/proto/ContractNetResponder.java \
		jade/proto/FipaRequestInitiatorBehaviour.java 
		jade/proto/FIPAProtocolNames.java \
		jade/tools/DummyAgent/package.html \
		jade/tools/DummyAgent/DummyAgent.java \
		jade/tools/DummyAgent/DummyAgentGui.java \
		jade/tools/DummyAgent/DummyBehaviour.java \
		jade/tools/DummyAgent/GuiProperties.java \
		jade/tools/DummyAgent/MsgIndication.java \
		jade/tools/DummyAgent/ToFromCellRenderer.java \
		jade/tools/ToolAgent.java \
		jade/tools/ToolNotifier.java \
		jade/tools/applet/package.html \
		jade/tools/SocketProxyAgent/SocketProxyAgent.java \
		jade/tools/applet/AppletRequestProto.java \
		jade/tools/applet/DFApplet.java \
		jade/tools/applet/DFAppletCommunicator.java \
		jade/tools/applet/FIPAAppletRequestProto.java \
		jade/tools/applet/JADEAppletRequestProto.java \
		jade/tools/dfgui/AgentNameTableModel.java \
		jade/tools/dfgui/DFGUI.java \
		jade/tools/dfgui/DFGUIAboutAction.java \
		jade/tools/dfgui/DFGUICloseGuiAction.java \
		jade/tools/dfgui/DFGUIDeregisterAction.java \
		jade/tools/dfgui/DFGUIExitDFAction.java \
		jade/tools/dfgui/DFGUIFederateAction.java \
		jade/tools/dfgui/DFGUIModifyAction.java \
		jade/tools/dfgui/DFGUIRefreshAppletAction.java \
		jade/tools/dfgui/DFGUIRegisterAction.java \
		jade/tools/dfgui/DFGUISearchAction.java \
		jade/tools/dfgui/DFGUIViewAction.java \
		jade/tools/dfgui/DFGuiProperties.java

API_FILES7 =	jade/tools/introspector/gui/AboutBoxAction.java \
		jade/tools/introspector/gui/BehaviourPanel.java \
		jade/tools/introspector/gui/IntrospectorGUI.java \
		jade/tools/introspector/gui/MainBar.java \
		jade/tools/introspector/gui/MainBarListener.java \
		jade/tools/introspector/gui/MainWindow.java \
		jade/tools/introspector/gui/MessagePanel.java \
		jade/tools/introspector/gui/MessageTableModel.java \
		jade/tools/introspector/gui/MyDialog.java \
		jade/tools/introspector/gui/PopUpMouser.java \
		jade/tools/introspector/gui/StatePanel.java \
		jade/tools/introspector/gui/StateUpdater.java \
		jade/tools/introspector/gui/TableMouseListener.java \
		jade/tools/introspector/gui/TablePopupMenu.java \
		jade/tools/introspector/gui/TablePopupMenuListener.java \
		jade/tools/introspector/gui/TableUpdater.java \
		jade/tools/introspector/gui/TreeAgentPopupMenu.java \
		jade/tools/introspector/gui/TreeMouseListener.java \
		jade/tools/introspector/gui/TreePanel.java \
		jade/tools/introspector/gui/TreePopupMenu.java \
		jade/tools/introspector/gui/TreePopupMenuListener.java \
		jade/tools/introspector/gui/TreeUpdater.java \
		jade/tools/introspector/Introspector.java \
		jade/tools/rma/ActionProcessor.java \
		jade/tools/rma/AddRemotePlatformAction.java \
		jade/tools/rma/AddRemotePlatformFromURLAction.java \
		jade/tools/rma/AgentAction.java \
		jade/tools/rma/CloneAgentAction.java \
		jade/tools/rma/CloseRMAAction.java \
		jade/tools/rma/ContainerAction.java \
		jade/tools/rma/CustomAction.java \
		jade/tools/rma/DummyAgentAction.java \
		jade/tools/rma/ExitAction.java \
		jade/tools/rma/FixedAction.java \
		jade/tools/rma/GenericAction.java \
		jade/tools/rma/InstallMTPAction.java \
		jade/tools/rma/InstallMTPDialog.java \
		jade/tools/rma/IntrospectorAction.java \
		jade/tools/rma/KillAction.java

API_FILES8 =	jade/tools/rma/MainMenu.java \
		jade/tools/rma/MainPanel.java \
		jade/tools/rma/MainWindow.java \
		jade/tools/rma/MoveAgentAction.java \
		jade/tools/rma/MoveDialog.java \
		jade/tools/rma/PlatformAction.java \
		jade/tools/rma/PopupMenuAgent.java \
		jade/tools/rma/PopupMenuContainer.java \
		jade/tools/rma/PopupMenuPlatform.java \
		jade/tools/rma/PopupMenuRemotePlatform.java \
		jade/tools/rma/PopupMouser.java \
		jade/tools/rma/RMAAction.java \
		jade/tools/rma/RefreshAMSAgentAction.java \
		jade/tools/rma/RefreshAPDescriptionAction.java \
		jade/tools/rma/RegisterRemoteAgentAction.java \
		jade/tools/rma/RemoveRemoteAMSAction.java \
		jade/tools/rma/ResumeAction.java \
		jade/tools/rma/ShowDFGuiAction.java \
		jade/tools/rma/ShutDownAction.java \
		jade/tools/rma/SnifferAction.java \
		jade/tools/rma/StartDialog.java \
		jade/tools/rma/StartException.java \
		jade/tools/rma/StartNewAgentAction.java \
		jade/tools/rma/SuspendAction.java \
		jade/tools/rma/TablePanel.java \
		jade/tools/rma/ToolBar.java \
		jade/tools/rma/UninstallMTPAction.java \
		jade/tools/rma/ViewAPDescriptionAction.java \
		jade/tools/rma/WindowCloser.java \
		jade/tools/rma/rma.java \
		jade/tools/sl/SLFormatter.java \
		jade/tools/sniffer/AboutBoxAction.java \
		jade/tools/sniffer/AbstractPopup.java \
		jade/tools/sniffer/ActionProcessor.java \
		jade/tools/sniffer/Agent.java \
		jade/tools/sniffer/AgentAction.java \
		jade/tools/sniffer/AgentList.java \
		jade/tools/sniffer/ClearCanvasAction.java \
		jade/tools/sniffer/DisplayLogFileAction.java \
		jade/tools/sniffer/DoNotSnifferAction.java \
		jade/tools/sniffer/DoSnifferAction.java \
		jade/tools/sniffer/ExitAction.java \
		jade/tools/sniffer/FixedAction.java \
		jade/tools/sniffer/GuiProperties.java \
		jade/tools/sniffer/MMCanvas.java \
		jade/tools/sniffer/MainMenu.java \
		jade/tools/sniffer/MainPanel.java \
		jade/tools/sniffer/MainWindow.java \
		jade/tools/sniffer/Message.java \
		jade/tools/sniffer/MessageList.java

API_FILES9 =	jade/tools/sniffer/PanelCanvas.java \
		jade/tools/sniffer/PopMouserAgent.java \
		jade/tools/sniffer/PopMouserMessage.java \
		jade/tools/sniffer/PopNoSniffAgent.java \
		jade/tools/sniffer/PopShowAgent.java \
		jade/tools/sniffer/PopSniffAgent.java \
		jade/tools/sniffer/PopupAgent.java \
		jade/tools/sniffer/PopupMenuAgent.java \
		jade/tools/sniffer/PopupMessage.java \
		jade/tools/sniffer/PopupMouser.java \
		jade/tools/sniffer/ProgramCloser.java \
		jade/tools/sniffer/ShowOnlyAction.java \
		jade/tools/sniffer/Sniffer.java \
		jade/tools/sniffer/SnifferAction.java \
		jade/tools/sniffer/StartException.java \
		jade/tools/sniffer/ToolBar.java \
		jade/tools/sniffer/ViewMessage.java \
		jade/tools/sniffer/WriteLogFileAction.java \
		jade/tools/sniffer/WriteMessageListAction.java \
		jade/util/leap/package.html \
		jade/util/leap/ArrayList.java \
		jade/util/leap/Collection.java \
		jade/util/leap/Comparable.java \
		jade/util/leap/HashMap.java \
		jade/util/leap/Iterator.java \
		jade/util/leap/Properties.java \
		jade/util/leap/Serializable.java \
		jade/util/leap/LinkedList.java \
		jade/util/leap/List.java \
		jade/util/leap/Map.java \
		jade/util/leap/Set.java \
		jade/util/leap/SortedSet.java \
		jade/util/leap/SortedSetImpl.java \
		jade/wrapper/package.html \
		jade/wrapper/Agent.java \
		jade/wrapper/AgentContainer.java \
		jade/wrapper/MainContainer.java \
		jade/wrapper/StaleProxyException.java \
		jade/content/abs/AbsAgentAction.java \
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
		jade/content/abs/AbsHelper.java

API_FILES10 =	jade/content/acl/Inform.java \
		jade/content/acl/Request.java \
		jade/content/acl/CommunicativeActBase.java \
		jade/content/AgentAction.java \
		jade/content/Concept.java \
		jade/content/ContentElement.java \
		jade/content/ContentElementList.java \
		jade/content/ContentManager.java \
		jade/content/Predicate.java \
		jade/content/Term.java \
		jade/content/OntoACLMessage.java \
		jade/content/OntoAID.java \
		jade/content/lang/j/JCodec.java \
		jade/content/lang/leap/LEAPCodec.java \
		jade/content/lang/Codec.java \
		jade/content/lang/sl/SLCodec.java \
		jade/content/onto/ACLOntology.java \
		jade/content/onto/BasicIntrospector.java \
		jade/content/onto/BasicOntology.java \
		jade/content/onto/Introspectable.java \
		jade/content/onto/Introspector.java \
		jade/content/onto/MicroIntrospector.java \
		jade/content/onto/Ontology.java \
		jade/content/onto/OntologyException.java \
		jade/content/onto/ReflectiveIntrospector.java \
		jade/content/onto/UngroundedException.java \
		jade/content/onto/UnknownSchemaException.java \
		jade/content/schema/AgentActionSchema.java \
		jade/content/schema/AggregateSchema.java \
		jade/content/schema/ConceptSchema.java \
		jade/content/schema/ContentElementListSchema.java \
		jade/content/schema/ContentElementSchema.java \
		jade/content/schema/Facet.java \
		jade/content/schema/IRESchema.java \
		jade/content/schema/ObjectSchema.java \
		jade/content/schema/PredicateSchema.java \
		jade/content/schema/PrimitiveSchema.java \
		jade/content/schema/TermSchema.java \
		jade/content/schema/VariableSchema.java \

API_CLASSES = 	$(API_FILES1) $(API_FILES2) $(API_FILES3) $(API_FILES4) $(API_FILES5) \
		$(API_FILES6) $(API_FILES7) $(API_FILES8) $(API_FILES9) $(API_FILES10)

API_PACKAGES = 	jade.core jade.core.behaviours jade.core.event jade.lang jade.lang.acl jade.lang.sl jade.domain \
		jade.domain.FIPAAgentManagement jade.domain.JADEAgentManagement jade.proto jade.onto jade.onto.basic \
		jade.gui jade.mtp jade.mtp.iiop jade.onto.JADEMetaOntology jade.wrapper jade.domain.DFGUIManagement \
		jade.domain.introspection jade.content jade.content.abs jade.content.acl jade.content.lang \
		jade.content.lang.j jade.content.lang.leap jade.content.onto jade.content.schema jade.util.leap

API_FLAGS = -d $(DOC_OUTDIR) -overview $(DOC_MAIN) -author -windowtitle $(DOC_WINTITLE)

SNIFFER_OUTDIR = $(DOCDIR)/tools/sniffer/html/jdoc
SNIFFER_FLAGS =  -d $(SNIFFER_OUTDIR) -author -windowtitle "$(PACKAGE) $(VERSION) Sniffer Reference"
SNIFFER_PACKAGES = jade.tools.sniffer

.PHONY: all clean api tools sniffer batch

all: api tools

api: $(DOC_OUTDIR)
	mkdir -p $(TMPSRCDIR); \
	cd $(SRCDIR) ; \
	$(ZIP) $(ZIPFLAGS) $(TMPSRCDIR)/src.$(ZIPEXT) $(API_CLASSES); \
	cd $(TMPSRCDIR); \
	$(UNZIP) $(UNZIPFLAGS) src.$(UNZIPEXT); \
	$(DOC) -sourcepath $(TMPSRCDIR) $(API_FLAGS) $(API_PACKAGES); \
	rm -rf $(TMPSRCDIR)

$(DOC_OUTDIR):
	mkdir -p $(DOC_OUTDIR)

tools: sniffer

sniffer: $(SNIFFER_OUTDIR)
	$(DOC) -sourcepath $(SRCDIR) $(SNIFFER_FLAGS) $(SNIFFER_PACKAGES)

$(SNIFFER_OUTDIR):
	mkdir -p $(SNIFFER_OUTDIR)

clean:
	rm -fr $(DOC_OUTDIR)/[a-z]*
	rm -fr $(SNIFFER_OUTDIR)/[a-z]*

batch: $(DOC_OUTDIR) $(SNIFFER_OUTDIR)
	rm -f $(BATCH_DOC);
	echo '@REM ===============================================================' > $(BATCH_DOC)
	echo '@REM =           Generated by JADE Makefile. DO NOT EDIT           =' >> $(BATCH_DOC)
	echo '@REM ===============================================================' >> $(BATCH_DOC)
	echo 'cd src' >> $(BATCH_DOC)
	echo 'mkdir $(subst /,\,$(subst $(DOCDIR),..\doc,$(TMPSRCDIR)))' >> $(BATCH_DOC)
	echo '$(ZIP) $(ZIPFLAGS) $(subst /,\,$(subst $(DOCDIR),..\doc,$(TMPSRCDIR)\src.$(ZIPEXT) $(API_FILES1)))' >> $(BATCH_DOC)
	echo '$(ZIP) $(ZIPUPDATEFLAGS) $(subst /,\,$(subst $(DOCDIR),..\doc,$(TMPSRCDIR)\src.$(ZIPEXT) $(API_FILES2)))' >> $(BATCH_DOC)
	echo '$(ZIP) $(ZIPUPDATEFLAGS) $(subst /,\,$(subst $(DOCDIR),..\doc,$(TMPSRCDIR)\src.$(ZIPEXT) $(API_FILES3)))' >> $(BATCH_DOC)
	echo '$(ZIP) $(ZIPUPDATEFLAGS) $(subst /,\,$(subst $(DOCDIR),..\doc,$(TMPSRCDIR)\src.$(ZIPEXT) $(API_FILES4)))' >> $(BATCH_DOC)
	echo '$(ZIP) $(ZIPUPDATEFLAGS) $(subst /,\,$(subst $(DOCDIR),..\doc,$(TMPSRCDIR)\src.$(ZIPEXT) $(API_FILES5)))' >> $(BATCH_DOC)
	echo '$(ZIP) $(ZIPUPDATEFLAGS) $(subst /,\,$(subst $(DOCDIR),..\doc,$(TMPSRCDIR)\src.$(ZIPEXT) $(API_FILES6)))' >> $(BATCH_DOC)
	echo '$(ZIP) $(ZIPUPDATEFLAGS) $(subst /,\,$(subst $(DOCDIR),..\doc,$(TMPSRCDIR)\src.$(ZIPEXT) $(API_FILES7)))' >> $(BATCH_DOC)
	echo '$(ZIP) $(ZIPUPDATEFLAGS) $(subst /,\,$(subst $(DOCDIR),..\doc,$(TMPSRCDIR)\src.$(ZIPEXT) $(API_FILES8)))' >> $(BATCH_DOC)
	echo '$(ZIP) $(ZIPUPDATEFLAGS) $(subst /,\,$(subst $(DOCDIR),..\doc,$(TMPSRCDIR)\src.$(ZIPEXT) $(API_FILES9)))' >> $(BATCH_DOC)
	echo '$(ZIP) $(ZIPUPDATEFLAGS) $(subst /,\,$(subst $(DOCDIR),..\doc,$(TMPSRCDIR)\src.$(ZIPEXT) $(API_FILES10)))' >> $(BATCH_DOC)
	echo 'cd $(subst /,\,$(subst $(ROOTDIR),..,$(TMPSRCDIR)))' >> $(BATCH_DOC)
	echo '$(UNZIP) $(UNZIPFLAGS) src.$(UNZIPEXT)' >> $(BATCH_DOC)
	echo 'cd ..\..' >> $(BATCH_DOC)
	echo '$(subst /,\,$(subst $(ROOTDIR),.,$(DOC))) -sourcepath $(subst /,\,$(subst $(ROOTDIR),.,$(TMPSRCDIR))) $(subst /,\,$(subst $(ROOTDIR),.,$(API_FLAGS))) $(API_PACKAGES)' >> $(BATCH_DOC)
	echo 'rmdir /S /Q $(subst /,\,$(subst $(ROOTDIR),.,$(TMPSRCDIR)))' >> $(BATCH_DOC) # For Win NT
	echo 'deltree /Y $(subst /,\,$(subst $(ROOTDIR),.,$(TMPSRCDIR)))' >> $(BATCH_DOC)  # For Win 95/98

	echo >> $(BATCH_DOC)
	echo '$(subst /,\,$(subst $(ROOTDIR),.,$(DOC))) -sourcepath $(subst /,\,$(subst $(ROOTDIR),.,$(SRCDIR))) $(subst /,\,$(subst $(ROOTDIR),.,$(SNIFFER_FLAGS))) $(SNIFFER_PACKAGES)' >> $(BATCH_DOC)
	echo >> $(BATCH_DOC)
	echo 'del $(subst /,\,$(subst $(ROOTDIR),.,$(DOC_OUTDIR)))\*.html' >> $(BATCH_CLEAN)
	echo 'del $(subst /,\,$(subst $(ROOTDIR),.,$(DOC_OUTDIR)))\*.css' >> $(BATCH_CLEAN)
	echo 'del $(subst /,\,$(subst $(ROOTDIR),.,$(DOC_OUTDIR)))\*.' >> $(BATCH_CLEAN)
	echo 'rmdir /S /Q $(subst /,\,$(subst $(ROOTDIR),.,$(DOC_OUTDIR)))\jade' >> $(BATCH_CLEAN) # For Win NT
	echo 'deltree /Y $(subst /,\,$(subst $(ROOTDIR),.,$(DOC_OUTDIR)))\jade' >> $(BATCH_CLEAN)  # For Win 95/98
	echo 'del $(subst /,\,$(subst $(ROOTDIR),.,$(SNIFFER_OUTDIR)))\*.html' >> $(BATCH_CLEAN)
	echo 'del $(subst /,\,$(subst $(ROOTDIR),.,$(SNIFFER_OUTDIR)))\*.css' >> $(BATCH_CLEAN)
	echo 'del $(subst /,\,$(subst $(ROOTDIR),.,$(SNIFFER_OUTDIR)))\*.' >> $(BATCH_CLEAN)
	echo 'rmdir /S /Q $(subst /,\,$(subst $(ROOTDIR),.,$(SNIFFER_OUTDIR)))\jade' >> $(BATCH_CLEAN) # For Win NT
	echo 'deltree /Y $(subst /,\,$(subst $(ROOTDIR),.,$(SNIFFER_OUTDIR)))\jade' >> $(BATCH_CLEAN)  # For Win 95/98

