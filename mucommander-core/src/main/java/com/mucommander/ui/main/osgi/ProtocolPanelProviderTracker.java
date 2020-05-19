package com.mucommander.ui.main.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.protocol.ui.ProtocolPanelProvider;
import com.mucommander.ui.dialog.server.ServerConnectDialog;
import com.mucommander.ui.main.DrivePopupButton;

public class ProtocolPanelProviderTracker extends ServiceTracker<ProtocolPanelProvider, ProtocolPanelProvider> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolPanelProviderTracker.class);


	public ProtocolPanelProviderTracker(BundleContext context) {
        super(context, ProtocolPanelProvider.class, null);
    }

    @Override
    public ProtocolPanelProvider addingService(ServiceReference<ProtocolPanelProvider> reference) {
        ProtocolPanelProvider service = super.addingService(reference);
        ServerConnectDialog.register(service);
        if (service.getPanelClass() != null)
            DrivePopupButton.register(service);
        LOGGER.info("ProtocolPanelProvider is registered: " + service);
        return service;
    }

    @Override
    public void removedService(ServiceReference<ProtocolPanelProvider> reference, ProtocolPanelProvider service) {
        ServerConnectDialog.unregister(service);
        DrivePopupButton.unregister(service);
        super.removedService(reference, service);
        LOGGER.info("ProtocolPanelProvider is unregistered: " + service);
    }
}
