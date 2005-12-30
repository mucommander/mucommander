package com.mucommander.conf;

/**
 * Used to monitor configuration file changes.
 * <p>
 * Once a <i>ConfigurationListener</i> instance has been registered to the
 * {@link com.mucommander.conf.ConfigurationManager} through the
 * {@link com.mucommander.conf.ConfigurationManager#addConfigurationListener(ConfigurationListener)}
 * method, it will be warned whenever a configuration variable has been changed.
 * </p>
 * <p>
 * Configuration listeners have the possibility of vetoing a configuration change.
 * </p>
 * @author Nicolas Rinaudo
 */
public interface ConfigurationListener {
    /**
     * Called whenever a configuration variable has been changed.
     * @param  event describes the configuration variable that has been modified.
     * @return true if the configuration change is accepted, false otherwise.
     */
    public boolean configurationChanged(ConfigurationEvent event);
}
