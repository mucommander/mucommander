
package com.mucommander.ui.viewer;

import com.mucommander.text.Translator;
import com.mucommander.file.AbstractFile;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.menu.MenuToolkit;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.DataInputStream;
import java.io.*;


//public class ImageViewer extends FileViewer implements ComponentListener {
public class ImageViewer extends FileViewer implements KeyListener, ActionListener {
	
//	private final static int MAX_WIDTH_SCALED = 480;
//	private final static int MAX_HEIGHT_SCALED = 360;
	
	private Image image;
	private Image scaledImage;

	private double zoomFactor;
	
	private AbstractFile file;

	private JMenuItem prevImageItem;
	private JMenuItem nextImageItem;
	private JMenuItem zoomInItem;
	private JMenuItem zoomOutItem;
	
	private boolean isSeparateWindow;
	
	
	public ImageViewer() {
	}
	
		
	public ImageViewer(ViewerFrame frame) {
		super(frame);
		
		MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
		MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

		// Go menu
		JMenuBar menuBar = frame.getJMenuBar();
		JMenu controlsMenu = MenuToolkit.addMenu(Translator.get("image_viewer.controls_menu"), menuMnemonicHelper, null);
		nextImageItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.next_image"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), this);
		prevImageItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.previous_image"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), this);
		controlsMenu.add(new JSeparator());
		zoomInItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.zoom_in"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), this);
		zoomOutItem = MenuToolkit.addMenuItem(controlsMenu, Translator.get("image_viewer.zoom_out"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), this);
		menuBar.add(controlsMenu);
	}
	

	public void startViewing(AbstractFile file, boolean isSeparateWindow) throws IOException {
		this.isSeparateWindow = isSeparateWindow;

		loadImage(file);
		
//		setLayout(new BorderLayout());
//		label = new JLabel();
//		label.setIcon(new ImageIcon(image));

		addKeyListener(this);
//		addComponentListener(this);
	}
	
	
	public boolean canViewFile(AbstractFile file) {
		String name = file.getName();
		String nameLowerCase = name.toLowerCase();
		return nameLowerCase.endsWith(".png")
			||nameLowerCase.endsWith(".gif")
			||nameLowerCase.endsWith(".jpg")
			||nameLowerCase.endsWith(".jpeg");
	}

	public synchronized Dimension getPreferredSize() {
//		return new Dimension(Math.min(480, image.getWidth(null)), Math.min(360,image.getHeight(null)));
System.out.println("ImageViewer.getPreferredSize()= "+scaledImage.getWidth(null)+" "+scaledImage.getHeight(null));
		return new Dimension(scaledImage.getWidth(null), scaledImage.getHeight(null));
	}

	
	private synchronized void loadImage(AbstractFile file) throws IOException {
		this.file = file;
		frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		byte b[] = new byte[(int)file.getSize()];
		DataInputStream din = new DataInputStream(file.getInputStream());
		din.readFully(b);
		din.close();

		this.scaledImage = null;
		this.zoomFactor = 1.0;
		this.image = getToolkit().createImage(b);
		this.scaledImage = image;
		waitForImage(image);
		
		frame.setCursor(Cursor.getDefaultCursor());
	}

	
	private void waitForImage(Image image) {
//if(com.mucommander.Debug.ON) System.out.println("Waiting for image to load "+image);
		MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(image, 0);
		try { tracker.waitForID(0); }
		catch(InterruptedException e) {}
//if(com.mucommander.Debug.ON) System.out.println("Image loaded "+image);
	}
	
	
	private synchronized void zoom(double factor) {
		this.scaledImage = image.getScaledInstance((int)(image.getWidth(null)*factor), (int)(image.getHeight(null)*factor), Image.SCALE_DEFAULT);
		waitForImage(scaledImage);
		updateFrame();
	}

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

	
	private void updateFrame() {
		// Revalidate, pack and repaint should be called in this order
		revalidate();
		frame.pack();
		frame.getContentPane().repaint();
	}

	
	public void paint(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		
		g.setColor(ViewerFrame.BG_COLOR);
			g.fillRect(0, 0, width, height);
		
		if(scaledImage!=null) {
			int imageWidth = scaledImage.getWidth(null);
			int imageHeight = scaledImage.getHeight(null);
			g.drawImage(scaledImage, Math.max(0, (width-imageWidth)/2), Math.max(0, (height-imageHeight)/2), null);
		}
	}
	
/*
	private Image getScaledImage(Image image, int maxScaledWidth, int maxScaledHeight) {
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);

		if(imageWidth<maxScaledWidth && imageHeight<maxScaledHeight)
			return image;
				
		float ratio = imageWidth/(float)imageHeight;
		int scaledWidth;
		int scaledHeight;
		if (imageWidth>imageHeight) {
			scaledWidth = maxScaledWidth;
			scaledHeight = (int)(scaledWidth/ratio);
		}
		else {
			scaledHeight = maxScaledHeight;
			scaledWidth = (int)(scaledHeight*ratio);
		}
		return image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_DEFAULT);
	}

	
	public void componentResized(ComponentEvent e) {

//System.out.println("panel "+getWidth()+" "+getHeight());
		Image scaledImage = getScaledImage(image, getWidth(), getHeight());
//System.out.println("scaledImage "+scaledImage.getWidth(null)+" "+scaledImage.getHeight(null));
		label.setIcon(new ImageIcon(scaledImage));
//System.out.println("scaledImage "+scaledImage.getWidth(null)+" "+scaledImage.getHeight(null));
		label.setSize(scaledImage.getWidth(null), scaledImage.getHeight(null));
//System.out.println("label "+label.getWidth()+" "+label.getHeight());
		label.repaint();	

	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

*/

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if(source == prevImageItem)
			goToImage(false);
		else if(source == nextImageItem)
			goToImage(true);
		else {
			if(source == zoomInItem) {
				if(zoomFactor<2.0) {
					zoomFactor = zoomFactor*2;
					zoom(zoomFactor);
				}
			}
			else if(source == zoomOutItem) {
				if(zoomFactor>0.25) {
					zoomFactor = zoomFactor/2;
					zoom(zoomFactor);
				}
			}
			
			zoomInItem.setEnabled(zoomFactor<2.0);
			zoomOutItem.setEnabled(zoomFactor>0.25);
		}
	}


	public void keyTyped(KeyEvent e) {

	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		AbstractFile newFile = null;

		switch(keyCode) {
			case KeyEvent.VK_SPACE:
				goToImage(true);
				break;
				
			case KeyEvent.VK_BACK_SPACE:
				goToImage(false);
				break;
		}
		
	}

}