package com.mucommander.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.InputStream;
import java.util.Vector;
import java.util.Calendar;

public class IsoArchiveFile extends AbstractArchiveFile {
    long sector_offset = 0;
    byte buffer[] = new byte[2048];
    Calendar calendar = Calendar.getInstance();
    byte date_buf[] = new byte[7];
    todo todo_idr;
    stat fstat_buf;
    String name_buf;
    boolean cooked;

    public IsoArchiveFile(AbstractFile file) {
        super(file);
        if (file.getSize() % 2048 != 0) {
            cooked = true;
        }
    }

    protected Vector getEntries() throws IOException {
        Vector entries = new Vector();

        try {
            RandomAccessFile raf = new RandomAccessFile(new File(file.getAbsolutePath()), "r");
            int start = 16;
            if ("nrg".equals(getExtension())) {
                start += 150;
                sector_offset = -150;
            }
            isoPvd pvd = new isoPvd(raf, start, cooked);
            isoDr idr = new isoDr(pvd.root_directory_record, 0);
            parse_dir("", isonum_733(idr.extent), isonum_733(idr.size), raf, entries, cooked);
            todo td = todo_idr;
            while (td != null) {
                parse_dir(td.name, td.extent, td.length, raf, entries, cooked);
                td = td.next;
            }
        } catch (Exception e) {
            if (com.mucommander.Debug.ON) {
                com.mucommander.Debug.trace("Exception caught while parsing iso, throwing IOException");
                e.printStackTrace();
            }
            throw new IOException();
        }
        return entries;
    }


    InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(new File(file.getAbsolutePath()), "r");
        return new isoInputStream(raf, (IsoEntry) entry, cooked);

    }

    private String newString(byte b[], int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, max = b.length; i < max && i < len; i++) {
            if (b[i] == 0)
                break;
            sb.append((char) (b[i] & 0xff));
        }
        return sb.toString();
    }

    private void parse_dir(String rootname, int extent, int len, RandomAccessFile raf, Vector entries, boolean cooked) throws Exception {
        todo td;
        int i;
        isoDr idr;

        while (len > 0) {
            raf.seek(sector(extent - sector_offset, cooked));
            raf.read(buffer);
            len -= buffer.length;
            extent++;
            i = 0;
            while (true) {
                idr = new isoDr(buffer, i);
                if (idr.length[0] == 0) break;
                fstat_buf = new stat();
                name_buf = "";
                fstat_buf.st_size = isonum_733(idr.size);
                if ((idr.flags[0] & 2) > 0)
                    fstat_buf.st_mode |= S_IFDIR;
                else
                    fstat_buf.st_mode |= S_IFREG;

                if (idr.name_len[0] == 1 && idr.name[0] == 0)
                    name_buf = ".";
                else if (idr.name_len[0] == 1 && idr.name[0] == 1)
                    name_buf = "..";
                else {
                    name_buf = name_buf + newString(idr.name, idr.name_len[0]);
                    int p = name_buf.lastIndexOf(";");
                    if (p != -1)
                        name_buf = name_buf.substring(0, p);
                }
                System.arraycopy(idr.date, 0, date_buf, 0, idr.date.length);

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
                if (!(".".equals(name_buf) || "..".equals(name_buf))) {
                    StringBuffer name = new StringBuffer(rootname);
                    name.append(name_buf);

                    if (S_ISDIR(fstat_buf.st_mode)) {
                        dir = true;
                        if (!name_buf.endsWith("/")) {
                            name.append("/");
                        }
                    }
                    calendar.set((date_buf[0] & 0xff) + 1900, date_buf[1] - 1, date_buf[2], date_buf[3], date_buf[4], date_buf[5]);
                    // date_buf[6]
                    // offset from Greenwich Mean Time, in 15-minute intervals, as a twos complement signed number,
                    // positive for time zones east of Greenwich, and negative for time zones
                    calendar.setTimeZone(new java.util.SimpleTimeZone(15 * 60 * 1000 * date_buf[6], ""));
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
        RandomAccessFile raf;
        int pos;
        long size;
        boolean cooked;

        public isoInputStream(RandomAccessFile raf, IsoEntry entry, boolean cooked) throws IOException {
            this.raf = raf;
            this.size = entry.getSize();
            this.pos = 0;
            this.cooked = cooked;
            raf.seek(sector(entry.getExtent(), cooked));
        }

        public int read() throws IOException {
            return raf.read();
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
                    ret = raf.read(b, cur, 2048);
                    if (ret != -1)
                        pos += ret;
                    raf.skipBytes(280 + 24);
                    cur += 2048;
                }
                ret = raf.read(b, cur, half);
                if (ret != -1)
                    pos += ret;
                ret = toRead;
            } else {
                ret = raf.read(b, off, toRead);
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
        public byte[] name = new byte[38];

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

        public isoPvd(RandomAccessFile raf, int start, boolean cooked) throws Exception {
            byte[] pvd = new byte[2048];
            this.cooked = cooked;
            raf.seek(sector(start, cooked));
            raf.read(pvd);
            load(pvd);
        }

        public isoPvd(byte src[]) {
            load(src);
        }

        void load(byte src[]) {
            for (int i = 0, pos = 0, max = dataPvr.length; i < max; i++) {
                System.arraycopy(src, pos, dataPvr[i], 0, dataPvr[i].length);
                pos += dataPvr[i].length;
            }
        }
    }
}