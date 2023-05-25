/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.protocol.adb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.process.ProcessListener;
import com.mucommander.process.ProcessRunner;

import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

/**
 * @author Oleg Trifonov, Arik Hadas
 * Created on 25/12/15.
 */
public class AdbUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdbUtils.class);

    private static Map<String, String> lastDeviceNames;

    /**
     * Get list of connected ADB devices
     * @return null if adb doesn't found
     */
    public static List<String> getDevices()  {
        try {
            JadbConnection connection = new JadbConnection();
            List<JadbDevice> devices = connection.getDevices();
            List<String> names = new ArrayList<>();
            for (JadbDevice device : devices) {
                names.add(device.getSerial());
            }
            return names;
        } catch (JadbException | IOException e) {
            LOGGER.error("failed to list android devices");
            LOGGER.debug("failed to list android devices", e);
        }
        return null;
    }

    /**
     *
     * @return true if adb found
     */
    public static boolean checkAdb() {
        AbstractFile adbFile = getAdbPath();
        if (OsFamily.getCurrent().isUnixBased() && adbFile != null) {
            try {
                int result = ProcessRunner.execute(new String[]{"./adb", "devices", "-l", adbFile.toString()}).waitFor();
                return result == 0;
            } catch (IOException | InterruptedException ignore) {}
        }
        try {
            int result = ProcessRunner.execute(new String[]{"adb", "devices", "-l", adbFile.toString()}).waitFor();
            return result == 0;
        } catch (IOException | InterruptedException ignore) {}
        return false;
    }

    /**
     * Tried to found adb utility location
     *
     * @return path to adb utility or null
     */
    private static AbstractFile getAdbPath() {
        String path;
        try {
            path = System.getenv("ANDROID_HOME");
        } catch (SecurityException ignore) {
            path = null;
        }
        if (path != null) {
            String adb = path + (OsFamily.WINDOWS .isCurrent() ? "\\platform-tools\\adb.exe" : "/platform-tools/adb");
            AbstractFile result = FileFactory.getFile(adb);
            if (result != null && result.exists() && !result.isDirectory()) {
                return result.getParent();
            }
        }
        try {
            path = System.getenv("ADB_HOME");
        } catch (SecurityException ignore) {
            path = null;
        }
        if (path != null) {
            String adb = path + (OsFamily.getCurrent() == OsFamily.WINDOWS ? "\\adb.exe" : "/adb");
            AbstractFile result = FileFactory.getFile(adb);
            if (result != null && result.exists() && !result.isDirectory()) {
                return result.getParent();
            }
        }
        if (OsFamily.MAC_OS.isCurrent()) {
            String defaultPath = System.getProperty("user.home") + "/Library/Android/sdk/platform-tools/adb";
            AbstractFile result = FileFactory.getFile(defaultPath);
            if (result != null && result.exists() && !result.isDirectory()) {
                return result.getParent();
            }
        }
        return null;
    }

    /**
     *
     * @param serial the device serial number
     *
     * @return device name (or null if unknown)
     */
    public static String getDeviceName(String serial) {
        if (lastDeviceNames == null || !lastDeviceNames.containsKey(serial)) {
            lastDeviceNames = getDeviceNames();
        }
        return lastDeviceNames.get(serial);
    }


    /**
     *
     * @return serial to name
     */
    public static Map<String, String> getDeviceNames() {
        final Map<String, String> result = new HashMap<>();
        ProcessListener listener = new ProcessListener() {
            @Override
            public void processOutput(String output) {
                parseDevicesList(result, output);
            }
        };
        var adbFile = getAdbPath();
        if (OsFamily.getCurrent().isUnixBased() && adbFile != null) {
            String adbPath = adbFile.getAbsolutePath();
            try {
                ProcessRunner.execute(new String[]{"./adb", "devices", "-l", adbPath}, listener);
            } catch (IOException e) {
                try {
                    ProcessRunner.execute(new String[]{"adb", "devices", "-l", adbPath}, listener);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
        return result;
    }

    private static void parseDevicesList(Map<String, String> result, String output) {
        String[] lines = output.split("\\r?\\n");
        for (String s : lines) {
            String[] columns = s.split("\\s+");
            for (String val : columns) {
                if (val.startsWith("model:")) {
                    String serial = columns[0];
                    String name = val.substring(6); // "model:"
                    name = name.replace('_', ' ');
                    result.put(serial, name);
                }
            }
        }
    }

}