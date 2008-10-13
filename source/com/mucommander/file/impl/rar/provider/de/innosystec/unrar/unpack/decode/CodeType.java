/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 01.06.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 * 
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;" 
 */
package com.mucommander.file.impl.rar.provider.de.innosystec.unrar.unpack.decode;

/**
 * DOCUMENT ME
 * 
 * the unrar licence applies to all junrar source and binary distributions 
 * you are not allowed to use this source to re-create the RAR compression algorithm
 * 
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class CodeType {
	public static final CodeType CODE_HUFFMAN        = new CodeType();
	public static final CodeType CODE_LZ             = new CodeType();
	public static final CodeType CODE_LZ2            = new CodeType();
	public static final CodeType CODE_REPEATLZ       = new CodeType();
	public static final CodeType CODE_CACHELZ        = new CodeType();
	public static final CodeType CODE_STARTFILE      = new CodeType();
	public static final CodeType CODE_ENDFILE        = new CodeType();
	public static final CodeType CODE_VM             = new CodeType();
	public static final CodeType CODE_VMDATA         = new CodeType();
	
	private CodeType() {}
}
