# Makefile for JADE project - lib directory

# The following targets are not file names
.PHONY: all clean

AR = jar
ARFLAGS1 = c0vf
ARFLAGS2 = *.class jade/*.class jade/core/*.class jade/core/behaviours/*.class jade/domain/*.class jade/lang/acl/*.class jade/proto/*.class jade/tools/DummyAgent/*.class jade/tools/DummyAgent/images/*.gif jade/tools/rma/*.class jade/tools/rma/images/*.gif jade/gui/*.class

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


