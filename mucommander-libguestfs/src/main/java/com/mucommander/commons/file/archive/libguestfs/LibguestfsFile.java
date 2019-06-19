package com.mucommander.commons.file.archive.libguestfs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.archive.ArchiveEntry;
import com.redhat.et.libguestfs.GuestFS;
import com.redhat.et.libguestfs.LibGuestFSException;
import com.redhat.et.libguestfs.StatNS;

public class LibguestfsFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibguestfsFile.class);

    /** Interface to libguestfs-java */
    private GuestFS guestFS;
    private List<ArchiveEntry> entries;

    public LibguestfsFile(AbstractFile file) throws LibGuestFSException {
        guestFS = new GuestFS();
        guestFS.add_drive(file.getAbsolutePath());
        try {
            guestFS.launch();
        } catch(Exception e) {
            LOGGER.error("failed to launch LibguestfsFile");
            throw new LibGuestFSException(e.getMessage());
        }
        mount();
    }

    private void mount() throws LibGuestFSException {
        for (String root : guestFS.inspect_os()) {
            guestFS.inspect_get_mountpoints(root).forEach((mp, dev) -> {
                try {
                    guestFS.mount(dev, mp);
                } catch (LibGuestFSException e) { /* some devices may fail and that's fine */ }
            });
        }
    }

    private void umount() throws LibGuestFSException {
        guestFS.umount_all();
    }

    interface Visitor {
        void visit(String path, StatNS stats, boolean dir);
    }

    public List<ArchiveEntry> ls() throws LibGuestFSException {
        if (entries == null) {
            entries = new ArrayList<>();
            walk((String path, StatNS stats, boolean dir) -> entries.add(createArchiveEntry(path, stats, dir)));
        }
        return entries;
    }

    private ArchiveEntry createArchiveEntry(String path, StatNS stats, boolean dir) {
        return new ArchiveEntry(path.substring(1), dir, stats.st_mtime_sec * 1000, stats.st_size, true);
    }

    private void walk(Visitor visitor) throws LibGuestFSException {
        walk(0, "/", visitor);
    }

    private void walk(int depth, String dir, Visitor visitor) throws LibGuestFSException {
        String[] names = guestFS.ls(dir);
        StatNS[] stats = guestFS.lstatnslist(dir, names);
        for (int i=0; i<names.length; ++i) {
            String path = dir + (dir.endsWith("/") ? "" : "/") + names[i];
            boolean folder = isDir(stats[i].st_mode);
            visitor.visit(path, stats[i], folder);
            if (folder)
                walk(depth+1, path, visitor);
        }
    }

    private boolean isDir(long mode) {
        return (mode & 0170000) == 0040000;
    }

    public void add(ArchiveEntry entry) throws IOException {
        entries.add(entry);
        String path = "/"+entry.getPath();
        if (entry.isDirectory()) {
            try {
                guestFS.mkdir(path);
            } catch (LibGuestFSException e) {
                LOGGER.error("failed to make directory", e);
                throw new IOException(e);
            }
            chmod(path);
        }
    }

    public void delete(ArchiveEntry entry) throws IOException {
        try {
            String path = "/"+entry.getPath();
            if (entry.isDirectory()) {
                guestFS.rm_rf(path);
            } else {
                guestFS.rm_f(path);
            }
        } catch (LibGuestFSException e) {
            throw new IOException(e);
        }
        entries.remove(entry);
    }

    public void read(File file, ArchiveEntry entry, long offset, long size) throws IOException {
        try {
            guestFS.download_offset("/"+entry.getPath(), file.getAbsolutePath(), offset, size);
        } catch(LibGuestFSException e) {
            throw new IOException(e);
        }
    }

    public void write(ArchiveEntry entry, File file, long offset) throws IOException {
        try {
            guestFS.upload_offset(file.getAbsolutePath(), "/"+entry.getPath(), offset);
        } catch(LibGuestFSException e) {
            throw new IOException(e);
        }
    }

    public void chmod(String path) {
        try {
            guestFS.chmod(511, path);
        } catch (LibGuestFSException e) {
            LOGGER.error("failed to change permissions of %s", path);
        }
    }
}
