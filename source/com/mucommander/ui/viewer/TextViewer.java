
package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * 
 *
 * @author Maxence Bernard
 */
//public class TextViewer extends FileViewer implements ActionListener, WindowListener {
public class TextViewer extends FileViewer implements ActionListener {

    //	private String encoding;

    private JMenuItem copyItem;
    private JMenuItem selectAllItem;
	
    private JTextArea textArea;
	
	
    //	public TextViewer(ViewerFrame frame) {
    //		super(frame);
    public TextViewer() {
        setLayout(new BorderLayout());
        textArea = new JTextArea();
        textArea.setEditable(false);
        add(textArea, BorderLayout.NORTH);
    }

	
    public void view(AbstractFile file) throws IOException {
        InputStreamReader isr = new InputStreamReader(file.getInputStream());
        textArea.read(isr, null);
        isr.close();
		
        textArea.setCaretPosition(0);
        //		pack();

        ViewerFrame frame = getFrame();
        if(frame!=null) {
            // Create Edit menu
            MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
            JMenu editMenu = frame.addMenu(Translator.get("text_viewer.edit"));
            copyItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.copy"), menuItemMnemonicHelper, null, this);
            selectAllItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.select_all"), menuItemMnemonicHelper, null, this);
        }
    }

	
    public Insets getInsets() {
        return new Insets(4, 3, 4, 3);
    }
	
	
    public static boolean canViewFile(AbstractFile file) {
        return true;
    }


    public long getMaxRecommendedSize() {
        return 131072;
    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        if(source == copyItem)
            textArea.copy();
        else if(source == selectAllItem)
            textArea.selectAll();
    }

}
