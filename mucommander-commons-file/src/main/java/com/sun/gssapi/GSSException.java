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
 
package com.sun.gssapi;

/**
 * This exception is thrown whenever a fatal GSS-API error occurs
 * including mechanism specific errors. It contains 
 * both the major and minor JGSS status codes. The mechanism 
 * implementers are responsible for setting appropriate minor status 
 * codes when throwing this exception. Methods are included to retrieve
 * the error string representation for both major and minor codes.
 * <DL><DT><b>RFC 2078</b>
 * <DD> GSS-API major status codes are divided into fatal and
 * informatory status codes. In JGSS, fatal codes are represented using
 * exceptions of this class, and informatory codes are returned through
 * instances of the MesssageProp class used in the GSSContext methods.
 * This class also provides the functionality of gss_display_status.</dl>
 * @see MessageProp
 * @see GSSContext#unwrap
 * @see GSSContext#verifyMIC
 */
public class GSSException extends Exception {

    /**
     * Channel bindings mismatch error.
     */
    public static final int BAD_BINDINGS = 1; //start with 1

    /**
     * Unsupported mechanism requested error.
     */
    public static final int BAD_MECH = 2;

    /**
     * Invalid name provided error.
     */
    public static final int BAD_NAME = 3;

    /**
     * Name of unsupported type provided error.
     */
    public static final int BAD_NAMETYPE = 4;

    /**
     * Invalid status code error - this is the default status value.
     */     
    public static final int BAD_STATUS = 5;

    /**
     * Token had invalid integrity check error.
     */
    public static final int BAD_MIC = 6;

    /**
     * Specified security context expired error.
     */
    public static final int CONTEXT_EXPIRED = 7;

    /**
     * Expired credentials detected error.
     */
    public static final int CREDENTIALS_EXPIRED  = 8;

    /**
     * Defective credential error.
     */
    public static final int DEFECTIVE_CREDENTIAL = 9;

    /**
     * Defective token error.
     */
    public static final int DEFECTIVE_TOKEN = 10;

    /**
     * General failure, unspecified at GSS-API level.
     */
    public static final int FAILURE = 11;

    /**
     * Invalid security context error.
     */
    public static final int NO_CONTEXT = 12;

    /**
     * Invalid credentials error.
     */
    public static final int NO_CRED = 13;
        
    /**
     * Unsupported QOP value error.
     */
    public static final int BAD_QOP = 14;

    /**
     * Operation unauthorized error.
     */
    public static final int UNAUTHORIZED = 15;
    
    /**
     * Operation unavailable error.
     */
    public static final int UNAVAILABLE = 16;

    /**
     * Duplicate credential element requested error.
     */
    public static final int DUPLICATE_ELEMENT = 17;

    /**
     * Name contains multi-mechanism elements error.
     */
    public static final int NAME_NOT_MN = 18;


    /**
     * The token was a duplicate of an earlier token.
     * This is a fatal error code that may occur during
     * context establishment.  It is not used to indicate
     * supplementary status values. The MessageProp object is
     * used for that purpose.
     */
    public static final int DUPLICATE_TOKEN = 19;


    /**
     * The token's validity period has expired.  This is a
     * fatal error code that may occur during context establishment.
     * It is not used to indicate supplementary status values.
     * The MessageProp object is used for that purpose.
     */
    public static final int OLD_TOKEN = 20;


    /**
     * A later token has already been processed.  This is a
     * fatal error code that may occur during context establishment.
     * It is not used to indicate supplementary status values.
     * The MessageProp object is used for that purpose.
     */
    public static final int UNSEQ_TOKEN = 21;

    
    /**
     * An expected per-message token was not received.  This is a
     * fatal error code that may occur during context establishment.
     * It is not used to indicate supplementary status values.
     * The MessageProp object is used for that purpose.
     */
    public static final int GAP_TOKEN = 22;


    private static String[] messages = {
        "channel binding mismatch", // BAD_BINDINGS
        "unsupported mechanism requested", // BAD_MECH
        "invalid name provided", // BAD_NAME
        "name of unsupported type provided", //BAD_NAMETYPE
        "invalid input status selector", // BAD_STATUS
        "token had invalid integrity check", // BAD_SIG
        "specified security context expired", // CONTEXT_EXPIRED
        "expired credentials detected", // CREDENTIALS_EXPIRED
        "defective credential detected", // DEFECTIVE_CREDENTIAL
        "defective token detected", // DEFECTIVE_TOKEN
        "failure, unspecified at GSS-API level", // FAILURE
        "security context init/accept not yet called or context deleted",
						// NO_CONTEXT  
        "no valid credentials provided", // NO_CRED
        "unsupported QOP value", // BAD_QOP
        "operation unauthorized", // UNAUTHORIZED
        "operation unavailable", // UNAVAILABLE
        "duplicate credential element requested", //DUPLICATE_ELEMENT
        "name contains multi-mechanism elements", // NAME_NOT_MN
        "the token was a duplicate of an earlier token", //DUPLICATE_TOKEN
        "the token's validity period has expired", //OLD_TOKEN
        "a later token has already been processed", //UNSEQ_TOKEN
        "an expected per-message token was not received", //GAP_TOKEN
    };

