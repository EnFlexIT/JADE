# Makefile for JADE project - lib directory

# The following targets are not file names
.PHONY: all clean

AR = jar
ARFLAGS = cvMf
MANIFESTARFLAGS = cmf

MODULEDIR = $(LIBDIR)/modules

# Low level modules

BOOT_CLASSES = jade/*.class jade/util/leap/Properties.class
BOOT_LIBNAME = boot.jar

MAINCONTAINER_CLASSES = jade/core/AgentManager*.class jade/core/ContainerTable*.class jade/core/GADT.class 	\
			jade/core/MainContainerImpl*.class jade/domain/ams*.class jade/domain/df*.class 	\
			jade/domain/KB.class jade/domain/KBAbstractImpl.class
MAINCONTAINER_LIBNAME = main_container.jar

KERNELJ2SE_CLASSES = jade/lang/acl/ISO8601.class jade/lang/acl/MessageTemplate*.class jade/lang/acl/StringACLCodec.class 	\
		     jade/core/Agent.class jade/core/Agent$*.class jade/core/RealMobilityManager*.class 			\
		     jade/core/RealNotificationManager.class jade/core/JADEClassLoader.class jade/core/FullAcc.class 		\
		     jade/core/FullResourceManager.class jade/core/Runtime*.class jade/util/leap/Serializable.class 		\
		     jade/proto/FipaRequestResponderBehaviour*.class jade/domain/AMSServiceCommunicator.class 			\
		     jade/domain/DFServiceCommunicator.class jade/domain/FIPAServiceCommunicator.class 				\
		     jade/domain/DFGUIAdapter.class jade/domain/DFGUIInterface.class 						\
		     jade/domain/FIPAAgentManagement/APDescription.class jade/domain/FIPAAgentManagement/Quit.class 		\
		     jade/domain/FIPAAgentManagement/APTransportDescription.class 						\
		     jade/domain/FIPAAgentManagement/AlreadyRegistered.class 							\
		     jade/domain/FIPAAgentManagement/DFAgentDescription.class jade/domain/FIPAAgentManagement/Register.class 	\
		     jade/domain/FIPAAgentManagement/Deregister.class jade/domain/FIPAAgentManagement/Search.class 		\
		     jade/domain/FIPAAgentManagement/FIPAAgentManagementOntology.class 						\
		     jade/domain/FIPAAgentManagement/ServiceDescription.class 							\
		     jade/domain/FIPAAgentManagement/FailureException.class jade/domain/FIPAAgentManagement/Unauthorised.class 	\
		     jade/domain/FIPAAgentManagement/GetDescription.class jade/domain/FIPAAgentManagement/UnexpectedAct.class 	\
		     jade/domain/FIPAAgentManagement/InternalError.class 							\
		     jade/domain/FIPAAgentManagement/UnexpectedParameter.class 							\
		     jade/domain/FIPAAgentManagement/MTPDescription.class 							\
		     jade/domain/FIPAAgentManagement/UnrecognisedParameterValue.class 						\
		     jade/domain/FIPAAgentManagement/Modify.class jade/domain/FIPAAgentManagement/UnsupportedAct.class 		\
		     jade/domain/FIPAAgentManagement/NotRegistered.class jade/domain/FIPAAgentManagement/UnsupportedValue.class \
		     jade/domain/DFGUIManagement jade/domain/JADEAgentManagement jade/lang/sl jade/onto
KERNELJ2SE_LIBNAME = kernel_j2se.jar

UTILS_CLASSES = jade/util/leap/ArrayList*.class jade/util/leap/Comparable.class jade/util/leap/HashMap*.class 			\
		jade/util/leap/Iterator.class jade/util/leap/LinkedList*.class jade/util/leap/SortedSetImpl*.class
UTILS_LIBNAME = utils.jar

TOOLS_CLASSES = jade/gui jade/tools
TOOLS_LIBNAME = tools.jar

IMTPRMI_CLASSES = jade/imtp/rmi
IMTPRMI_LIBNAME = imtp_rmi.jar

IIOP_CLASSES = FIPA jade/mtp/iiop
IIOP_LIBNAME = iiop.jar

KERNELSHARE_CLASSES = jade
KERNELSHARE_LIBNAME = kernel_share.jar

JADE_LIBNAME = jade.jar
JADETOOLS_LIBNAME = jadeTools.jar
BASE64_LIBNAME = Base64.jar

all:
	@echo Making library ...

#	Higher level modules, for backward compatibility
	cd $(CLSDIR);										\
	$(AR) $(MANIFESTARFLAGS) jade.mf $(LIBDIR)/$(JADE_LIBNAME) Notice.txt;			\
	$(AR) $(MANIFESTARFLAGS) jadeTools.mf $(LIBDIR)/$(JADETOOLS_LIBNAME) Notice.txt;	\
	$(AR) $(ARFLAGS) $(LIBDIR)/$(IIOP_LIBNAME) $(IIOP_CLASSES); 				\
	rm -rf $(IIOP_CLASSES); 								\
	$(AR) $(ARFLAGS) $(LIBDIR)/$(BASE64_LIBNAME) starlight;					\
	rm -rf starlight

#	Lower level modules, the ones really needed
	cd $(CLSDIR); 										\
	$(AR) $(ARFLAGS) $(MODULEDIR)/$(BOOT_LIBNAME) $(BOOT_CLASSES); 				\
	rm -rf $(BOOT_CLASSES); 								\
	$(AR) $(ARFLAGS) $(MODULEDIR)/$(MAINCONTAINER_LIBNAME) $(MAINCONTAINER_CLASSES); 	\
	rm -rf $(MAINCONTAINER_CLASSES); 							\
	$(AR) $(ARFLAGS) $(MODULEDIR)/$(KERNELJ2SE_LIBNAME) $(KERNELJ2SE_CLASSES); 		\
	rm -rf $(KERNELJ2SE_CLASSES); 								\
	$(AR) $(ARFLAGS) $(MODULEDIR)/$(UTILS_LIBNAME) $(UTILS_CLASSES); 			\
	rm -rf $(UTILS_CLASSES); 								\
	$(AR) $(ARFLAGS) $(MODULEDIR)/$(TOOLS_LIBNAME) $(TOOLS_CLASSES); 			\
	rm -rf $(TOOLS_CLASSES); 								\
	$(AR) $(ARFLAGS) $(MODULEDIR)/$(IMTPRMI_LIBNAME) $(IMTPRMI_CLASSES); 			\
	rm -rf $(IMTPRMI_CLASSES); 								\
	$(AR) $(ARFLAGS) $(MODULEDIR)/$(KERNELSHARE_LIBNAME) $(KERNELSHARE_CLASSES); 		\
	rm -rf $(KERNELSHARE_CLASSES);


#	$(AR) $(ARFLAGS) $(LIBDIR)/$(LIBAPPLETNAME) jade/core/AgentGroup.class jade/core/IIOPAddress*.class jade/core/IIOPFormatException.class jade/domain/AgentManagementOntology*.class jade/domain/AgentManagementParser*.class jade/domain/*Exception.class jade/domain/ASCII_CharStream.class jade/domain/Token*.class jade/gui/AboutJadeAction*.class jade/gui/AgentNameTableModel.class jade/gui/ConstraintDlg*.class jade/gui/DFAgentDscDlg*.class jade/gui/DFGUI*.class jade/gui/GUI2DFCommunicatorInterface.class jade/gui/IntRetValue.class jade/gui/ServiceDsc*.class jade/gui/StringDlg*.class jade/gui/TreeHelp*.class jade/gui/GuiEvent.class jade/gui/images jade/gui/help/*.html jade/lang/acl/*.class jade/onto/*.class jade/tools/applet/*.class  \

clean:
	rm -f $(LIBDIR)/$(BOOT_LIBNAME)
	rm -f $(LIBDIR)/$(MAINCONTAINER_LIBNAME)
	rm -f $(LIBDIR)/$(KERNELJ2SE_LIBNAME)
	rm -f $(LIBDIR)/$(UTILS_LIBNAME)
	rm -f $(LIBDIR)/$(TOOLS_LIBNAME)
	rm -f $(LIBDIR)/$(IMTPRMI_LIBNAME)
	rm -f $(LIBDIR)/$(IIOP_LIBNAME)
	rm -f $(LIBDIR)/$(KERNELSHARE_LIBNAME)
	rm -f $(LIBDIR)/$(JADE_LIBNAME)
	rm -f $(LIBDIR)/$(JADETOOLS_LIBNAME)
	rm -f $(LIBDIR)/$(BASE64_LIBNAME)

	rm -f `find . -name '*~' -print`
	rm -f `find . -name '#*#' -print`
	rm -f `find . -name '*.class' -print`

batch:
	rm -f $(BATCH_LIB);
	echo '@REM ===============================================================' > $(BATCH_LIB)
	echo '@REM =           Generated by JADE Makefile. DO NOT EDIT           =' >> $(BATCH_LIB)
	echo '@REM ===============================================================' >> $(BATCH_LIB)
	echo >> $(BATCH_LIB)
	echo 'cd $(subst /,\,$(subst $(ROOTDIR),.,$(CLSDIR)))' >> $(BATCH_LIB)
	echo '@REM Not distributed for this version $(AR) $(ARFLAGS) ..\lib\$(LIBAPPLETNAME) jade\core\AgentGroup.class jade\core\IIOPAddress*.class jade\core\IIOPFormatException.class jade\domain\AgentManagementOntology*.class jade\domain\AgentManagementParser*.class jade\domain\*Exception.class jade\domain\ASCII_CharStream.class jade\domain\Token*.class jade\gui\AboutJadeAction*.class jade\gui\AgentNameTableModel.class jade\gui\ConstraintDlg*.class jade\gui\DFAgentDscDlg*.class jade\gui\DFGUI*.class jade\gui\GUI2DFCommunicatorInterface.class jade\gui\IntRetValue.class jade\gui\ServiceDsc*.class jade\gui\StringDlg*.class jade\gui\TreeHelp*.class jade\gui\GuiEvent.class jade\gui\images jade\gui\help\*.html jade\lang\acl\*.class jade\onto\*.class jade\tools\applet\*.class ' >> $(BATCH_LIB)
	echo >> $(BATCH_LIB)

	echo '$(AR) $(MANIFESTARFLAGS) jade.mf $(subst /,\,$(LIBDIR)\$(JADE_LIBNAME)) Notice.txt' >> $(BATCH_LIB)
	echo '$(AR) $(MANIFESTARFLAGS) jadeTools.mf $(subst /,\,$(LIBDIR)\$(JADETOOLS_LIBNAME)) Notice.txt' >> $(BATCH_LIB)

	echo '$(AR) $(ARFLAGS) $(subst /,\,$(LIBDIR)\$(IIOP_LIBNAME)) $(subst /,\,$(IIOP_CLASSES))' >> $(BATCH_LIB)
	echo 'rmdir /S /Q $(subst /,\,$(IIOP_CLASSES))' >> $(BATCH_LIB) # For Win NT 4
	echo 'deltree /Y $(subst /,\,$(IIOP_CLASSES))' >> $(BATCH_LIB)  # For Win 95/98	
	echo >> $(BATCH_LIB)

	echo '$(AR) $(ARFLAGS) $(subst /,\,$(MODULEDIR)\$(BOOT_LIBNAME)) $(subst /,\,$(BOOT_CLASSES))' >> $(BATCH_LIB)
	echo 'rmdir /S /Q $(subst /,\,$(BOOT_CLASSES))' >> $(BATCH_LIB) # For Win NT 4
	echo 'deltree /Y $(subst /,\,$(BOOT_CLASSES))' >> $(BATCH_LIB)  # For Win 95/98	
	echo >> $(BATCH_LIB)

	echo '$(AR) $(ARFLAGS) $(subst /,\,$(MODULEDIR)\$(MAINCONTAINER_LIBNAME)) $(subst /,\,$(MAINCONTAINER_CLASSES))' >> $(BATCH_LIB)
	echo 'rmdir /S /Q $(subst /,\,$(MAINCONTAINER_CLASSES))' >> $(BATCH_LIB) # For Win NT 4
	echo 'deltree /Y $(subst /,\,$(MAINCONTAINER_CLASSES))' >> $(BATCH_LIB)  # For Win 95/98	
	echo >> $(BATCH_LIB)

	echo '$(AR) $(ARFLAGS) $(subst /,\,$(MODULEDIR)\$(KERNELJ2SE_LIBNAME)) $(subst /,\,$(KERNELJ2SE_CLASSES))' >> $(BATCH_LIB)
	echo 'rmdir /S /Q $(subst /,\,$(KERNELJ2SE_CLASSES))' >> $(BATCH_LIB) # For Win NT 4
	echo 'deltree /Y $(subst /,\,$(KERNELJ2SE_CLASSES))' >> $(BATCH_LIB)  # For Win 95/98	
	echo >> $(BATCH_LIB)

	echo '$(AR) $(ARFLAGS) $(subst /,\,$(MODULEDIR)\$(UTILS_LIBNAME)) $(subst /,\,$(UTILS_CLASSES))' >> $(BATCH_LIB)
	echo 'rmdir /S /Q $(subst /,\,$(UTILS_CLASSES))' >> $(BATCH_LIB) # For Win NT 4
	echo 'deltree /Y $(subst /,\,$(UTILS_CLASSES))' >> $(BATCH_LIB)  # For Win 95/98	
	echo >> $(BATCH_LIB)

	echo '$(AR) $(ARFLAGS) $(subst /,\,$(MODULEDIR)\$(TOOLS_LIBNAME)) $(subst /,\,$(TOOLS_CLASSES))' >> $(BATCH_LIB)
	echo 'rmdir /S /Q $(subst /,\,$(TOOLS_CLASSES))' >> $(BATCH_LIB) # For Win NT 4
	echo 'deltree /Y $(subst /,\,$(TOOLS_CLASSES))' >> $(BATCH_LIB)  # For Win 95/98	
	echo >> $(BATCH_LIB)

	echo '$(AR) $(ARFLAGS) $(subst /,\,$(MODULEDIR)\$(IMTPRMI_LIBNAME)) $(subst /,\,$(IMTPRMI_CLASSES))' >> $(BATCH_LIB)
	echo 'rmdir /S /Q $(subst /,\,$(IMTPRMI_CLASSES))' >> $(BATCH_LIB) # For Win NT 4
	echo 'deltree /Y $(subst /,\,$(IMTPRMI_CLASSES))' >> $(BATCH_LIB)  # For Win 95/98	
	echo >> $(BATCH_LIB)

	echo '$(AR) $(ARFLAGS) $(subst /,\,$(MODULEDIR)\$(KERNELSHARE_LIBNAME)) $(subst /,\,$(KERNELSHARE_CLASSES))' >> $(BATCH_LIB)
	echo 'rmdir /S /Q $(subst /,\,$(KERNELSHARE_CLASSES))' >> $(BATCH_LIB) # For Win NT 4
	echo 'deltree /Y $(subst /,\,$(KERNELSHARE_CLASSES))' >> $(BATCH_LIB)  # For Win 95/98	
	echo >> $(BATCH_LIB)





	echo '$(AR) $(ARFLAGS) ..\lib\$(LIBIIOPNAME) jade\mtp\iiop FIPA' >> $(BATCH_LIB)
	echo 'rmdir /S /Q jade\mtp\iiop' >> $(BATCH_LIB) # For Win NT 4
	echo 'deltree /Y jade\mtp\iiop' >> $(BATCH_LIB)  # For Win 95/98
	echo 'rmdir /S /Q  FIPA' >> $(BATCH_LIB) # For Win NT 4
	echo 'deltree /Y  FIPA' >> $(BATCH_LIB)  # For Win 95/98
	echo >> $(BATCH_LIB)
	echo '$(AR) $(MANIFESTARFLAGS) ..\lib\$(LIBNAME)  jade' >> $(BATCH_LIB)
	echo 'rmdir /S /Q  jade' >> $(BATCH_LIB) # For Win NT 4
	echo 'deltree /Y  jade' >> $(BATCH_LIB)  # For Win 95/98
	echo >> $(BATCH_LIB)
	echo '$(AR) $(ARFLAGS) ..\lib\$(LIBBASE64NAME) starlight' >> $(BATCH_LIB)
	echo 'rmdir /S /Q starlight' >> $(BATCH_LIB) # For NT 4
	echo 'deltree /Y starlight' >> $(BATCH_LIB)  # For Win 95/98
	echo 'cd ..' >> $(BATCH_LIB)


	echo 'del $(subst /,\,$(subst $(ROOTDIR),.,$(LIBDIR)))\$(LIBTOOLSNAME)' >> $(BATCH_CLEAN)
	echo 'del $(subst /,\,$(subst $(ROOTDIR),.,$(LIBDIR)))\$(LIBNAME)' >> $(BATCH_CLEAN)
	echo 'del $(subst /,\,$(subst $(ROOTDIR),.,$(LIBDIR)))\$(LIBBASE64NAME)' >> $(BATCH_CLEAN)



