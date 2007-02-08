
package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;
import com.mucommander.ui.theme.*;

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
public class TextViewer extends FileViewer implements ActionListener, ThemeListener {

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
        initTheme();
        add(textArea, BorderLayout.NORTH);
        ThemeManager.addThemeListener(this);
    }

    private void initTheme() {
        textArea.setForeground(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        textArea.setBackground(ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR));
        textArea.setSelectedTextColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_FOREGROUND_COLOR));
        textArea.setSelectionColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_BACKGROUND_COLOR));
        textArea.setFont(ThemeManager.getCurrentFont(Theme.EDITOR_FONT));
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



    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Receives theme color changes notifications.
     * @param colorId identifier of the color that has changed.
     * @param color   new value for the color.
     */
    public void colorChanged(int colorId, Color color) {
        switch(colorId) {
        case Theme.EDITOR_FOREGROUND_COLOR:
            textArea.setForeground(color);
            break;

        case Theme.EDITOR_BACKGROUND_COLOR:
            textArea.setBackground(color);
            break;

        case Theme.EDITOR_SELECTED_FOREGROUND_COLOR:
            textArea.setSelectedTextColor(color);
            break;

        case Theme.EDITOR_SELECTED_BACKGROUND_COLOR:
            textArea.setSelectionColor(color);
            break;
        }
    }

    /**
     * Receives theme font changes notifications.
     * @param fontId identifier of the font that has changed.
     * @param font   new value for the font.
     */
    public void fontChanged(int fontId, Font font) {
        if(fontId == Theme.EDITOR_FONT)
            textArea.setFont(font);
    }
}
