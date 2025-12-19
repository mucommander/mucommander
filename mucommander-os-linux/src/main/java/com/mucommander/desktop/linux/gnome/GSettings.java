package com.mucommander.desktop.linux.gnome;

import java.io.IOException;

public class GSettings extends OSCommand {
    private static final String COMMAND = "gsettings";

    private static final String GSETTINGS_MOUSE_PATH = "org.gnome.desktop.peripherals.mouse";
    private static final String GSETTINGS_DOUBLE_CLICK_CONFIG_KEY = "double-click";

    public static int getIntValue(String path, String key) throws IOException, InterruptedException {
        return runCommandWithIntReturn(COMMAND, "get", path, key);
    }

    public static int getMultiClickInterval() throws IOException, InterruptedException {
        return getIntValue(GSETTINGS_MOUSE_PATH, GSETTINGS_DOUBLE_CLICK_CONFIG_KEY);
    }
}
