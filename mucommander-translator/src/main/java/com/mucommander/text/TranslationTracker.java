package com.mucommander.text;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranslationTracker extends ServiceTracker<TranslationService, TranslationService> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationTracker.class);

    private TranslationService service;

    public TranslationTracker(BundleContext context) {
        super(context, TranslationService.class, null);
    }

    @Override
    public TranslationService addingService(ServiceReference<TranslationService> reference) {
        service = super.addingService(reference);
        Translator.init(service.getDictionaryBundle(), service.getLanguagesBundle(), service.getAvailableLanguages());
        LOGGER.info("TranslationServce is registered: " + service);
        return service;
    }

    @Override
    public void removedService(ServiceReference<TranslationService> reference, TranslationService service) {
        super.removedService(reference, service);
        LOGGER.info("TranslationService is unregistered: " + service);
    }

    public TranslationService getTranslationService() {
        return service;
    }
}
