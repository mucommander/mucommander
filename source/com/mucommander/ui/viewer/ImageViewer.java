
package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.DataInputStream;
import java.io.*;

//public class ImageViewer extends FileViewer implements ComponentListener {
public class ImageViewer extends FileViewer {
	
//	private final static int MAX_WIDTH_SCALED = 480;
//	private final static int MAX_HEIGHT_SCALED = 360;
	
	private Image image;
	private JLabel label;
	
	private boolean isSeparateWindow;
	
	public ImageViewer(ViewerFrame frame) {
		super(frame);
	}	
	
	
	public void startViewing(AbstractFile fileToView, boolean isSeparateWindow) throws IOException {
		this.isSeparateWindow = isSeparateWindow;
		byte b[] = new byte[(int)fileToView.getSize()];
		DataInputStream din = new DataInputStream(fileToView.getInputStream());
		din.readFully(b);
		din.close();

		image = getToolkit().createImage(b);
		// Wait for the image to finish loading
		// so that we can access its width and height
		MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(image, 0);
		try { tracker.waitForID(0); }
		catch(InterruptedException e) {}
		
		setLayout(new BorderLayout());
		label = new JLabel();
		label.setIcon(new ImageIcon(image));
//		add(label, BorderLayout.CENTER);
		add(new JScrollPane(label, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		
//		addComponentListener(this);
	}
	
	public static boolean canViewFile(AbstractFile file) {
		String name = file.getName();
		String nameLowerCase = name.toLowerCase();
		return nameLowerCase.endsWith(".png")
			||nameLowerCase.endsWith(".gif")
			||nameLowerCase.endsWith(".jpg")
			||nameLowerCase.endsWith(".jpeg");
	}

	public Dimension getPreferredSize() {
//		return new Dimension(Math.min(480, image.getWidth(null)), Math.min(360,image.getHeight(null)));
System.out.println("ImageViewer.getPreferredSize()= "+image.getWidth(null)+" "+image.getHeight(null));
		return new Dimension(image.getWidth(null), image.getHeight(null));
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
}