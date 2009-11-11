/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 27.11.2007
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
 * extended archive CRC header
 *
 */
public class EAHeader 
extends SubBlockHeader 
{
	private Log logger = LogFactory.getLog(getClass());
	
	public static final short EAHeaderSize = 10;
	
	private int unpSize;
	private byte unpVer;
	private byte method;
	private int EACRC;
	
	public EAHeader(SubBlockHeader sb, byte[] eahead)
	{
		super(sb);
		int pos = 0;
		unpSize = Raw.readIntLittleEndian(eahead, pos);
		pos+=4;
		unpVer |= eahead[pos]&0xff;
		pos++;
		method |= eahead[pos]&0xff;
		pos++;
		EACRC = Raw.readIntLittleEndian(eahead, pos);
	}

	/**
	 * @return the eACRC
	 */
	public int getEACRC() {
		return EACRC;
	}

	/**
	 * @return the method
	 */
	public byte getMethod() {
		return method;
	}

	/**
	 * @return the unpSize
	 */
	public int getUnpSize() {
		return unpSize;
	}

	/**
	 * @return the unpVer
	 */
	public byte getUnpVer() {
		return unpVer;
	}
	
	@Override
    public void print()
	{
		super.print();
		logger.info("unpSize: "+unpSize);
		logger.info("unpVersion: " + unpVer);
		logger.info("method: "+method);
		logger.info("EACRC:" + EACRC);
	}
}

