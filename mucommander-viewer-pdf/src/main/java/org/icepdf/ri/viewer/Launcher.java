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
package org.icepdf.ri.viewer;

import org.icepdf.core.util.Defs;
import org.icepdf.ri.common.ViewModel;
import org.icepdf.ri.util.FontPropertiesManager;
import org.icepdf.ri.util.PropertiesManager;
import org.icepdf.ri.util.URLAccess;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Launches the Viewer Application.  The following parameters can be used
 * to optionally load a PDF document at startup.</p>
 * <table border="1">
 * <tr>
 * <td><b>Option</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td>-loadfile <i>filename</i></td>
 * <td>Starts the ICEpdf Viewer and displays the specified local PDF file.
 * Use the following syntax: <br />
 * -loadfile c:/examplepath/file.pdf</td>
 * </tr>
 * <tr>
 * <td>-loadfile <i>filename</i></td>
 * <td>Starts the ICEpdf Viewer and displays the PDF file at the specified
 * URL. Use the following syntax: <br />
 * -loadurl http://www.examplesite.com/file.pdf</td>
 * </tr>
 * </table>
 */
public class Launcher {

    private static final Logger logger =
            Logger.getLogger(Launcher.class.toString());

    // manages open windows
    public static WindowManager windowManager;
    // stores properties used by ICEpdf
    private static PropertiesManager propertiesManager;

    public static void main(String[] argv) {

        boolean brokenUsage = false;

        String contentURL = "";
        String contentFile = "";
        String contentProperties = null;
        // parse command line arguments
        for (int i = 0; i < argv.length; i++) {
            if (i == argv.length - 1) { //each argument requires another
                brokenUsage = true;
                break;
            }
            String arg = argv[i];
            if (arg.equals("-loadfile")) {
                contentFile = argv[++i].trim();
            } else if (arg.equals("-loadurl")) {
                contentURL = argv[++i].trim();
            } else if (arg.equals("-loadproperties")) {
                contentProperties = argv[++i].trim();
            } else {
                brokenUsage = true;
                break;
            }
        }

        // load message bundle
        ResourceBundle messageBundle = ResourceBundle.getBundle(
                PropertiesManager.DEFAULT_MESSAGE_BUNDLE);

        // Quit if there where any problems parsing the command line arguments
        if (brokenUsage) {
            System.out.println(messageBundle.getString("viewer.commandLin.error"));
            System.exit(1);
        }
        // start the viewer
        run(contentFile, contentURL, contentProperties, messageBundle);
    }

    /**
     * Starts the viewe application.
     *
     * @param contentFile       URI of a file which will be loaded at runtime, can be
     *                          null.
     * @param contentURL        URL of a file which will be loaded at runtime, can be
     *                          null.
     * @param contentProperties URI of a properties file which will be used in
     *                          place of the default path
     * @param messageBundle     messageBundle to pull strings from
     */
    private static void run(String contentFile,
                            String contentURL,
                            String contentProperties,
                            ResourceBundle messageBundle) {

        // initiate the properties manager.
        Properties sysProps = System.getProperties();
        propertiesManager = new PropertiesManager(sysProps, contentProperties, messageBundle);

        // initiate font Cache manager, reads system font data and stores summary
        // information in a properties file.  If new font are added to the OS
        // then the properties file can be deleted to initiate a re-read of the
        // font data.
        new FontPropertiesManager(propertiesManager, sysProps, messageBundle);

        // input new System properties
        System.setProperties(sysProps);

        // set look & feel
        setupLookAndFeel(messageBundle);

        ViewModel.setDefaultFilePath(propertiesManager.getDefaultFilePath());
        ViewModel.setDefaultURL(propertiesManager.getDefaultURL());


        // application instance
        windowManager = WindowManager.createInstance(propertiesManager, messageBundle);
        if (contentFile != null && contentFile.length() > 0) {
            windowManager.newWindow(contentFile);
            ViewModel.setDefaultFilePath(contentFile);
        }

        // load a url if specified
        if (contentURL != null && contentURL.length() > 0) {
            URLAccess urlAccess = URLAccess.doURLAccess(contentURL);
            urlAccess.closeConnection();
            if (urlAccess.errorMessage != null) {

                // setup a patterned message
                Object[] messageArguments = {urlAccess.errorMessage,
                        urlAccess.urlLocation
                };
                MessageFormat formatter = new MessageFormat(
                        messageBundle.getString("viewer.launcher.URLError.dialog.message"));

                JOptionPane.showMessageDialog(
                        null,
                        formatter.format(messageArguments),
                        messageBundle.getString("viewer.launcher.URLError.dialog.title"),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                windowManager.newWindow(urlAccess.url);
            }
            ViewModel.setDefaultURL(urlAccess.urlLocation);
            urlAccess.dispose();
        }

        // Start an empy viewer if there was no command line parameters
        if (((contentFile == null || contentFile.length() == 0) &&
                (contentURL == null || contentURL.length() == 0))
                || (windowManager.getNumberOfWindows() == 0)
                ) {
            windowManager.newWindow("");
        }
    }

    /**
     * If a L&F has been specifically set then try and use it. If not
     * then resort to the 'native' system L&F.
     *
     * @param messageBundle
     */
    private static void setupLookAndFeel(ResourceBundle messageBundle) {

        // Do Mac related-setup (if running on a Mac)
        if (Defs.sysProperty("mrj.version") != null) {
            // Running on a mac
            // take the menu bar off the jframe
            Defs.setSystemProperty("apple.laf.useScreenMenuBar", "true");
            // set the name of the application menu item (must precede the L&F setup)
            String appName = messageBundle.getString("viewer.window.title.default");
            Defs.setSystemProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        String className =
                propertiesManager.getLookAndFeel("application.lookandfeel",
                        null);

        if (className != null) {
            try {
                UIManager.setLookAndFeel(className);
                return;
            } catch (Exception e) {

                // setup a patterned message
                Object[] messageArguments = {
                        propertiesManager.getString("application.lookandfeel")
                };
                MessageFormat formatter = new MessageFormat(
                        messageBundle.getString("viewer.launcher.URLError.dialog.message"));

                // Error - unsupported L&F (probably windows)
                JOptionPane.showMessageDialog(
                        null,
                        formatter.format(messageArguments),
                        messageBundle.getString("viewer.launcher.lookAndFeel.error.message"),
                        JOptionPane.ERROR_MESSAGE);

            }
        }

        try {
            String defaultLF = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(defaultLF);
        } catch (Exception e) {
            logger.log(Level.FINE, "Error setting Swing Look and Feel.", e);
        }

    }
}
