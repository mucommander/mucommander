package com.mucommander.ui.viewer;

import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.main.MainFrame;

/**
 * This class is used as an abstraction for the {@link EditorFrame} and {@link ViewerFrame}.
 * 
 * @author Arik Hadas
 */
public abstract class FileFrame extends JFrame {
	
	protected final static String CUSTOM_DISPOSE_EVENT = "CUSTOM_DISPOSE_EVENT";
	
	private MainFrame mainFrame;
	
	FileFrame(MainFrame mainFrame, Image icon) {
		this.mainFrame = mainFrame;

		setIconImage(icon);
		
		// Call #dispose() on close (default is hide)
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setResizable(true);
	}
	
	protected MainFrame getMainFrame() {
		return mainFrame;
	}
	
	////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public void pack() {
        super.pack();

        DialogToolkit.fitToScreen(this);
        DialogToolkit.fitToMinDimension(this, getMinimumSize());

        DialogToolkit.centerOnWindow(this, getMainFrame());
    }
    
    //////////////////////
    // Abstract methods //
    //////////////////////
    
    protected abstract void showGenericErrorDialog();
}
