# Makefile for Agent Development Kit project - src directory

# The following targets are not file names
.PHONY: all clean

export MAKE = make
export STUBGEN = rmic
export PARSERGEN = javacc

export JC = javac
export JFLAGS = -deprecation


PARSERS     = 	fipa/lang/acl/ACLParser.jj
EXECS       = 	fipa/core/StartPlatform.java fipa/core/StartContainer.java
ROOTCLASSES =	fipa/core/ComplexBehaviour.java fipa/core/SimpleBehaviour.java
STUBDIR     =	.
STUBS       = 	fipa.core.AgentContainerImpl \
		fipa.core.MessageDispatcherImpl \
		fipa.core.AgentPlatformImpl


all: classes parsers stubs 
	@echo
	@echo Build complete.
	@echo

parsers:
#	$(PARSERGEN) $(PARSERS)

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


