package com.mucommander.conf;

/**
 * Used to monitor configuration file changes.
 * <p>
 * Once a <i>ConfigurationListener</i> instance has been registered to the
 * {@link net.muwire.common.manager.ConfigurationManager} through the
 * {@link net.muwire.common.manager.ConfigurationManager#addConfigurationListener(ConfigurationListener)}
 * method, it will be warned whenever a configuration variable has been changed.
 * </p>
 * <p>
 * Configuration listeners have the possibility of vetoing a configuration change. This feature was added
 * because Muwire configuration is (1) dynamic and (2) not typed. Without it, client software wouldn't be able
 * to perform syntax checking when changing the configuration.<br>
 * Note that this presents a big security risk: a poorly coded listener could veto <i>all</i>
 * changes, resulting in a static configuration.
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
/* End of interface ConfigurationListener */
