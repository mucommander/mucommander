package com.mucommander.file.impl.rar.provider;

import com.mucommander.file.FileLogger;

public class RarDebug {
	private static final boolean RAR_DEBUG_ON = true;
	
	public static void trace(String message) {
		if (RAR_DEBUG_ON)
			FileLogger.finest(message);
	}
}
