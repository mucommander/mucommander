
package com.mucommander.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class SinEffectPanel extends JPanel implements Runnable, ComponentListener {
	private BufferedImage bi;
	private int imagePixels[];
	private int width;
	private int height;

	private Thread computeThread;
	
	private final static int INITIAL_SIN_WIDTH = 20;
	private final static int MIN_SIN_WIDTH = 5;
	private final static int MAX_SIN_WIDTH = 50;

	private final static int BG_COLOR = 0x000084;
	
	private boolean hasStarted;
	
	/**
	 * Creates a new SinEffectPanel. The given image must be completly loaded.
	 */
	public SinEffectPanel(Image image) {
		this.width = image.getWidth(null);
		this.height = image.getHeight(null);

		// Grabs pixels from the image
		this.imagePixels = new int[width * height];
		PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, imagePixels, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {}

		// Initializes the BufferedImage
		this.bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	
		addComponentListener(this);
	}

    public void start() {
        hasStarted = true;
		computeThread = new Thread(this);
        computeThread.start();
    }
    
    public void stop() {
        computeThread = null;
    }

	public void run() {
		WritableRaster wr = bi.getRaster();
		
		// Clears background with specified color
		Graphics g = bi.getGraphics();
		g.setColor(new Color(BG_COLOR));
		g.fillRect(0, 0, width, height);
	
		int temp[] = new int[3];
		int xOffset;
		int yOffset = 0;
		int pixel;
		double twoPi = 2*Math.PI;
		int sinWidth = INITIAL_SIN_WIDTH;
		boolean sinInc = true;

		// Computes sin values once for all (faster)
		float sinValues[] = new float[height];
		for(int i=0; i<sinValues.length; i++)
			sinValues[i] = (float)Math.sin(twoPi * i/height);

		while (computeThread!=null) {
			if(!isVisible())
				continue;
			
			for(int y=0; y<height; y++)  {
				xOffset = (int)(sinValues[(y+yOffset)%height] * sinWidth);
//				xOffset = (int)(Math.sin(twoPi * (y+yOffset)/height ) * sinWidth);
//				xOffset = (int)(Math.sin(twoPi * (y+yOffset)/height ) * MAX_SIN_WIDTH);

//				System.out.println(y+" "+xOffset+" "+(yOffset%SIN_WIDTH));
				
				for(int x=0; x<width; x++)  {
					if(x+xOffset<0 || x+xOffset>width-1)
						continue;

					pixel = imagePixels[y*width + x + xOffset];
					temp[0] = (pixel >> 16) & 0xff;
					temp[1] = (pixel >>  8) & 0xff;
					temp[2] = (pixel      ) & 0xff;

					wr.setPixel(x,y,temp);
				}
			}
			
			yOffset++;

//	Changes sin width, cool but creates 'hickups' when not fast enough
//			if (yOffset%(height/16) == 0) {
//			System.out.println("sin change");
//				if(sinInc && sinWidth==MAX_SIN_WIDTH)
//					sinInc = false;
//				else if(sinWidth==MIN_SIN_WIDTH)
//					sinInc = true;
//			
//				if(sinInc)
//					sinWidth+=1;
//				else
//					sinWidth-=1;
//			}

			repaint();
		
			try {
				computeThread.sleep(5);
			}
			catch(InterruptedException e) {}
		}
	}

	public void paint(Graphics g) {
		g.drawImage(bi, 0, 0, null);
	}

	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	/*****************************
	 * ComponentListener methods *
	 *****************************/
	 
	public void componentResized(ComponentEvent e) {
		if(!hasStarted)
			start();
	}

	public void componentHidden(ComponentEvent e) {
	}
	
	public void componentMoved(ComponentEvent e) {
	}
	
	public void componentShown(ComponentEvent e) {
	}     

}