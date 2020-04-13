/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.ri.util;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bare Bones Browser Launch for Java<br> Utility class to open a web page from
 * a Swing application in the user's default browser.<br> Supports: Mac OS X,
 * GNU/Linux, Unix, Windows XP<br> Example Usage:<code><br> &nbsp; &nbsp; String
 * url = "http://www.google.com/";<br> &nbsp; &nbsp; BareBonesBrowserLaunch.openURL(url);<br></code>
 * Latest Version: <a href="http://www.centerkey.com/java/browser/">http://www.centerkey.com/java/browser</a><br>
 * Author: Dem Pilafian<br> Public Domain Software -- Free to Use as You Like
 *
 * @version 1.5, December 10, 2005
 */
public class BareBonesBrowserLaunch {

    private static final Logger logger =
            Logger.getLogger(BareBonesBrowserLaunch.class.toString());
    private static final String errMsg =
            "Error attempting to launch web browser";

    public static final String FILE_PREFIX = "file://";

    private static String os;

    static {
        os = System.getProperty("os.name").toLowerCase();
    }

    /**
     * Opens the specified web page in a web browser
     *
     * @param url An absolute URL of a web page (ex: "http://www.google.com/")
     */
    public static void openURL(String url) {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Opening URL: " + url);
            }

            if (isMac()) {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[]{
                                String.class});
                openURL.invoke(null, url);
            } else if (isWindows())
                Runtime.getRuntime()
                        .exec("rundll32 url.dll,FileProtocolHandler " + url);
            else if (isUnix()) {
                String[] browsers = {
                        "firefox", "opera", "konqueror", "epiphany", "mozilla",
                        "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null;
                     count++)
                    if (Runtime.getRuntime().exec(
                            new String[]{"which", browsers[count]}).waitFor() ==
                            0)
                        browser = browsers[count];
                if (browser == null)
                    throw new Exception("Could not find web browser");
                else
                    Runtime.getRuntime().exec(new String[]{browser, url});
            } else {
                JOptionPane.showMessageDialog(null, errMsg);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, errMsg + ":\n" +
                    e.getLocalizedMessage());
        }
    }


    /**
     * Opens the specified file path using the OS's preferred application binding
     *
     * @param filePath to open on host OS.
     */
    public static void openFile(String filePath) {
        openURL(FILE_PREFIX + filePath);
    }

    public static boolean isWindows() {
        //windows
        return (os.contains("win"));

    }

    public static boolean isMac() {
        //Mac
        return (os.contains("mac"));
    }

    public static boolean isUnix() {
        //linux or unix
        return (os.contains("nix") || os.contains("nux"));
    }


}

