package com.mucommander.ui.action;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * MuteProxyAction is an implementation of {@link ProxyAction} where {@link #actionPerformed(java.awt.event.ActionEvent)}
 * does nothing. 
 *
 * @author Maxence Bernard
 */
public class MuteProxyAction extends ProxyAction {

    public MuteProxyAction(Action proxiedAction) {
        super(proxiedAction);
    }

    /**
     * This method is a No-op, i.e. does absolutely nothing. 
     */
    public void actionPerformed(ActionEvent actionEvent) {
    }
}
