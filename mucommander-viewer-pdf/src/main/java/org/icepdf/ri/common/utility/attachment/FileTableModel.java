package org.icepdf.ri.common.utility.attachment;

import org.icepdf.core.pobjects.EmbeddedFileStream;
import org.icepdf.core.pobjects.FileSpecification;
import org.icepdf.core.util.Utils;

import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Table model for displaying file data using the following columns: name, description, modified, size and
 * compressed size.
 *
 * @since 6.2
 */
@SuppressWarnings("serial")
public class FileTableModel extends AbstractTableModel {

    public static final int NAME_COLUMN = 0;
    public static final int DESCRIPTION_COLUMN = 1;
    public static final int MODIFIED_COLUMN = 2;
    public static final int SIZE_COLUMN = 3;
    public static final int COMPRESSION_COLUMN = 4;
    public static final int DATA_COLUMN = 5;

    private String[] columnNames;
    private Object[][] data;

    public FileTableModel(ResourceBundle messageBundle, HashMap<String, FileSpecification> files) {

        // build the headers.
        columnNames = new String[]{
                messageBundle.getString("viewer.utilityPane.attachments.column.fileName.title"),
                messageBundle.getString("viewer.utilityPane.attachments.column.description.title"),
                messageBundle.getString("viewer.utilityPane.attachments.column.modified.title"),
                messageBundle.getString("viewer.utilityPane.attachments.column.size.title"),
                messageBundle.getString("viewer.utilityPane.attachments.column.compressedSize.title"),
        };

        // build the column data.
        if (files != null) {
            Set<String> keys = files.keySet();
            int rows = keys.size();
            int columns = 6;
            data = new Object[rows][columns];
            int i = 0;
            for (String key : keys) {
                FileSpecification fileSpecification = files.get(key);
                EmbeddedFileStream embeddedFileStream = fileSpecification.getEmbeddedFileStream();
                data[i][NAME_COLUMN] = fileSpecification.getUnicodeFileSpecification() != null ?
                        fileSpecification.getUnicodeFileSpecification() : fileSpecification.getFileSpecification() != null ?
                        fileSpecification.getFileSpecification() : "";
                data[i][DESCRIPTION_COLUMN] = fileSpecification.getDescription() != null ?
                        fileSpecification.getDescription() : "";
                data[i][MODIFIED_COLUMN] = embeddedFileStream.getParamLastModifiedData() != null ?
                        embeddedFileStream.getParamLastModifiedData() : "";
                data[i][SIZE_COLUMN] = Utils.byteFormatter(embeddedFileStream.getParamUncompressedSize(), true);
                data[i][COMPRESSION_COLUMN] = Utils.byteFormatter(embeddedFileStream.getCompressedSize(), true);
                data[i][DATA_COLUMN] = fileSpecification;
                i++;
            }
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
}