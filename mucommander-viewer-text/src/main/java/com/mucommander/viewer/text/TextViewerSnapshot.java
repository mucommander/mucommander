/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mucommander.viewer.text;

import com.mucommander.commons.conf.Configuration;
import static com.mucommander.snapshot.MuSnapshot.FILE_PRESENTER_SECTION;
import com.mucommander.snapshot.SnapshotHandler;

/**
 *
 * @author hajdam
 */
public final class TextViewerSnapshot implements SnapshotHandler {

    /**
     * Section describing information specific to text file presenter.
     */
    private static final String TEXT_FILE_PRESENTER_SECTION = FILE_PRESENTER_SECTION + "." + "text";
    /**
     * Whether or not to wrap long lines.
     */
    public static final String TEXT_FILE_PRESENTER_LINE_WRAP = TEXT_FILE_PRESENTER_SECTION + "." + "line_wrap";
    /**
     * Default wrap value.
     */
    public static final boolean DEFAULT_LINE_WRAP = false;
    /**
     * Whether or not to show line numbers.
     */
    public static final String TEXT_FILE_PRESENTER_LINE_NUMBERS = TEXT_FILE_PRESENTER_SECTION + "." + "line_numbers";
    /**
     * Default line numbers value.
     */
    public static final boolean DEFAULT_LINE_NUMBERS = true;
    /**
     * Last known file presenter full screen mode.
     */
    public static final String TEXT_FILE_PRESENTER_FULL_SCREEN = TEXT_FILE_PRESENTER_SECTION + "." + "full_screen";

    @Override
    public void read(Configuration configuration) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void write(Configuration configuration) {
    	configuration.setVariable(TEXT_FILE_PRESENTER_FULL_SCREEN, TextViewer.isFullScreen());
    	configuration.setVariable(TEXT_FILE_PRESENTER_LINE_WRAP, TextViewer.isLineWrap());
    	configuration.setVariable(TEXT_FILE_PRESENTER_LINE_NUMBERS, TextViewer.isLineNumbers());
    }
}
