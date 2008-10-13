/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 01.06.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
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
package com.mucommander.file.impl.rar.provider.de.innosystec.unrar.unpack.ppm;


/**
 * DOCUMENT ME
 * 
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class BlockTypes
{
	private static final short BLOCK_LZ_VALUE  = 0;
	private static final short BLOCK_PPM_VALUE = 1;
	private short m_value;
	
	public static BlockTypes BLOCK_LZ  = new BlockTypes(BLOCK_LZ_VALUE);
	public static BlockTypes BLOCK_PPM = new BlockTypes(BLOCK_PPM_VALUE);
	
	private BlockTypes(short value) {
		m_value = value;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof BlockTypes)
			return m_value == ((BlockTypes)obj).m_value;
		return false;
	}
}
