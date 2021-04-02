package com.mucommander.conf;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext context) throws Exception {
        LOGGER.debug("starting");
        PlatformManager.setPreferencesFolder(preferences(context));
        MuConfigurations.loadPreferences();
    }

    private String preferences(BundleContext context) {
        return context.getProperty("mucommander.preferences");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        LOGGER.debug("stopping");
    }

}
