# Makefile for JADE demo applications.

.PHONY:	all clean

all: MeetingSchedulerAgent
	@echo Making demo applications

MeetingSchedulerAgent:
	@echo Making Meeting Scheduler application
	$(JC) $(JFLAGS) -classpath $(CLASSPATH):$(SRCDIR):$(CLSDIR):$(DEMODIR)/MeetingScheduler/components.jar $(DEMODIR)/MeetingScheduler/MeetingSchedulerAgent.java

clean:
	rm -f `find . -name '*~' -print`
	rm -f `find . -name '#*#' -print`
