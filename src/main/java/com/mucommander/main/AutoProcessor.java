/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.mucommander.main;

import java.io.File;
import java.util.*;
import org.osgi.framework.*;
import org.osgi.service.startlevel.*;

public class AutoProcessor
{
    /**
     * The property name used for the bundle directory.
     * @deprecated use {@link AutoProcessor#AUTO_DEPLOY_DIR_PROPERTY}
    **/
    public static final String AUTO_DEPLOY_DIR_PROPERY = "felix.auto.deploy.dir";
    /**
     * The property name used for the bundle directory.
    **/
    public static final String AUTO_DEPLOY_DIR_PROPERTY = "felix.auto.deploy.dir";
    /**
     * The default name used for the bundle directory.
    **/
    public static final String AUTO_DEPLOY_DIR_VALUE = "bundle";
    /**
     * The property name used to specify auto-deploy actions.
     * @deprecated use {@link AutoProcessor#AUTO_DEPLOY_ACTION_PROPERTY}
    **/
    public static final String AUTO_DEPLOY_ACTION_PROPERY = "felix.auto.deploy.action";
    /**
     * The property name used to specify auto-deploy actions.
    **/
    public static final String AUTO_DEPLOY_ACTION_PROPERTY = "felix.auto.deploy.action";
    /**
     * The property name used to specify auto-deploy start level.
     * @deprecated use {@link AutoProcessor#AUTO_DEPLOY_STARTLEVEL_PROPERTY}
    **/
    public static final String AUTO_DEPLOY_STARTLEVEL_PROPERY = "felix.auto.deploy.startlevel";
    /**
     * The property name used to specify auto-deploy start level.
    **/
    public static final String AUTO_DEPLOY_STARTLEVEL_PROPERTY = "felix.auto.deploy.startlevel";
    /**
     * The name used for the auto-deploy install action.
    **/
    public static final String AUTO_DEPLOY_INSTALL_VALUE = "install";
    /**
     * The name used for the auto-deploy start action.
    **/
    public static final String AUTO_DEPLOY_START_VALUE = "start";
    /**
     * The name used for the auto-deploy update action.
    **/
    public static final String AUTO_DEPLOY_UPDATE_VALUE = "update";
    /**
     * The name used for the auto-deploy uninstall action.
    **/
    public static final String AUTO_DEPLOY_UNINSTALL_VALUE = "uninstall";
    /**
     * The property name prefix for the launcher's auto-install property.
    **/
    public static final String AUTO_INSTALL_PROP = "felix.auto.install";
    /**
     * The property name prefix for the launcher's auto-start property.
    **/
    public static final String AUTO_START_PROP = "felix.auto.start";

    /**
     * Used to instigate auto-deploy directory process and auto-install/auto-start
     * configuration property processing during.
     * @param configMap Map of configuration properties.
     * @param context The system bundle context.
    **/
    public static void process(Map configMap, BundleContext context)
    {
        configMap = (configMap == null) ? new HashMap() : configMap;
        processAutoDeploy(configMap, context);
        processAutoProperties(configMap, context);
    }

