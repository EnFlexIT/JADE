# Makefile for JADE project

VERSION    = 1.25
ARCHIVE    = JADE

ROOTDIR = $(shell pwd)
ROOTNAME = $(shell basename $(ROOTDIR))
DOCDIR  = $(ROOTDIR)/doc
SRCDIR  = $(ROOTDIR)/src
LIBDIR  = $(ROOTDIR)/lib
LIBNAME = JADE.jar
EXAMPLESDIR = $(SRCDIR)/examples
DEMODIR = $(SRCDIR)/demo
MAKE = make

export ROOTDIR
export ROOTNAME
export SRCDIR
export DOCDIR
export LIBDIR
export LIBNAME
export EXAMPLESDIR
export DEMODIR
export MAKE

# The following targets are not file names
.PHONY: all clean doc archive src lib examples

all: lib examples demo
	@echo JADE project built

doc: clean
	cd $(DOCDIR); $(MAKE) all
	@echo HTML documentation built

lib: src
	cd $(LIBDIR); $(MAKE) all
	@echo Libraries built

src:
	cd $(SRCDIR); $(MAKE) all
	@echo Sources built

examples:
	cd $(EXAMPLESDIR); $(MAKE) all
	@echo Examples built.

demo:
	cd $(DEMODIR); $(MAKE) all
	@echo Demo applications built.

clean:
	rm -f *~ "#*#" JADE.IOR JADE.URL
	cd $(SRCDIR); $(MAKE) clean
	cd $(DOCDIR); $(MAKE) clean
	cd $(LIBDIR); $(MAKE) clean
	cd $(EXAMPLESDIR); $(MAKE) clean
	cd $(DEMODIR); $(MAKE) clean

realclean: clean
	cd $(SRCDIR); $(MAKE) idlclean

archive: clean
	cd $(ROOTDIR)/..; \
	tar zcvf $(ARCHIVE)-$(VERSION).tgz $(ROOTNAME); \
	cd $(ROOTDIR)
