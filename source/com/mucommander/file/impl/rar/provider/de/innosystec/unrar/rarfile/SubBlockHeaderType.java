/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 20.11.2007
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

public class SubBlockHeaderType {

	public static final short EA_HEAD_CODE     = 0x100;
	public static final short UO_HEAD_CODE     = 0x101;
	public static final short MAC_HEAD_CODE    = 0x102;
	public static final short BEEA_HEAD_CODE   = 0x103;
	public static final short NTACL_HEAD_CODE  = 0x104;
	public static final short STREAM_HEAD_CODE = 0x105;
	
	public static final SubBlockHeaderType EA_HEAD 	   = new SubBlockHeaderType(EA_HEAD_CODE);
	public static final SubBlockHeaderType UO_HEAD 	   = new SubBlockHeaderType(UO_HEAD_CODE);
	public static final SubBlockHeaderType MAC_HEAD    = new SubBlockHeaderType(MAC_HEAD_CODE);
	public static final SubBlockHeaderType BEEA_HEAD   = new SubBlockHeaderType(BEEA_HEAD_CODE);
	public static final SubBlockHeaderType NTACL_HEAD  = new SubBlockHeaderType(NTACL_HEAD_CODE);
	public static final SubBlockHeaderType STREAM_HEAD = new SubBlockHeaderType(STREAM_HEAD_CODE);
	
	private static final SubBlockHeaderType[] types = {EA_HEAD, UO_HEAD, MAC_HEAD, BEEA_HEAD, NTACL_HEAD, STREAM_HEAD};
	
	private short type;
	
	private SubBlockHeaderType(short type) {
		this.type = type;
	}
	
	/**
	 * @return the short representation of this enum
	 */
	public short getSubblocktype() {
		return type;
	}
	
	/**
	 * find the header type for the given short value
	 * @param SubType the short value
	 * @return the correspo nding enum or null
	 */
	public static SubBlockHeaderType findSubblockHeaderType(short type) {
		return types[type - 0x100];
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof SubBlockHeaderType)
			return type == ((SubBlockHeaderType) obj).getSubblocktype();
		return false;
	}}
