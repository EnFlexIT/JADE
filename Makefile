# Makefile for JADE project

VERSION    = 1.3
PACKAGE    = JADE

ZIP = tar
ZIPFLAGS = zcvf
ZIPEXT = tgz

ROOTDIR = $(shell pwd)
ROOTNAME = $(shell basename $(ROOTDIR))
DOCDIR  = $(ROOTDIR)/doc
SRCDIR  = $(ROOTDIR)/src
CLSDIR  = $(ROOTDIR)/classes
LIBDIR  = $(ROOTDIR)/lib
LIBNAME = jade.jar
LIBTOOLSNAME = jadeTools.jar
LIBBASE64NAME = Base64.jar
EXAMPLESDIR = $(SRCDIR)/examples
DEMODIR = $(SRCDIR)/demo
MAKE = make

JC = javac
JFLAGS = -deprecation -d $(CLSDIR)

BATCH_ALL = $(ROOTDIR)/build.bat
BATCH_DOC = $(ROOTDIR)/makedoc.bat
BATCH_LIB = $(ROOTDIR)/makelib.bat
BATCH_CLEAN = $(ROOTDIR)/clean.bat

export VERSION
export PACKAGE
export ROOTDIR
export ROOTNAME
export DOCDIR
export SRCDIR
export CLSDIR
export LIBDIR
export LIBNAME
export LIBTOOLSNAME
export LIBBASE64NAME
export EXAMPLESDIR
export DEMODIR
export MAKE

export JC
export JFLAGS

export BATCH_ALL
export BATCH_DOC
export BATCH_LIB
export BATCH_CLEAN

# The following targets are not file names
.PHONY: all clean doc archive binarchive src lib examples

all: src examples demo
	@echo JADE project built

doc: $(DOCDIR) clean
	cd $(DOCDIR); $(MAKE) all
	@echo HTML documentation built

lib:
	cd $(LIBDIR); $(MAKE) all
	@echo Libraries built

src: $(CLSDIR)
	cd $(SRCDIR); $(MAKE) all
	@echo Sources built

examples: $(CLSDIR)
	cd $(EXAMPLESDIR); $(MAKE) all
	@echo Examples built.

demo: $(CLSDIR)
	cd $(DEMODIR); $(MAKE) all
	@echo Demo applications built.

clean:
	rm -f `find . -name '*~'`
	rm -f `find . -name '#*#'`
	rm -f `find . -name JADE.IOR`
	rm -f `find . -name JADE.URL`
	rm -fr $(CLSDIR)/*
	cd $(DOCDIR); $(MAKE) clean
	cd $(LIBDIR); $(MAKE) clean
	cd $(EXAMPLESDIR); $(MAKE) clean
	cd $(DEMODIR); $(MAKE) clean

realclean: clean
	cd $(SRCDIR); $(MAKE) idlclean
	cd $(SRCDIR); $(MAKE) jjclean
	rm -f $(BATCH_ALL)
	rm -f $(BATCH_DOC)
	rm -f $(BATCH_LIB)

idlclean: clean
	cd $(SRCDIR); $(MAKE) idlclean

jjclean: clean
	cd $(SRCDIR); $(MAKE) jjclean

archive: $(CLSDIR) $(DOCDIR) doc 
	cd $(ROOTDIR)/..; \
	$(ZIP) $(ZIPFLAGS) $(PACKAGE)-$(VERSION)-src.tgz $(ROOTNAME); \
	cd $(ROOTDIR)

binarchive: $(CLSDIR) $(DOCDIR) doc all lib
	cd $(ROOTDIR)/..; \
	$(ZIP) $(ZIPFLAGS) $(PACKAGE)-$(VERSION)-bin.$(ZIPEXT) $(ROOTNAME); \
	cd $(ROOTDIR)

batch:
	cd $(SRCDIR); $(MAKE) batch
	cd $(EXAMPLESDIR); $(MAKE) batch
	cd $(DOCDIR); $(MAKE) batch
	cd $(LIBDIR); $(MAKE) batch

$(CLSDIR):
	mkdir $(CLSDIR)

$(DOCDIR):
	mkdir $(DOCDIR)
