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
public class VMOpType {

	private static final int VM_OPREG_CODE    = 0;
	private static final int VM_OPINT_CODE    = 1;
	private static final int VM_OPREGMEM_CODE = 2;
	private static final int VM_OPNONE_CODE   = 3;
	
	public static final VMOpType VM_OPREG    = new VMOpType(VM_OPREG_CODE);
	public static final VMOpType VM_OPINT    = new VMOpType(VM_OPINT_CODE);
	public static final VMOpType VM_OPREGMEM = new VMOpType(VM_OPREGMEM_CODE);
	public static final VMOpType VM_OPNONE   = new VMOpType(VM_OPNONE_CODE);
	
	private int opType;
	
	private VMOpType(int opType){
		this.opType = opType;
	}

	public boolean equals(Object obj) {
		if (obj instanceof VMOpType)
			return opType == ((VMOpType) obj).getOpType();
		return false;
	}
		
	public int getOpType() {
		return opType;
	}

/*
	public boolean equals(int opType){
		return this.opType == opType;
	}
	
	public static VMOpType findOpType(int opType){
		
		if (VM_OPREG.equals(opType)) {
			return VM_OPREG;
		}		 
		
		
		if (VM_OPINT.equals(opType)) {
			return VM_OPINT;
		}		 
		
		if (VM_OPREGMEM.equals(opType)) {
			return VM_OPREGMEM;
		}		
		
		if (VM_OPNONE.equals(opType)) {
			return VM_OPNONE;
		}		 
		return null;
	} */
}
