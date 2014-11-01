#
# Copyright (c) 1997-1999, 2007 Sun Microsystems, Inc.
# All Rights Reserved.
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

PKGDIR=	com/sun/nfs

include ../Makefile.defs

CLASSES= Buffer Fattr Fattr2 Fattr3 \
	NfsSecurity Mount Nfs Nfs2 Nfs3 \
	NfsConnect NfsException NfsURL NfsHandler \
	XFileAccessor XFileExtensionAccessor \
	nfsXFileExtensionAccessor

JFILES=	$(CLASSES:%=%.java)

#
# properties file
#
PROP_FILES=  nfssec.properties
PFILES= $(PROP_FILES:%=$(CDIR)/$(PKGDIR)/%)


all: classes $(PFILES)

classes:        $(CDIR)/$(PKGDIR) $(TFILE)

$(PFILES):
	$(RM) $@; install -s -m 644 -f $(@D) $(PROP_FILES)
 
clean:
	$(RM) *.class $(CDIR)/$(PKGDIR)/*.class $(TFILE) $(PFILES)
 
$(TFILE):	$(JFILES)
	$(JC) -d $(CDIR) $(JCFLAGS) $?
	@touch $(TFILE)

$(CDIR)/$(PKGDIR): .FRC
	@if [ ! -d $@ ]; then echo mkdir $@ ; mkdir -p $@ ; fi
 
.FRC:
 
.KEEP_STATE:

