# Makefile for JADE project - lib directory

# The following targets are not file names
.PHONY: all clean

AR = jar
ARFLAGS = c0vf

all:
	@echo Making library ...
	cd $(CLSDIR); \
	$(AR) $(ARFLAGS) $(LIBDIR)/$(LIBTOOLSNAME) jade/tools; \
	rm -rf jade/tools; \
	$(AR) $(ARFLAGS) $(LIBDIR)/$(LIBNAME) *.class jade; \
	rm -rf jade *.class; \
	cd $(LIBDIR)

clean:
	rm -f $(LIBDIR)/$(LIBTOOLSNAME)
	rm -f $(LIBDIR)/$(LIBNAME)
	rm -f `find . -name '*~' -print`
	rm -f `find . -name '#*#' -print`
	rm -f `find . -name '*.class' -print`
