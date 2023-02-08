/*
 * Copyright (c) 1999, 2007 Sun Microsystems, Inc. 
 * All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE
 * AS A RESULT OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE
 * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE
 * LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
 * AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed,licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package com.sun.xfile;

/**
 * This is an abstract class to intended to be extended with
 * filesystem-specific methods.
 * <p>
 * An XFileExtensionAccessor class must be associated with an
 * XFileAccessor. An XFileExtensionAccessor can be used to
 * provide access to filesystem-specific methods that are not
 * defined within the XFileAccessor interface.
 * A subclass of XFileExtensionAccessor must be declared as:
 * <pre><code>
 *     import com.sun.xfile.*;
 *     <p>
 *     public class XFileExtensionAccessor extends com.sun.xfile.XFileExtensionAccessor {
 *      :
 *
 * </code></pre>
 * <p>
 * An XFileExtensionAccessor class is loaded when the 
 * <code>XFile.getExtensionAccessor()</code> method is invoked.  The
 * class loading process is identical to that of an
 * XFileAccessor except for the final component of the package
 * name: "XFileExtensionAccessor" instead of "XFileAccessor".
 * <p>
 * An application that needs to use the methods within the
 * XFileExtensionAccessor must cast the result of XFile.getExtensionAccessor.
 * <pre><code>
 *    import com.sun.xfile.*;
 *    <p>
 *    XFile xf = new XFile("ftp://server/path");
 *    com.acme.ftp.XFileExtensionAccessor xftp = 
 *        (com.acme.ftp.XFileExtensionAccessor) xf.getExtensionAccessor();
 *    xftp.login();
 *      :
 * </code></pre>
 *
 *
 * @author  Brent Callaghan
 * @see     com.sun.xfile.XFile#getExtensionAccessor()
 * @see     com.sun.xfile.XFileAccessor
 */
public abstract class XFileExtensionAccessor {
	
    private XFile xf;

    /*
     * Constructor for the XFileExtensionAccessor.
     *
     * Invoked by the XFile class when its getExtensionAccessor
     * method is called.  The <code>XFile</code> argument of
     * the constructor provides context for the methods
     * within the class.
     */
    public XFileExtensionAccessor(XFile xf) {
        this.xf = xf;
    }
}
