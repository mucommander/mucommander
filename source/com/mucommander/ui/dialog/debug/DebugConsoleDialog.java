/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.Launcher;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.RefreshAction;
import com.mucommander.ui.action.impl.ShowDebugConsoleAction;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This dialog shows the last log messages collected by {@link DebugConsoleHandler} and allows them to be copied
 * to the clipboard. It also makes it possible to change the log level, the level combo box being preset to the
 * level returned by {@link Launcher#getLogLevel()}.
 *
 * @see ShowDebugConsoleAction
 * @see DebugConsoleHandler
 * @see Launcher#setLogLevel(Level)
 * @author Maxence Bernard
 */
public class DebugConsoleDialog extends FocusDialog implements ActionListener, ItemListener {

    /** Displays log records, and allows to copy their values to the clipboard */
    private JList recordsList;

    /** Allows the log level to be changed */
    private JComboBox levelComboBox;

    /** Closes the debug console when pressed */
    private JButton closeButton;

    /** Refreshes the list with the latest log records when pressed */
    private JButton refreshButton;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(600,400);
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(700,500);

    /**
     * Enumerates the log levels in the order in which they will appear in the level combo box.
     */
    private final static Level[] LEVELS = {
        Level.OFF,
        Level.SEVERE,
        Level.WARNING,
        Level.INFO,
        Level.CONFIG,
        Level.FINE,
        Level.FINER,
        Level.FINEST
    };


    /**
     * Creates a new {@link DebugConsoleDialog} using the given {@link MainFrame} as a parent.
     *
     * @param mainFrame the {@link MainFrame} to use as a parent
     */
    public DebugConsoleDialog(MainFrame mainFrame) {
        super(mainFrame, ActionProperties.getActionLabel(ShowDebugConsoleAction.Descriptor.ACTION_ID), mainFrame);

        Container contentPane = getContentPane();

        recordsList = new JList();
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
        Level logLevel = Launcher.getLogLevel();

        levelComboBox = new JComboBox();
        for(int i=0; i<LEVELS.length; i++) {
            levelComboBox.addItem(LEVELS[i].getName());
            if(LEVELS[i].equals(logLevel))
                levelComboBox.setSelectedIndex(i);
        }
        levelComboBox.addItemListener(this);

        comboPanel.add(levelComboBox);

        return comboPanel;
    }

    /**
     * Refreshes the JList with the log records contained by {@link DebugConsoleHandler}.
     */
    private void refreshLogRecords() {
        DefaultListModel listModel = new DefaultListModel();
        DebugConsoleHandler handler = DebugConsoleHandler.getInstance();

        final LogRecord[] records = handler.getLogRecords();
        Formatter formatter = handler.getFormatter();

        for (LogRecord record : records) {
            if (handler.isLoggable(record))
                listModel.addElement(new LogRecordListItem(record, formatter));
        }

        recordsList.setModel(listModel);        

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                recordsList.ensureIndexIsVisible(records.length-1);
            }
        });
    }

    /**
     * Changes the log level to the selected combo box value.
     */
    private void updateLogLevel() {
        Level newLevel = LEVELS[levelComboBox.getSelectedIndex()];

        Launcher.setLogLevel(newLevel);
        Launcher.updateLogLevel(newLevel);
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
                Level level = ((LogRecordListItem)value).getLogRecord().getLevel();
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
