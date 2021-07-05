package com.mucommander.preferences.lookandfeel;

import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class LookAndFeelPreferencesPanel extends PreferencesPanel {

    static final String TITLE = "Look and Feel";
    // not keeping logger if we don't use instance any more:
    private final Logger LOG = LoggerFactory.getLogger(LookAndFeelPreferencesPanel.class);

    private LookAndFeelsListModel listModel;
    private JList<LAFInfo> list;
    private PreviewRenderer previewRenderer;

    public LookAndFeelPreferencesPanel(PreferencesDialog parent) {
        super(parent, TITLE);
        initUI();
        initListeners();
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        listModel = new LookAndFeelsListModel();

        list = new JList<>(listModel);
        list.setDragEnabled(false);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new LookAndFeelListCellRenderer());

        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        contentPane.add(listScroll, BorderLayout.WEST);

        previewRenderer = new PreviewRenderer(listModel, list::getSelectedIndex);
        JPanel previewRenderPanel = new JPanel(new BorderLayout());
        previewRenderPanel.add(previewRenderer, BorderLayout.CENTER);
        previewRenderPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        contentPane.add(previewRenderPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);
    }

    private void initListeners() {
        list.addListSelectionListener(e -> {
            previewRenderer.repaint();
        });
        listModel.addLoadingDoneListener(() -> {
            String currentLafClass = UIManager.getLookAndFeel().getClass().getName();
            for (int i = 0; i < listModel.getSize(); i++) {
                if (currentLafClass.equals(listModel.getElementAt(i).className)) {
                    list.setSelectedIndex(i);
                    return;
                }
            }
        });
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                list.requestFocusInWindow();
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });
    }

    @Override
    protected void commit() {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex < 0) {
            LOG.warn("Commit was requested while nothing is selected, ignoring.");
            return;
        }

        try {
            LAFInfo lafInfo = listModel.getElementAt(selectedIndex);

            if (lafInfo.className.equals(UIManager.getLookAndFeel().getClass().getName())) {
                return;
            }

            LOG.info("Applying LookAndFeel: {}", lafInfo.name);
            UIManager.setLookAndFeel(lafInfo.className);
        } catch (Exception ex) {
            LOG.warn("Commit failed", ex);
        }
    }
}
