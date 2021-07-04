package com.mucommander.preferences.osgi;

import com.mucommander.ui.dialog.pref.general.GeneralPreferencesDialog;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferencePanelProviderTracker extends ServiceTracker<PreferencePanelProvider, PreferencePanelProvider> {

    private static final Logger LOG = LoggerFactory.getLogger(PreferencePanelProviderTracker.class);

    public PreferencePanelProviderTracker(BundleContext context) {
        super(context, PreferencePanelProvider.class, null);
    }

    @Override
    public PreferencePanelProvider addingService(ServiceReference<PreferencePanelProvider> reference) {
        PreferencePanelProvider preferencePanelProvider = super.addingService(reference);
        LOG.info("Registering preference panel provider: {}", preferencePanelProvider.getTitle());
        GeneralPreferencesDialog.addPreferencePanelProvider(preferencePanelProvider);
        return preferencePanelProvider;
    }

    @Override
    public void removedService(ServiceReference<PreferencePanelProvider> reference, PreferencePanelProvider preferencePanelProvider) {
        LOG.info("Unregistering preference panel provider: {}", preferencePanelProvider.getTitle());
        GeneralPreferencesDialog.removePreferencePanelProvider(preferencePanelProvider);
        super.removedService(reference, preferencePanelProvider);
    }
}
