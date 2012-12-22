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


package com.mucommander.ui.viewer.image;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeListener;
import com.mucommander.ui.theme.ThemeManager;
import com.mucommander.ui.viewer.FileFrame;
import com.mucommander.ui.viewer.FileViewer;


/**
 * A simple image viewer, capable of displaying <code>PNG</code>, <code>GIF</code> and <code>JPEG</code> images. 
 *
 * @author Maxence Bernard, Arik Hadas
 */
class ImageViewer extends FileViewer implements ActionListener {
    private Image image;
    private Image scaledImage;
    private double zoomFactor;
	
    /** Menu bar */
    // Menus //
    private JMenu controlsMenu;
    // Items //
    //	private JMenuItem prevImageItem;
    //	private JMenuItem nextImageItem;
    private JMenuItem zoomInItem;
    private JMenuItem zoomOutItem;
	
    private ImageViewerImpl imageViewerImpl;
    
    public ImageViewer() {
    	imageViewerImpl = new ImageViewerImpl();
    	
    	setComponentToPresent(imageViewerImpl);
    	
    	// Create Go menu
    	MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
    	controlsMenu = MenuToolkit.addMenu(Translator.get("image_viewer.controls_menu"), menuMnemonicHelper, null);
    	
        //		nextImageItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.next_image"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), this);
        //		prevImageItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.previous_image"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), this);
        //		controlsMenu.add(new JSeparator());
        zoomInItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.zoom_in"), menuMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), this);
        zoomOutItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.zoom_out"), menuMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), this);
    }
    
    @Override
    public JMenuBar getMenuBar() {
    	JMenuBar menuBar = super.getMenuBar();
    	
        menuBar.add(controlsMenu);
    	
    	return menuBar;
    }

    private synchronized void loadImage(AbstractFile file) throws IOException {
        FileFrame frame = getFrame();
        frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
        int read;
        byte buffer[] = new byte[1024];
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        InputStream in = file.getInputStream();
        while ((read=in.read(buffer, 0, buffer.length))!=-1)
            bout.write(buffer, 0, read);

        byte imageBytes[] = bout.toByteArray();
        bout.close();
        in.close();

        this.scaledImage = null;
        this.image = imageViewerImpl.getToolkit().createImage(imageBytes);

        waitForImage(image);

        int width = image.getWidth(null);
        int height = image.getHeight(null);
        this.zoomFactor = 1.0;
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        while(width>d.width || height>d.height) {
            width = width/2;
            height = height/2;
            zoomFactor = zoomFactor/2;
        }
		
        if(zoomFactor==1.0)
            this.scaledImage = image;
        else
            zoom(zoomFactor);
			
        checkZoom();
        frame.setCursor(Cursor.getDefaultCursor());
    }

	
    private void waitForImage(Image image) {
        //AppLogger.finest("Waiting for image to load "+image);
        MediaTracker tracker = new MediaTracker(imageViewerImpl);
        tracker.addImage(image, 0);
        try { tracker.waitForID(0); }
        catch(InterruptedException e) {}
        tracker.removeImage(image);
        //AppLogger.finest("Image loaded "+image);
    }
	
	
    private synchronized void zoom(double factor) {
        FileFrame frame = getFrame();

        frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        this.scaledImage = image.getScaledInstance((int)(image.getWidth(null)*factor), (int)(image.getHeight(null)*factor), Image.SCALE_DEFAULT);
        waitForImage(scaledImage);

        frame.setCursor(Cursor.getDefaultCursor());
    }

    private void updateFrame() {
    	FileFrame frame = getFrame();

        // Revalidate, pack and repaint should be called in this order
        frame.setTitle(this.getTitle());
        imageViewerImpl.revalidate();
        frame.pack();
        frame.getContentPane().repaint();
    }

    private void checkZoom() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		
        zoomInItem.setEnabled(zoomFactor<1.0 || (2*zoomFactor*image.getWidth(null) < d.width
                                                 && 2*zoomFactor*image.getHeight(null) < d.height));

        zoomOutItem.setEnabled(zoomFactor>1.0 || (zoomFactor/2*image.getWidth(null)>160
                                                  && zoomFactor/2*image.getHeight(null)>120));
    }

    ///////////////////////////////
    // FileViewer implementation //
    ///////////////////////////////

    @Override
    public void show(AbstractFile file) throws IOException {
        loadImage(file);
    }

    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    @Override
    public String getTitle() {
        return super.getTitle()+" - "+image.getWidth(null)+"x"+image.getHeight(null)+" - "+((int)(zoomFactor*100))+"%";
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        //		if(source == prevImageItem)
        //			goToImage(false);
        //		else if(source == nextImageItem)
        //			goToImage(true);
        //		else {

        if(source==zoomInItem && zoomInItem.isEnabled()) {
            zoomFactor = zoomFactor*2;
            zoom(zoomFactor);
            updateFrame();
        }
        else if(source==zoomOutItem && zoomOutItem.isEnabled()) {
            zoomFactor = zoomFactor/2;
            zoom(zoomFactor);
            updateFrame();
        }
        else {
        	super.actionPerformed(e);
        	return;
        }
			
        checkZoom();
        //		}
    }
    
    private class ImageViewerImpl extends JPanel implements ThemeListener {

    	private Color backgroundColor;
    	
    	ImageViewerImpl() {
    		backgroundColor = ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR);
            ThemeManager.addCurrentThemeListener(this);
    	}
    	
    	////////////////////////
        // Overridden methods //
        ////////////////////////

        @Override
        public void paint(Graphics g) {
            int width = getWidth();
            int height = getHeight();

            g.setColor(backgroundColor);
            g.fillRect(0, 0, width, height);

            if(scaledImage!=null) {
                int imageWidth = scaledImage.getWidth(null);
                int imageHeight = scaledImage.getHeight(null);
                g.drawImage(scaledImage, Math.max(0, (width-imageWidth)/2), Math.max(0, (height-imageHeight)/2), null);
            }
        }
        
        @Override
        public synchronized Dimension getPreferredSize() {
            return new Dimension(scaledImage.getWidth(null), scaledImage.getHeight(null));
        }
    	
    	//////////////////////////////////
        // ThemeListener implementation //
        //////////////////////////////////

        /**
         * Receives theme color changes notifications.
         */
        public void colorChanged(ColorChangedEvent event) {
            if(event.getColorId() == Theme.EDITOR_BACKGROUND_COLOR) {
                backgroundColor = event.getColor();
                repaint();
            }
        }

        /**
         * Not used, implemented as a no-op.
         */
        public void fontChanged(FontChangedEvent event) {}
    }
}
