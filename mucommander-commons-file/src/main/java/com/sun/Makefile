#
# Copyright (c) 1997-1999, 2007 Sun Microsystems, Inc. 
# All  Rights Reserved.
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions 
# are met:
# 
# -Redistributions of source code must retain the above copyright notice,
# this list of conditions and the following disclaimer.
# 
# -Redistribution in binary form must reproduce the above copyright
# notice, this list of conditions and the following disclaimer in the
# documentation and/or other materials provided with the distribution.
# 
# Neither the name of Sun Microsystems, Inc. or the names of contributors
# may be used to endorse or promote products derived from this software
# without specific prior written permission.
# 
# This software is provided "AS IS," without a warranty of any kind. ALL
# EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
# ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
# PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS
# SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE
# AS A RESULT OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE
# SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE
# LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
# SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
# AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
# INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGES.
# 
# You acknowledge that this software is not designed,licensed or intended
# for use in the design, construction, operation or maintenance of any
# nuclear facility.
# 
#

SRCDIR=		$(CODEMGR_WS)/src
DOCDIR=		$(CODEMGR_WS)/doc
CDIR=		$(CODEMGR_WS)/classes
SUBDIRS=	xfile gssapi rpc nfs file xhandler xfilechooser # http
PKGS=		com.sun.rpc com.sun.nfs com.sun.xfile \
		com.sun.xfilechooser \
#		com.sun.http com.sun.gssapi com.sun.file 

DOCCLASSPATH=	/usr/java/lib/classes.zip:$(CODEMGR_WS)/classes

all clean:
	@for i in ${SUBDIRS}; do \
		echo "cd $$i ; ${MAKE} $@" ; \
		( cd $$i ; ${MAKE} $@ ) || exit 1 ; \
	done

jar: all
	cd $(CDIR)/com/sun/xfilechooser; \
	tar -xf $(SRCDIR)/com/sun/xfilechooser/beanImages.tar; \
	cd $(CDIR); \
	jar cvfm $(CDIR)/xfilechooser.jar \
	   $(SRCDIR)/com/sun/xfilechooser/manifest.bean com sun

zip: all
	cd $(CDIR); zip -r webnfs com sun -x *.tfile

javadoc: $(DOCDIR)
	javadoc -classpath $(SRCDIR):$(DOCCLASSPATH) -d $(DOCDIR) -public $(PKGS)


$(DOCDIR):
	@if [ ! -d $@ ]; then echo mkdir $@ ; mkdir -p $@ ; fi
	@sccs get -s images.tar
	@cd $@ ; tar -xf $(SRCDIR)/com/sun/images.tar
