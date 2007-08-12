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

package com.mucommander.file.impl.iso;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractROArchiveFile;
import com.mucommander.file.ArchiveEntry;
import com.mucommander.io.RandomAccessInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Vector;

/**
 * IsoArchiveFile provides read-only access to archives in the ISO and NRG formats.
 *
 * <p>Reference: http://www.ecma-international.org/publications/files/ECMA-ST/Ecma-119.pdf
 *
 * Todo:
 *      * test with more images
 *      * rewrite/sanitize InputStream for cooked
 *      * add RockRidge, UDF and others extensions
 *      * add DiscJuggler & other weirdo file formats
 *
 * @author Xavier Martin 
 */
public class IsoArchiveFile extends AbstractROArchiveFile {

    private long sector_offset = 0;
    private byte buffer[] = new byte[2048];
    private Calendar calendar = Calendar.getInstance();
    private todo todo_idr;
    private boolean cooked;
    private RandomAccessInputStream rais;

    public IsoArchiveFile(AbstractFile file) {
        super(file);
        if (file.getSize() % 2048 != 0) {
            cooked = true;
        }
    }

    public Vector getEntries() throws IOException {
        Vector entries = new Vector();

        try {
            this.rais = getRandomAccessInputStream();
            int start = 16;
            if ("nrg".equals(getExtension())) {
                start += 150;
                sector_offset = -150;
            }

            isoPvd pvd = null;

            int level = 0;
            for (int i = 1; i < 17; i++) {  // fuzzy search, can have type=0 (bootable el torito), type=2 (svd)
                pvd = new isoPvd(rais, start + i, cooked);
                if (pvd.type[0] == 2 && pvd.id[0] == 'C' && pvd.id[1] == 'D' && pvd.id[2] == '0' && pvd.id[3] == '0' && pvd.id[4] == '1')
                {
                    // gotta read docs a little more about those UCS-2 Escape Sequences
                    switch (pvd.unused3[2]) {
                        case 0x40:
                            level = 1;
                            break;
                        case 0x43:
                            level = 2;
                            break;
                        case 0x45:
                            level = 3;
                    }
                    break;
                }
            }
            if (level == 0) // if no SVD with Joliet, fallback to plain-old ISO9660
                pvd = new isoPvd(rais, start, cooked);

            isoDr idr = new isoDr(pvd.root_directory_record, 0);
            parse_dir("", isonum_733(idr.extent), isonum_733(idr.size), rais, entries, cooked, level);
            todo td = todo_idr;
            while (td != null) {
                parse_dir(td.name, td.extent, td.length, rais, entries, cooked, level);
                td = td.next;
            }
        } catch (Exception e) {
            if (com.mucommander.Debug.ON) com.mucommander.Debug.trace("Exception caught while parsing iso:"+e+", throwing IOException");

            throw new IOException();
        }
        return entries;
    }


    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        return new isoInputStream(rais, (IsoEntry) entry, cooked);
    }

    private void newString(byte b[], int len, int level, StringBuffer name) throws Exception {
        byte d[] = new byte[len];

        System.arraycopy(b, 0, d, 0, len);
        name.append((level == 0) ? new String(d) : new String(d, "UnicodeBigUnmarked"));
    }

    private void parse_dir(String rootname, int extent, int len, RandomAccessInputStream rais, Vector entries, boolean cooked, int level) throws Exception {
        todo td;
        int i;
        isoDr idr;

        while (len > 0) {
            rais.seek(sector(extent - sector_offset, cooked));
            rais.read(buffer);
            len -= buffer.length;
            extent++;
            i = 0;
            while (true) {
                idr = new isoDr(buffer, i);
                if (idr.length[0] == 0) break;
                stat fstat_buf = new stat();
                StringBuffer name_buf = new StringBuffer();
                fstat_buf.st_size = isonum_733(idr.size);
                if ((idr.flags[0] & 2) > 0)
                    fstat_buf.st_mode |= S_IFDIR;
                else
                    fstat_buf.st_mode |= S_IFREG;
                if (idr.name_len[0] == 1 && idr.name[0] == 0)
                    name_buf.append(".");
                else if (idr.name_len[0] == 1 && idr.name[0] == 1)
                    name_buf.append("..");
                else {
                    newString(idr.name, idr.name_len[0] & 0xff, level, name_buf);
                    if (level == 0) { // strip ;VERSION
                        int p = name_buf.lastIndexOf(";");
                        if (p != -1)
                            name_buf.setLength(p);
                        p = name_buf.lastIndexOf("."); // strip empty extension
                        if (p != -1) {
                            int s = name_buf.length() - 1;
                            if (p == s)
                                name_buf.setLength(s);
                        }
                    }
                }

                if ((idr.flags[0] & 2) != 0
                        && (idr.name_len[0] != 1
                        || (idr.name[0] != 0 && idr.name[0] != 1))) {
                    td = todo_idr;
                    if (td != null) {
                        while (td.next != null) td = td.next;
                        td.next = new todo();
                        td = td.next;
                    } else {
                        todo_idr = td = new todo();
                    }
                    td.next = null;
                    td.extent = isonum_733(idr.extent);
                    td.length = isonum_733(idr.size);
                    td.name = rootname + name_buf + "/";
                } else {
                    // file only
                }
                boolean dir = false;
                String n = name_buf.toString();
                if (!(".".equals(n) || "..".equals(n))) {
                    StringBuffer name = new StringBuffer(rootname);
                    name.append(n);

                    if (S_ISDIR(fstat_buf.st_mode)) {
                        dir = true;
                        if (!n.endsWith("/")) {
                            name.append('/');
                        }
                    }
                    calendar.set((idr.date[0] & 0xff) + 1900, idr.date[1] - 1, idr.date[2], idr.date[3], idr.date[4], idr.date[5]);
                    // date_buf[6]
                    // offset from Greenwich Mean Time, in 15-minute intervals, as a twos complement signed number,
                    // positive for time zones east of Greenwich, and negative for time zones
                    calendar.setTimeZone(new java.util.SimpleTimeZone(15 * 60 * 1000 * idr.date[6], ""));
                    entries.add(new IsoEntry(name.toString(), calendar.getTimeInMillis(), fstat_buf.st_size, dir, isonum_733(idr.extent) - sector_offset));
                }

                i += (buffer[i] & 0xff);
                if (i > 2048 - idr.s_length) break;
            }
        }

    }

    // ======================================
    private static int ISODCL(int start, int end) {
        return (end - start + 1);
    }

    private static int isonum_731(byte p[]) {
        return ((p[0] & 0xff)
                | ((p[1] & 0xff) << 8)
                | ((p[2] & 0xff) << 16)
                | ((p[3] & 0xff) << 24));
    }

    private static int isonum_733(byte p[]) {
        return (isonum_731(p));
    }

    private static boolean S_ISDIR(int m) {
        return ((m & S_IFDIR) == S_IFDIR);
    }

    public static long sector(long index, boolean cooked) {
        return (cooked) ? (index * 2352) + 24 : index << 11;
    }

    // ======================================
    private static int S_IFREG = 0100000;
    private static int S_IFDIR = 0040000;

    private class stat {
        int st_size;
        int st_mode;
    }

    private class todo {
        private todo next;
        private String name;
        private int extent;
        private int length;
    }

    // WIP rewrite it cleanly for cooked
    private class isoInputStream extends InputStream {
        private RandomAccessInputStream rais;
        private int pos;
        private long size;
        private boolean cooked;

        public isoInputStream(RandomAccessInputStream rais, IsoEntry entry, boolean cooked) throws IOException {
            this.rais = rais;
            this.size = entry.getSize();
            this.pos = 0;
            this.cooked = cooked;
            rais.seek(sector(entry.getExtent(), cooked));
        }

        public int read() throws IOException {
            return rais.read();
        }

        public int read(byte b[]) throws IOException {
            return read(b, 0, b.length);
        }

        public int read(byte b[], int off, int len) throws IOException {
            int available = available();
            int toRead = len;

            if (toRead > available) {
                toRead = available;
            }

            if (available == 0) {
                return -1;
            }

            int ret;
            if (cooked) {
                // atm it work because it's called for 8192 (2048 * 4)
                int full = toRead >> 11;
                int half = toRead % 2048;

                int cur = off;
                for (int i = 0; i < full; i++) {
                    ret = rais.read(b, cur, 2048);
                    if (ret != -1)
                        pos += ret;
                    rais.skip(280 + 24);
                    cur += 2048;
                }
                ret = rais.read(b, cur, half);
                if (ret != -1)
                    pos += ret;
                ret = toRead;
            } else {
                ret = rais.read(b, off, toRead);
                if (ret != -1)
                    pos += ret;
            }
            return ret;
        }

        public int available() throws IOException {
            int available = (int) (size - pos);
            return (available < 0) ? 0 : available;
        }
    }

    private class isoDr {
        public byte[] length = new byte[ISODCL(1, 1)];
        public byte[] ext_attr_length = new byte[ISODCL(2, 2)];
        public byte[] extent = new byte[ISODCL(3, 10)];
        public byte[] size = new byte[ISODCL(11, 18)];
        public byte[] date = new byte[ISODCL(19, 25)];
        public byte[] flags = new byte[ISODCL(26, 26)];
        public byte[] file_unit_size = new byte[ISODCL(27, 27)];
        public byte[] interleave = new byte[ISODCL(28, 28)];
        public byte[] volume_sequence_number = new byte[ISODCL(29, 32)];
        public byte[] name_len = new byte[ISODCL(33, 33)];
        public byte[] name = new byte[/*38*/128];   // quickly bumped to 128 for Joliet : doesn't lead to a crash yet :)

        public int s_length = 34;

        public byte dataDr[][] = {length, ext_attr_length, extent, size,
                date, flags, file_unit_size, interleave,
                volume_sequence_number, name_len, name};

        public isoDr(byte src[], int pos) {
            for (int i = 0, max = dataDr.length; i < max; i++) {
                int l = dataDr[i].length;
                if ((src.length - pos) < dataDr[i].length && i == 10)
                    l = src.length - pos;
                System.arraycopy(src, pos, dataDr[i], 0, l);
                pos += dataDr[i].length;
            }
        }
    }

    private class isoPvd {
        public byte[] type = new byte[ISODCL(1, 1)];
        public byte[] id = new byte[ISODCL(2, 6)];
        public byte[] version = new byte[ISODCL(7, 7)];
        public byte[] unused1 = new byte[ISODCL(8, 8)];
        public byte[] system_id = new byte[ISODCL(9, 40)];
        public byte[] volume_id = new byte[ISODCL(41, 72)];
        public byte[] unused2 = new byte[ISODCL(73, 80)];
        public byte[] volume_space_size = new byte[ISODCL(81, 88)];
        public byte[] unused3 = new byte[ISODCL(89, 120)];
        public byte[] volume_set_size = new byte[ISODCL(121, 124)];
        public byte[] volume_sequence_number = new byte[ISODCL(125, 128)];
        public byte[] logical_block_size = new byte[ISODCL(129, 132)];
        public byte[] path_table_size = new byte[ISODCL(133, 140)];
        public byte[] type_l_path_table = new byte[ISODCL(141, 144)];
        public byte[] opt_type_l_path_table = new byte[ISODCL(145, 148)];
        public byte[] type_m_path_table = new byte[ISODCL(149, 152)];
        public byte[] opt_type_m_path_table = new byte[ISODCL(153, 156)];
        public byte[] root_directory_record = new byte[ISODCL(157, 190)];
        public byte[] volume_set_id = new byte[ISODCL(191, 318)];
        public byte[] publisher_id = new byte[ISODCL(319, 446)];
        public byte[] preparer_id = new byte[ISODCL(447, 574)];
        public byte[] application_id = new byte[ISODCL(575, 702)];
        public byte[] copyright_file_id = new byte[ISODCL(703, 739)];
        public byte[] abstract_file_id = new byte[ISODCL(740, 776)];
        public byte[] bibliographic_file_id = new byte[ISODCL(777, 813)];
        public byte[] creation_date = new byte[ISODCL(814, 830)];
        public byte[] modification_date = new byte[ISODCL(831, 847)];
        public byte[] expiration_date = new byte[ISODCL(848, 864)];
        public byte[] effective_date = new byte[ISODCL(865, 881)];
        public byte[] file_structure_version = new byte[ISODCL(882, 882)];
        public byte[] unused4 = new byte[ISODCL(883, 883)];
        public byte[] application_data = new byte[ISODCL(884, 1395)];
        public byte[] unused5 = new byte[ISODCL(1396, 2048)];

        byte dataPvr[][] = {type, id, version, unused1,
                system_id, volume_id, unused2, volume_space_size,
                unused3, volume_set_size, volume_sequence_number, logical_block_size,
                path_table_size, type_l_path_table, opt_type_l_path_table, type_m_path_table,
                opt_type_m_path_table, root_directory_record, volume_set_id, publisher_id,
                preparer_id, application_id, copyright_file_id, abstract_file_id,
                bibliographic_file_id, creation_date, modification_date, expiration_date,
                effective_date, file_structure_version, unused4, application_data,
                unused5};

        boolean cooked;

        public isoPvd(RandomAccessInputStream rais, int start, boolean cooked) throws Exception {
            byte[] pvd = new byte[2048];
            this.cooked = cooked;
            rais.seek(sector(start, cooked));
            rais.read(pvd);
            load(pvd);
        }

        void load(byte src[]) {
            for (int i = 0, pos = 0, max = dataPvr.length; i < max; i++) {
                System.arraycopy(src, pos, dataPvr[i], 0, dataPvr[i].length);
                pos += dataPvr[i].length;
            }
        }
    }
}