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


package com.mucommander.commons.file.impl.hadoop;

import com.mucommander.commons.file.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.UnixUserGroupInformation;

import java.io.IOException;
import java.net.URI;

/**
 * {@link HadoopFile} implementation for the HDFS protocol.
 *
 * @author Maxence Bernard
 */
public class HDFSFile extends HadoopFile {

    // TODO: allow a custom group to be set (see TODO below)
//    /** Name of the property holding the file's group */
//    public static final String GROUP_PROPERTY_NAME = "group";

    /** Default username */
    private static String DEFAULT_USERNAME;

    /** Default group */
    private static String DEFAULT_GROUP;

    /** Default file permissions */
    private final static FilePermissions DEFAULT_PERMISSIONS = new SimpleFilePermissions(
       FsPermission.getDefault().applyUMask(FsPermission.getUMask(DEFAULT_CONFIGURATION)).toShort() & PermissionBits.FULL_PERMISSION_INT
    );


    static {
        try {
            UnixUserGroupInformation ugi = UnixUserGroupInformation.login(DEFAULT_CONFIGURATION);
            DEFAULT_USERNAME = ugi.getUserName();
            // Do not use default groups, as these are pretty much useless
        }
        catch(Exception e) {
            // Should never happen but default to a reasonable value if it does
            DEFAULT_USERNAME = System.getProperty("user.name");
        }

        DEFAULT_GROUP = DEFAULT_CONFIGURATION.get("dfs.permissions.supergroup", "supergroup");
    }


    protected HDFSFile(FileURL url) throws IOException {
        super(url);
    }

    protected HDFSFile(FileURL url, FileSystem fs, FileStatus fileStatus) throws IOException {
        super(url, fs, fileStatus);
    }

    public static String getDefaultUsername() {
        return DEFAULT_USERNAME;
    }

    public static String getDefaultGroup() {
        return DEFAULT_GROUP;
    }

    private static String getUsername(FileURL url) {
        Credentials credentials = url.getCredentials();
        String username;
        if(credentials==null||(username=credentials.getLogin()).equals(""))
            username = getDefaultUsername();

        return username;
    }

    private static String getGroup(FileURL url) {
//        // Import the group from the URL's 'group' property, if set
//        String group = url.getProperty(GROUP_PROPERTY_NAME);
//        if(group==null || group.equals(""))
//            group = getDefaultGroup();
//
//        return group;

        return getDefaultGroup();
    }


    ///////////////////////////////
    // HadoopFile implementation //
    ///////////////////////////////

    @Override
    protected FileSystem getHadoopFileSystem(FileURL url) throws IOException {
        // Note: getRealm returns a fresh instance every time
        FileURL realm = url.getRealm();

        Configuration conf = new Configuration();

        // Import the user from the URL's authority, if set
        // TODO: for some reason, setting the group has no effect: files are still created with the default supergroup
        conf.setStrings(UnixUserGroupInformation.UGI_PROPERTY_NAME, getUsername(url), getGroup(url));

        return FileSystem.get(URI.create(realm.toString(false)), conf);
    }

    @Override
    protected void setDefaultFileAttributes(FileURL url, HadoopFile.HadoopFileAttributes atts) {
        atts.setOwner(getUsername(url));
        atts.setGroup(getGroup(url));
        atts.setPermissions(DEFAULT_PERMISSIONS);
    }
}
