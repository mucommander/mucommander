
package com.mucommander.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class LifeEffectPanel extends JPanel implements Runnable, ComponentListener {
    private BufferedImage bi;
    private Image logoImage;
    private int width;
    private int height;

    private Thread computeThread;

    private boolean hasStarted;

    private final static int NB_CELLS_X = 40;
    private final static int NB_CELLS_Y = 40;
    
    private boolean cells[][];
    private int cellsBuffer[][];
    private int cellWidth;
    private int cellHeight;


    /**
     * Creates a new SinEffectPanel. The given image must be completly loaded.
     */
    public LifeEffectPanel(Image image) {
        this.logoImage = image;

        this.width = image.getWidth(null);
        this.height = image.getHeight(null);

        this.cellWidth = width/NB_CELLS_X;
        this.cellHeight = height/NB_CELLS_Y;

        this.cells = new boolean[NB_CELLS_X][NB_CELLS_Y];
        this.cellsBuffer = new int[NB_CELLS_X][NB_CELLS_Y];

        // Initializes the BufferedImage
        this.bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        addComponentListener(this);
    }

    public void start() {
        hasStarted = true;
        initCells();

        computeThread = new Thread(this);
        computeThread.start();
    }

    public void stop() {
        computeThread = null;
    }

    public void run() {
        while (computeThread!=null) {
            paintBuffer();
            nextGeneration();

            repaint();
            
            try {
                computeThread.sleep(3000);
            }
            catch(InterruptedException e) {}
        }
    }

    /**
     * Initializes new cells and life form.
     */ 
    private void initCells() {
System.out.println("INITCELLS()");
        java.util.Random random = new java.util.Random();

        // Randomizes cells (33% percent of occupied cells)
        for(int i=0; i<NB_CELLS_X; i++) {
            for(int j=0; j<NB_CELLS_Y; j++) {
                cells[i][j] = ((Math.abs(random.nextInt()))%3)==1;
            }
        }
    }


    // create next generation of shape
    private void nextGeneration() {
System.out.println("NEXTGENERATION()");
        int x;
        int y;

        // clear the buffer
        for( x=0; x<NB_CELLS_X; x++ ) {
            for( y=0; y<NB_CELLS_Y; y++ ) {
                cellsBuffer[x][y] = 0;
            }
        }

        int xMin1, xMax1;
        int yMin1, yMax1;
        // count neighbors with rolling edges
        for(x=0; x<NB_CELLS_X; x++ ) {
            xMin1 = x==0?NB_CELLS_X-1:x-1;
            xMax1 = x==NB_CELLS_X-1?0:x+1;

            for( y=0; y<NB_CELLS_Y; y++ ) {
                yMin1 = y==0?NB_CELLS_Y-1:y-1;
                yMax1 = y==NB_CELLS_Y-1?0:y+1;

                if ( cells[x][y] ) {
                    cellsBuffer[xMin1][yMin1]++;
                    cellsBuffer[x][yMin1]++;
                    cellsBuffer[xMax1][yMin1]++;
                    cellsBuffer[xMin1][y]++;
                    cellsBuffer[xMax1][y]++;
                    cellsBuffer[xMin1][yMax1]++;
                    cellsBuffer[x][yMax1]++;
                    cellsBuffer[xMax1][yMax1]++;
                }
            }
        }

        // game of life rules
        for( x=0; x<NB_CELLS_X; x++ ) {
            for( y=0; y<NB_CELLS_Y; y++ ) {
                switch( cellsBuffer[x][y] ) {
                    case 2:
                        // no change
                        break;
                    case 3:
                        cells[x][y] = true;
                        break;
                    default:
                        cells[x][y] = false;
                        break;
                }
            }
        }
    }

    private void paintBuffer() {
System.out.println("PAINTBUFFER()");

        WritableRaster wr = bi.getRaster();

        // Clears background with specified color
        Graphics g = bi.getGraphics();

        g.drawImage(logoImage, 0, 0, null);
        
        g.setColor(new Color(0xFF0000BB));

        int cellX;
        int cellY = 0;
        for(int j=0; j<NB_CELLS_Y; j++) {
            cellX = 0;
            
            for(int i=0; i<NB_CELLS_X; i++) {
                System.out.println("FILL RECT "+cellX+" "+cellY+" "+(cellX+cellWidth)+" "+(cellY+cellHeight));
                if(cells[i][j])
                    g.fillRect(cellX, cellY, cellX+cellWidth, cellY+cellHeight);
                cellX += cellWidth;
            }

            cellY += cellHeight;
        }
    }


    public void paint(Graphics g) {
System.out.println("PAINT()");
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