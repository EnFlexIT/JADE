# Makefile for JADE  project 

VERSION    = 0.91
ARCHIVE    = JADE


export ROOTDIR = $(shell pwd)
export ROOTNAME = $(shell basename $(ROOTDIR))
export SRCDIR  = $(ROOTDIR)/src
export LIBDIR  = $(ROOTDIR)/lib
export LIBNAME = JADE.zip

export EXAMPLESDIR = $(SRCDIR)/examples

export MAKE = make

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
