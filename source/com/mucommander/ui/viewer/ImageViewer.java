
package com.mucommander.ui.viewer;

import com.mucommander.text.Translator;
import com.mucommander.file.AbstractFile;

import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.*;


/**
 * 
 *
 * @author Maxence Bernard
 */
//public class ImageViewer extends FileViewer implements ComponentListener {
public class ImageViewer extends FileViewer implements ActionListener {
	
    private Image image;
    private Image scaledImage;

    private double zoomFactor;
	
    //	private JMenuItem prevImageItem;
    //	private JMenuItem nextImageItem;
    private JMenuItem zoomInItem;
    private JMenuItem zoomOutItem;
	
	
    public ImageViewer() {
    }	


    public void view(AbstractFile file) throws IOException {

        ViewerFrame frame = getFrame();
        if(frame!=null) {
            MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
	
            // Create Go menu
            JMenu controlsMenu = frame.addMenu(Translator.get("image_viewer.controls_menu"));
            //		nextImageItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.next_image"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), this);
            //		prevImageItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.previous_image"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), this);
            //		controlsMenu.add(new JSeparator());
            zoomInItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.zoom_in"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), this);
            zoomOutItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.zoom_out"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), this);
        }

        loadImage(file);
    }
	
	
    public String getTitle() {
        return file.getName()+" - "+image.getWidth(null)+"x"+image.getHeight(null)+" - "+((int)(zoomFactor*100))+"%";
    }
	
	
    public static boolean canViewFile(AbstractFile file) {
        String name = file.getName();
        String nameLowerCase = name.toLowerCase();
        return nameLowerCase.endsWith(".png")
            ||nameLowerCase.endsWith(".gif")
            ||nameLowerCase.endsWith(".jpg")
            ||nameLowerCase.endsWith(".jpeg");
    }

	
    public synchronized Dimension getPreferredSize() {
        return new Dimension(scaledImage.getWidth(null), scaledImage.getHeight(null));
    }

	
    private synchronized void loadImage(AbstractFile file) throws IOException {
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
        this.image = getToolkit().createImage(imageBytes);

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
        //if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Waiting for image to load "+image);
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(image, 0);
        try { tracker.waitForID(0); }
        catch(InterruptedException e) {}
        tracker.removeImage(image);
        //if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Image loaded "+image);
    }
	
	
    private synchronized void zoom(double factor) {
        frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        this.scaledImage = image.getScaledInstance((int)(image.getWidth(null)*factor), (int)(image.getHeight(null)*factor), Image.SCALE_DEFAULT);
        waitForImage(scaledImage);

        frame.setCursor(Cursor.getDefaultCursor());
    }

    /*
      private synchronized void goToImage(boolean next) {
      AbstractFile newFile;
      if(next)
      newFile = getNextFileInFolder(file, true);
      else
      newFile = getPreviousFileInFolder(file, true);
		
      if(newFile!=null) {
      try {
      loadImage(newFile);
      frame.setCurrentFile(newFile);
      updateFrame();
      }
      catch(IOException ex) {
      }
      }
      }
    */
	
    private void updateFrame() {
        // Revalidate, pack and repaint should be called in this order
        revalidate();
        frame.pack();
        frame.getContentPane().repaint();
    }

	
    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
		
        g.setColor(BG_COLOR);
        g.fillRect(0, 0, width, height);
		
        if(scaledImage!=null) {
            int imageWidth = scaledImage.getWidth(null);
            int imageHeight = scaledImage.getHeight(null);
            g.drawImage(scaledImage, Math.max(0, (width-imageWidth)/2), Math.max(0, (height-imageHeight)/2), null);
        }
    }
	

    private void checkZoom() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		
        zoomInItem.setEnabled(zoomFactor<1.0 || (2*zoomFactor*image.getWidth(null) < d.width
                                                 && 2*zoomFactor*image.getHeight(null) < d.height));

        zoomOutItem.setEnabled(zoomFactor>1.0 || (zoomFactor/2*image.getWidth(null)>160
                                                  && zoomFactor/2*image.getHeight(null)>120));
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
			
        checkZoom();
        //		}
    }
}
