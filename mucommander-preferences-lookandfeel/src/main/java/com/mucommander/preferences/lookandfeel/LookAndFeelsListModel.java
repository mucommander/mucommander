package com.mucommander.preferences.lookandfeel;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LookAndFeelsListModel extends AbstractListModel<LAFInfo> {

    private final List<LoadingDoneListener> loadingDoneListeners = new ArrayList<>();
    private final List<LAFInfo> lookAndFeelInfos = new ArrayList<>();
    private volatile boolean loadingDone = false;

    LookAndFeelsListModel() {
        new PreviewsLoader().execute();
    }

    public int currentLookAndFeelIndex() {
        String currentLafClass = UIManager.getLookAndFeel().getClass().getName();
        for (int i = 0; i < lookAndFeelInfos.size(); i++) {
            if (currentLafClass.equals(lookAndFeelInfos.get(i).className)) {
                return i;
            }
        }
        return -1;
    }

    private List<LookAndFeelInfo> listPossibleLookAndFeels() {
        List<LookAndFeelInfo> list = new ArrayList<>();

        list.addAll(Arrays.asList(UIManager.getInstalledLookAndFeels()));
        list.add(new LookAndFeelInfo(FlatDarkLaf.NAME, FlatDarkLaf.class.getName()));
        list.add(new LookAndFeelInfo(FlatLightLaf.NAME, FlatLightLaf.class.getName()));
        list.add(new LookAndFeelInfo(FlatDarculaLaf.NAME, FlatDarculaLaf.class.getName()));
        list.add(new LookAndFeelInfo(FlatIntelliJLaf.NAME, FlatIntelliJLaf.class.getName()));
        list.addAll(Arrays.asList(FlatAllIJThemes.INFOS));

        return list;
    }

    public void addLoadingDoneListener(LoadingDoneListener loadingDoneListener) {
        loadingDoneListeners.add(loadingDoneListener);
        if (loadingDone) {
            notifyLoadingDoneListeners();
        }
    }

    private void notifyLoadingDoneListeners() {
        loadingDoneListeners.forEach(LoadingDoneListener::loadingDone);
    }

    @Override
    public int getSize() {
        return lookAndFeelInfos.size();
    }

    @Override
    public LAFInfo getElementAt(int index) {
        return lookAndFeelInfos.get(index);
    }

    private class PreviewsLoader extends SwingWorker<List<LAFInfo>, LAFInfo> {

        @Override
        protected List<LAFInfo> doInBackground() throws Exception {
            List<LookAndFeelInfo> possibleLookAndFeels = listPossibleLookAndFeels();
            List<LAFInfo> result = new ArrayList<>(possibleLookAndFeels.size());
            for (LookAndFeelInfo info : possibleLookAndFeels) {

                try (InputStream in = getClass().getResourceAsStream("preview/" + info.getName() + ".png")) {

                    if (in == null) {
                        // we do not have preview for this LAF, skipping it:
                        continue;
                    }
                    BufferedImage image = ImageIO.read(in);
                    LAFInfo lafInfo = new LAFInfo(info.getName(), info.getClassName(), image);
                    publish(lafInfo);
                    result.add(lafInfo);

                } catch (Exception ignore) {
                    // nop
                }
            }
            return result;
        }

        @Override
        protected void process(List<LAFInfo> chunks) {
            int firstPos = lookAndFeelInfos.size();
            lookAndFeelInfos.addAll(chunks);
            fireIntervalAdded(LookAndFeelsListModel.this, firstPos, lookAndFeelInfos.size() - 1);
        }

        @Override
        protected void done() {
            loadingDone = true;
            notifyLoadingDoneListeners();
        }
    }

    public interface LoadingDoneListener {
        void loadingDone();
    }
}
