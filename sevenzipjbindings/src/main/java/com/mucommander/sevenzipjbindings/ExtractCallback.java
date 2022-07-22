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

package com.mucommander.sevenzipjbindings;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;

/**
 * @author Oleg Trifonov
 */
public class ExtractCallback implements IArchiveExtractCallback {
    private boolean skipExtraction;
    private IInArchive inArchive;
    private OutputStream os;

    public ExtractCallback(IInArchive inArchive, OutputStream os) {
        this.inArchive = inArchive;
        this.os = os;
    }

    public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
        skipExtraction = (Boolean) inArchive.getProperty(index, PropID.IS_FOLDER);
        if (skipExtraction || extractAskMode != ExtractAskMode.EXTRACT) {
            return null;
        }
        return data -> {
            try {
                os.write(data);
            } catch (IOException e) {
                throw new SevenZipException(e);
            }
            return data.length; // Return amount of proceed data
        };
    }

    public void prepareOperation(ExtractAskMode extractAskMode) {
//System.out.println("prepare  " + index);
    }

    public void setOperationResult(ExtractOperationResult extractOperationResult) {
        if (skipExtraction) {
            return;
        }
        if (extractOperationResult != ExtractOperationResult.OK) {
            System.err.println("Extraction error  = " + extractOperationResult);
        } else {
//System.out.println(String.format("%9X | %10s | %s", hash, size, inArchive.getProperty(index, PropID.PATH)));
        }
    }

    public void setCompleted(long completeValue) {
//System.out.println("completed  " + completeValue);
    }

    public void setTotal(long total) {
//System.out.println("total  " + index + "   " + total);
    }
}