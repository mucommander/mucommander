package org.jpedal.jbig2.examples.jai;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

@SuppressWarnings("serial")
public class JBIG2ReaderPluginTester extends JFrame {
    final static String appTitle = "JBIG2 Reader Plug-in Tester";

    final static int FORMAT_NAME = 0;
    final static int INPUT = 1;
    final static int MIME_TYPE = 2;
    final static int SUFFIX = 3;

    // Most recently read PCX image.

    BufferedImage biImage;

    // Offset in destination image where future decoded pixels will be placed.

    int dstOffX, dstOffY;

    // Image height and width.

    int height, width;

    // Source region definition.

    int srcX, srcY, srcWidth, srcHeight = 1;

    // Subsampling horizontal and vertical periods.

    int xSS = 1, ySS = 1;

    // Application status bar -- holds path and name of most recent PCX file.

    JLabel lblStatus;

    // Current method for getting an image reader.

    int method = FORMAT_NAME;

    // The picture panel displays the contents of the most recently read PCX
    // image.

    PicPanel pp;

    // The scroll pane allows the user to scroll around images that are bigger
    // than the picture panel.

    JScrollPane jsp;

    // Construct the PCXRPT GUI and indirectly start AWT helper threads.

    public JBIG2ReaderPluginTester(String title) {
        // Pass application title to superclass so that it appears on the title
        // bar.

        super(title);

        // Terminate the application when the user clicks the tiny x button on
        // the title bar.

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Construct an open file chooser. Initialize the starting directory to
        // the current directory.

        final JFileChooser fcOpen = new JFileChooser();
        fcOpen.setCurrentDirectory(new File(System.getProperty("user.dir")));

        // Construct the application's menu bar.

        JMenuBar mb = new JMenuBar();

        // The only menu to appear on the menu bar is File. The user invokes
        // menu items on this menu to open PCX images, configure the PCX reader
        // plug-in, and terminate the application.

        JMenu menuFile = new JMenu("File");

        // Create and install the open menu item.

        JMenuItem miOpen = new JMenuItem("Open...");

        ActionListener openl;
        openl = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Present the "open" file chooser without any file selected. If
                // the user cancels this file chooser, exit this method.

                fcOpen.setSelectedFile(null);
                if (fcOpen.showOpenDialog(JBIG2ReaderPluginTester.this) != JFileChooser.APPROVE_OPTION)
                    return;

                // Attempt to read the image from the selected file. If something
                // goes wrong, doOpen() presents an appropriate error message and
                // false returns. Exit this method.

                if (!doOpen(fcOpen.getSelectedFile()))
                    return;

                // Provide the user with assorted information.

                lblStatus.setText("Width: " + width + ", Height: " + height + ", File: " + fcOpen.getSelectedFile().getAbsolutePath());

                // Display the new PCX image in the picture panel. The picture
                // panel automatically adjusts its dimensions, causing the
                // scrollpane to determine if scrollbars should be displayed.

                pp.setBufferedImage(biImage);

                // Reset the scroll positions so that the image's upper-left
                // corner is visible.

                jsp.getHorizontalScrollBar().setValue(0);
                jsp.getVerticalScrollBar().setValue(0);
            }
        };
        miOpen.addActionListener(openl);

        menuFile.add(miOpen);

        // Create and install the configure menu item.

        JMenuItem miConfigure = new JMenuItem("Configure...");

        ActionListener cfgl;
        cfgl = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CfgDialog cfgdlg = new CfgDialog(JBIG2ReaderPluginTester.this, dstOffX, dstOffY, method, srcX, srcY, srcWidth, srcHeight, xSS, ySS);
                cfgdlg.setVisible(true);

                if (cfgdlg.isCanceled())
                    return;

                dstOffX = cfgdlg.getDstOffX();
                dstOffY = cfgdlg.getDstOffY();

                method = cfgdlg.getMethod();

                srcX = cfgdlg.getSrcX();
                srcY = cfgdlg.getSrcY();
                srcWidth = cfgdlg.getSrcWidth();
                srcHeight = cfgdlg.getSrcHeight();

                xSS = cfgdlg.getXSS();
                ySS = cfgdlg.getYSS();
            }
        };
        miConfigure.addActionListener(cfgl);

        menuFile.add(miConfigure);

        menuFile.addSeparator();

        // Create and install the exit menu item.

        JMenuItem miExit = new JMenuItem("Exit");

        ActionListener exitl;
        exitl = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        miExit.addActionListener(exitl);

        menuFile.add(miExit);

        // Add the file menu to the menu bar.

        mb.add(menuFile);

        // Install the menu bar.

        setJMenuBar(mb);

        // Create an initial picture panel that does not display a picture, but
        // has a default size.

        pp = new PicPanel(null);

        // Indirectly add the picture panel, by way of a scrollpane, to the
        // application's contentpane.

        getContentPane().add(jsp = new JScrollPane(pp));

        // Create a status bar that displays the method used to obtain an image
        // reader, the height and width of the current image, and the path and
        // name of the current image's file. Initialize the status bar text to
        // one space so that it will be displayed at a height corresponding to
        // its current font's size. Surround the status bar with an etched
        // border to visually separate this component from the picture panel.

        lblStatus = new JLabel(" ");
        lblStatus.setBorder(BorderFactory.createEtchedBorder());

        // Add the status bar to the bottom of the application's contentpane.

        getContentPane().add(lblStatus, BorderLayout.SOUTH);

        // Resize all components to their preferred sizes.

        pack();

        // Display GUI and start GUI processing.

        setVisible(true);
    }

    // Open the specified JBIG2 file and read the file's JBIG2 image.

    boolean doOpen(File file) {
        if (!file.exists()) {
            JOptionPane.showMessageDialog(JBIG2ReaderPluginTester.this, "File does not exist!", appTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }

        try {
            // Validate file extension.

            String path = file.getAbsolutePath().toLowerCase();

            if (!path.endsWith(".jbig2") && !path.endsWith(".jb2")) {
                JOptionPane.showMessageDialog(JBIG2ReaderPluginTester.this, "Incorrect file extension!", appTitle, JOptionPane.ERROR_MESSAGE);

                return false;
            }

            // Obtain an appropriate reader.

            ImageInputStream iis = ImageIO.createImageInputStream(file);

            Iterator iter;
            if (method == FORMAT_NAME)
                iter = ImageIO.getImageReadersByFormatName("jbig2");
            else if (method == MIME_TYPE)
                iter = ImageIO.getImageReadersByMIMEType("image/x-jbig2");
            else if (method == SUFFIX)
                iter = ImageIO.getImageReadersBySuffix("jbig2");
            else
                iter = ImageIO.getImageReaders(iis);

            // Validate existence of reader. A reader will not be returned by
            // getImageReaders() if JBIG2ImageReaderSpi's canDecodeInput() method
            // returns false.

            if (!iter.hasNext()) {
                JOptionPane.showMessageDialog(JBIG2ReaderPluginTester.this, "Unable to obtain reader!", appTitle, JOptionPane.ERROR_MESSAGE);

                return false;
            }

            // Extract reader.

            ImageReader reader = (ImageReader) iter.next();

            // Configure reader's input source.

            reader.setInput(iis, true);

            // Configure reader parameters.

            ImageReadParam irp = reader.getDefaultReadParam();

            if (!(dstOffX == 0 && dstOffY == 0))
                irp.setDestinationOffset(new Point(dstOffX, dstOffY));

            if (srcWidth != 0)
                irp.setSourceRegion(new Rectangle(srcX, srcY, srcWidth, srcHeight));

            if (!(xSS == 1 && ySS == 1))
                irp.setSourceSubsampling(xSS, ySS, 0, 0);

            // Read the image.
            biImage = reader.read(0, irp);

            // Obtain the image's width and height.

            width = reader.getWidth(0);
            height = reader.getHeight(0);

            // Cleanup.

            reader.dispose();

            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(JBIG2ReaderPluginTester.this, e.getMessage(), appTitle, JOptionPane.ERROR_MESSAGE);

            return false;
        }
    }

    // Application entry point.

    public static void main(String[] args) {
        // Create the application's GUI and start the application.

        new JBIG2ReaderPluginTester(appTitle);
    }
}

