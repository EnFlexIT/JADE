# Makefile for JADE project

VERSION    = 0.96
ARCHIVE    = JADE

ROOTDIR = $(shell pwd)
ROOTNAME = $(shell basename $(ROOTDIR))
SRCDIR  = $(ROOTDIR)/src
LIBDIR  = $(ROOTDIR)/lib
LIBNAME = JADE.zip
EXAMPLESDIR = $(SRCDIR)/examples
MAKE = make

export ROOTDIR
export ROOTNAME
export SRCDIR
export LIBDIR
export LIBNAME
export EXAMPLESDIR
export MAKE

# The following targets are not file names
.PHONY: all clean doc archive src lib examples

all: doc lib examples
	@echo JADE project built

doc:
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
	rm -f *~ "#*#"
	cd $(SRCDIR); $(MAKE) clean
	cd $(LIBDIR); $(MAKE) clean
	cd $(EXAMPLESDIR); $(MAKE) clean

archive: clean
	cd $(ROOTDIR)/..; \
	tar zcvf $(ARCHIVE)-$(VERSION).tgz $(ROOTNAME); \
	cd $(ROOTDIR)
