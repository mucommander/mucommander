
package com.mucommander.ui;

import com.mucommander.file.util.FileSet;
import com.mucommander.job.FileJob;
import com.mucommander.job.PropertiesJob;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.dialog.DialogToolkit;
import com.mucommander.ui.comp.dialog.FocusDialog;
import com.mucommander.ui.comp.layout.XAlignedComponentPanel;
import com.mucommander.ui.comp.layout.YBoxPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;


/**
 * This dialog shows properties of a file or a group of files : number of files, file kind,
 * combined size and location.
 *
 * @author Maxence Bernard
 */
public class PropertiesDialog extends FocusDialog implements Runnable, ActionListener {
    private MainFrame mainFrame;
    private PropertiesJob job;
    private Thread repaintThread;
	
    private JLabel counterLabel;
    private JLabel sizeLabel;

    private JButton okCancelButton;

    // Dialog width is constrained to 320, height is not an issue (always the same)
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);	

    /** How often should progress information be refreshed (in ms) */
    private final static int REFRESH_RATE = 500;
	
    /* Window title without status */
    private String title;

	
    public PropertiesDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, "", mainFrame);
        this.mainFrame = mainFrame;

        // Set dialog's title
        if(files.size()>1)
            this.title = Translator.get(com.mucommander.ui.action.ShowFilePropertiesAction.class.getName()+".label");
        else
            this.title = Translator.get("properties_dialog.file_properties", files.fileAt(0).getName());

        setTitle(title+" ("+Translator.get("properties_dialog.calculating")+")");
		
        // Display wait cursor while calculating size
        mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
        this.job = new PropertiesJob(files, mainFrame);
		
        Container contentPane = getContentPane();
	
        XAlignedComponentPanel mainPanel = new XAlignedComponentPanel(10);
		
        // Contents (set later)
        counterLabel = new JLabel("");
        mainPanel.addRow(Translator.get("properties_dialog.contents")+":", counterLabel, 10);

        // Location (set here)
        String location = files.getBaseFolder().getAbsolutePath();
        JLabel locationLabel = new JLabel(location);
        locationLabel.setToolTipText(location);
        mainPanel.addRow(Translator.get("location")+":", locationLabel, 10);

        // Combined size (set later)
        sizeLabel = new JLabel("");
        mainPanel.addRow(Translator.get("size")+":", sizeLabel, 5);

        updateLabels();
        YBoxPanel yPanel = new YBoxPanel(5);
        yPanel.add(mainPanel);
        contentPane.add(yPanel, BorderLayout.NORTH);
		
        okCancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKPanel(okCancelButton, this), BorderLayout.SOUTH);

        // OK button will receive initial focus
        setInitialFocusComponent(okCancelButton);		
		
        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okCancelButton);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		
        start();
    }


    private void updateLabels() {
        int nbFiles = job.getNbFilesRecurse();
        int nbFolders = job.getNbFolders();
        counterLabel.setText(
                             (nbFiles>0?Translator.get("properties_dialog.nb_files", ""+nbFiles):"")
                             +(nbFiles>0&&nbFolders>0?", ":"")
                             +(nbFolders>0?Translator.get("properties_dialog.nb_folders", ""+nbFolders):"")
                             );
        sizeLabel.setText(SizeFormat.format(job.getTotalBytes(), SizeFormat.DIGITS_SHORT | SizeFormat.UNIT_LONG | SizeFormat.INCLUDE_SPACE| SizeFormat.ROUND_TO_KB) + 
			  " (" + SizeFormat.format(job.getTotalBytes(), SizeFormat.DIGITS_FULL | SizeFormat.UNIT_LONG | SizeFormat.INCLUDE_SPACE| SizeFormat.ROUND_TO_KB) + ")");
		
        counterLabel.repaint(REFRESH_RATE);
        sizeLabel.repaint(REFRESH_RATE);
    }


    public void start() {
        job.start();
		
        repaintThread = new Thread(this, "com.mucommander.ui.PropertiesDialog's Thread");
        repaintThread.start();
    }

	
    //////////////////////
    // Runnable methods //
    //////////////////////

    public void run() {
        while(repaintThread!=null && job.getState()!= FileJob.FINISHED) {
            updateLabels();
			
            try { Thread.sleep(REFRESH_RATE); }
            catch(InterruptedException e) {}
        }

        // Change title and button's label to indicate that calculation is over
        updateLabels();
        setTitle(title);
        okCancelButton.setText(Translator.get("ok"));
        mainFrame.setCursor(Cursor.getDefaultCursor());
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

    public void windowClosed(WindowEvent e) {
        super.windowClosed(e);
		
        // Stop threads
        job.interrupt();
        repaintThread = null;
    }
}
