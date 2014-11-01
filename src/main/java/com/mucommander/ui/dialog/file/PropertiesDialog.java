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

package com.mucommander.ui.dialog.file;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.file.util.OSXFileUtils;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.runtime.OsVersion;
import com.mucommander.job.FileJob;
import com.mucommander.job.PropertiesJob;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ShowFilePropertiesAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.FileLabel;
import com.mucommander.ui.text.MultiLineLabel;

/**
 * This dialog shows properties of a file or a group of files : number of files, file kind,
 * combined size and location.
 *
 * @author Maxence Bernard
 */
public class PropertiesDialog extends FocusDialog implements Runnable, ActionListener {
    private PropertiesJob job;
    private Thread repaintThread;
    private SpinningDial dial;
	
    private JLabel counterLabel;
    private JLabel sizeLabel;

    private JButton okCancelButton;

    // Dialog width is constrained to 320, height is not an issue (always the same)
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(360,0);
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(450,10000);	

    /** How often should progress information be refreshed (in ms) */
    private final static int REFRESH_RATE = 500;

    /** Dimension of the large file icon displayed on left side of the dialog */
    private final static Dimension ICON_DIMENSION = new Dimension(64, 64);

	
    public PropertiesDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame,
              files.size() > 1 ? ActionProperties.getActionLabel(ShowFilePropertiesAction.Descriptor.ACTION_ID) :
              Translator.get("properties_dialog.file_properties", files.elementAt(0).getName()), mainFrame);

        this.job = new PropertiesJob(files, mainFrame);
		
        Container contentPane = getContentPane();

        JPanel fileDetailsPanel = new JPanel(new BorderLayout());

        Icon icon;
        boolean isSingleFile = files.size()==1;
        AbstractFile singleFile = isSingleFile?files.elementAt(0):null;
        if(isSingleFile) {
            icon = FileIcons.getFileIcon(singleFile, ICON_DIMENSION);
        }
        else {
            ImageIcon imageIcon = IconManager.getIcon(IconManager.COMMON_ICON_SET, "many_files.png");
            icon = IconManager.getScaledIcon(imageIcon, (float)ICON_DIMENSION.getWidth()/imageIcon.getIconWidth());
        }

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setVerticalAlignment(JLabel.TOP);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 8));

        fileDetailsPanel.add(iconLabel, BorderLayout.WEST);

        XAlignedComponentPanel labelPanel = new XAlignedComponentPanel(10);

        // Contents (set later)
        counterLabel = new JLabel("");
        labelPanel.addRow(Translator.get("properties_dialog.contents")+":", counterLabel, 6);

        // Location (set here)
        labelPanel.addRow(Translator.get("location")+":", new FileLabel(files.getBaseFolder(), true), 6);

        // Combined size (set later)
        JPanel sizePanel;
        sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sizePanel.add(sizeLabel = new JLabel(""));
        sizePanel.add(new JLabel(dial = new SpinningDial()));
        labelPanel.addRow(Translator.get("size")+":", sizePanel, 6);

        if(OsFamily.MAC_OS_X.isCurrent() && OsVersion.MAC_OS_X_10_4.isCurrentOrHigher()
        && isSingleFile && singleFile.hasAncestor(LocalFile.class)) {
            String comment = OSXFileUtils.getSpotlightComment(singleFile);
            JLabel commentLabel = new JLabel(Translator.get("comment")+":");
            commentLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
            commentLabel.setVerticalAlignment(SwingConstants.TOP);

            labelPanel.addRow(commentLabel, new MultiLineLabel(comment), 6);
        }

        updateLabels();

        fileDetailsPanel.add(labelPanel, BorderLayout.CENTER);

        YBoxPanel yPanel = new YBoxPanel(5);
        yPanel.add(fileDetailsPanel);
        contentPane.add(yPanel, BorderLayout.NORTH);

        okCancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKPanel(okCancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // OK button will receive initial focus
        setInitialFocusComponent(okCancelButton);		
		
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		
        start();
    }


    private void updateLabels() {
        int nbFiles = job.getNbFilesRecurse();
        int nbFolders = job.getNbFolders();
        counterLabel.setText(
                             (nbFiles>0?Translator.get("nb_files", ""+nbFiles):"")
                             +(nbFiles>0&&nbFolders>0?", ":"")
                             +(nbFolders>0?Translator.get("nb_folders", ""+nbFolders):"")
                             );
        sizeLabel.setText(SizeFormat.format(job.getTotalBytes(), SizeFormat.DIGITS_MEDIUM | SizeFormat.UNIT_LONG | SizeFormat.INCLUDE_SPACE| SizeFormat.ROUND_TO_KB) +
			  " (" + SizeFormat.format(job.getTotalBytes(), SizeFormat.DIGITS_FULL | SizeFormat.UNIT_LONG | SizeFormat.INCLUDE_SPACE) + ")");

        counterLabel.repaint(REFRESH_RATE);
        sizeLabel.repaint(REFRESH_RATE);
    }


    public void start() {
        job.start();
		
        repaintThread = new Thread(this, "com.mucommander.ui.dialog.file.PropertiesDialog's Thread");
        repaintThread.start();
    }

	
    //////////////////////
    // Runnable methods //
    //////////////////////

    public void run() {
        dial.setAnimated(true);
        while(repaintThread!=null && job.getState()!= FileJob.FINISHED) {
            updateLabels();
			
            try { Thread.sleep(REFRESH_RATE); }
            catch(InterruptedException e) {}
        }

        // Updates button labels and stops spinning dial.
        updateLabels();
        okCancelButton.setText(Translator.get("ok"));
        dial.setAnimated(false);
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////

    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==okCancelButton)
            dispose();
    }


    ///////////////////////////////////////
    // Overridden WindowListener methods // 
    ///////////////////////////////////////

    @Override
    public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
		
        // Stop threads
        job.interrupt();
        repaintThread = null;
    }
}
