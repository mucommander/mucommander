package com.mucommander.ui.plaf;

import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class LookAndFeelPreferencesPanel extends PreferencesPanel {

    static final String TITLE = "Look and Feel";

    public LookAndFeelPreferencesPanel(PreferencesDialog parent) {
        super(parent, TITLE);
        initUI();
    }

    // - UI initialisation ------------------------------------------------------
    // --------------------------------------------------------------------------
    private void initUI() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("the quick brown fox jumps over the lazy dog");
        add(label, BorderLayout.CENTER);
    }

    @Override
    protected void commit() {
        LoggerFactory.getLogger(LookAndFeelPreferencesPanel.class).warn("Commit not implemented");
    }
}
