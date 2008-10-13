/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 04.06.2007
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
package com.mucommander.file.impl.rar.provider.de.innosystec.unrar.unsigned;


/**
 * DOCUMENT ME
 *
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class UnsignedByte {
	
	private short value;
	
	public UnsignedByte(byte value) {
		this.value = (short) (value & 0xff);
	}
	
	public short getValue() {
		return value;
	}
	
	public void add(short value) {
		this.value += value;
	}
	
	public char toChar() {
		return (char) value;
	}
	
	public short add(byte value) {
		return (short) (this.value + ((short) value & 0xff));
	}
}
