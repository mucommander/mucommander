/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.impl.lst;

import com.mucommander.Debug;
import com.mucommander.file.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * IsoArchiveFile provides read-only access to archives in the LST format made popular by Total Commander.
 *
 * <p>Entries are parsed from the .lst file and can be read (an InputStream can be opened) if the file exists locally.
 *
 * <p>For reference, here's a short LST file:
 * <pre>
 * c:\
 * cygwin\	0	2006.10.2	19:35.2
 * cygwin.bat	57	2006.10.2	19:34.58
 * cygwin.ico	7022	2006.10.2	19:40.52
 * cygwin\bin\	0	2006.10.2	19:40.52
 * addftinfo.exe	67072	2002.12.16	10:3.24
 * afmtodit	8544	2002.12.16	10:3.22
 * apropos	1786	2005.5.4	2:12.50
 * ascii.exe	7168	2006.3.20	20:44.24
 * ash.exe	74240	2004.1.27	2:14.20
 * awk.exe	19	2006.10.2	19:34.4
 * </pre>
 *
 * @author Maxence Bernard
 */
public class LstArchiveFile extends AbstractROArchiveFile {

    private String baseFolder;

    
    public LstArchiveFile(AbstractFile file) {
        super(file);
    }


    public Vector getEntries() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
        Vector entries = new Vector();

        try {
            baseFolder = br.readLine();

            if(baseFolder==null)
                return entries;

            String line;
            StringTokenizer st;
            String name;
            String path;
            String currentDir = "";
            long size;
            long date;
            boolean isDirectory;
            SimpleDateFormat lstDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm.ss");

            while((line=br.readLine())!=null) {
                try {
                    st = new StringTokenizer(line, "\t");
                    name = st.nextToken().replace('\\', '/');
                    size = Long.parseLong(st.nextToken());
                    date = lstDateFormat.parse((st.nextToken()+" "+st.nextToken())).getTime();

                    if(name.endsWith("/")) {
                        isDirectory = true;
                        currentDir = name;
                        path = currentDir;
                    }
                    else {
                        isDirectory = false;
                        path = currentDir+name;
                    }

                    entries.add(new SimpleArchiveEntry(path, date, size, isDirectory));
                }
                catch(Exception e) {    // Catches exceptions thrown by StringTokenizer and SimpleDateFormat
                    if(Debug.ON) {
                        Debug.trace("Exception caught while parsing LST file:");
                        e.printStackTrace();
                    }

                    throw new IOException();
                }
            }

            return entries;
        }
        finally {
            if(br!=null)
                try { br.close(); }
                catch(IOException e) {}
        }
    }

    
    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        // Will throw an IOException if the file designated by the entry doesn't exist 
        return FileFactory.getFile(baseFolder+entry.getPath(), true).getInputStream();
    }
}