    /**
     * <p>
     * Processes bundles in the auto-deploy directory, performing the
     * specified deploy actions.
     * </p>
     */
    private static void processAutoDeploy(Map configMap, BundleContext context)
    {
        // Determine if auto deploy actions to perform.
        String action = (String) configMap.get(AUTO_DEPLOY_ACTION_PROPERTY);
        action = (action == null) ? "" : action;
        List actionList = new ArrayList();
        StringTokenizer st = new StringTokenizer(action, ",");
        while (st.hasMoreTokens())
        {
            String s = st.nextToken().trim().toLowerCase();
            if (s.equals(AUTO_DEPLOY_INSTALL_VALUE)
                || s.equals(AUTO_DEPLOY_START_VALUE)
                || s.equals(AUTO_DEPLOY_UPDATE_VALUE)
                || s.equals(AUTO_DEPLOY_UNINSTALL_VALUE))
            {
                actionList.add(s);
            }
        }

        // Perform auto-deploy actions.
        if (actionList.size() > 0)
        {
            // Retrieve the Start Level service, since it will be needed
            // to set the start level of the installed bundles.
            StartLevel sl = (StartLevel) context.getService(
                context.getServiceReference(org.osgi.service.startlevel.StartLevel.class.getName()));

            // Get start level for auto-deploy bundles.
            int startLevel = sl.getInitialBundleStartLevel();
            if (configMap.get(AUTO_DEPLOY_STARTLEVEL_PROPERTY) != null)
            {
                try
                {
                    startLevel = Integer.parseInt(
                        configMap.get(AUTO_DEPLOY_STARTLEVEL_PROPERTY).toString());
                }
                catch (NumberFormatException ex)
                {
                    // Ignore and keep default level.
                }
            }

            // Get list of already installed bundles as a map.
            Map installedBundleMap = new HashMap();
            Bundle[] bundles = context.getBundles();
            for (int i = 0; i < bundles.length; i++)
            {
                installedBundleMap.put(bundles[i].getLocation(), bundles[i]);
            }

            // Get the auto deploy directory.
            String autoDir = (String) configMap.get(AUTO_DEPLOY_DIR_PROPERTY);
            autoDir = (autoDir == null) ? AUTO_DEPLOY_DIR_VALUE : autoDir;
            // Look in the specified bundle directory to create a list
            // of all JAR files to install.
            File[] files = new File(autoDir).listFiles();
            List jarList = new ArrayList();
            if (files != null)
            {
                Arrays.sort(files);
                for (int i = 0; i < files.length; i++)
                {
                    if (files[i].getName().endsWith(".jar"))
                    {
                        jarList.add(files[i]);
                    }
                }
            }

            // Install bundle JAR files and remember the bundle objects.
            final List startBundleList = new ArrayList();
            for (int i = 0; i < jarList.size(); i++)
            {
                // Look up the bundle by location, removing it from
                // the map of installed bundles so the remaining bundles
                // indicate which bundles may need to be uninstalled.
                Bundle b = (Bundle) installedBundleMap.remove(
                    ((File) jarList.get(i)).toURI().toString());

                try
                {
                    // If the bundle is not already installed, then install it
                    // if the 'install' action is present.
                    if ((b == null) && actionList.contains(AUTO_DEPLOY_INSTALL_VALUE))
                    {
                        b = context.installBundle(
                            ((File) jarList.get(i)).toURI().toString());
                    }
                    // If the bundle is already installed, then update it
                    // if the 'update' action is present.
                    else if ((b != null) && actionList.contains(AUTO_DEPLOY_UPDATE_VALUE))
                    {
                        b.update();
                    }

                    // If we have found and/or successfully installed a bundle,
                    // then add it to the list of bundles to potentially start
                    // and also set its start level accordingly.
                    if ((b != null) && !isFragment(b))
                    {
                        startBundleList.add(b);
                        sl.setBundleStartLevel(b, startLevel);
                    }
                }
                catch (BundleException ex)
                {
                    System.err.println("Auto-deploy install: "
                        + ex + ((ex.getCause() != null) ? " - " + ex.getCause() : ""));
                }
            }

            // Uninstall all bundles not in the auto-deploy directory if
            // the 'uninstall' action is present.
            if (actionList.contains(AUTO_DEPLOY_UNINSTALL_VALUE))
            {
                for (Iterator it = installedBundleMap.entrySet().iterator(); it.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) it.next();
                    Bundle b = (Bundle) entry.getValue();
                    if (b.getBundleId() != 0)
                    {
                        try
                        {
                            b.uninstall();
                        }
                        catch (BundleException ex)
                        {
                        System.err.println("Auto-deploy uninstall: "
                            + ex + ((ex.getCause() != null) ? " - " + ex.getCause() : ""));
                        }
                    }
                }
            }

            // Start all installed and/or updated bundles if the 'start'
            // action is present.
            if (actionList.contains(AUTO_DEPLOY_START_VALUE))
            {
                for (int i = 0; i < startBundleList.size(); i++)
                {
                    try
                    {
                        ((Bundle) startBundleList.get(i)).start();
                    }
                    catch (BundleException ex)
                    {
                        System.err.println("Auto-deploy start: "
                            + ex + ((ex.getCause() != null) ? " - " + ex.getCause() : ""));
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Processes the auto-install and auto-start properties from the
     * specified configuration properties.
     * </p>
     */
    private static void processAutoProperties(Map configMap, BundleContext context)
    {
        // Retrieve the Start Level service, since it will be needed
        // to set the start level of the installed bundles.
        StartLevel sl = (StartLevel) context.getService(
            context.getServiceReference(org.osgi.service.startlevel.StartLevel.class.getName()));

        // Retrieve all auto-install and auto-start properties and install
        // their associated bundles. The auto-install property specifies a
        // space-delimited list of bundle URLs to be automatically installed
        // into each new profile, while the auto-start property specifies
        // bundles to be installed and started. The start level to which the
        // bundles are assigned is specified by appending a ".n" to the
        // property name, where "n" is the desired start level for the list
        // of bundles. If no start level is specified, the default start
        // level is assumed.
        for (Iterator i = configMap.keySet().iterator(); i.hasNext(); )
        {
            String key = ((String) i.next()).toLowerCase();

            // Ignore all keys that are not an auto property.
            if (!key.startsWith(AUTO_INSTALL_PROP) && !key.startsWith(AUTO_START_PROP))
            {
                continue;
            }

            // If the auto property does not have a start level,
            // then assume it is the default bundle start level, otherwise
            // parse the specified start level.
            int startLevel = sl.getInitialBundleStartLevel();
            if (!key.equals(AUTO_INSTALL_PROP) && !key.equals(AUTO_START_PROP))
            {
                try
                {
                    startLevel = Integer.parseInt(key.substring(key.lastIndexOf('.') + 1));
                }
                catch (NumberFormatException ex)
                {
                    System.err.println("Invalid property: " + key);
                }
            }

            // Parse and install the bundles associated with the key.
            StringTokenizer st = new StringTokenizer((String) configMap.get(key), "\" ", true);
            for (String location = nextLocation(st); location != null; location = nextLocation(st))
            {
                try
                {
                    Bundle b = context.installBundle(location, null);
                    sl.setBundleStartLevel(b, startLevel);
                }
                catch (Exception ex)
                {
                    System.err.println("Auto-properties install: " + location + " ("
                        + ex + ((ex.getCause() != null) ? " - " + ex.getCause() : "") + ")");
if (ex.getCause() != null)
    ex.printStackTrace();
                }
            }
        }

        // Now loop through the auto-start bundles and start them.
        for (Iterator i = configMap.keySet().iterator(); i.hasNext(); )
        {
            String key = ((String) i.next()).toLowerCase();
            if (key.startsWith(AUTO_START_PROP))
            {
                StringTokenizer st = new StringTokenizer((String) configMap.get(key), "\" ", true);
                for (String location = nextLocation(st); location != null; location = nextLocation(st))
                {
                    // Installing twice just returns the same bundle.
                    try
                    {
                        Bundle b = context.installBundle(location, null);
                        if (b != null)
                        {
                            b.start();
                        }
                    }
                    catch (Exception ex)
                    {
                        System.err.println("Auto-properties start: " + location + " ("
                            + ex + ((ex.getCause() != null) ? " - " + ex.getCause() : "") + ")");
                    }
                }
            }
        }
    }

    private static String nextLocation(StringTokenizer st)
    {
        String retVal = null;

        if (st.countTokens() > 0)
        {
            String tokenList = "\" ";
            StringBuffer tokBuf = new StringBuffer(10);
            String tok = null;
            boolean inQuote = false;
            boolean tokStarted = false;
            boolean exit = false;
            while ((st.hasMoreTokens()) && (!exit))
            {
                tok = st.nextToken(tokenList);
                if (tok.equals("\""))
                {
                    inQuote = ! inQuote;
                    if (inQuote)
                    {
                        tokenList = "\"";
                    }
                    else
                    {
                        tokenList = "\" ";
                    }

                }
                else if (tok.equals(" "))
                {
                    tokBuf.append(tok);
                    /*if (tokStarted)
                    {
                        retVal = tokBuf.toString();
                        tokStarted=false;
                        tokBuf = new StringBuffer(10);
                        exit = true;
                    }*/
                }
                else
                {
                    tokStarted = true;
                    tokBuf.append(tok.trim());
                }
            }

            // Handle case where end of token stream and
            // still got data
            if ((!exit) && (tokStarted))
            {
                retVal = tokBuf.toString();
            }
        }

        return retVal;
    }

    private static boolean isFragment(Bundle bundle)
    {
        return bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
    }
}
