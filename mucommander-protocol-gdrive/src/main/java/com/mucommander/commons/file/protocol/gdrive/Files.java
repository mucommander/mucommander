/**
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.protocol.gdrive;

import com.google.api.services.drive.model.File;

public class Files {

    public final static String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    public final static String DOCUMENT_MIME_TYPE = "application/vnd.google-apps.document";
    public final static String SPREADSHEET_MIME_TYPE = "application/vnd.google-apps.spreadsheet";
    public final static String PRESENTATION_MIME_TYPE = "application/vnd.google-apps.presentation";
    public final static String DRAWING_MIME_TYPE = "application/vnd.google-apps.drawing";

    public static boolean isNotFolder(File file) {
        return !isFolder(file);
    }

    public static boolean isFolder(File file) {
        return FOLDER_MIME_TYPE.equals(file.getMimeType());
    }

    public static boolean isNotTrashed(File file) {
        return !file.getTrashed();
    }
}
