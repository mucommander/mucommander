/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: Edmund Wagner
 * Creation date: 31.05.2007
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
package com.mucommander.file.impl.rar.provider.de.innosystec.unrar.unpack.vm;

/**
 * DOCUMENT ME
 *
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class VMFlags {

	private static final int VM_FC_CODE = 1;
	private static final int VM_FZ_CODE = 2;
	private static final int VM_FS_CODE= 0x80000000;
	
	public static final VMFlags VM_FC = new VMFlags(VM_FC_CODE);
	public static final VMFlags VM_FZ = new VMFlags(VM_FZ_CODE);
	public static final VMFlags VM_FS = new VMFlags(VM_FS_CODE);

	
	private int flag;
	
	private VMFlags(int flag){
		this.flag = flag;
	}
	
	/**
	 * Returns the VMFlags Type of the given int or null
	 * @param flag as int
	 * @return VMFlag of the int value
	 */
	/*public static VMFlags findFlag(int flag){
		if(VM_FC.equals(flag)){
			return VM_FC;
		}
		if(VM_FS.equals(flag)){
			return VM_FS;
		}
		if(VM_FZ.equals(flag)){
			return VM_FZ;
		}
		return null;
	} */
		
	public boolean equals(Object obj) {
		if (obj instanceof VMFlags)
			return flag == ((VMFlags) obj).getFlag();
		return false;
	}
	
	/**
	 * Returns true if the flag provided as int is equal to the enum
	 * @param flag
	 * @return returns true if the flag is equal to the enum
	 */
	
	public boolean equals(int flag){
		return this.flag == flag;
	}
	
	/**
	 * @return the flag as int
	 */
	
	public int getFlag() {
		return flag;
	}
}