@SuppressWarnings("serial")
class CfgDialog extends JDialog {
    private final static int MAX_DSTOFFX = 9999;
    private final static int MAX_DSTOFFY = 9999;

    private final static int MAX_XSS = 9999;
    private final static int MAX_YSS = 9999;

    private final static int MAX_SRCX = 9999;
    private final static int MAX_SRCY = 9999;

    private final static int MAX_SRCWIDTH = 9999;
    private final static int MAX_SRCHEIGHT = 9999;

    private boolean canceled;

    private int dstOffX, dstOffY;

    private int srcHeight, srcWidth, srcX, srcY;

    private int xSS, ySS;

    private int method;

    CfgDialog(JFrame f, int dstOffX, int dstOffY, int method, int srcX, int srcY, int srcWidth, int srcHeight, int xSS, int ySS) {
        // Assign title to dialog box's title bar and ensure dialog box is
        // modal.

        super(f, "Configure", true);

        // Create a main layout panel that divides the GUI into several
        // sections, where each section has the same width and height.

        JPanel pnlLayout = new JPanel();
        pnlLayout.setLayout(new GridLayout(5, 1));

        // Create and install the destination offset section.

        JPanel pnl = new JPanel();
        Border bd = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        pnl.setBorder(BorderFactory.createTitledBorder(bd, "Destination Offset"));

        pnl.add(new JLabel("X"));
        final JSpinner spnDstOffX = new JSpinner(new SpinnerNumberModel(dstOffX, 0, MAX_DSTOFFX, 1));
        pnl.add(spnDstOffX);

        pnl.add(new JLabel("Y"));
        final JSpinner spnDstOffY = new JSpinner(new SpinnerNumberModel(dstOffY, 0, MAX_DSTOFFY, 1));
        pnl.add(spnDstOffY);

        pnlLayout.add(pnl);

        // Create and install the method section.

        pnl = new JPanel();
        bd = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        pnl.setBorder(BorderFactory.createTitledBorder(bd, "Method"));

        final JRadioButton rbChoice1 = new JRadioButton("Format name");
        if (method == JBIG2ReaderPluginTester.FORMAT_NAME)
            rbChoice1.setSelected(true);
        pnl.add(rbChoice1);

        final JRadioButton rbChoice2 = new JRadioButton("Input");
        if (method == JBIG2ReaderPluginTester.INPUT)
            rbChoice2.setSelected(true);
        pnl.add(rbChoice2);

        final JRadioButton rbChoice3 = new JRadioButton("MIME type");
        if (method == JBIG2ReaderPluginTester.MIME_TYPE)
            rbChoice3.setSelected(true);
        pnl.add(rbChoice3);

        final JRadioButton rbChoice4 = new JRadioButton("Suffix");
        if (method == JBIG2ReaderPluginTester.SUFFIX)
            rbChoice4.setSelected(true);
        pnl.add(rbChoice4);

        final ButtonGroup bg = new ButtonGroup();
        bg.add(rbChoice1);
        bg.add(rbChoice2);
        bg.add(rbChoice3);
        bg.add(rbChoice4);

        pnlLayout.add(pnl);

        // Create and install the source region section.

        pnl = new JPanel();
        bd = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        pnl.setBorder(BorderFactory.createTitledBorder(bd, "Source Region"));

        pnl.add(new JLabel("Src X"));
        final JSpinner spnSrcX = new JSpinner(new SpinnerNumberModel(srcX, 0, MAX_SRCX, 1));
        pnl.add(spnSrcX);

        pnl.add(new JLabel("Src Y"));
        final JSpinner spnSrcY = new JSpinner(new SpinnerNumberModel(srcY, 0, MAX_SRCY, 1));
        pnl.add(spnSrcY);

        pnl.add(new JLabel("Src Width"));
        final JSpinner spnSrcWidth = new JSpinner(new SpinnerNumberModel(srcWidth, 0, MAX_SRCWIDTH, 1));
        pnl.add(spnSrcWidth);

        pnl.add(new JLabel("Src Height"));
        final JSpinner spnSrcHeight = new JSpinner(new SpinnerNumberModel(srcHeight, 1, MAX_SRCHEIGHT, 1));
        pnl.add(spnSrcHeight);

        pnlLayout.add(pnl);

        // Create and install the source subsampling section.

        pnl = new JPanel();
        bd = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        pnl.setBorder(BorderFactory.createTitledBorder(bd, "Source Subsampling"));

        pnl.add(new JLabel("X Subsampling"));
        final JSpinner spnXSS = new JSpinner(new SpinnerNumberModel(xSS, 1, MAX_XSS, 1));
        pnl.add(spnXSS);

        pnl.add(new JLabel("Y Subsampling"));
        final JSpinner spnYSS = new JSpinner(new SpinnerNumberModel(ySS, 1, MAX_YSS, 1));
        pnl.add(spnYSS);

        pnlLayout.add(pnl);

        // Create and install the button section.

        pnl = new JPanel();
        pnl.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton btn = new JButton("OK");
        pnl.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canceled = false;

                if (rbChoice1.isSelected())
                    CfgDialog.this.method = JBIG2ReaderPluginTester.FORMAT_NAME;
                else if (rbChoice2.isSelected())
                    CfgDialog.this.method = JBIG2ReaderPluginTester.INPUT;
                else if (rbChoice3.isSelected())
                    CfgDialog.this.method = JBIG2ReaderPluginTester.MIME_TYPE;
                else
                    CfgDialog.this.method = JBIG2ReaderPluginTester.SUFFIX;

                CfgDialog.this.dstOffX = (Integer) spnDstOffX.getValue();
                CfgDialog.this.dstOffY = (Integer) spnDstOffY.getValue();

                CfgDialog.this.xSS = (Integer) spnXSS.getValue();
                CfgDialog.this.ySS = (Integer) spnYSS.getValue();

                CfgDialog.this.srcX = (Integer) spnSrcX.getValue();
                CfgDialog.this.srcY = (Integer) spnSrcY.getValue();
                CfgDialog.this.srcWidth = (Integer) spnSrcWidth.getValue();
                CfgDialog.this.srcHeight = (Integer) spnSrcHeight.getValue();

                dispose();
            }
        });

        btn = new JButton("Cancel");
        pnl.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canceled = true;

                dispose();
            }
        });

        pnlLayout.add(pnl);

        // Add main layout panel to content pane.

        getContentPane().add(pnlLayout);

        // Resize dialog box to union of collective preferred sizes of all
        // contained components.

        pack();
    }

    int getDstOffX() {
        return dstOffX;
    }

    int getDstOffY() {
        return dstOffY;
    }

    int getMethod() {
        return method;
    }

    int getSrcHeight() {
        return srcHeight;
    }

    int getSrcWidth() {
        return srcWidth;
    }

    int getSrcX() {
        return srcX;
    }

    int getSrcY() {
        return srcY;
    }

    int getXSS() {
        return xSS;
    }

    int getYSS() {
        return ySS;
    }

    boolean isCanceled() {
        return canceled;
    }
}

