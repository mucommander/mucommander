/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.dialog.debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import ch.qos.logback.classic.spi.ILoggingEvent;

import com.mucommander.MuLogger;
import com.mucommander.MuLogger.Level;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.RefreshAction;
import com.mucommander.ui.action.impl.ShowDebugConsoleAction;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * This dialog shows the last log messages collected by {@link DebugConsoleHandler} and allows them to be copied
 * to the clipboard. It also makes it possible to change the log level, the level combo box being preset to the
 * level returned by {@link MuLogger#getLogLevel()}.
 *
 * @see ShowDebugConsoleAction
 * @see DebugConsoleHandler
 * @see MuLogger#setLogLevel(Level)
 * @author Maxence Bernard
 */
public class DebugConsoleDialog extends FocusDialog implements ActionListener, ItemListener {

    /** Displays log records, and allows to copy their values to the clipboard */
    private JList<LogRecordListItem> recordsList;

    /** Allows the log level to be changed */
    private JComboBox<Level> levelComboBox;

    /** Closes the debug console when pressed */
    private JButton closeButton;

    /** Refreshes the list with the latest log records when pressed */
    private JButton refreshButton;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(600,400);
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(700,500);

    /**
     * Creates a new {@link DebugConsoleDialog} using the given {@link MainFrame} as a parent.
     *
     * @param mainFrame the {@link MainFrame} to use as a parent
     */
    public DebugConsoleDialog(MainFrame mainFrame) {
        super(mainFrame, ActionProperties.getActionLabel(ShowDebugConsoleAction.Descriptor.ACTION_ID), mainFrame);

        Container contentPane = getContentPane();

        recordsList = new JList<LogRecordListItem>();
        // Autoscroll when dragged
        recordsList.setAutoscrolls(true);
        recordsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        recordsList.setCellRenderer(new DebugListCellRenderer());
        refreshLogRecords();

        JScrollPane scrollPane = new JScrollPane(recordsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(createComboPanel(), BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        refreshButton = new JButton(Translator.get(new RefreshAction.Descriptor().getLabel()));
        refreshButton.addActionListener(this);
        buttonPanel.add(refreshButton);

        closeButton = new JButton(Translator.get("close"));
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        southPanel.add(buttonPanel, BorderLayout.EAST);
        contentPane.add(southPanel, BorderLayout.SOUTH);

        setInitialFocusComponent(closeButton);
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        setInitialFocusComponent(closeButton);
    }

    /**
     * Creates and returns a panel containing the level combo box and a leading localized label describing it.
     *
     * @return a panel containing the level combo box and a leading localized label describing it
     */
    private JPanel createComboPanel() {
        JPanel comboPanel = new JPanel(new FlowLayout());
        comboPanel.add(new JLabel(Translator.get("debug_console_dialog.level")+":"));
        Level logLevel = MuLogger.getLogLevel();

        levelComboBox = new JComboBox<Level>();
        for(Level level:Level.values())
            levelComboBox.addItem(level);
        		
        levelComboBox.setSelectedItem(logLevel);
        		
        levelComboBox.addItemListener(this);

        comboPanel.add(levelComboBox);

        return comboPanel;
    }

    /**
     * Refreshes the JList with the log records contained by {@link DebugConsoleHandler}.
     */
    private void refreshLogRecords() {
    	DefaultListModel<LogRecordListItem> listModel = new DefaultListModel<LogRecordListItem>();
        DebugConsoleHandler handler = DebugConsoleHandler.getInstance();

        final ILoggingEvent[] records = handler.getLogRecords();
        final Level currentLogLevel = MuLogger.getLogLevel();
        
        for (ILoggingEvent record : records) {
        	Level recordLevel = getLevel(record);
        	if (recordLevel.isInScopeOf(currentLogLevel))
        		listModel.addElement(new LogRecordListItem(record, recordLevel));
        }

        recordsList.setModel(listModel);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                recordsList.ensureIndexIsVisible(records.length-1);
            }
        });
    }
    
    public Level getLevel(ILoggingEvent lr) {
    	switch(lr.getLevel().toInt()) {
    	case ch.qos.logback.classic.Level.OFF_INT:
    		return Level.OFF;
    	case ch.qos.logback.classic.Level.ERROR_INT:
    		return Level.SEVERE;
    	case ch.qos.logback.classic.Level.WARN_INT:
    		return Level.WARNING;
    	case ch.qos.logback.classic.Level.INFO_INT:
    		return Level.INFO;
    	case ch.qos.logback.classic.Level.DEBUG_INT:
    		return Level.FINE;
    	case ch.qos.logback.classic.Level.TRACE_INT:
    		return Level.FINEST;
    	default:
    		return Level.OFF;
    	}
    }

    /**
     * Changes the log level to the selected combo box value.
     */
    private void updateLogLevel() {
        Level newLevel = (Level) levelComboBox.getSelectedItem();

        MuLogger.setLogLevel(newLevel);
        MuLogger.updateLogLevel(newLevel);
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source==refreshButton) {
            refreshLogRecords();
        }
        else if(source==closeButton) {
            dispose();
        }
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent e) {
        // Refresh the log records displayed in the JList whenever the selected level has been changed.
        int selectedIndex = levelComboBox.getSelectedIndex();
        if(selectedIndex!=-1) {
            updateLogLevel();
            refreshLogRecords();
        }
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * Custom {@link ListCellRenderer} that renders {@link LogRecordListItem} instances.
     */
    private class DebugListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value==null)
                return null;

            // TODO: line-wrap log items when the text is too long to fit on a single line
            // A single-column JTable may be the easiest way to go, see:
            // http://javaspecialists.co.za/archive/newsletter.do?issue=106&locale=en_US
            // http://forums.sun.com/thread.jspa?threadID=702740&start=0&tstart=0

            // Using a JTextArea with line-wrapping enabled does not work as a JList has by design a fixed height
            // for cells

            JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // Change the label's foreground color to match the level of the log record
            if(!isSelected) {
                Level level = ((LogRecordListItem)value).getLevel();
                Color color;

                if(level.equals(Level.SEVERE))
                    color = Color.RED;
                else if(level.equals(Level.WARNING))
                    color = new Color(255, 100, 0);     // Dark orange
                else if(level.equals(Level.CONFIG))
                    color = Color.BLUE;
                else if(level.equals(Level.INFO))
                    color = Color.BLACK;
                else if(level.equals(Level.FINE))
                    color = Color.DARK_GRAY;
                else
                    color = new Color(110, 110, 110);    // Between Color.GRAY and Color.DARK_GRAY

                label.setForeground(color);
            }

            // TODO: remove this when line-wrapping has been implemented
            // If component's preferred width is larger than the list's width then the component is not entirely
            // visible. In that case, we set a tooltip text that will display the whole text when mouse is over the
            // component
            if (recordsList.getVisibleRect().getWidth() < label.getPreferredSize().getWidth())
                label.setToolTipText(label.getText());
            // Have to set it to null because of the rubber-stamp rendering scheme (last value is kept)
            else
                label.setToolTipText(null);
            
            return label;
        }
    }
}
