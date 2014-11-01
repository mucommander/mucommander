/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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


package com.mucommander.commons.file.impl.iso;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.io.BufferPool;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;

/**
 * Parses entries contained in an ISO/NRG file.
 * <p>
 * <pre>
 * Reference: http://www.ecma-international.org/publications/files/ECMA-ST/Ecma-119.pdf
 * <p/>
 * Todo:
 *      * test with more images
 *      * rewrite/sanitize InputStream for cooked
 *      * add RockRidge, UDF and others extensions
 *      * add DiscJuggler & other weirdo file formats
 * </pre>
 * </p>
 *
 * @author Xavier Martin
 */
class IsoParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsoParser.class);
    public static Vector<IsoArchiveEntry> getEntries(byte[] buffer, RandomAccessInputStream rais, int sectSize, long sector_offset, long shiftOffset) throws Exception {
        Vector<IsoArchiveEntry> entries = new Vector<IsoArchiveEntry>();

        Calendar calendar = Calendar.getInstance();
        int start = 16;
        isoPvd pvd = null;
        todo todo_idr = null;

        int level = 0;
        for (int i = 1; i < 17; i++) {  // fuzzy search, can have type=0 (bootable el torito), type=2 (svd)
            pvd = new isoPvd(buffer, rais, start + i, sectSize, shiftOffset);
            if (pvd.type[0] == 2 && pvd.id[0] == 'C' && pvd.id[1] == 'D' && pvd.id[2] == '0' && pvd.id[3] == '0' && pvd.id[4] == '1') {
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
            pvd = new isoPvd(buffer, rais, start, sectSize, shiftOffset);

        isoDr idr = new isoDr(pvd.root_directory_record, 0);
        todo_idr = parse_dir(todo_idr, "", isonum_733(idr.extent), isonum_733(idr.size), rais, buffer, entries, sectSize, level, shiftOffset, sector_offset, calendar);
        while (todo_idr != null) {
            todo_idr = parse_dir(todo_idr, todo_idr.name, todo_idr.extent, todo_idr.length, rais, buffer, entries, sectSize, level, shiftOffset, sector_offset, calendar);
            todo_idr = todo_idr.next;
        }

        return entries;
    }

    /**
     * Parses the given ISO file and returns the list of entries it contains. The specified stream will *not* be closed
     * by this method.
     *
     * @param file the ISO file to parse
     * @param rais random access stream to read the ISO file. It will *not* be closed by this method.
     * @return the list of entries contained by the ISO file
     * @throws IOException if an I/O error occurs
     */
    static Vector<IsoArchiveEntry> getEntries(AbstractFile file, RandomAccessInputStream rais) throws IOException {
        byte[] buffer = BufferPool.getByteArray(IsoUtil.MODE1_2048);

        try {
            if ("nrg".equals(file.getExtension())) {
                return NrgParser.getEntries(buffer, file, rais);
            }

            int sectSize = IsoUtil.guessSectorSize(file);

            // sector shift : 0 most of the time
            long sector_offset = 0;

            // bytes : depend if there's earlier track we discard
            long shiftOffset = 0;

            return getEntries(buffer, rais, sectSize, sector_offset, shiftOffset);

            /*
            if ("cdi".equals(file.getExtension())) {
                // WIP
                // http://cvs.berlios.de/cgi-bin/viewcvs.cgi/libdiscmage/libdiscmage/src/filter/cdi.c?revision=1.3&view=markup
                int len = rais.available();
                long offset = -1;

                rais.seek(len - 7);
                rais.read(buffer, 0, 8);
                long version = IsoUtil.toDwordBE(buffer, 0);
                offset = IsoUtil.toDwordBE(buffer, 4);
                FileLogger.finest("cdi root " + Long.toHexString(offset) + " version " + Long.toHexString(version));

            }
            */
        }
        catch (Exception e) {
            LOGGER.info("Exception caught while parsing iso, throwing IOException", e);

            throw new IOException();
        }
        finally {
            // Release the buffer
            BufferPool.releaseByteArray(buffer);
        }
    }


    private static void newString(byte b[], int len, int level, StringBuffer name) throws Exception {
        name.append((level == 0) ? new String(b, 0, len) : new String(b, 0, len, "UnicodeBigUnmarked"));
    }

    public static todo parse_dir(todo todo_idr, String rootname, int extent, int len, RandomAccessInputStream rais, byte[] buffer, Vector<IsoArchiveEntry> entries, int sectSize, int level, long shiftOffset, long sector_offset, Calendar calendar) throws Exception {
        todo td;
        int i;
        isoDr idr;

        while (len > 0) {
            rais.seek(IsoUtil.offsetInSector(extent - sector_offset, sectSize, false) + shiftOffset);
            StreamUtils.readFully(rais, buffer);
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
                    //if (level == 0) { // strip ;VERSION
                    int p = name_buf.lastIndexOf(";");
                    if (p != -1)
                        name_buf.setLength(p);
                    p = name_buf.lastIndexOf("."); // strip empty extension
                    if (p != -1) {
                        int s = name_buf.length() - 1;
                        if (p == s)
                            name_buf.setLength(s);
                    }
                    //}
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
                    entries.add(
                            new IsoArchiveEntry(
                                    name.toString(),
                                    dir,
                                    calendar.getTimeInMillis(),
                                    fstat_buf.st_size,
                                    isonum_733(idr.extent) - sector_offset,
                                    sectSize,
                                    shiftOffset,
                                    false)
                    );
                }

                i += (buffer[i] & 0xff);
                if (i > IsoUtil.MODE1_2048 - idr.s_length) break;
            }
        }

        return todo_idr;
    }

    // ======================================
    private static int ISODCL(int start, int end) {
        return (end - start + 1);
    }

    private static int isonum_731(byte p[]) {
        return IsoUtil.toDwordBE(p, 0);
    }

    public static int isonum_733(byte p[]) {
        return (isonum_731(p));
    }

    private static boolean S_ISDIR(int m) {
        return ((m & S_IFDIR) == S_IFDIR);
    }

    // ======================================
    private static int S_IFREG = 0100000;
    private static int S_IFDIR = 0040000;

    private static class stat {
        int st_size;
        int st_mode;
    }

    public static class todo {
        public todo next;
        public String name;
        public int extent;
        public int length;
    }

    public static class isoDr {
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

    public static class isoPvd {
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
        public byte[] unused5 = new byte[ISODCL(1396, IsoUtil.MODE1_2048)];

        byte dataPvr[][] = {type, id, version, unused1,
                system_id, volume_id, unused2, volume_space_size,
                unused3, volume_set_size, volume_sequence_number, logical_block_size,
                path_table_size, type_l_path_table, opt_type_l_path_table, type_m_path_table,
                opt_type_m_path_table, root_directory_record, volume_set_id, publisher_id,
                preparer_id, application_id, copyright_file_id, abstract_file_id,
                bibliographic_file_id, creation_date, modification_date, expiration_date,
                effective_date, file_structure_version, unused4, application_data,
                unused5};

        public isoPvd(byte[] pvd, RandomAccessInputStream rais, int start, int sectSize, long shiftOffset) throws IOException {
            rais.seek(IsoUtil.offsetInSector(start, sectSize, false) + shiftOffset);
            if (rais.read(pvd) == -1)
                throw new IOException("unable to read PVD");
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
