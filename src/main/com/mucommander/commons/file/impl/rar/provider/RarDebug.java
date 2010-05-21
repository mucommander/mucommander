package com.mucommander.commons.file.impl.rar.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RarDebug {
    private static final Logger LOGGER = LoggerFactory.getLogger(RarDebug.class);
	private static final boolean RAR_DEBUG_ON = true;
	
	public static void trace(String message) {
		if (RAR_DEBUG_ON)
			LOGGER.trace(message);
	}
}