    private int m_major; // the major code for this exception
    private int m_minor = 0; // the minor code for this exception
    private String m_minorMessage = null; //text string for minor code


    /**
     * Construct a GSSException object with a specified major code.
     *
     * @param majorCode the fatal error code causing this exception.
     *    This value should be one of the ones defined in this
     *     class. Invalid error codes get mapped to BAD_STATUS value.
     */
    public GSSException (int majorCode) {
        
        if (validateMajor(majorCode))
            m_major = majorCode;
        else
            m_major = BAD_STATUS;
    }

    /**
     * Construct a GSSException object with a specified major and
     * minor codes and minor explanation string.
     *
     * @param majorCode the fatal error code causing this exception.
     *     This value should be one of the ones defined in this class.
     *     Invalid error codes get mapped to BAD_STATUS value.
     * @param minorCode the mechanism specific error code.
     * @param minorString explanation of the minorCode.
     */
    public GSSException (int majorCode, int minorCode, String minorString) {
        
        if (validateMajor(majorCode))
            m_major = majorCode;
        else
            m_major = BAD_STATUS;

        m_minor = minorCode;
        m_minorMessage = minorString;
    }

    /**
     * Returns the major code representing the error that caused this
     * exception to be thrown.
     *
     * <DL><DT><b>RFC 2078</b>
     *    <DD> equivalent to major code being returned from function
     *    </dl> 
     * @return int the fatal error code causing this exception
     * @see #getMajorString
     */
    public int getMajor() {

        return m_major;
    }

    /**
     * Returns the mechanism error that caused this exception. The
     * minor code is set by the underlying mechanism. Value of 0
     * indicates that mechanism error code is not set.
     *
     * <DL><DT><b>RFC 2078</b>
     *     <DD>equivalent to minor status codes in GSS-API functions
     *    </dl>
     *
     * @return int the mechanism error code; 0 indicates that it has
     *    not been set.
     * @see #setMinor
     * @see #getMinorString
     */
    public int  getMinor(){
        
        return m_minor;
    }


    /**
     * Returns a string explaining the major code in this exception.
     *
     * <DL><DT><b>RFC 2078</b>
     *     <DD> equivalent to gss_display_status for the major error
     *    code.</dl>
     * @return String explanation string for the major error code
     * @see #getMajor
     * @see #toString
     */
    public String getMajorString() {
    
        return messages[m_major - 1];
    }


    /**
     * Returns a string explaining the mechanism specific error code.
     * Can be used with the getMajorString call to provide mechanism
     * specific error details. If the minor status code is 0, then
     * no other error details will be available.
     *
     * <DL><DT><b>RFC 2078</b>
     *     <DD>equivalent to gss_display_status for the minor code.
     *    </dl>
     * @return String a textual explanation of mechanism error code
     * @see #getMinor
     * @see #getMajorString
     * @see #toString
     */
    public String getMinorString() {
    
        if (m_minorMessage == null)
            m_minorMessage = "";
            
        return m_minorMessage;
    }


    /**
     * Used by the exception thrower to set the mechanism error code
     * and its string explanation.  This is used by mechanism
     * providers to indicate error details.
     *
     * @param minorCode the mechanism specific error code
     * @param message textual explanation of the mechanism error code
     */
    public void setMinor(int minorCode, String message) {
    
        m_minor = minorCode;
        m_minorMessage = message;
    }
    

    /** 
     * Returns string representing both the major and minor status
     * codes.
     *
     * @return a String with the error descriptions
     * @overrides Object#toString
     */
    public String toString() {

        if (m_minor == 0)
            return (getMajorString());
            
        return (getMajorString() + "(" + getMinorString() + ")");
    }


    /** 
     * Returns string representing both the major and minor status
     * codes.
     *
     * @return a String with the error descriptions
     * @overrides Throwable#geMessage
     */
    public String getMessage() {

        return (toString());
    }


    /*
     * Validates the major code in the proper range.
     */
    private boolean validateMajor(int major) {
    
        if (m_major > 0 && m_major <= messages.length)
            return (true);
            
        return (false);
    }
}
