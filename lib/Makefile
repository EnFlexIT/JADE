# Makefile for Agent Development Kit project - lib directory

# The following targets are not file names
.PHONY: all clean


AR = zip
ARFLAGS1 = -0r
ARFLAGS2 = . -i '*.class'

all: 
	@echo Making library ...
	cd $(SRCDIR); \
	$(AR) $(ARFLAGS1) $(LIBDIR)/$(LIBNAME) $(ARFLAGS2); \
	cd $(LIBDIR)

clean:
	rm -f $(LIBDIR)/$(LIBNAME)
	rm -f `find . -name '*~' -print`
	rm -f `find . -name '#*#' -print`
	rm -f `find . -name '*.class' -print`