@SuppressWarnings("serial")
class PicPanel extends JPanel {
    // Dimensions of picture panel's preferred size.

    final static int WIDTH = 600;
    final static int HEIGHT = 440;

    // Reference to BufferedImage whose image is displayed in panel. If null
    // reference, nothing is displayed in panel area.

    private BufferedImage bi;

    // Create a picture panel component.

    PicPanel(BufferedImage bi) {
        // Install the buffered image for this panel.

        setBufferedImage(bi);
    }

    // Retrieve this component's preferred size for layout purposes.

    public Dimension getPreferredSize() {
        // When the program starts, there is no installed buffered image so a
        // default preferred size is chosen. After a buffered image has been
        // installed, that buffered image's size is returned as the preferred
        // size.

        if (bi == null)
            return new Dimension(WIDTH, HEIGHT);
        else
            return new Dimension(bi.getWidth(), bi.getHeight());
    }

    // Redraw the picture panel.

    public void paintComponent(Graphics g) {
        // Paint the component's background to prevent artifacts from appearing.

        super.paintComponent(g);

        // If a buffered image has been installed, paint its contents on the
        // panel.

        if (bi != null)
            g.drawImage(bi, 0, 0, this);
    }

    // Install a new buffered image into the picture panel.

    public void setBufferedImage(BufferedImage bi) {
        // Save the buffered image for future painting.

        this.bi = bi;

        // The following method call invalidates this component and then adds
        // this component's validateRoot (the JScrollPane in which the picture
        // panel is contained) to a list of components that need to be validated.
        // Validation results in a call to this component's getPreferredSize()
        // method; this information will be used by the scrollpane to determine
        // if its scrollbars should be shown.

        revalidate();

        // Paint the new image on the panel.

        repaint();
    }
}
