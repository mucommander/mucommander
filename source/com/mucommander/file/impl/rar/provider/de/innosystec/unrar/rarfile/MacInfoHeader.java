/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 26.11.2007
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

import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.io.Raw;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mac File attribute header
 *
 */
public class MacInfoHeader 
extends SubBlockHeader 
{
	private Log logger = LogFactory.getLog(getClass());
	
	public static final short MacInfoHeaderSize = 8;
	
	private int fileType;
	private int fileCreator;
	
	public MacInfoHeader(SubBlockHeader sb, byte[] macHeader)
	{
		super(sb);
		int pos = 0;
		fileType = Raw.readIntLittleEndian(macHeader, pos);
		pos+=4;
		fileCreator = Raw.readIntLittleEndian(macHeader, pos);
	}

	/**
	 * @return the fileCreator
	 */
	public int getFileCreator() {
		return fileCreator;
	}

	/**
	 * @param fileCreator the fileCreator to set
	 */
	public void setFileCreator(int fileCreator) {
		this.fileCreator = fileCreator;
	}

	/**
	 * @return the fileType
	 */
	public int getFileType() {
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(int fileType) {
		this.fileType = fileType;
	}
	
	@Override
    public void print(){
		super.print();
		logger.info("filetype: "+fileType);
		logger.info("creator :"+fileCreator);
	}
	
}
