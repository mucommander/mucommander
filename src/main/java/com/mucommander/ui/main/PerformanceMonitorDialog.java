package com.mucommander.ui.main;

import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.TogglePerformanceMonitorAction;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.main.statusbar.MemoryLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.TimeUnit;


public class PerformanceMonitorDialog extends FocusDialog implements ActionListener {

    /**
     * Panel for displaying memory usage information
     */
    private static class MemoryPanel extends JPanel implements Runnable {

        private static final long SLEEP_AMOUNT = TimeUnit.SECONDS.toMillis(1);
        private static final long WAIT_AMOUNT = 500;
        private static final Font FONT = new Font("Dialog", Font.PLAIN, 11);
        private static final Color GRAPH_COLOR = new Color(46, 139, 87);
        private static final Color FREE_DATA_COLOR = new Color(0, 100, 0);
        private static final Color INFO_COLOR = Color.GREEN;
        private static final Color BACKGROUND = Color.BLACK;
        private static final Runtime RUNTIME = Runtime.getRuntime();

        private Thread thread;
        private int w;
        int h;
        Graphics2D graphics;
        private BufferedImage bufferedImage;
        private int columnInc;
        private int pts[];
        private int ptNum;
        int ascent;
        int descent;
        private Rectangle graphOutlineRect = new Rectangle();
        private Rectangle2D mfRect = new Rectangle2D.Float();
        private Rectangle2D muRect = new Rectangle2D.Float();
        private Line2D graphLine = new Line2D.Float();


        MemoryPanel() {
            setBackground(BACKGROUND);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getPreferredSize() {
            return PR_DIMENSION;
        }

        protected float getFreeData() {
            return RUNTIME.freeMemory();
        }

        protected float getTotalData() {
            return RUNTIME.totalMemory();
        }

        protected String getMaxValueString(float max) {
            return SizeFormat.format((long) max, MemoryLabel.MEMORY_INFO_SIZE_FORMAT);
        }

        protected String getMinValueString(float min) {
            return SizeFormat.format((long) min, MemoryLabel.MEMORY_INFO_SIZE_FORMAT);
        }

        @Override
        public void paint(Graphics g) {
            if (graphics == null) {
                return;
            }

            graphics.setBackground(getBackground());
            graphics.clearRect(0, 0, w, h);

            final float freeData = getFreeData();
            final float totalData = getTotalData();

            // .. Draw allocated and used strings ..
            graphics.setColor(INFO_COLOR);
            graphics.drawString(getMaxValueString(totalData), 4.0f, (float) ascent + 0.5f);
            graphics.drawString(getMinValueString(totalData - freeData), 4, h - descent);

            // Calculate remaining size
            float ssH = ascent + descent;
            float remainingHeight = h - (ssH * 2) - 0.5f;
            float blockHeight = remainingHeight / 10;
            float blockWidth = 20.0f;

            // .. Free ..
            graphics.setColor(FREE_DATA_COLOR);
            int memUsage = (int) ((freeData / totalData) * 10);
            int i = 0;
            for (; i < memUsage; i++) {
                mfRect.setRect(5, ssH + i * blockHeight, blockWidth, blockHeight - 1);
                graphics.fill(mfRect);
            }

            // .. Used ..
            graphics.setColor(INFO_COLOR);
            for (; i < 10; i++) {
                muRect.setRect(5, ssH + i * blockHeight, blockWidth, blockHeight - 1);
                graphics.fill(muRect);
            }

            // .. Draw History Graph ..
            graphics.setColor(GRAPH_COLOR);
            int graphX = 30;
            int graphY = (int) ssH;
            int graphW = w - graphX - 5;
            int graphH = (int) remainingHeight;
            graphOutlineRect.setRect(graphX, graphY, graphW, graphH);
            graphics.draw(graphOutlineRect);

            int graphRow = graphH / 10;

            // .. Draw row ..
            for (int j = graphY; j <= graphH + graphY; j += graphRow) {
                graphLine.setLine(graphX, j, graphX + graphW, j);
                graphics.draw(graphLine);
            }

            // .. Draw animated column movement ..
            int graphColumn = graphW / 15;

            if (columnInc == 0) {
                columnInc = graphColumn;
            }

            for (int j = graphX + columnInc; j < graphW + graphX; j += graphColumn) {
                graphLine.setLine(j, graphY, j, graphY + graphH);
                graphics.draw(graphLine);
            }

            --columnInc;

            if (pts == null) {
                pts = new int[graphW];
                ptNum = 0;
            } else if (pts.length != graphW) {
                int tmp[];
                if (ptNum < graphW) {
                    tmp = new int[ptNum];
                    System.arraycopy(pts, 0, tmp, 0, tmp.length);
                } else {
                    tmp = new int[graphW];
                    System.arraycopy(pts, pts.length - tmp.length, tmp, 0, tmp.length);
                    ptNum = tmp.length - 2;
                }
                pts = new int[graphW];
                System.arraycopy(tmp, 0, pts, 0, tmp.length);
            } else {
                graphics.setColor(Color.YELLOW);
                pts[ptNum] = (int) (graphY + graphH * (freeData / totalData));
                for (int j = graphX + graphW - ptNum, k = 0; k < ptNum; k++, j++) {
                    if (k != 0) {
                        if (pts[k] != pts[k - 1]) {
                            graphics.drawLine(j - 1, pts[k - 1], j, pts[k]);
                        } else {
                            graphics.fillRect(j, pts[k], 1, 1);
                        }
                    }
                }
                if (ptNum + 2 == pts.length) {
                    // throw out oldest point
                    System.arraycopy(pts, 1, pts, 0, ptNum - 1);
                    --ptNum;
                } else {
                    ptNum++;
                }
            }
            g.drawImage(bufferedImage, 0, 0, this);
        }

        protected void start() {
            if (thread == null) {
                thread = new Thread(this);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setName(createThreadName());
                thread.setDaemon(true);
                thread.start();
            }
        }

        protected String createThreadName() {
            return "MemoryMonitor";
        }

        @Override
        public void run() {
            final Thread me = Thread.currentThread();
            while (thread == me && !isShowing() || getSize().width == 0) {
                try {
                    Thread.sleep(WAIT_AMOUNT);
                } catch (InterruptedException e) {
                    return;
                }
            }
            while (thread == me) {
                final Dimension d = getSize();
                if (d.width != w || d.height != h) {
                    w = d.width;
                    h = d.height;
                    bufferedImage = (BufferedImage) createImage(w, h);
                    graphics = bufferedImage.createGraphics();
                    graphics.setFont(FONT);
                    final FontMetrics fm = graphics.getFontMetrics(FONT);
                    ascent = fm.getAscent();
                    descent = fm.getDescent();
                }
                SwingUtilities.invokeLater(this::repaint);
                try {
                    Thread.sleep(SLEEP_AMOUNT);
                } catch (InterruptedException e) {
                    break;
                }
            }
            thread = null;
        }

    }

