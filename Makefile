# Makefile for Agent Development Kit project 

VERSION    = 0.6
ARCHIVE    = ADK


export ROOTDIR = $(shell pwd)
export SRCDIR  = $(ROOTDIR)/src
export LIBDIR  = $(ROOTDIR)/lib
export LIBNAME = ADK.zip

export TESTDIR = $(ROOTDIR)/test

export MAKE = make

# The following targets are not file names
.PHONY: all clean archive src lib test

all: lib test
	@echo ACL parser built

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
	tar zcvf $(ARCHIVE)-$(VERSION).tgz $(ROOTDIR); \
	cd $(ROOTDIR)
