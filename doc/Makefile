# Makefile for JADE project - 'doc' directory

DOC = javadoc -sourcepath $(SRCDIR)

DOC_MAIN = $(SRCDIR)/jade-main.html
DOC_OUTDIR = $(DOCDIR)/api
DOC_WINTITLE = "$(PACKAGE) $(VERSION) API Reference"

API_PACKAGES = jade jade.core jade.core.behaviours jade.lang.acl jade.domain jade.proto jade.gui jade.tools.DummyAgent

API_FLAGS = -d $(DOC_OUTDIR) -overview $(DOC_MAIN) -author -windowtitle $(DOC_WINTITLE)

SNIFFER_OUTDIR = ../doc/tools/sniffer/html/jdoc
SNIFFER_FLAGS =  -d $(SNIFFER_OUTDIR) -author -windowtitle "$(PACKAGE) $(VERSION) Sniffer Reference"
SNIFFER_PACKAGES = jade.tools.sniffer

.PHONY: all clean api tools sniffer

all: api tools

api: $(DOC_OUTDIR)
	$(DOC) $(API_FLAGS) $(API_PACKAGES)

$(DOC_OUTDIR):
	mkdir -p $(DOC_OUTDIR)

tools: sniffer

sniffer: $(SNIFFER_OUTDIR)
	$(DOC) $(SNIFFER_FLAGS) $(SNIFFER_PACKAGES)

$(SNIFFER_OUTDIR):
	mkdir -p $(SNIFFER_OUTDIR)

clean:

	rm -fr $(DOC_OUTDIR)/[a-z]*
	rm -fr $(SNIFFER_OUTDIR)/[a-z]*