    /**
     * Panel for displaying CPU usage information
     */
    private static class CpuPanel extends MemoryPanel {

        private OperatingSystemMXBean operatingSystemMXBean;

        CpuPanel() {
            final MBeanServerConnection platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            try {
                operatingSystemMXBean = ManagementFactory.newPlatformMXBeanProxy(platformMBeanServer, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        @Override
        protected String createThreadName() {
            return "CpuMonitor";
        }

        @Override
        protected float getFreeData() {
            return 100F - (float) operatingSystemMXBean.getSystemLoadAverage();
        }

        @Override
        protected float getTotalData() {
            return 100F;
        }

        @Override
        protected String getMaxValueString(float max) {
            return "100 %";
        }

        @Override
        protected String getMinValueString(float min) {
            return "0 %";
        }

    }

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceMonitorDialog.class);

    /**
     * Default panel size
     */
    private static final Dimension PR_DIMENSION = new Dimension(650, 250);

    /**
     * Action that opens and closes this dialog (needed to update menu tet on close)
     */
    private final MuAction closeAction;
    /**
     * Button that closes the dialog.
     */
    private JButton okButton;

    PerformanceMonitorDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get("PerformanceMonitor.title"), mainFrame);
        setModal(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        closeAction = ActionManager.getActionInstance(TogglePerformanceMonitorAction.Descriptor.ACTION_ID, mainFrame);

        Container contentPane = getContentPane();
        contentPane.add(createCpuPanel(), BorderLayout.NORTH);
        contentPane.add(createMemoryPanel(), BorderLayout.CENTER);
        contentPane.add(createOkButton(), BorderLayout.SOUTH);

        pack();

        setInitialFocusComponent(okButton);
    }

    private Component createMemoryPanel() {
        final MemoryPanel memoryPanel = new MemoryPanel();
        memoryPanel.start();
        return memoryPanel;
    }

    private Component createCpuPanel() {
        final CpuPanel cpuPanel = new CpuPanel();
        cpuPanel.start();
        return cpuPanel;
    }

    private Component createOkButton() {
        okButton = new JButton(Translator.get("ok"));
        okButton.addActionListener(this);
        final FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.RIGHT);
        final JPanel panel = new JPanel(layout);
        panel.add(okButton);
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            closeAction.performAction();
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        closeAction.performAction();
    }

}
