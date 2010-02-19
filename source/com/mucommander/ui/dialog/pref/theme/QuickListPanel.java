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

package com.mucommander.ui.dialog.pref.theme;

import com.mucommander.text.Translator;
import com.mucommander.ui.chooser.FontChooser;
import com.mucommander.ui.chooser.PreviewLabel;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.icon.CustomFileIconProvider;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.quicklist.GenericPopupDataListWithIcons;
import com.mucommander.ui.quicklist.QuickList;
import com.mucommander.ui.quicklist.item.DataList;
import com.mucommander.ui.quicklist.item.HeaderMenuItem;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.ThemeData;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Arik Hadas
 */
public class QuickListPanel extends ThemeEditorPanel implements PropertyChangeListener {
	// - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Used to preview the quick list. */
	private JPanel quickListPreviewPanel;
	
	/** The header of the sample quick list */
    private HeaderMenuItem header = new HeaderMenuItem(Translator.get("sample_text"));

    /** The items of the sample quick list */
    private final static Object[] sampleData;
    
    /** The icon of the sample items */
    private final Icon sampleIcon = IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.FOLDER_ICON_NAME);

    static {
        String sampleText = Translator.get("sample_text");
        sampleData = new Object[10];
        for(int i=0; i<sampleData.length; i++)
            sampleData[i] = sampleText + " " + (i+1)+"   ";
    }

    /** The list of items of the sample quick list */
    private DataList list = new GenericPopupDataListWithIcons(sampleData) {
		@Override
        public Icon getImageIconOfItem(Object item) {
			return sampleIcon;
		}
		
		@Override
        public void colorChanged(ColorChangedEvent event) {
			super.colorChanged(event);
			repaint();
		}
		
		@Override
        protected void addMouseListenerToList() {
	    	addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
	        });
	    }
		
		@Override
        protected void addKeyListenerToList() {
			addKeyListener(new KeyListener() {

				public void keyPressed(KeyEvent e) {			
					switch(e.getKeyCode()) {
					case KeyEvent.VK_UP:
						{
							int numOfItems = getModel().getSize();				
							if (numOfItems > 0 && getSelectedIndex() == 0) {
								setSelectedIndex(numOfItems - 1);
								ensureIndexIsVisible(numOfItems - 1);
								e.consume();
							}
						}
						break;
					case KeyEvent.VK_DOWN:
						{
							int numOfItems = getModel().getSize();
							if (numOfItems > 0 && getSelectedIndex() == numOfItems - 1) {				
								setSelectedIndex(0);
								ensureIndexIsVisible(0);
								e.consume();
							}						
						}
						break;
					}
				}

				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
    };
    
    /**
     * Creates the quick list preview panel.
     * @return the quick list preview panel.
     */
    private JPanel createPreviewPanel() {
    	JPanel      panel;  // Preview panel.
        JScrollPane scroll; // Wraps the preview quick list.

        // add JScrollPane that contains the TablePopupDataList to the popup.
		scroll = new JScrollPane(list,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);				
		scroll.setBorder(null);		        
		scroll.getVerticalScrollBar().setFocusable( false ); 
        scroll.getHorizontalScrollBar().setFocusable( false );

        // Creates the panel.
        panel = new JPanel();        
        quickListPreviewPanel = new YBoxPanel();        
        quickListPreviewPanel.add(header);
        quickListPreviewPanel.add(scroll);
        quickListPreviewPanel.setBorder(new QuickList.PopupsBorder());
        panel.add(quickListPreviewPanel);
        panel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));
        
        return panel;
    }
    
	// - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new quick list editor.
     * @param parent    dialog containing the panel.
     * @param themeData  themeData being edited.
     */
    public QuickListPanel(PreferencesDialog parent, ThemeData themeData) {
        super(parent, Translator.get("quick_lists_menu"), themeData);
        initUI();
    }
	
	// - UI initialisation ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates the JPanel that contains all of the item's color configuration elements.
     * @param fontChooser font chooser used by the editor panel.
     * @return the JPanel that contains all of the item's color configuration elements.
     */
    private JPanel createItemColorsPanel(FontChooser fontChooser) {
        ProportionalGridPanel gridPanel;   // Contains all the color buttons.
        JPanel                colorsPanel; // Used to wrap the colors panel in a flow layout.
        PreviewLabel          label;

        // Initialisation.
        gridPanel = new ProportionalGridPanel(3);

        // Header.
        addLabelRow(gridPanel, false);

        label = new PreviewLabel();
        
        // Color buttons.
        addColorButtons(gridPanel, fontChooser, "theme_editor.normal",
                        ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR, ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR, label).addPropertyChangeListener(this);
        addColorButtons(gridPanel, fontChooser, "theme_editor.selected",
                ThemeData.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR, ThemeData.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR, label).addPropertyChangeListener(this);
        label.addPropertyChangeListener(this);
        
        // Wraps everything in a flow layout.
        colorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorsPanel.add(gridPanel);
        colorsPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));

        return colorsPanel;
    }
    
    /**
     * Creates the JPanel that contains all of the header's color configuration elements.
     * @param fontChooser font chooser used by the editor panel.
     * @return the JPanel that contains all of the header's color configuration elements.
     */
    private JPanel createHeaderColorsPanel(FontChooser fontChooser) {
        ProportionalGridPanel gridPanel;   // Contains all the color buttons.
        JPanel                colorsPanel; // Used to wrap the colors panel in a flow layout.
        PreviewLabel          label;

        // Initialisation.
        gridPanel = new ProportionalGridPanel(3);

        // Header.
        addLabelRow(gridPanel, false);

        label = new PreviewLabel();
        
        // Color buttons.        
        addColorButtons(gridPanel, fontChooser, "",
        		ThemeData.QUICK_LIST_HEADER_FOREGROUND_COLOR, ThemeData.QUICK_LIST_HEADER_BACKGROUND_COLOR, label).addPropertyChangeListener(this);
        label.addPropertyChangeListener(this);
        
        gridPanel.add(createCaptionLabel(""));
        gridPanel.add(new JLabel());
        PreviewLabel label3 = new PreviewLabel();        
        ColorButton butt;
        gridPanel.add(butt = new ColorButton(parent, themeData, ThemeData.QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR, PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, label3));//.addPropertyChangeListener(this);
        label3.setTextPainted(true);
        label3.addPropertyChangeListener(this);
        butt.addUpdatedPreviewComponent(label3);

        // Wraps everything in a flow layout.
        colorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorsPanel.add(gridPanel);
        colorsPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));

        return colorsPanel;
    }

	/**
     * Initialises the panel's UI.
     */
    private void initUI() {
        YBoxPanel   headerConfigurationPanel; // Contains all the configuration elements.
        YBoxPanel   itemConfigurationPanel; // Contains all the configuration elements.
        FontChooser fontChooser1;        // Used to select a font.
        FontChooser fontChooser2;        // Used to select a font.
        JPanel      mainPanel;          // Main panel.
        JTabbedPane tabbedPane;
        
        header.addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent e) {}

			public void componentMoved(ComponentEvent e) {}

			public void componentResized(ComponentEvent e) {
				quickListPreviewPanel.repaint();
			}

			public void componentShown(ComponentEvent e) {}
        });


        // Font chooser and preview initialization.
        fontChooser1 = createFontChooser(ThemeData.QUICK_LIST_HEADER_FONT);
        fontChooser2 = createFontChooser(ThemeData.QUICK_LIST_ITEM_FONT);
        addFontChooserListener(fontChooser1, header);
        addFontChooserListener(fontChooser2, list);

        // Header configuration panel initialization.
        headerConfigurationPanel = new YBoxPanel();
        headerConfigurationPanel.add(fontChooser1);
        headerConfigurationPanel.addSpace(10);
        headerConfigurationPanel.add(createHeaderColorsPanel(fontChooser1));

        // Item configuration panel initialization.
        itemConfigurationPanel = new YBoxPanel();
        itemConfigurationPanel.add(fontChooser2);
        itemConfigurationPanel.addSpace(10);
        itemConfigurationPanel.add(createItemColorsPanel(fontChooser1));
        
        // Create the tabbed pane.
        tabbedPane = new JTabbedPane();
        tabbedPane.add(Translator.get("theme_editor.header"), headerConfigurationPanel);
        tabbedPane.add(Translator.get("theme_editor.item"), itemConfigurationPanel);
        
        // Main layout.
        mainPanel   = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(createPreviewPanel(), BorderLayout.EAST);
        
        // Layout.
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.NORTH);
    }
    
    /**
     * Listens on changes on the foreground and background colors.
     */
    public void propertyChange(PropertyChangeEvent event) {
        // Background color changed.
        if(event.getPropertyName().equals(PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME)) {
            header.setBackgroundColors(themeData.getColor(ThemeData.QUICK_LIST_HEADER_BACKGROUND_COLOR),
            		themeData.getColor(ThemeData.QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR));
            list.setBackgroundColors(themeData.getColor(ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR),
            						 themeData.getColor(ThemeData.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR));
        }

        // Foreground color changed.
        else if(event.getPropertyName().equals(PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME)) {
            header.setForegroundColor(themeData.getColor(ThemeData.QUICK_LIST_HEADER_FOREGROUND_COLOR));            
            list.setForegroundColors(themeData.getColor(ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR),
            						 themeData.getColor(ThemeData.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR));
        }
    }

    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Ignored.
     */
    @Override
    public void commit() {}
}
