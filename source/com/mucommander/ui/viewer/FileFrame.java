package com.mucommander.ui.viewer;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import com.mucommander.AppLogger;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamilies;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.helper.FocusRequester;
import com.mucommander.ui.layout.AsyncPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * This class is used as an abstraction for the {@link EditorFrame} and {@link ViewerFrame}.
 * 
 * @author Arik Hadas
 */
public abstract class FileFrame extends JFrame {
	
	protected final static String CUSTOM_DISPOSE_EVENT = "CUSTOM_DISPOSE_EVENT";
	
	// The file presenter within this frame
	private FilePresenter filePresenter;
	
	// The main frame from which this frame was initiated
	private MainFrame mainFrame;
	
	FileFrame(MainFrame mainFrame, AbstractFile file, Image icon) {
		this.mainFrame = mainFrame;

		setIconImage(icon);
		
		// Call #dispose() on close (default is hide)
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setResizable(true);
        
        initContentPane(file);
	}
	
	protected void initContentPane(final AbstractFile file) {
        AsyncPanel asyncPanel = new AsyncPanel() {
        	
            @Override
            public JComponent getTargetComponent() {
                try {
                	filePresenter = createFilePresenter(file);

                    // Ask the presenter to present the file
                	filePresenter.open(file);
                }
                catch(Exception e) {
                    AppLogger.fine("Exception caught", e);

                    // May be a UserCancelledException if the user canceled (refused to confirm the operation after a warning)
                    if(!(e instanceof UserCancelledException))
                        showGenericErrorDialog();

                    dispose();
                    return filePresenter==null?new JPanel():filePresenter;
                }

                setJMenuBar(filePresenter.getMenuBar());
                
                // Catch Apple+W keystrokes under Mac OS X to close the window
                if(OsFamilies.MAC_OS_X.isCurrent()) {
                	filePresenter.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.META_MASK), CUSTOM_DISPOSE_EVENT);
                	filePresenter.getActionMap().put(CUSTOM_DISPOSE_EVENT, new AbstractAction() {
                		public void actionPerformed(ActionEvent e){
                			dispose();
                		}
                	});
                }

                return filePresenter;
            }

            @Override
            protected void updateLayout() {
                super.updateLayout();

                // Request focus on the viewer when it is visible
                FocusRequester.requestFocus(filePresenter);
            }
        };

        // Add the AsyncPanel to the content pane
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(asyncPanel, BorderLayout.CENTER);
        setContentPane(contentPane);

        // Sets panel to preferred size, without exceeding a maximum size and with a minimum size
        pack();
        setVisible(true);
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
    
    @Override
    public void dispose() {
    	if (filePresenter != null)
    		filePresenter.beforeCloseHook();
    	super.dispose();
    }
    
    //////////////////////
    // Abstract methods //
    //////////////////////
    
    protected abstract void showGenericErrorDialog();
    
    protected abstract FilePresenter createFilePresenter(AbstractFile file) throws UserCancelledException, Exception;
}
