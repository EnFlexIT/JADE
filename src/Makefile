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

EXECS       = 	jade/Boot.java
ROOTCLASSES =	jade/core/ComplexBehaviour.java jade/core/SimpleBehaviour.java
STUBDIR     =	.
STUBS       = 	jade.core.AgentContainerImpl \
		jade.core.MessageDispatcherImpl \
		jade.core.AgentPlatformImpl


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
	rm -f jade/lang/acl/ACLParser.java
	rm -f jade/lang/acl/ACLParserConstants.java
	rm -f jade/lang/acl/ACLParserTokenManager.java
	rm -f jade/lang/acl/ASCII_CharStream.java
	rm -f jade/lang/acl/ParseException.java
	rm -f jade/lang/acl/Token.java
	rm -f jade/lang/acl/TokenMgrError.java


