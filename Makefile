# Makefile for Agent Development Kit project 

ROOTDIR    = ADK
VERSION    = 0.5
ARCHIVE    = ADK
SRCDIR     = src
LIBDIR     = lib
TESTDIR    = test

export MAKE = make

# The following targets are not file names
.PHONY: all clean archive $(SRCDIR) $(BINDIR)

all: $(LIBDIR) $(TESTDIR) 
	@echo ACL parser built

$(LIBDIR): $(SRCDIR)
	cd $(LIBDIR); $(MAKE) all
	@echo libraries built

$(SRCDIR):
	cd $(SRCDIR); $(MAKE) all
	@echo sources built

clean:
	rm -f *~ "#*#"
	cd $(SRCDIR); $(MAKE) clean
	cd $(LIBDIR); $(MAKE) clean
	cd $(TESTDIR); $(MAKE) clean

archive: clean
	cd ..; \
	tar zcvf $(ARCHIVE)-$(VERSION).tgz $(ROOTDIR); \
	cd $(ROOTDIR);

