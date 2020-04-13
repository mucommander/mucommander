/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.ri.common;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * <p>Utility class for creating file extension file filters.
 *
 * @since 2.0
 */
public class FileExtensionUtils {
    public final static String pdf = "pdf";
    public final static String svg = "svg";
    public final static String ps = "ps";
    public final static String txt = "txt";

    public static FileFilter getPDFFileFilter() {
        return new ExtensionFileFilter("Adobe PDF Files (*.pdf)", pdf);
    }

    public static FileFilter getTextFileFilter() {
        return new ExtensionFileFilter("Text Files (*.txt)", txt);
    }

    public static FileFilter getSVGFileFilter() {
        return new ExtensionFileFilter("SVG Files (*.svg)", svg);
    }

    public static String getExtension(File f) {
        return getExtension(f.getName());
    }

    public static String getExtension(String s) {
        String ext = null;
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    private static class ExtensionFileFilter extends FileFilter {
        private String description;
        private String extension;

        ExtensionFileFilter(String desc, String ext) {
            description = desc;
            extension = ext;
        }

        //Accept all directories and all files with extension
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;

            String ext = FileExtensionUtils.getExtension(f);
            if (ext != null) {
                if (ext.equals(extension))
                    return true;
                else
                    return false;
            }
            return false;
        }

        //The description of this filter
        public String getDescription() {
            return description;
        }
    }
}
