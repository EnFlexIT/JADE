# Makefile for JADE project - 'doc' directory

DOC = javadoc

DOC_MAIN = $(SRCDIR)/jade-main.html
DOC_OUTDIR = ./api
DOC_WINTITLE = "JADE 1.2 API Reference"

API_PACKAGES = jade jade.core jade.core.behaviours jade.lang.acl jade.domain jade.proto jade.gui jade.tools.DummyAgent

API_FLAGS = -d $(DOC_OUTDIR) -overview $(DOC_MAIN) -author -windowtitle $(DOC_WINTITLE)

SNIFFER_OUTDIR = ../doc/sniffer/html/jdoc
SNIFFER_FLAGS =  -d $(SNIFFER_OUTDIR) -author -windowtitle "JADE 1.2 Sniffer Reference"
SNIFFER_PACKAGES = jade.tools.sniffer

.PHONY: all clean api tools sniffer

all: api tools

api:
#	 mkdir $(DOC_OUTDIR)
	$(DOC) $(API_FLAGS) $(API_PACKAGES)

tools: sniffer

sniffer:
	$(DOC) $(SNIFFER_FLAGS) $(SNIFFER_PACKAGES)

clean:

	rm -fr $(DOC_OUTDIR)/[a-z]*
	rm -fr $(SNIFFER_OUTDIR)/[a-z]*
