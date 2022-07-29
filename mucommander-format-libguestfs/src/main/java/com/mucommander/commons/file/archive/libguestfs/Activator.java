package com.mucommander.commons.file.archive.libguestfs;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.mucommander.commons.file.osgi.FileFormatService;
import com.mucommander.commons.runtime.OsFamily;

public class Activator implements BundleActivator {

    private ServiceRegistration<FileFormatService> serviceRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        // TODO: replace with a better check
        if (!OsFamily.LINUX.isCurrent())
            return;

        serviceRegistration = context.registerService(FileFormatService.class, LibguestfsFormatProvider::new, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (serviceRegistration != null)
            serviceRegistration.unregister();
    }

}
