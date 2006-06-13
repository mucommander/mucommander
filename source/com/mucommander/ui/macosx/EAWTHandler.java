
package com.mucommander.ui.macosx;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;


/**
 * This class registers the About, Preferences and Quit handlers using the com.apple.eawt API available
 * under Java 1.4 and up.
 *
 * @author Maxence Bernard
 */
class EAWTHandler implements ApplicationListener {

    public EAWTHandler() {
        Application app = new Application();
        // Enable the 'About' menu item
        app.setEnabledAboutMenu(true);
        // Enable the 'Preferences' menu item
        app.setEnabledPreferencesMenu(true);
        // Register this ApplicationListener
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
        // Accept or reject the request to quit based on user's response
        event.setHandled(OSXIntegration.doQuit());
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