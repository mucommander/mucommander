
package com.mucommander.ui.macosx;

import com.apple.eawt.ApplicationListener;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.Application;


class EAWTHandler implements ApplicationListener {

    public EAWTHandler() {
        Application app = new Application();
        app.setEnabledAboutMenu(true);
        app.setEnabledPreferencesMenu(true);
        app.addApplicationListener(this);
    }

    public void handleAbout(ApplicationEvent event) {
        event.setHandled(true);
        OSXIntegration.showAbout();
    }

    public void handlePreferences(ApplicationEvent event) {
        event.setHandled(true);
        OSXIntegration.showPreferences();
    }

    public void handleQuit(ApplicationEvent event) {
        event.setHandled(true);
        OSXIntegration.doQuit();
    }

    public void handleOpenApplication(ApplicationEvent event) {
        // No-op
    }

    public void handleReOpenApplication(ApplicationEvent event) {
        // No-op
    }

    public void handleOpenFile(ApplicationEvent event) {
        // No-op
    }

    public void handlePrintFile(ApplicationEvent event) {
        // No-op
    }

}