# Makefile for JADE project

VERSION    = 1.0
ARCHIVE    = JADE

ROOTDIR = $(shell pwd)
ROOTNAME = $(shell basename $(ROOTDIR))
DOCDIR  = $(ROOTDIR)/doc
SRCDIR  = $(ROOTDIR)/src
LIBDIR  = $(ROOTDIR)/lib
LIBNAME = JADE.jar
EXAMPLESDIR = $(SRCDIR)/examples
MAKE = make

export ROOTDIR
export ROOTNAME
export SRCDIR
export DOCDIR
export LIBDIR
export LIBNAME
export EXAMPLESDIR
export MAKE

# The following targets are not file names
.PHONY: all clean doc archive src lib examples

all: lib examples
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

clean:
	rm -f *~ "#*#" *.IOR
	cd $(SRCDIR); $(MAKE) clean
	cd $(DOCDIR); $(MAKE) clean
	cd $(LIBDIR); $(MAKE) clean
	cd $(EXAMPLESDIR); $(MAKE) clean

archive: clean
	cd $(ROOTDIR)/..; \
	tar zcvf $(ARCHIVE)-$(VERSION).tgz $(ROOTNAME); \
	cd $(ROOTDIR)
