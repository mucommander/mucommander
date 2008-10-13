/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 22.05.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 * 
 * 
 * the unrar licence applies to all junrar source and binary distributions 
 * you are not allowed to use this source to re-create the RAR compression algorithm
 *
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;" 
 */
package com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile;

/**
 * DOCUMENT ME
 *
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class UnrarHeadertype {
	
	public static final byte MarkHeaderCode    = 0x72;
	public static final byte MainHeaderCode    = 0x73;   
	public static final byte FileHeaderCode    = 0x74;
	public static final byte CommHeaderCode    = 0x75;
	public static final byte AvHeaderCode      = 0x76;
	public static final byte SubHeaderCode     = 0x77;
    public static final byte ProtectHeaderCode = 0x78;
    public static final byte SignHeaderCode    = 0x79;
    public static final byte NewSubHeaderCode  = 0x7a;
    public static final byte EndArcHeaderCode  = 0x7b;
    
    public static final UnrarHeadertype MainHeader    = new UnrarHeadertype(MainHeaderCode);
    public static final UnrarHeadertype MarkHeader    = new UnrarHeadertype(MarkHeaderCode);
    public static final UnrarHeadertype FileHeader    = new UnrarHeadertype(FileHeaderCode);
    public static final UnrarHeadertype CommHeader    = new UnrarHeadertype(CommHeaderCode);
    public static final UnrarHeadertype AvHeader      = new UnrarHeadertype(AvHeaderCode);
    public static final UnrarHeadertype SubHeader     = new UnrarHeadertype(SubHeaderCode);
    public static final UnrarHeadertype ProtectHeader = new UnrarHeadertype(ProtectHeaderCode);
    public static final UnrarHeadertype SignHeader    = new UnrarHeadertype(SignHeaderCode);
    public static final UnrarHeadertype NewSubHeader  = new UnrarHeadertype(NewSubHeaderCode);
    public static final UnrarHeadertype EndArcHeader  = new UnrarHeadertype(EndArcHeaderCode);
    
    private static final UnrarHeadertype[] headerTypes = {MainHeader, MarkHeader, FileHeader, 
    	CommHeader,	AvHeader, SubHeader, ProtectHeader, SignHeader, NewSubHeader, EndArcHeader};
    
    private byte code;
    
    private UnrarHeadertype(byte code) {
    	this.code = code;
    }
    
    /*public static UnrarHeadertype getHeaderType(byte code) {
    	if (code < 0x72 || code > 0x7b)
    		return new UnrarHeadertype(code);
    	return headerTypes[code - 0x72];
    }*/
    
    public boolean equals(Object obj) {
    	if (obj instanceof UnrarHeadertype)
    		return code == ((UnrarHeadertype) obj).getCode();
    	return false;
    }
    
    public boolean equals(byte code) {
    	return this.code == code;
    }
    
    public byte getCode() {
    	return code;
    }
}
