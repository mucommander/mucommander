/**
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.osgi.FileEditorServiceTracker;
import com.mucommander.osgi.FileViewerServiceTracker;
import com.mucommander.text.TranslationTracker;
import com.mucommander.ui.main.osgi.ProtocolPanelProviderTracker;

/**
 * This is the OSGi bundle-activator of the core component of muCommander.
 * Besides reacting to the {@link #start(BundleContext)} and {@link #stop(BundleContext)} operations,
 * this activator also handles executing shutdown tasks when:
 * 1. Shutdown action is initiated by the application
 * 2. When the Java virtual machine goes down (e.g., via CNTL+C)
 * @author Arik Hadas
 */
public class Activator implements BundleActivator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ProtocolPanelProviderTracker protocolPanelTracker;
    private TranslationTracker translationTracker;
    private FileViewerServiceTracker viewersTracker;
    private FileEditorServiceTracker editorsTracker;

    /** Registered shutdown-hook */
    private ShutdownHook shutdownHook;

    private BundleContext context;

    @Override
    public void start(BundleContext context) throws Exception {
        LOGGER.debug("starting");
        this.context = context;
        protocolPanelTracker = new ProtocolPanelProviderTracker(context);
        protocolPanelTracker.open();
        translationTracker = new TranslationTracker(context);
        translationTracker.open();
        viewersTracker = new FileViewerServiceTracker(context);
        viewersTracker.open();
        editorsTracker = new FileEditorServiceTracker(context);
        editorsTracker.open();

        // Traps VM shutdown
        Runtime.getRuntime().addShutdownHook(shutdownHook = new ShutdownHook());
        Application.run(this);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOGGER.debug("stopping");
        protocolPanelTracker.close();
        translationTracker.close();
        viewersTracker.close();
        editorsTracker.close();
        // if the activator performs the shutdown tasks, no need for the shutdown-hook
        if (ShutdownHook.performShutdownTasks())
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    /**
     * Stops the whole application
     */
    public void stopAll() throws BundleException {
        // stop the system bundle
        context.getBundle(0).stop();
    }

    public List<String> getInitialFolders() {
        String folders = context.getProperty("mucommander.folders");
        if (folders == null || folders.length() == 0) {
            return Collections.emptyList();
        }

        return Arrays.asList(folders.split(","));
    }

    public boolean silent() {
        return Boolean.parseBoolean(context.getProperty("mucommander.silent"));
    }

    public boolean fatalWarnings() {
        return Boolean.parseBoolean(context.getProperty("mucommander.fatalWarnings"));
    }

    public String assoc() {
        return context.getProperty("mucommander.assoc");
    }

    public String bookmark() {
        return context.getProperty("mucommander.bookmark");
    }

    public String configuration() {
        return context.getProperty("mucommander.configuration");
    }

    public String commandbar() {
        return context.getProperty("mucommander.commandbar");
    }

    public String extensions() {
        return context.getProperty("mucommander.extensions");
    }

    public String commands() {
        return context.getProperty("mucommander.commands");
    }

    public String keymap() {
        return context.getProperty("mucommander.keymap");
    }

    public String preferences() {
        return context.getProperty("mucommander.preferences");
    }

    public String shellHistory() {
        return context.getProperty("mucommander.shellHistory");
    }

    public String toolbar() {
        return context.getProperty("mucommander.toolbar");
    }

    public String credentials() {
        return context.getProperty("mucommander.credentials");
    }
}
