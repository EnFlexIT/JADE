# Makefile for JADE  project 

VERSION    = 0.7
ARCHIVE    = JADE


export ROOTDIR = $(shell pwd)
export ROOTNAME = $(shell basename $(ROOTDIR))
export SRCDIR  = $(ROOTDIR)/src
export LIBDIR  = $(ROOTDIR)/lib
export LIBNAME = JADE.zip

export TESTDIR = $(ROOTDIR)/test

export MAKE = make

# The following targets are not file names
.PHONY: all clean archive src lib test

all: lib test
	@echo JADE project built

lib: src
	cd $(LIBDIR); $(MAKE) all
	@echo libraries built

src:
	cd $(SRCDIR); $(MAKE) all
	@echo Sources built

test:
	cd $(TESTDIR); $(MAKE) all
	@echo Test examples built

clean:
	rm -f *~ "#*#"
	cd $(SRCDIR); $(MAKE) clean
	cd $(LIBDIR); $(MAKE) clean
	cd $(TESTDIR); $(MAKE) clean

archive: clean
	cd $(ROOTDIR)/..; \
	tar zcvf $(ARCHIVE)-$(VERSION).tgz $(ROOTNAME); \
	cd $(ROOTDIR)
