package com.mucommander.desktop.linux.gnome;

import java.io.IOException;

public class GConfTool extends OSCommand {
    private static final String COMMAND = "gconftool";

    private static final String GCONFTOOL_DOUBLE_CLICK_CONFIG_KEY = "/desktop/gnome/peripherals/mouse/double_click";

    public static int getIntValue(String key) throws IOException, InterruptedException {
        return runCommandWithIntReturn(COMMAND, "-g", key);
    }

    public static int getMultiClickInterval() throws IOException, InterruptedException {
        return getIntValue(GCONFTOOL_DOUBLE_CLICK_CONFIG_KEY);
    }
}
