# Makefile for Agent Development Kit project - src directory

# The following targets are not file names
.PHONY: all clean

export MAKE = make
export STUBGEN = rmic
export PARSERGEN = javacc

export JC = javac
export JFLAGS = -deprecation


ACLFLAGS    =   -OUTPUT_DIRECTORY:fipa/lang/acl
ACLSPEC     = 	fipa/lang/acl/ACLParser.jj

EXECS       = 	fipa/core/StartPlatform.java fipa/core/StartContainer.java
ROOTCLASSES =	fipa/core/ComplexBehaviour.java fipa/core/SimpleBehaviour.java
STUBDIR     =	.
STUBS       = 	fipa.core.AgentContainerImpl \
		fipa.core.MessageDispatcherImpl \
		fipa.core.AgentPlatformImpl


all: parsers classes stubs 
	@echo
	@echo Build complete.
	@echo

parsers: ACLParser
	@echo Parsers generated

ACLParser:
	$(PARSERGEN) $(ACLFLAGS) $(ACLSPEC)

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
	rm -f fipa/lang/acl/ACLParser.java
	rm -f fipa/lang/acl/ACLParserConstants.java
	rm -f fipa/lang/acl/ACLParserTokenManager.java
	rm -f fipa/lang/acl/ASCII_CharStream.java
	rm -f fipa/lang/acl/ParseException.java
	rm -f fipa/lang/acl/Token.java
	rm -f fipa/lang/acl/TokenMgrError.java


