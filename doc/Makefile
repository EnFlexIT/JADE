# Makefile for JADE project - 'doc' directory

DOC = javadoc

DOC_MAIN = $(SRCDIR)/jade-main.html
DOC_OUTDIR = ./api
DOC_WINTITLE = "JADE 1.0 API Reference"

PACKAGES = jade jade.core jade.core.behaviours jade.lang.acl jade.domain jade.proto jade.gui jade.tools.DummyAgent

DOCFLAGS = -d $(DOC_OUTDIR) -overview $(DOC_MAIN) -author -windowtitle $(DOC_WINTITLE)

.PHONY: all clean

all:

#	 mkdir $(DOC_OUTDIR)
	$(DOC) $(DOCFLAGS) $(PACKAGES)

clean:

	rm -fr $(DOC_OUTDIR)/[a-z]*
