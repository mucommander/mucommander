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
public class HostSystem {
	private static final byte msdos = 0;
	private static final byte os2 = 1;
	private static final byte win32 = 2;
	private static final byte unix = 3;
	private static final byte macos = 4;
	private static final byte beos = 5;	
	
	public static final HostSystem MSDOS = new HostSystem(msdos, "MS-DOS");
	public static final HostSystem OS2   = new HostSystem(os2, "OS/2");
	public static final HostSystem WIN32 = new HostSystem(win32, "Windows");
	public static final HostSystem UNIX  = new HostSystem(unix, "Unix");
	public static final HostSystem MACOS = new HostSystem(macos, "Mac");
	public static final HostSystem BEOS  = new HostSystem(beos, "Beos");
	
	private static final HostSystem[] hostSystems = {MSDOS, OS2, WIN32, UNIX, UNIX, BEOS};
	
	private byte host;
	private String name;
	
	public String toString() { return name; }
	
	private HostSystem(byte host, String name) {
		this.host = host;
		this.name = name;
	}
	
	public static final HostSystem findHostSystem(byte os) {
		if (os >= 0 && os <= hostSystems.length)
			return hostSystems[os];
		return null;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof HostSystem)
			return host == ((HostSystem) obj).getHostSystemType();
		return false;
	}
	
	public int getHostSystemType() {
		return host;
	} 
}
