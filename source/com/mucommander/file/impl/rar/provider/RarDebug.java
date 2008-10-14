package com.mucommander.file.impl.rar.provider;

import com.mucommander.Debug;

public class RarDebug extends Debug {
	private static final boolean RAR_DEBUG_ON = true;
	
	public static void trace(String message) {
		if (RAR_DEBUG_ON && ON)
			Debug.trace(message);
	}
}
