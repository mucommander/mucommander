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
public class VMStandardFilters {

	public static final int VMSF_NONE_CODE    = 0;
	public static final int VMSF_E8_CODE      = 1;
	public static final int VMSF_E8E9_CODE    = 2;
	public static final int VMSF_ITANIUM_CODE = 3;
	public static final int VMSF_RGB_CODE     = 4;
	public static final int VMSF_AUDIO_CODE   = 5;
	public static final int VMSF_DELTA_CODE   = 6;
	public static final int VMSF_UPCASE_CODE  = 7;
	
	public static final VMStandardFilters VMSF_NONE    = new VMStandardFilters(VMSF_NONE_CODE);
	public static final VMStandardFilters VMSF_E8      = new VMStandardFilters(VMSF_E8_CODE);
	public static final VMStandardFilters VMSF_E8E9    = new VMStandardFilters(VMSF_E8E9_CODE);
	public static final VMStandardFilters VMSF_ITANIUM = new VMStandardFilters(VMSF_ITANIUM_CODE); 
	public static final VMStandardFilters VMSF_RGB     = new VMStandardFilters(VMSF_RGB_CODE); 
	public static final VMStandardFilters VMSF_AUDIO   = new VMStandardFilters(VMSF_AUDIO_CODE); 
	public static final VMStandardFilters VMSF_DELTA   = new VMStandardFilters(VMSF_DELTA_CODE);
	public static final VMStandardFilters VMSF_UPCASE  = new VMStandardFilters(VMSF_UPCASE_CODE);
	
	private int m_filter;
	
	private VMStandardFilters(int filter){
		m_filter=filter;
	}

	public int getFilterType() {
		return m_filter;
	}
	
	public boolean equals(int filter){
		return m_filter == filter;
	}
	
	
	public static VMStandardFilters findFilter(int filter){
		if (VMSF_NONE_CODE == filter) {
			return VMSF_NONE;
		}		 
		
		if (VMSF_E8_CODE == filter) {
			return VMSF_E8;
		}		 
		
		if (VMSF_E8E9_CODE == filter) {
			return VMSF_E8E9;
		}		 
		if (VMSF_ITANIUM_CODE == filter) {
			return VMSF_ITANIUM;
		}		 
		
		if (VMSF_RGB_CODE == filter) {
			return VMSF_RGB;
		}		  
		
		if (VMSF_AUDIO_CODE == filter) {
			return VMSF_AUDIO;
		}		 
		if (VMSF_DELTA_CODE == filter) {
			return VMSF_DELTA;
		}		
		return null;
	} 
}
